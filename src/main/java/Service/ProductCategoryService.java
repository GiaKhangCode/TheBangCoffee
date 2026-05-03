/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DatabaseAccessObject.ProductCategoryDAO;
import Model.ProductCategoryListModel;

/**
 * @author FAKK
 */
public class ProductCategoryService {
    private ProductCategoryDAO productCategoryDAO;
    
    public ProductCategoryService(){
        productCategoryDAO = new ProductCategoryDAO();
    }
    
    public ProductCategoryListModel getAllCategory(){
        return new ProductCategoryListModel(productCategoryDAO.getAllCategoriesFull());
    }
    
    // [SỬA LẠI] Thêm tham số defaultVat
    public boolean addCategory(String categoryName, String status, double defaultVat) {
        return productCategoryDAO.insertCategory(categoryName, status, defaultVat);
    }
    
    public boolean deleteCategory(int categoryId) {
        return productCategoryDAO.deleteCategory(categoryId);
    }
    
    // [SỬA LẠI] Thêm tham số defaultVat
    public boolean updateCategory(int categoryId, String newName, String status, double defaultVat) {
        return productCategoryDAO.updateCategory(categoryId, newName, status, defaultVat);
    }
}