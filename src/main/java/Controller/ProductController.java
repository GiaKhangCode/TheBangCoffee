package Controller;

import Common.ValidationUtil;
import Model.OptionModel;
import Model.ProductCategoryListModel;
import Model.ProductListModel;
import Model.OptionGroupModel;
import Service.OptionService;
import Service.ProductCategoryService;
import Service.ProductService;
import View.MenuPanel;
import View.MainFrame;
import View.ProductDetailDialog;
import java.awt.GridLayout;
import java.io.File;
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
    private File selectedFile;
    private OptionService optionService;
    
    public ProductController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.menuPanel = mainFrame.getMenuPanel();
        this.productService = new ProductService();
        this.categoryService = new ProductCategoryService();
        this.optionService = new OptionService();
        
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
            //CHƯA UPDATE CHƯA XỬ LÝ
            productDetailDialogFrame = new ProductDetailDialog(mainFrame);
            loadDialogData();
            productDetailDialogFrame.setImage(product.getImageData());
            productDetailDialogFrame.setVisible(true);
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
            
            String validateProductDetail = ValidationUtil.validateProductDetail(productName, basicPrice, category, status);
            if (!(validateProductDetail.equalsIgnoreCase("Hợp lệ"))) {
                JOptionPane.showMessageDialog(productDetailDialogFrame, validateProductDetail);
                return;
            }
            productService.insertProduct(category, productName, basicPrice, selectedFile, status, selectedOptions);
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
                    loadDialogData(); // Refresh lại View
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
                    loadDialogData(); // Refresh lại View
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
                    loadDialogData(); // Refresh lại View
                } else {
                    JOptionPane.showMessageDialog(productDetailDialogFrame, "Thêm thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // Tách riêng hàm load UI ngoài màn hình chính
    private void loadMainMenuData() {
        productList = productService.getProductList();
        menuPanel.displayProductList(productList);
    }
    
    // Đẩy TOÀN BỘ dữ liệu nguyên bản (Model) sang cho View tự xử lý hiển thị
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