/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;
import Model.ProductListModel;
import Model.ProductModel;
import Service.ProductCategoryService;
import Service.ProductService;
import View.MenuPanel;
import View.MainFrame;
import View.ProductDetailDialog;
import java.io.File;
import java.util.ArrayList;

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
    
    public ProductController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.menuPanel = mainFrame.getMenuPanel();
        this.productService = new ProductService();
        this.productList = productService.getProductList();
        
        this.parent = mainFrame;
        
        this.productDetailDialogFrame = new ProductDetailDialog(parent);
        this.categoryService = new ProductCategoryService();
        
        this.selectedFile = null;
        
        initListeners();
        loadData();
        loadCategory();
    }

    private void initListeners() {

        // Nút thêm sản phẩm
        menuPanel.addAddProductListener(e -> {
            productDetailDialogFrame.setVisible(true);
        });

        // Click vào card
        menuPanel.setProductClickListener(product -> {
            View.ProductDetailDialog dialog = new View.ProductDetailDialog(parent);
            dialog.setCategoryList(categoryService
                .getProductCategoryList()
                .getProductCategoryList());

            dialog.setImage(product.getImageData());
            // Có thể set data vào dialog nếu cần
            dialog.setVisible(true);
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
            
            
            if (productName.isEmpty() || productName.equals("")) {
                JOptionPane.showMessageDialog(productDetailDialogFrame, "Tên sản phẩm không được để trống");
                return;
            }
            productService.insertProduct(category, productName, basicPrice, selectedFile, status);
            JOptionPane.showMessageDialog(productDetailDialogFrame, "Lưu thành công");
            loadData();
        });
    }

    private void loadData() {
        productService.getProductList();
        menuPanel.displayProductList(productList);
    }
    private void loadCategory(){
        // Load category từ DB
        List<String> categories = categoryService
                .getProductCategoryList()
                .getProductCategoryList();

        productDetailDialogFrame.setCategoryList(categories);
    }
}
