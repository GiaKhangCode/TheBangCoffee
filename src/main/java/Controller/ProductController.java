/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;
import Common.ValidationUtil;
import Model.OptionModel;
import Model.ProductListModel;
import Service.OptionService;
import Service.ProductCategoryService;
import Service.ProductService;
import View.MenuPanel;
import View.MainFrame;
import View.ProductDetailDialog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import java.util.List;
/**
 *
 * @author FAKK
 */

public class ProductController {

    private ProductService productService;
    private MenuPanel menuPanel;
    private MainFrame mainFrame;
    private ProductListModel productList;
    private ProductDetailDialog productDetailDialogFrame;
    private ProductCategoryService categoryService;
    private File selectedFile;
    private JFrame parent;
    private HashMap<String, ArrayList<OptionModel>> optionHashMap;
    private OptionService optionService;
    
    public ProductController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.menuPanel = mainFrame.getMenuPanel();
        this.productService = new ProductService();
        this.productList = productService.getProductList();
        
        this.parent = mainFrame;
        
        this.productDetailDialogFrame = new ProductDetailDialog(parent);
        this.categoryService = new ProductCategoryService();
        
        this.selectedFile = null;
        this.optionService = new OptionService();
        this.optionHashMap = optionService.getOption();
        
        initListeners();
        loadData();
        loadProductDetailDialogFrame();
    }

    private void initListeners() {

        // Nút thêm sản phẩm
        menuPanel.addAddProductListener(e -> {
            productDetailDialogFrame.clearForm();
            productDetailDialogFrame.setVisible(true);
        });

        // Click vào card
        menuPanel.setProductClickListener(product -> {
            View.ProductDetailDialog dialog = new View.ProductDetailDialog(parent);
            dialog.setCategoryList(categoryService
                .getProductCategory()
                .getProductCategoryList());

            dialog.setImage(product.getImageData());
            // Có thể set data vào dialog nếu cần
            dialog.setVisible(true);
            loadData();
        });
        
        productDetailDialogFrame.addChooseImageListener(e -> {
            selectedFile = productService.chooseImageFile();
            if (selectedFile != null) {
                ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                productDetailDialogFrame.setImage(icon);
            }
        });
        
        productDetailDialogFrame.addSaveListener(e -> {

            String productName = productDetailDialogFrame.getTenSanPham();
            double basicPrice = productDetailDialogFrame.getGiaBan();
            String category = productDetailDialogFrame.getLoaiSanPham();
            String status = productDetailDialogFrame.getTrangThai();
            
            String validateProductDetail = ValidationUtil.validateProductDetail(productName, basicPrice, category, status);
            if (!(validateProductDetail.equalsIgnoreCase("Hợp lệ"))) {
                JOptionPane.showMessageDialog(productDetailDialogFrame, validateProductDetail);
                return;
            }
            productService.insertProduct(category, productName, basicPrice, selectedFile, status);
            JOptionPane.showMessageDialog(productDetailDialogFrame, "Lưu thành công");
            loadData();
        });
    }

    private void loadData() {
        productList = productService.getProductList();
        menuPanel.displayProductList(productList);
    }
    private void loadProductDetailDialogFrame(){
        // Load category từ DB
        List<String> categories = categoryService
                .getProductCategory()
                .getProductCategoryList();

        productDetailDialogFrame.setCategoryList(categories);
        productDetailDialogFrame.setOptionGroups(optionHashMap);
    }

}
