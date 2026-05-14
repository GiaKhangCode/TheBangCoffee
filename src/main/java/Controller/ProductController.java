package Controller;

import Model.ProductListModel;
import Model.ProductModel;
import Model.RecipeModel;
import Model.ToppingModel;
import Model.VariantModel;
import Service.IngredientService;
import Service.ToppingService;
import Service.CategoryService;
import Service.ProductService;
import Service.RecipeService;
import Service.VariantService;
import View.MenuPanel;
import View.MainFrame;
import View.ProductEditDialog;

import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ProductController {

    private ProductService productService;
    private MenuPanel menuPanel;
    private MainFrame mainFrame;
    private ProductListModel productList;
    private List<ProductModel> allProducts;
    private CategoryService categoryService;
    private IngredientService ingredientService;
    private RecipeService recipeService;
    private File selectedFile;
    private VariantService variantService;
    private ToppingService toppingService; 
    
    public ProductController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.menuPanel = mainFrame.getMenuPanel();
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
        this.toppingService = new ToppingService(); 
        this.ingredientService = new IngredientService();
        this.recipeService = new RecipeService();
        this.variantService = new VariantService();
        
        this.selectedFile = null;
        
        initListeners();
        loadMainMenuData();
    }

    private void initListeners() {
        menuPanel.addAddProductListener(e -> {
            ProductEditDialog createDialog = new ProductEditDialog(mainFrame, true);
            setupDialogData(createDialog, null); 
            createDialog.clearForm();
            
            createDialog.addChooseImageListener(ev -> {
                selectedFile = productService.chooseImageFile();
                if (selectedFile != null) createDialog.setImage(new ImageIcon(selectedFile.getAbsolutePath()));
            });
            
            createDialog.addUpdateListener(ev -> {
                String productName = createDialog.getProductName();
                
                long dineInPrice = createDialog.getDineInPrice();
                long takeawayPrice = createDialog.getTakeawayPrice();
                long holidayPrice = createDialog.getHolidayPrice();
                
                double vat = createDialog.getVat();
                String category = createDialog.getCategory();
                String status = createDialog.getStatus();
                String description = createDialog.getDescription();
                List<VariantModel> listVariants = createDialog.getVariantsFromTable();
                List<Integer> selectedToppings = createDialog.getSelectedToppingIds();

                if (listVariants.isEmpty()) {
                    JOptionPane.showMessageDialog(createDialog, "Sản phẩm phải có ít nhất 1 Size (Biến thể)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                for (VariantModel var : listVariants) {
                    if (var.getSizeName().trim().isEmpty() || var.getDineInPrice() <= 0) {
                        JOptionPane.showMessageDialog(createDialog, "Tên Size không được để trống và Giá Size (Tại quán) phải lớn hơn 0!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                productService.insertProduct(category, productName, dineInPrice, takeawayPrice, holidayPrice, vat, selectedFile, status, description, listVariants, selectedToppings);
                JOptionPane.showMessageDialog(createDialog, "Tạo sản phẩm thành công! \n(Lưu ý: Chuyển qua Sửa để cấu hình công thức cho các Size vừa tạo)");
                createDialog.dispose();
                loadMainMenuData();
                
                // [MỚI] Báo cho bên POS biết vừa có thay đổi để reload
                if (mainFrame.getPosController() != null) {
                    mainFrame.getPosController().reloadPosData();
                }
            });
            
            createDialog.setVariantDeleteListener(row -> {
                String sizeName = createDialog.getVariantNameAt(row);
                int confirm = JOptionPane.showConfirmDialog(createDialog, 
                    "Bạn có chắc muốn xóa Size [" + (sizeName.isEmpty() ? "Mới" : sizeName) + "] khỏi danh sách thêm mới không?", 
                    "Xác nhận xóa Size", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    createDialog.removeVariantRow(row);
                }
            });
            
            createDialog.setVisible(true);
        });
        
        menuPanel.addSearchListener(new KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterProducts();
            }
        });

        menuPanel.setProductClickListener(product -> {
            ProductEditDialog editDialog = new ProductEditDialog(mainFrame, false);
            setupDialogData(editDialog, product); 
            
            editDialog.addVariantSelectionListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    VariantModel selectedVariant = (VariantModel) e.getItem();
                    loadRecipeAndCalculateCost(editDialog, selectedVariant);
                }
            });
            
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                VariantModel firstVar = product.getVariants().get(0);
                loadRecipeAndCalculateCost(editDialog, firstVar);
            }
            
            editDialog.addAddRecipeToTableListener(ev -> {
                VariantModel selectedVar = editDialog.getSelectedVariantForRecipe();
                if (selectedVar == null || selectedVar.getVariantID() == 0) {
                    JOptionPane.showMessageDialog(editDialog, "Bạn phải lưu Sản phẩm để tạo Size trước khi thêm công thức!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String ingName = editDialog.getIngredientName();
                double qty = editDialog.getQuantitative();
                
                if (ingName == null || ingName.trim().isEmpty() || qty <= 0) {
                    JOptionPane.showMessageDialog(editDialog, "Vui lòng chọn nguyên liệu và nhập định lượng lớn hơn 0!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int ingId = ingredientService.getIngredientIdByName(ingName); 
                String unit = ingredientService.getUnitByName(ingName);
                
                boolean exists = false;
                for (RecipeModel r : editDialog.getRecipesFromTable()) {
                    if (r.getIngredientID() == ingId) {
                        JOptionPane.showMessageDialog(editDialog, "Nguyên liệu này đã có trong công thức! Vui lòng ấn 'Sửa' trên bảng thay vì thêm mới.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        exists = true;
                        break;
                    }
                }
                
                if (!exists) {
                    editDialog.addRecipeRow(ingId, ingName, unit, qty);
                    updateEstimatedCostFromTable(editDialog);
                }
            });

            editDialog.setRecipeTableListener(new ProductEditDialog.ProductActionListener() {
                @Override
                public void onEdit(int row) {
                    String ingName = editDialog.getRecipeIngredientNameAt(row);
                    double currentQty = editDialog.getRecipeQuantitativeAt(row);
                    
                    String newQtyStr = JOptionPane.showInputDialog(editDialog, "Nhập định lượng mới cho [" + ingName + "]:", currentQty);
                    if (newQtyStr != null && !newQtyStr.trim().isEmpty()) {
                        try {
                            double newQty = Double.parseDouble(newQtyStr.trim());
                            if (newQty <= 0) throw new NumberFormatException();
                            
                            editDialog.markRecipeRowAsEdited(row, newQty);
                            updateEstimatedCostFromTable(editDialog);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(editDialog, "Định lượng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                @Override
                public void onDelete(int row) {
                    editDialog.removeRecipeRow(row);
                    updateEstimatedCostFromTable(editDialog);
                }
            });

            editDialog.addSaveRecipeListener(ev -> {
                VariantModel selectedVar = editDialog.getSelectedVariantForRecipe();
                if (selectedVar == null) return;
                
                List<RecipeModel> tableRecipes = editDialog.getRecipesFromTable();
                List<RecipeModel> dbRecipes = recipeService.getRecipeByVariantId(selectedVar.getVariantID());
                
                if (dbRecipes != null) {
                    for (RecipeModel dbRec : dbRecipes) {
                        boolean stillExists = tableRecipes.stream().anyMatch(t -> t.getIngredientID() == dbRec.getIngredientID());
                        if (!stillExists) {
                            recipeService.deleteRecipe(selectedVar.getVariantID(), dbRec.getIngredientID());
                        }
                    }
                }
                
                for (RecipeModel tRec : tableRecipes) {
                    String rowStatus = tRec.getIngredientName();
                    
                    if ("NEW".equals(rowStatus) || "EDITED".equals(rowStatus)) {
                        try {
                            recipeService.upsertRecipe(selectedVar.getVariantID(), tRec.getIngredientID(), tRec.getQuantityRequired());
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                }
                
                JOptionPane.showMessageDialog(editDialog, "Đã lưu toàn bộ công thức cho Size [" + selectedVar.getSizeName() + "] thành công!");
                loadRecipeAndCalculateCost(editDialog, selectedVar);
            });
            
            editDialog.addChooseImageListener(ev -> {
                selectedFile = productService.chooseImageFile();
                if (selectedFile != null) editDialog.setImage(new ImageIcon(selectedFile.getAbsolutePath()));
            });
            
            editDialog.addUpdateListener(ev -> {
                String productName = editDialog.getProductName();
                
                long dineInPrice = editDialog.getDineInPrice();
                long takeawayPrice = editDialog.getTakeawayPrice();
                long holidayPrice = editDialog.getHolidayPrice();
                
                double vat = editDialog.getVat();
                String category = editDialog.getCategory();
                String status = editDialog.getStatus();
                String description = editDialog.getDescription();
                List<VariantModel> listVariants = editDialog.getVariantsFromTable();
                List<Integer> selectedToppings = editDialog.getSelectedToppingIds();

                if (listVariants.isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "Sản phẩm phải có ít nhất 1 Size (Biến thể)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                for (VariantModel var : listVariants) {
                    if (var.getSizeName().trim().isEmpty() || var.getDineInPrice() <= 0) {
                        JOptionPane.showMessageDialog(editDialog, "Tên Size không được để trống và Giá Size (Tại quán) phải lớn hơn 0!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                        return; 
                    }
                }
                
                productService.updateProduct(product.getProductID(), category, productName, dineInPrice, takeawayPrice, holidayPrice, vat, selectedFile, status, description, listVariants, selectedToppings);
                JOptionPane.showMessageDialog(editDialog, "Cập nhật sản phẩm thành công!");
                editDialog.dispose();
                loadMainMenuData();
                
                // [MỚI] Báo cho bên POS biết vừa có thay đổi để reload
                if (mainFrame.getPosController() != null) {
                    mainFrame.getPosController().reloadPosData();
                }
            });

            editDialog.addDeleteListener(ev -> {
                int confirm = JOptionPane.showConfirmDialog(editDialog, "Xóa sản phẩm [" + product.getProductName() + "]?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        productService.deleteProduct(product.getProductID());
                        JOptionPane.showMessageDialog(editDialog, "Xóa sản phẩm thành công!");
                        editDialog.dispose();
                        loadMainMenuData();
                        
                        // [MỚI] Báo cho bên POS biết vừa có thay đổi để reload
                        if (mainFrame.getPosController() != null) {
                            mainFrame.getPosController().reloadPosData();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(editDialog, "Không thể xóa do sản phẩm đang có trong hóa đơn/phiếu nhập!", "Lỗi FK", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            editDialog.setVariantDeleteListener(row -> {
                int variantId = editDialog.getVariantIdAt(row);
                String sizeName = editDialog.getVariantNameAt(row);
                
                int confirm = JOptionPane.showConfirmDialog(editDialog, 
                    "Bạn có chắc muốn XÓA VĨNH VIỄN Size [" + (sizeName.isEmpty() ? "Mới" : sizeName) + "] khỏi hệ thống không?\n"
                  + "Lưu ý: Hành động này sẽ xóa sạch công thức của Size này!", 
                    "Xác nhận xóa Size", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
                    
                if (confirm == JOptionPane.YES_OPTION) {
                    if (variantId > 0) {
                        if (variantService.deleteVariant(variantId)) {
                            JOptionPane.showMessageDialog(editDialog, "Đã xóa vĩnh viễn Size khỏi CSDL!");
                            editDialog.removeVariantRow(row);
                            editDialog.loadVariantData(editDialog.getVariantsFromTable());
                        } else {
                            JOptionPane.showMessageDialog(editDialog, "Lỗi khi xóa Size khỏi Database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        editDialog.removeVariantRow(row);
                    }
                }
            });
            
            editDialog.setVisible(true);
        });
    }

    private void loadMainMenuData() {
        productList = productService.getProductList();
        
        if (productList != null && productList.getProductList() != null) {
            allProducts = new ArrayList<>(productList.getProductList()); 
        } else {
            allProducts = new ArrayList<>();
        }
        
        menuPanel.displayProductList(productList);
    }
    
    private void setupDialogData(ProductEditDialog dialog, ProductModel product) {
        this.selectedFile = null;
        try {
            List<Model.CategoryModel> cats = categoryService.getAllCategories();
            List<String> categoryNames = new ArrayList<>();
            for(Model.CategoryModel cat : cats) {
                if(cat.getCategoryStatus().equals("Đang sử dụng")) {
                    categoryNames.add(cat.getCategoryName());
                }
            }
            dialog.setCategoryList(categoryNames);
            
            ArrayList<ToppingModel> allToppings = toppingService.getAllToppings();
            List<ToppingModel> selectedToppings = (product != null) ? toppingService.getToppingsByProductID(product.getProductID()) : null;
            dialog.loadToppingCheckboxes(allToppings, selectedToppings);
            
            dialog.setIngredientList(ingredientService.getIngredientNames());
            dialog.addIngredientSelectionListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedIng = dialog.getIngredientName();
                    if (selectedIng != null && !selectedIng.isEmpty()) {
                        String unit = ingredientService.getUnitByName(selectedIng);
                        dialog.setUnitText(unit != null ? unit : "");
                    }
                }
            });

            if (product != null) {
                dialog.setProductData(
                    product.getProductName(), product.getCategoryName(), 
                    product.getDineInPrice(), product.getTakeawayPrice(), product.getHolidayPrice(), 
                    product.getVat(), product.getProductStatus(), product.getDescription(), product.getImageData()
                );
                dialog.loadVariantData(product.getVariants());
                if (dialog.getIngredientName() != null) {
                    dialog.setUnitText(ingredientService.getUnitByName(dialog.getIngredientName()));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadRecipeAndCalculateCost(ProductEditDialog editDialog, VariantModel selectedVariant) {
        if (selectedVariant != null && selectedVariant.getVariantID() > 0) {
            List<RecipeModel> recipes = recipeService.getRecipeByVariantId(selectedVariant.getVariantID());
            editDialog.loadRecipeData(recipes);
            
            long totalCost = 0;
            if (recipes != null) {
                for (RecipeModel r : recipes) {
                    double donGiaBQ = ingredientService.getAveragePrice(r.getIngredientID());
                    totalCost += Math.round(r.getQuantityRequired() * donGiaBQ);
                }
            }
            editDialog.setEstimatedCost(totalCost);
        } else {
            editDialog.loadRecipeData(null);
            editDialog.setEstimatedCost(0);
        }
    }
    
    private void filterProducts() {
        String keyword = menuPanel.getSearchText().toLowerCase();
        
        if (keyword.equals("tìm kiếm tên món...")) {
            keyword = "";
        }

        ProductListModel filteredModel = new ProductListModel();
        
        for (ProductModel p : allProducts) {
            if (p.getProductName().toLowerCase().contains(keyword) || 
                p.getCategoryName().toLowerCase().contains(keyword)) {
                filteredModel.addProduct(p); 
            }
        }
        
        menuPanel.displayProductList(filteredModel);
    }
    
    private void updateEstimatedCostFromTable(ProductEditDialog editDialog) {
        long totalCost = 0;
        for (RecipeModel r : editDialog.getRecipesFromTable()) {
            double donGiaBQ = ingredientService.getAveragePrice(r.getIngredientID());
            totalCost += Math.round(r.getQuantityRequired() * donGiaBQ);
        }
        editDialog.setEstimatedCost(totalCost);
    }
}