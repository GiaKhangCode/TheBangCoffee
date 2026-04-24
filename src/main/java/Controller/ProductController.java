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
import View.MenuPanel;
import View.MainFrame;
import View.ProductDetailDialog;
import View.ProductEditDialog;
import java.awt.GridLayout;
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
    private File selectedFile;
    private OptionService optionService;
    
    public ProductController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.menuPanel = mainFrame.getMenuPanel();
        this.productService = new ProductService();
        this.categoryService = new ProductCategoryService();
        this.optionService = new OptionService();
        this.ingredientService = new IngredientService();
        
        this.productDetailDialogFrame = new ProductDetailDialog(mainFrame);
        this.selectedFile = null;
        
        initListeners();
        loadMainMenuData();
    }

    private void initListeners() {
        // Nút thêm sản phẩm
        menuPanel.addAddProductListener(e -> {
            loadDialogData();
            productDetailDialogFrame.clearForm();
            productDetailDialogFrame.setVisible(true);
        });

        // Click vào card sản phẩm
        menuPanel.setProductClickListener(product -> {
            ProductEditDialog editDialog = new ProductEditDialog(mainFrame);
            List<String> categoryNames = categoryService.getAllCategory().getCategoryNames();
            editDialog.setCategoryList(categoryNames);
            
            try {
                editDialog.loadOptionCheckboxes(optionService.getOption(), optionService.getSelectedOptionByID(product.getProductID()));
            } catch (SQLException ex) {
                System.getLogger(ProductController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            } catch (ClassNotFoundException ex) {
                System.getLogger(ProductController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            
            editDialog.setProductData(product.getProductName(), product.getCategoryName(), product.getBasicPrice(), product.getProductStatus(), product.getDescription(),product.getImageData());
            editDialog.setIngredientList(ingredientService.getIngredientNames());
            editDialog.addChooseImageListener(ev -> {
                selectedFile = productService.chooseImageFile();
                if (selectedFile != null) {
                    editDialog.setImage(new ImageIcon(selectedFile.getAbsolutePath()));
                }
            });
            
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
                
                productService.updateProduct(product.getProductID(),category, productName, price, selectedFile, status, description, selectedOptions);
                JOptionPane.showMessageDialog(editDialog, "Cập nhật sản phẩm thành công!");
                editDialog.dispose();
                loadMainMenuData();
            });

            editDialog.addDeleteListener(ev -> {
                int confirm = JOptionPane.showConfirmDialog(editDialog, "Bạn có chắc chắn muốn xóa sản phẩm [" + product.getProductName() + "]?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        productService.deleteProduct(product.getProductID());
                    } catch (SQLException ex) {
                        System.getLogger(ProductController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    } catch (ClassNotFoundException ex) {
                        System.getLogger(ProductController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                    JOptionPane.showMessageDialog(editDialog, "Xóa sản phẩm thành công!");
                    editDialog.dispose();
                    loadMainMenuData();
                }
            });

            editDialog.setVisible(true);
        });
        
        productDetailDialogFrame.addChooseImageListener(e -> {
            selectedFile = productService.chooseImageFile();
            if (selectedFile != null) {
                ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                productDetailDialogFrame.setImage(icon);
            }
        });
        
        productDetailDialogFrame.addSaveListener(e -> {
            String productName = productDetailDialogFrame.getProductName();
            double basicPrice = productDetailDialogFrame.getPrice();
            String category = productDetailDialogFrame.getCategory();
            String status = productDetailDialogFrame.getStatus();
            HashMap<String, List<String>> selectedOptions = productDetailDialogFrame.getSelectedOptionNamesByGroup();
            String description = productDetailDialogFrame.getDescription();
            String validateProductDetail = ValidationUtil.validateProductDetail(productName, basicPrice, category, status);
            if (!(validateProductDetail.equalsIgnoreCase("Hợp lệ"))) {
                JOptionPane.showMessageDialog(productDetailDialogFrame, validateProductDetail);
                return;
            }
            productService.insertProduct(category, productName, basicPrice, selectedFile, status, selectedOptions, description);
            JOptionPane.showMessageDialog(productDetailDialogFrame, "Lưu thành công");
            loadMainMenuData();
        });
        
        productDetailDialogFrame.addAddCategoryListener(e -> {
            String newCategoryName = JOptionPane.showInputDialog(
                    productDetailDialogFrame, 
                    "Nhập tên Loại Sản Phẩm mới:", 
                    "Thêm Loại Sản Phẩm", 
                    JOptionPane.PLAIN_MESSAGE
            );
            
            if (ValidationUtil.validateAddCategory(newCategoryName)) {
                newCategoryName = newCategoryName.trim();
                boolean isSuccess = categoryService.addCategory(newCategoryName, "Đang sử dụng");
                
                if (isSuccess) {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm loại sản phẩm thành công!"); 
                    loadDialogData();
                } 
                else {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm thất bại. Có thể do lỗi kết nối CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } 
            else if (newCategoryName != null) {
                JOptionPane.showMessageDialog(productDetailDialogFrame, "Tên loại sản phẩm không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        productDetailDialogFrame.addAddOptionGroupListener(e -> {
            String newGroupName = JOptionPane.showInputDialog(
                    productDetailDialogFrame, 
                    "Nhập tên Nhóm Tùy Chọn mới (VD: Kích cỡ, Topping):", 
                    "Thêm Nhóm Tùy Chọn", 
                    JOptionPane.PLAIN_MESSAGE
            );
            
            if  (ValidationUtil.validateAddOptionGroup(newGroupName)){
                boolean isSuccess = optionService.addOptionGroup(newGroupName.trim());
                if (isSuccess) {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm nhóm tùy chọn thành công!");
                    loadDialogData();
                } 
                else {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } 
            else if (newGroupName != null){
                JOptionPane.showMessageDialog(productDetailDialogFrame, "Tên nhóm tùy chọn không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        productDetailDialogFrame.addAddOptionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            JComboBox<String> cbNhom = new JComboBox<>();
            
            ArrayList<OptionGroupModel> groups = optionService.getAllOptionGroups();
            if (groups != null) {
                for (OptionGroupModel groupName : groups) {
                    cbNhom.addItem(groupName.getOptionGroupName());
                }
            }
            
            JTextField txtTenTuyChon = new JTextField();
            JTextField txtPhuThu = new JTextField("0"); 
            
            panel.add(new JLabel("Chọn Nhóm Tùy Chọn:"));
            panel.add(cbNhom);
            panel.add(new JLabel("Tên Tùy Chọn (VD: Size L, Trân châu đen):"));
            panel.add(txtTenTuyChon);
            panel.add(new JLabel("Giá Phụ Thu (VNĐ):"));
            panel.add(txtPhuThu);

            int result = JOptionPane.showConfirmDialog(
                    productDetailDialogFrame, panel, "Thêm Tùy Chọn Mới", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String tenNhom = (String) cbNhom.getSelectedItem();
                String tenTuyChon = txtTenTuyChon.getText().trim();
                double phuThu = 0;

                if (tenNhom == null || tenTuyChon.isEmpty()) {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    phuThu = Double.parseDouble(txtPhuThu.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Giá phụ thu phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int maNhom = optionService.getGroupIdByName(tenNhom); 
                if (maNhom <= 0) {
                     JOptionPane.showMessageDialog(productDetailDialogFrame, "Không tìm thấy mã nhóm hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                     return;
                }

                boolean isSuccess = optionService.addOption(maNhom, tenTuyChon, phuThu, "Đang sử dụng");
                if (isSuccess) {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm Tùy chọn thành công!");
                    loadDialogData();
                } else {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        productDetailDialogFrame.setCategoryTableListener(new ProductDetailDialog.ProductActionListener() {
            @Override
            public void onEdit(int row) {
                int selectedId = productDetailDialogFrame.getCategoryIdAt(row);
                String currentName = productDetailDialogFrame.getCategoryNameAt(row);
                JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
                JTextField txtName = new JTextField(currentName);
                JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đang sử dụng", "Chưa sử dụng"});

                panel.add(new JLabel("Tên Loại Sản Phẩm:")); panel.add(txtName);
                panel.add(new JLabel("Trạng thái:")); panel.add(cbStatus);

                int result = JOptionPane.showConfirmDialog(productDetailDialogFrame, panel, "Sửa Loại Sản Phẩm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                
                if (result == JOptionPane.OK_OPTION) {
                    String newName = txtName.getText().trim();
                    if (newName.isEmpty()) {
                        JOptionPane.showMessageDialog(productDetailDialogFrame, "Tên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    boolean isSuccess = categoryService.updateCategory(selectedId, newName, (String) cbStatus.getSelectedItem());
                    if (isSuccess) {
                        JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thành công!");
                        loadDialogData();
                        loadMainMenuData();
                    } else {
                        JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            @Override
            public void onDelete(int row) {
                int selectedId = productDetailDialogFrame.getCategoryIdAt(row);

                if (selectedId != -1) {
                    String catName = productDetailDialogFrame.getCategoryNameAt(row);

                    int confirm = JOptionPane.showConfirmDialog(
                            productDetailDialogFrame, 
                            "Bạn có chắc chắn muốn xóa danh mục: [" + catName + "]?\nThao tác này không thể hoàn tác.", 
                            "Xác nhận xóa", 
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean isSuccess = categoryService.deleteCategory(selectedId);
                        if (isSuccess) {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thành công!");
                            loadDialogData();
                        } else {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thất bại! Danh mục này đang được sử dụng cho các sản phẩm hiện có.", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Vui lòng chọn một dòng trên bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        productDetailDialogFrame.setOptionGroupTableListener(new ProductDetailDialog.ProductActionListener() {
            @Override
            public void onEdit(int row) {
                int selectedId = productDetailDialogFrame.getOptionGroupIdAt(row);
                String catName = productDetailDialogFrame.getOptionGroupNameAt(row);

                String newName = JOptionPane.showInputDialog(productDetailDialogFrame, "Sửa Tên Nhóm Tùy Chọn:", catName);
                if (newName != null && !newName.trim().isEmpty()) {
                    if (optionService.updateOptionGroup(selectedId, newName.trim())) {
                        JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thành công!");
                        loadDialogData();
                    } else {
                        JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            @Override
            public void onDelete(int row) {
                int selectedId = productDetailDialogFrame.getOptionGroupIdAt(row);

                if (selectedId != -1) {
                    String catName = productDetailDialogFrame.getOptionGroupNameAt(row);

                    int confirm = JOptionPane.showConfirmDialog(
                            productDetailDialogFrame, 
                            "Bạn có chắc chắn muốn xóa danh mục: [" + catName + "]?\nThao tác này không thể hoàn tác.", 
                            "Xác nhận xóa", 
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean isSuccess = optionService.deleteOptionGroup(selectedId);
                        if (isSuccess) {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thành công!");
                            loadDialogData();
                        } else {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thất bại! Danh mục này đang được sử dụng cho các sản phẩm hiện có.", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Vui lòng chọn một dòng trên bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        productDetailDialogFrame.setOptionTableListener(new ProductDetailDialog.ProductActionListener() {
            @Override
            public void onEdit(int row) {
                int selectedId = productDetailDialogFrame.getOptionIdAt(row);
                String currentName = productDetailDialogFrame.getOptionNameAt(row);

                JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
                JComboBox<String> cbGroup = new JComboBox<>();
                ArrayList<Model.OptionGroupModel> groups = optionService.getAllOptionGroups();
                if (groups != null) {
                    for (Model.OptionGroupModel group : groups) {
                        cbGroup.addItem(group.getOptionGroupName());
                    }
                }
                
                JTextField txtTenTuyChon = new JTextField(currentName);
                JTextField txtPhuThu = new JTextField();
                JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đang sử dụng", "Chưa sử dụng"});
                
                panel.add(new JLabel("Thuộc Nhóm:")); panel.add(cbGroup);
                panel.add(new JLabel("Tên Tùy Chọn:")); panel.add(txtTenTuyChon);
                panel.add(new JLabel("Giá Phụ Thu (VNĐ):")); panel.add(txtPhuThu);
                panel.add(new JLabel("Trạng Thái:")); panel.add(cbStatus);

                int result = JOptionPane.showConfirmDialog(productDetailDialogFrame, panel, "Sửa Tùy Chọn", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String selectedGroup = (String) cbGroup.getSelectedItem();
                    String newName = txtTenTuyChon.getText().trim();
                    if (selectedGroup == null || newName.isEmpty()) return;

                    try {
                        double newPrice = Double.parseDouble(txtPhuThu.getText().trim());
                        int groupID = optionService.getGroupIdByName(selectedGroup);
                        
                        if (optionService.updateOptionDetail(selectedId, groupID, newName, newPrice, (String) cbStatus.getSelectedItem())) {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thành công!");
                            loadDialogData();
                        } else {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(productDetailDialogFrame, "Giá phụ thu không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            @Override
            public void onDelete(int row) {
                int selectedId = productDetailDialogFrame.getOptionIdAt(row);

                if (selectedId != -1) {
                    String catName = productDetailDialogFrame.getOptionNameAt(row);

                    int confirm = JOptionPane.showConfirmDialog(
                            productDetailDialogFrame, 
                            "Bạn có chắc chắn muốn xóa danh mục: [" + catName + "]?\nThao tác này không thể hoàn tác.", 
                            "Xác nhận xóa", 
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean isSuccess = optionService.deleteOptionDetail(selectedId);
                        if (isSuccess) {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thành công!");
                            loadDialogData();
                        } else {
                            JOptionPane.showMessageDialog(productDetailDialogFrame, "Xóa thất bại! Danh mục này đang được sử dụng cho các sản phẩm hiện có.", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Vui lòng chọn một dòng trên bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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
        ProductCategoryListModel fullCategories = categoryService.getAllCategory();
        ArrayList<OptionGroupModel> groupList = optionService.getAllOptionGroups();
        HashMap<String, ArrayList<OptionModel>> optionHashMap = optionService.getOption();
        
        productDetailDialogFrame.loadCategoryData(fullCategories);
        productDetailDialogFrame.loadOptionGroupData(groupList);
        productDetailDialogFrame.loadOptionData(optionHashMap);
    }
}