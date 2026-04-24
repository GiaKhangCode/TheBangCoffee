package Controller;

import Common.ValidationUtil;
import Model.OptionModel;
import Model.ProductCategoryListModel;
import Model.ProductListModel;
import Model.OptionGroupModel;
import Service.IngredientService;
import Service.OptionService;
import Service.ProductCategoryService;
import Service.ProductService;
import Service.RecipeService;
import View.MenuPanel;
import View.MainFrame;
import View.ProductDetailDialog;
import View.ProductEditDialog;
import View.TableActionSupport;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;
import java.util.List;

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
    private OptionService optionService;

    public ProductController(MainFrame mainFrame) {
        this.mainFrame       = mainFrame;
        this.menuPanel       = mainFrame.getMenuPanel();
        this.productService  = new ProductService();
        this.categoryService = new ProductCategoryService();
        this.optionService   = new OptionService();
        this.ingredientService = new IngredientService();
        this.recipeService   = new RecipeService();

        this.productDetailDialogFrame = new ProductDetailDialog(mainFrame);
        this.selectedFile = null;

        initListeners();
        loadMainMenuData();
    }

    private void initListeners() {
        initMenuListeners();
        initProductDetailListeners();
    }

    private void initMenuListeners() {

        // Nút thêm sản phẩm mới
        menuPanel.addAddProductListener(e -> {
            loadDialogData();
            productDetailDialogFrame.clearForm();
            productDetailDialogFrame.setVisible(true);
        });

        // Click vào card sản phẩm → mở ProductEditDialog
        menuPanel.setProductClickListener(product -> {
            ProductEditDialog editDialog = new ProductEditDialog(mainFrame);

            // Nạp dữ liệu vào dialog
            editDialog.setCategoryList(categoryService.getAllCategory().getCategoryNames());
            try {
                editDialog.loadOptionCheckboxes(
                        optionService.getOption(),
                        optionService.getSelectedOptionByID(product.getProductID()));
            } catch (SQLException | ClassNotFoundException ex) {
                log(ex);
            }
            editDialog.setProductData(
                    product.getProductName(), product.getCategoryName(),
                    product.getBasicPrice(),  product.getProductStatus(),
                    product.getDescription(), product.getImageData());
            editDialog.setIngredientList(ingredientService.getIngredientNames());
            editDialog.loadRecipeData(recipeService.getRecipeByProductId(product.getProductID()));

            // Chọn ảnh
            editDialog.addChooseImageListener(ev -> {
                selectedFile = productService.chooseImageFile();
                if (selectedFile != null) editDialog.setImage(new ImageIcon(selectedFile.getAbsolutePath()));
            });

            // Cập nhật sản phẩm
            editDialog.addUpdateListener(ev -> {
                String productName = editDialog.getProductName();
                double price = editDialog.getPrice();
                String category = editDialog.getCategory();
                String status = editDialog.getStatus();
                String description = editDialog.getDescription();
                HashMap<String, List<String>> selectedOptions = editDialog.getSelectedOptionNamesByGroup();

                String validateMsg = ValidationUtil.validateProductDetail(productName, price, category, status);
                if (!validateMsg.equalsIgnoreCase("Hợp lệ")) {
                    JOptionPane.showMessageDialog(editDialog, validateMsg, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                productService.updateProduct(product.getProductID(), category, productName,
                        price, selectedFile, status, description, selectedOptions);
                JOptionPane.showMessageDialog(editDialog, "Cập nhật sản phẩm thành công!");
                editDialog.dispose();
                loadMainMenuData();
            });

            // Xóa sản phẩm
            editDialog.addDeleteListener(ev -> {
                int confirm = JOptionPane.showConfirmDialog(editDialog,
                        "Bạn có chắc chắn muốn xóa sản phẩm [" + product.getProductName() + "]?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        productService.deleteProduct(product.getProductID());
                    } catch (SQLException | ClassNotFoundException ex) {
                        log(ex);
                    }
                    JOptionPane.showMessageDialog(editDialog, "Xóa sản phẩm thành công!");
                    editDialog.dispose();
                    loadMainMenuData();
                }
            });

            // Thêm nguyên liệu vào công thức
            editDialog.addAddRecipeListener(ev -> {
                String ingName = editDialog.getIngredientName();
                String unit = editDialog.getUnit();
                double quantitative = editDialog.getQuantitative();

                ValidationUtil.validateAddRecipe(ingName, quantitative, editDialog);
                int ingId = ingredientService.getIngredientIdByName(ingName);
                if (ingId <= 0) {
                    JOptionPane.showMessageDialog(editDialog, "Không tìm thấy mã nguyên liệu hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean ok = recipeService.upsertRecipe(product.getProductID(), ingId, unit, quantitative);
                if (ok) {
                    editDialog.loadRecipeData(recipeService.getRecipeByProductId(product.getProductID()));
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Thêm nguyên liệu thất bại. Lỗi CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Sửa / Xóa dòng trong bảng công thức
            editDialog.setRecipeTableListener(new TableActionSupport.SplitActionListener() {

                @Override
                public void onEdit(int row) {
                    int ingId = editDialog.getRecipeIngredientIdAt(row);
                    String name = editDialog.getRecipeIngredientNameAt(row);
                    String unit = editDialog.getRecipeUnitAt(row);
                    double qty = editDialog.getRecipeQuantitativeAt(row);

                    // Giao diện dialog do View quản lý
                    Object[] result = editDialog.showEditRecipeDialog(name, unit, qty);
                    if (result == null) return;

                    try {
                        double newQty = Double.parseDouble(result[1].toString());
                        if (newQty <= 0) {
                            JOptionPane.showMessageDialog(editDialog, "Định lượng phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        boolean ok = recipeService.upsertRecipe(product.getProductID(), ingId, (String) result[0], newQty);
                        if (ok) {
                            JOptionPane.showMessageDialog(editDialog, "Cập nhật thành công!");
                            editDialog.loadRecipeData(recipeService.getRecipeByProductId(product.getProductID()));
                        } else {
                            JOptionPane.showMessageDialog(editDialog, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(editDialog, "Định lượng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }

                @Override
                public void onDelete(int row) {
                    int    ingId = editDialog.getRecipeIngredientIdAt(row);
                    String name  = editDialog.getRecipeIngredientNameAt(row);

                    int confirm = JOptionPane.showConfirmDialog(editDialog,
                            "Xóa nguyên liệu [" + name + "] khỏi công thức món này?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (recipeService.deleteRecipe(product.getProductID(), ingId)) {
                            JOptionPane.showMessageDialog(editDialog, "Xóa thành công!");
                            editDialog.loadRecipeData(recipeService.getRecipeByProductId(product.getProductID()));
                        } else {
                            JOptionPane.showMessageDialog(editDialog, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            editDialog.setVisible(true);
        });
    }

    private void initProductDetailListeners() {

        // Chọn ảnh
        productDetailDialogFrame.addChooseImageListener(e -> {
            selectedFile = productService.chooseImageFile();
            if (selectedFile != null) productDetailDialogFrame.setImage(new ImageIcon(selectedFile.getAbsolutePath()));
        });

        // Lưu sản phẩm mới
        productDetailDialogFrame.addSaveListener(e -> {
            String productName = productDetailDialogFrame.getProductName();
            double basicPrice  = productDetailDialogFrame.getPrice();
            String category = productDetailDialogFrame.getCategory();
            String status = productDetailDialogFrame.getStatus();
            String description = productDetailDialogFrame.getDescription();
            HashMap<String, List<String>> selectedOptions = productDetailDialogFrame.getSelectedOptionNamesByGroup();

            String validateMsg = ValidationUtil.validateProductDetail(productName, basicPrice, category, status);
            if (!validateMsg.equalsIgnoreCase("Hợp lệ")) {
                JOptionPane.showMessageDialog(productDetailDialogFrame, validateMsg);
                return;
            }
            productService.insertProduct(category, productName, basicPrice, selectedFile, status, selectedOptions, description);
            JOptionPane.showMessageDialog(productDetailDialogFrame, "Lưu thành công");
            loadMainMenuData();
        });

        // Thêm Loại Sản Phẩm
        productDetailDialogFrame.addAddCategoryListener(e -> {
            String name = JOptionPane.showInputDialog(productDetailDialogFrame,
                    "Nhập tên Loại Sản Phẩm mới:", "Thêm Loại Sản Phẩm", JOptionPane.PLAIN_MESSAGE);
            if (ValidationUtil.validateAddCategory(name)) {
                boolean ok = categoryService.addCategory(name.trim(), "Đang sử dụng");
                if (ok) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm loại sản phẩm thành công!"); loadDialogData(); }
                else    { JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm thất bại. Có thể do lỗi kết nối CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE); }
            } else if (name != null) {
                JOptionPane.showMessageDialog(productDetailDialogFrame, "Tên loại sản phẩm không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Thêm Nhóm Tùy Chọn
        productDetailDialogFrame.addAddOptionGroupListener(e -> {
            String name = JOptionPane.showInputDialog(productDetailDialogFrame,
                    "Nhập tên Nhóm Tùy Chọn mới (VD: Kích cỡ, Topping):", "Thêm Nhóm Tùy Chọn", JOptionPane.PLAIN_MESSAGE);
            if (ValidationUtil.validateAddOptionGroup(name)) {
                boolean ok = optionService.addOptionGroup(name.trim());
                if (ok) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm nhóm tùy chọn thành công!"); loadDialogData(); }
                else    { JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE); }
            } else if (name != null) {
                JOptionPane.showMessageDialog(productDetailDialogFrame, "Tên nhóm tùy chọn không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Thêm Tùy Chọn chi tiết
        productDetailDialogFrame.addAddOptionListener(e -> {
            ArrayList<OptionGroupModel> groups = optionService.getAllOptionGroups();
            Object[] result = productDetailDialogFrame.showAddOptionDialog(groups);
            if (result == null) return;

            String groupName  = (String) result[0];
            String optionName = (String) result[1];
            String priceStr   = (String) result[2];

            if (groupName == null || optionName.isEmpty()) {
                JOptionPane.showMessageDialog(productDetailDialogFrame, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                double extraPrice = Double.parseDouble(priceStr);
                int groupID = optionService.getGroupIdByName(groupName);
                if (groupID <= 0) {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Không tìm thấy mã nhóm hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean ok = optionService.addOption(groupID, optionName, extraPrice, "Đang sử dụng");
                if (ok) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm Tùy chọn thành công!"); loadDialogData(); }
                else    { JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE); }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(productDetailDialogFrame, "Giá phụ thu phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ── Bảng Loại Sản Phẩm ──────────────────────────────────────────
        productDetailDialogFrame.setCategoryTableListener(new TableActionSupport.SplitActionListener() {

            @Override
            public void onEdit(int row) {
                int    id      = productDetailDialogFrame.getCategoryIdAt(row);
                String curName = productDetailDialogFrame.getCategoryNameAt(row);

                Object[] result = productDetailDialogFrame.showEditCategoryDialog(curName);
                if (result == null) return;

                String newName = (String) result[0];
                if (newName.isEmpty()) {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Tên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean ok = categoryService.updateCategory(id, newName, (String) result[1]);
                if (ok) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thành công!"); loadDialogData(); loadMainMenuData(); }
                else    { JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE); }
            }

            @Override
            public void onDelete(int row) {
                int    id   = productDetailDialogFrame.getCategoryIdAt(row);
                String name = productDetailDialogFrame.getCategoryNameAt(row);
                if (id == -1) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Vui lòng chọn một dòng trên bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE); return; }

                int confirm = JOptionPane.showConfirmDialog(productDetailDialogFrame,
                        "Bạn có chắc chắn muốn xóa danh mục: [" + name + "]?\nThao tác này không thể hoàn tác.",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean ok = categoryService.deleteCategory(id);
                    if (ok) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thành công!"); loadDialogData(); }
                    else    { JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thất bại! Danh mục này đang được sử dụng.", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE); }
                }
            }
        });

        // ── Bảng Nhóm Tùy Chọn ──────────────────────────────────────────
        productDetailDialogFrame.setOptionGroupTableListener(new TableActionSupport.SplitActionListener() {

            @Override
            public void onEdit(int row) {
                int    id      = productDetailDialogFrame.getOptionGroupIdAt(row);
                String curName = productDetailDialogFrame.getOptionGroupNameAt(row);

                String newName = productDetailDialogFrame.showEditOptionGroupDialog(curName);
                if (newName == null) return;

                boolean ok = optionService.updateOptionGroup(id, newName);
                if (ok) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thành công!"); loadDialogData(); }
                else    { JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE); }
            }

            @Override
            public void onDelete(int row) {
                int    id   = productDetailDialogFrame.getOptionGroupIdAt(row);
                String name = productDetailDialogFrame.getOptionGroupNameAt(row);
                if (id == -1) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Vui lòng chọn một dòng trên bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE); return; }

                int confirm = JOptionPane.showConfirmDialog(productDetailDialogFrame,
                        "Bạn có chắc chắn muốn xóa nhóm: [" + name + "]?\nThao tác này không thể hoàn tác.",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean ok = optionService.deleteOptionGroup(id);
                    if (ok) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thành công!"); loadDialogData(); }
                    else    { JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thất bại! Nhóm đang được sử dụng.", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE); }
                }
            }
        });

        // ── Bảng Tùy Chọn Chi Tiết ──────────────────────────────────────
        productDetailDialogFrame.setOptionTableListener(new TableActionSupport.SplitActionListener() {

            @Override
            public void onEdit(int row) {
                int    id      = productDetailDialogFrame.getOptionIdAt(row);
                String curName = productDetailDialogFrame.getOptionNameAt(row);
                ArrayList<OptionGroupModel> groups = optionService.getAllOptionGroups();

                Object[] result = productDetailDialogFrame.showEditOptionDialog(curName, groups);
                if (result == null) return;

                String selectedGroup = (String) result[0];
                String newName       = (String) result[1];
                if (selectedGroup == null || newName.isEmpty()) return;

                try {
                    double newPrice = Double.parseDouble((String) result[2]);
                    int groupID = optionService.getGroupIdByName(selectedGroup);
                    boolean ok = optionService.updateOptionDetail(id, groupID, newName, newPrice, (String) result[3]);
                    if (ok) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thành công!"); loadDialogData(); }
                    else    { JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE); }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Giá phụ thu không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            public void onDelete(int row) {
                int    id   = productDetailDialogFrame.getOptionIdAt(row);
                String name = productDetailDialogFrame.getOptionNameAt(row);
                if (id == -1) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Vui lòng chọn một dòng trên bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE); return; }

                int confirm = JOptionPane.showConfirmDialog(productDetailDialogFrame,
                        "Bạn có chắc chắn muốn xóa tùy chọn: [" + name + "]?\nThao tác này không thể hoàn tác.",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean ok = optionService.deleteOptionDetail(id);
                    if (ok) { JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thành công!"); loadDialogData(); }
                    else    { JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thất bại! Tùy chọn đang được sử dụng.", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE); }
                }
            }
        });
    }

    private void loadMainMenuData() {
        productList = productService.getProductList();
        menuPanel.displayProductList(productList);
    }

    private void loadDialogData() {
        this.selectedFile = null;
        productDetailDialogFrame.loadCategoryData(categoryService.getAllCategory());
        productDetailDialogFrame.loadOptionGroupData(optionService.getAllOptionGroups());
        productDetailDialogFrame.loadOptionData(optionService.getOption());
    }

    private void log(Exception ex) {
        System.getLogger(ProductController.class.getName())
              .log(System.Logger.Level.ERROR, (String) null, ex);
    }
}