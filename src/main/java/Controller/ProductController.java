package Controller;

import Common.ValidationUtil;
import Model.ProductCategoryListModel;
import Model.ProductListModel;
import Model.ProductModel;
import Model.RecipeModel;
import Model.ToppingModel;
import Model.VariantModel;
import Service.IngredientService;
import Service.ToppingService;
import Service.ProductCategoryService;
import Service.ProductService;
import Service.RecipeService;
import Service.VariantService;
import View.MenuPanel;
import View.MainFrame;
import View.ProductDetailDialog;
import View.ProductEditDialog;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ProductController {

    private ProductService productService;
    private MenuPanel menuPanel;
    private MainFrame mainFrame;
    private ProductListModel productList;
    private ProductDetailDialog productDetailDialogFrame;
    private ProductCategoryService categoryService;
    private IngredientService ingredientService;
    private RecipeService recipeService;
    private File selectedFile;
    private VariantService variantService;
    private ToppingService toppingService; 
    
    public ProductController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.menuPanel = mainFrame.getMenuPanel();
        this.productService = new ProductService();
        this.categoryService = new ProductCategoryService();
        this.toppingService = new ToppingService(); 
        this.ingredientService = new IngredientService();
        this.recipeService = new RecipeService();
        
        this.productDetailDialogFrame = new ProductDetailDialog(mainFrame);
        this.selectedFile = null;
        
        initListeners();
        loadMainMenuData();
    }

    private void initListeners() {
        // ========================================================
        // 1. MÀN HÌNH CHÍNH & TẠO SẢN PHẨM MỚI
        // ========================================================
        menuPanel.addAddProductListener(e -> {
            loadDialogData();
            productDetailDialogFrame.clearForm();
            productDetailDialogFrame.setVisible(true);
        });

        // ========================================================
        // 2. MÀN HÌNH CHỈNH SỬA SẢN PHẨM (CLICK VÀO SẢN PHẨM)
        // ========================================================
        menuPanel.setProductClickListener(product -> {
            ProductEditDialog editDialog = new ProductEditDialog(mainFrame);
            
            // Đổ danh sách Category
            List<String> categoryNames = categoryService.getAllCategory().getCategoryNames();
            editDialog.setCategoryList(categoryNames);
            
            editDialog.addIngredientSelectionListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedIng = editDialog.getIngredientName();
                    if (selectedIng != null && !selectedIng.isEmpty()) {
                        String unit = ingredientService.getUnitByName(selectedIng);
                        editDialog.setUnitText(unit != null ? unit : "");
                    }
                }
            });

            // Tự động kích hoạt điền đơn vị cho nguyên liệu đầu tiên ngay khi mở form
            if (editDialog.getIngredientName() != null) {
                editDialog.setUnitText(ingredientService.getUnitByName(editDialog.getIngredientName()));
            }
            
            // Đổ danh sách Topping & Tick những Topping đã chọn
            try {
                ArrayList<ToppingModel> allToppings = toppingService.getAllToppings();
                List<ToppingModel> selectedToppings = toppingService.getToppingsByProductID(product.getProductID());
                editDialog.loadToppingCheckboxes(allToppings, selectedToppings);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            // [SỬA] Đổ thông tin cơ bản với 3 loại giá
            editDialog.setProductData(
                product.getProductName(), product.getCategoryName(), 
                product.getDineInPrice(), product.getTakeawayPrice(), product.getHolidayPrice(), 
                product.getVat(), product.getProductStatus(), product.getDescription(), product.getImageData()
            );
            
            // Đổ danh sách Biến thể (Size)
            editDialog.loadVariantData(product.getVariants());
            
            // Đổ danh sách Nguyên liệu
            editDialog.setIngredientList(ingredientService.getIngredientNames());
            
            // --- XỬ LÝ SỰ KIỆN Ở TAB CÔNG THỨC ---
            // Khi User chọn một Size khác ở ComboBox
            editDialog.addVariantSelectionListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    VariantModel selectedVariant = (VariantModel) e.getItem();
                    loadRecipeAndCalculateCost(editDialog, selectedVariant);
                }
            });
            
            // Khi mở form lần đầu, tự động load công thức của Size đầu tiên (Nếu có)
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                VariantModel firstVar = product.getVariants().get(0);
                loadRecipeAndCalculateCost(editDialog, firstVar);
            }
            
            // Sự kiện Thêm Công Thức
            editDialog.addAddRecipeListener(ev -> {
                VariantModel selectedVar = editDialog.getSelectedVariantForRecipe();
                if (selectedVar == null || selectedVar.getVariantID() == 0) {
                    JOptionPane.showMessageDialog(editDialog, "Bạn phải lưu Sản phẩm để tạo Size trước khi thêm công thức!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String ingName = editDialog.getIngredientName();
                double quantitative = editDialog.getQuantitative();
                
                if (ingName == null || ingName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "Vui lòng chọn nguyên liệu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (quantitative <= 0) {
                    JOptionPane.showMessageDialog(editDialog, "Định lượng phải là số lớn hơn 0!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int ingId = ingredientService.getIngredientIdByName(ingName); 
                if (ingId <= 0) {
                    JOptionPane.showMessageDialog(editDialog, "Không tìm thấy mã nguyên liệu hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean isSuccess = false;
                try {
                    isSuccess = recipeService.upsertRecipe(selectedVar.getVariantID(), ingId, quantitative);
                } catch (SQLException ex) {
                    System.getLogger(ProductController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                } catch (ClassNotFoundException ex) {
                    System.getLogger(ProductController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                if (isSuccess) {
                    loadRecipeAndCalculateCost(editDialog, selectedVar);
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Thêm nguyên liệu thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            // --- SỰ KIỆN UPLOAD ẢNH & UPDATE SẢN PHẨM ---
            editDialog.addChooseImageListener(ev -> {
                selectedFile = productService.chooseImageFile();
                if (selectedFile != null) editDialog.setImage(new ImageIcon(selectedFile.getAbsolutePath()));
            });
            
            editDialog.addUpdateListener(ev -> {
                String productName = editDialog.getProductName();
                
                // [SỬA] Lấy 3 loại giá từ View
                long dineInPrice = editDialog.getDineInPrice();
                long takeawayPrice = editDialog.getTakeawayPrice();
                long holidayPrice = editDialog.getHolidayPrice();
                
                double vat = editDialog.getVat();
                String category = editDialog.getCategory();
                String status = editDialog.getStatus();
                String description = editDialog.getDescription();
                List<VariantModel> listVariants = editDialog.getVariantsFromTable();
                List<Integer> selectedToppings = editDialog.getSelectedToppingIds();

                // Validate logic
                if (listVariants.isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "Sản phẩm phải có ít nhất 1 Size (Biến thể)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                for (VariantModel var : listVariants) {
                    // [SỬA] Kiểm tra theo giá tại quán
                    if (var.getSizeName().trim().isEmpty() || var.getDineInPrice() <= 0) {
                        JOptionPane.showMessageDialog(editDialog, "Tên Size không được để trống và Giá Size (Tại quán) phải lớn hơn 0!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                        return; // Dừng lại
                    }
                }
                
                // [SỬA] Gọi Service Update với 3 tham số giá
                productService.updateProduct(product.getProductID(), category, productName, dineInPrice, takeawayPrice, holidayPrice, vat, selectedFile, status, description, listVariants, selectedToppings);
                JOptionPane.showMessageDialog(editDialog, "Cập nhật sản phẩm thành công!");
                editDialog.dispose();
                loadMainMenuData();
            });

            editDialog.addDeleteListener(ev -> {
                int confirm = JOptionPane.showConfirmDialog(editDialog, "Xóa sản phẩm [" + product.getProductName() + "]?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        productService.deleteProduct(product.getProductID());
                        JOptionPane.showMessageDialog(editDialog, "Xóa sản phẩm thành công!");
                        editDialog.dispose();
                        loadMainMenuData();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(editDialog, "Không thể xóa do sản phẩm đang có trong hóa đơn/phiếu nhập!", "Lỗi FK", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // --- SỰ KIỆN SỬA / XÓA BẢNG CÔNG THỨC ---
            editDialog.setRecipeTableListener(new ProductEditDialog.ProductActionListener() {
                @Override
                public void onEdit(int row) {
                    VariantModel selectedVar = editDialog.getSelectedVariantForRecipe();
                    if (selectedVar == null) return;
                    
                    int ingId = editDialog.getRecipeIngredientIdAt(row);
                    String ingName = editDialog.getRecipeIngredientNameAt(row);
                    double currentQty = editDialog.getRecipeQuantitativeAt(row);
                    
                    // Hiển thị Popup nhập định lượng mới
                    String newQtyStr = JOptionPane.showInputDialog(editDialog, "Nhập định lượng mới cho [" + ingName + "]:", currentQty);
                    if (newQtyStr != null && !newQtyStr.trim().isEmpty()) {
                        try {
                            double newQty = Double.parseDouble(newQtyStr.trim());
                            if (newQty <= 0) {
                                JOptionPane.showMessageDialog(editDialog, "Định lượng phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            
                            // Gọi upsertRecipe để đè giá trị mới
                            if (recipeService.upsertRecipe(selectedVar.getVariantID(), ingId, newQty)) {
                                JOptionPane.showMessageDialog(editDialog, "Cập nhật thành công!");
                                // Load lại bảng
                                loadRecipeAndCalculateCost(editDialog, selectedVar);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(editDialog, "Định lượng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                @Override
                public void onDelete(int row) {
                    VariantModel selectedVar = editDialog.getSelectedVariantForRecipe();
                    if (selectedVar == null) return;
                    
                    int ingId = editDialog.getRecipeIngredientIdAt(row);
                    String ingName = editDialog.getRecipeIngredientNameAt(row);
                    
                    int confirm = JOptionPane.showConfirmDialog(editDialog, "Bạn có chắc chắn muốn xóa nguyên liệu [" + ingName + "] khỏi Size này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (recipeService.deleteRecipe(selectedVar.getVariantID(), ingId)) {
                            JOptionPane.showMessageDialog(editDialog, "Xóa thành công!");
                            // Load lại bảng
                            loadRecipeAndCalculateCost(editDialog, selectedVar);
                        } else {
                            JOptionPane.showMessageDialog(editDialog, "Lỗi khi xóa công thức!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
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
                        // Trường hợp Size ĐÃ CÓ trong Database -> Xóa thật
                        if (variantService.deleteVariant(variantId)) {
                            JOptionPane.showMessageDialog(editDialog, "Đã xóa vĩnh viễn Size khỏi CSDL!");
                            
                            // Xóa khỏi bảng
                            editDialog.removeVariantRow(row);
                            
                            // Load lại giao diện (Bảng và Combobox Tab Công Thức) cho đồng bộ
                            editDialog.loadVariantData(editDialog.getVariantsFromTable());
                        } else {
                            JOptionPane.showMessageDialog(editDialog, "Lỗi khi xóa Size khỏi Database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        // Trường hợp Size VỪA BẤM THÊM trên UI (chưa lưu xuống DB) -> Chỉ xóa trên bảng
                        editDialog.removeVariantRow(row);
                    }
                }
            });
            
            editDialog.setVisible(true);
        });
        
        // ========================================================
        // 3. CÁC SỰ KIỆN TẠI MÀN HÌNH TẠO MỚI SP (ProductDetailDialog)
        // ========================================================
        productDetailDialogFrame.addChooseImageListener(e -> {
            selectedFile = productService.chooseImageFile();
            if (selectedFile != null) {
                productDetailDialogFrame.setImage(new ImageIcon(selectedFile.getAbsolutePath()));
            }
        });
        
        productDetailDialogFrame.addSaveListener(e -> {
            String productName = productDetailDialogFrame.getProductName();
            
            // [SỬA] Lấy 3 loại giá từ Form thêm mới
            long dineInPrice = productDetailDialogFrame.getDineInPrice();
            long takeawayPrice = productDetailDialogFrame.getTakeawayPrice();
            long holidayPrice = productDetailDialogFrame.getHolidayPrice();
            
            double vat = productDetailDialogFrame.getVat();
            String category = productDetailDialogFrame.getCategory();
            String status = productDetailDialogFrame.getStatus();
            String description = productDetailDialogFrame.getDescription();
            
            List<VariantModel> listVariants = productDetailDialogFrame.getVariantsFromTable();
            List<Integer> selectedToppings = productDetailDialogFrame.getSelectedToppingIds();
            
            // Validate ...
            if (listVariants.isEmpty()) {
                JOptionPane.showMessageDialog(productDetailDialogFrame, "Sản phẩm phải có ít nhất 1 Size (Biến thể)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            for (VariantModel var : listVariants) {
                // [SỬA] Validate giá tại quán
                if (var.getSizeName().trim().isEmpty() || var.getDineInPrice() <= 0) {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Tên Size không được để trống và Giá Size (Tại quán) phải lớn hơn 0!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    return; // Dừng lại, không cho lưu xuống DB
                }
            }
            
            // [SỬA] Gọi Service Insert với 3 tham số giá
            productService.insertProduct(category, productName, dineInPrice, takeawayPrice, holidayPrice, vat, selectedFile, status, description, listVariants, selectedToppings);
            JOptionPane.showMessageDialog(productDetailDialogFrame, "Lưu thành công");
            productDetailDialogFrame.setVisible(false); // Có thể ẩn đi nếu muốn
            loadMainMenuData();
        });
        
        // ========================================================
        // 4. QUẢN LÝ DANH MỤC SẢN PHẨM & TOPPING
        // ========================================================
        productDetailDialogFrame.addAddCategoryListener(e -> {
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            JTextField txtName = new JTextField();
            JTextField txtVat = new JTextField("8");
            
            panel.add(new JLabel("Nhập tên Loại Sản Phẩm mới:")); panel.add(txtName);
            panel.add(new JLabel("Thuế mặc định (%):")); panel.add(txtVat);

            int result = JOptionPane.showConfirmDialog(productDetailDialogFrame, panel, "Thêm Danh Mục", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION && !txtName.getText().trim().isEmpty()) {
                double vat = 8;
                try { vat = Double.parseDouble(txtVat.getText()); } catch(Exception ex){}
                boolean isSuccess = categoryService.addCategory(txtName.getText().trim(), "Đang sử dụng", vat);
                if (isSuccess) {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm loại sản phẩm thành công!"); 
                    loadDialogData();
                } else JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // SỰ KIỆN THÊM TOPPING MỚI VÀO DANH MỤC
        productDetailDialogFrame.addAddToppingListener(e -> {
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            JTextField txtName = new JTextField();
            JTextField txtPrice = new JTextField("0"); 
            JComboBox<String> cbIng = new JComboBox<>();
            for (String ing : ingredientService.getIngredientNames()) cbIng.addItem(ing);
            JTextField txtLoss = new JTextField("0");
            JTextField txtVat = new JTextField("8");
            
            panel.add(new JLabel("Tên Topping (VD: Trân châu trắng):")); panel.add(txtName);
            panel.add(new JLabel("Giá Bán (VNĐ):")); panel.add(txtPrice);
            panel.add(new JLabel("Nguyên liệu liên kết (Bị trừ khi bán):")); panel.add(cbIng);
            panel.add(new JLabel("Định lượng trừ (Hao hụt):")); panel.add(txtLoss);
            panel.add(new JLabel("Thuế VAT (%):")); panel.add(txtVat);

            int result = JOptionPane.showConfirmDialog(productDetailDialogFrame, panel, "Thêm Topping Mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String topName = txtName.getText().trim();
                if (topName.isEmpty()) return;
                try {
                    long price = Long.parseLong(txtPrice.getText().trim());
                    double loss = Double.parseDouble(txtLoss.getText().trim());
                    double vat = Double.parseDouble(txtVat.getText().trim());
                    int ingId = ingredientService.getIngredientIdByName((String) cbIng.getSelectedItem());
                    
                    if (toppingService.addTopping(topName, price, ingId, loss, vat)) {
                        JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm Topping thành công!");
                        loadDialogData();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Giá/Hao hụt/Thuế phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Sự kiện Sửa Xóa Loại SP
        productDetailDialogFrame.setCategoryTableListener(new ProductDetailDialog.ProductActionListener() {
            @Override
            public void onEdit(int row) {
                int selectedId = productDetailDialogFrame.getCategoryIdAt(row);
                String currentName = productDetailDialogFrame.getCategoryNameAt(row);
                
                JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
                JTextField txtName = new JTextField(currentName);
                JTextField txtVat = new JTextField("8");
                JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đang sử dụng", "Chưa sử dụng"});

                panel.add(new JLabel("Tên Loại Sản Phẩm:")); panel.add(txtName);
                panel.add(new JLabel("Thuế VAT mặc định (%):")); panel.add(txtVat);
                panel.add(new JLabel("Trạng thái:")); panel.add(cbStatus);

                int result = JOptionPane.showConfirmDialog(productDetailDialogFrame, panel, "Sửa Loại SP", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String newName = txtName.getText().trim();
                    double vat = 8;
                    try { vat = Double.parseDouble(txtVat.getText()); } catch(Exception ex){}
                    
                    if (!newName.isEmpty() && categoryService.updateCategory(selectedId, newName, (String) cbStatus.getSelectedItem(), vat)) {
                        JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thành công!");
                        loadDialogData(); loadMainMenuData();
                    }
                }
            }
            @Override
            public void onDelete(int row) {
                int selectedId = productDetailDialogFrame.getCategoryIdAt(row);
                if (selectedId != -1) {
                    if (JOptionPane.showConfirmDialog(productDetailDialogFrame, "Xóa danh mục này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        if (categoryService.deleteCategory(selectedId)) {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thành công!"); loadDialogData();
                        } else JOptionPane.showMessageDialog(productDetailDialogFrame, "Lỗi! Danh mục đang có sản phẩm.");
                    }
                }
            }
        });
        
        // Sự kiện Sửa Xóa Topping
        productDetailDialogFrame.setToppingTableListener(new ProductDetailDialog.ProductActionListener() {
            @Override
            public void onEdit(int row) {
                int selectedId = productDetailDialogFrame.getToppingIdAt(row);
                if (selectedId == -1) return;

                String currentName = productDetailDialogFrame.getToppingNameAt(row);
                long currentPrice = productDetailDialogFrame.getToppingPriceAt(row);
                int currentIngId = productDetailDialogFrame.getToppingIngredientIdAt(row);
                double currentLoss = productDetailDialogFrame.getToppingLossAmountAt(row);
                double currentVat = productDetailDialogFrame.getToppingVatAt(row);

                JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
                JTextField txtName = new JTextField(currentName);
                JTextField txtPrice = new JTextField(String.valueOf(currentPrice)); 
                JComboBox<String> cbIng = new JComboBox<>();
                
                String selectedIngName = null;
                for (String ing : ingredientService.getIngredientNames()) {
                    cbIng.addItem(ing);
                    if (ingredientService.getIngredientIdByName(ing) == currentIngId) {
                        selectedIngName = ing;
                    }
                }
                if (selectedIngName != null) {
                    cbIng.setSelectedItem(selectedIngName);
                }

                JTextField txtLoss = new JTextField(String.valueOf(currentLoss));
                JTextField txtVat = new JTextField(String.valueOf(currentVat));
                
                panel.add(new JLabel("Tên Topping (VD: Trân châu trắng):")); panel.add(txtName);
                panel.add(new JLabel("Giá Bán (VNĐ):")); panel.add(txtPrice);
                panel.add(new JLabel("Nguyên liệu liên kết (Bị trừ khi bán):")); panel.add(cbIng);
                panel.add(new JLabel("Định lượng trừ (Hao hụt):")); panel.add(txtLoss);
                panel.add(new JLabel("Thuế VAT (%):")); panel.add(txtVat);

                int result = JOptionPane.showConfirmDialog(productDetailDialogFrame, panel, "Sửa Topping", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String topName = txtName.getText().trim();
                    if (topName.isEmpty()) {
                        JOptionPane.showMessageDialog(productDetailDialogFrame, "Tên Topping không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        long price = Long.parseLong(txtPrice.getText().trim());
                        double loss = Double.parseDouble(txtLoss.getText().trim());
                        double vat = Double.parseDouble(txtVat.getText().trim());
                        int ingId = ingredientService.getIngredientIdByName((String) cbIng.getSelectedItem());
                        
                        if (toppingService.updateTopping(selectedId, topName, price, ingId, loss, vat)) {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật Topping thành công!");
                            loadDialogData(); // Load lại bảng
                        } else {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thất bại do lỗi CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(productDetailDialogFrame, "Giá bán, Hao hụt và Thuế phải là chữ số hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            @Override
            public void onDelete(int row) {
                int selectedId = productDetailDialogFrame.getToppingIdAt(row);
                if (selectedId != -1) {
                    if (JOptionPane.showConfirmDialog(productDetailDialogFrame, "Xóa Topping này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        if (toppingService.deleteTopping(selectedId)) {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thành công!"); loadDialogData();
                        } else JOptionPane.showMessageDialog(productDetailDialogFrame, "Lỗi FK!");
                    }
                }
            }
        });
        
        productDetailDialogFrame.setVariantDeleteListener(row -> {
            String sizeName = productDetailDialogFrame.getVariantNameAt(row);
            
            int confirm = JOptionPane.showConfirmDialog(productDetailDialogFrame, 
                "Bạn có chắc muốn xóa Size [" + (sizeName.isEmpty() ? "Mới" : sizeName) + "] khỏi danh sách thêm mới không?", 
                "Xác nhận xóa Size", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                productDetailDialogFrame.removeVariantRow(row);
            }
        });
    }

    private void loadMainMenuData() {
        productList = productService.getProductList();
        menuPanel.displayProductList(productList);
    }
    
    private void loadDialogData() {
        this.selectedFile = null;
        ProductCategoryListModel fullCategories = categoryService.getAllCategory();
        ArrayList<ToppingModel> allToppings = toppingService.getAllToppings();
        
        productDetailDialogFrame.loadCategoryData(fullCategories);
        productDetailDialogFrame.loadToppingData(allToppings);
    }
    
    // Tính toán giá vốn và hiển thị ra giao diện EditDialog
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
}