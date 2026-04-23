/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DatabaseAccessObject.ProductDAO;
import Model.ProductListModel;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author FAKK
 */
public class ProductService {
    private ProductDAO productDAO;
    public ProductService(){
        productDAO = new ProductDAO();
    }
    public ProductListModel getProductList(){
        return productDAO.getAllProduct();
    }
    
    public File chooseImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh để lưu vào Database");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
    
    public void insertProduct(String categoryName, String productName, double basicPrice, File imageFile, String status, HashMap<String, List<String>> selectedOptions, String description){
        productDAO.insertProduct(categoryName, productName, basicPrice, imageFile, status, selectedOptions, description);
    }
    
    public void updateProduct(int productId, String categoryName, String productName, double basicPrice, File imageFile, String status, String description, HashMap<String, List<String>> selectedOptions){
        productDAO.updateProduct(productId, categoryName, productName, basicPrice, imageFile, status, description, selectedOptions);
    }
    
    public void deleteProduct(int id) throws SQLException, ClassNotFoundException{
        productDAO.deleteProduct(id);
    }
}
