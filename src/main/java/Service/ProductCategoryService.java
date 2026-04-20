/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DatabaseAccessObject.ProductCategoryDAO;
import Model.ProductCategoryListModel;

/**
 *
 * @author FAKK
 */
public class ProductCategoryService {
    private ProductCategoryDAO productCategoryDAO;
    
    public ProductCategoryService(){
        productCategoryDAO = new ProductCategoryDAO();
    }
    
    public ProductCategoryListModel getProductCategory(){
        ProductCategoryListModel productCategoryList = new ProductCategoryListModel(productCategoryDAO.getProductCategory());
        return productCategoryList;
    }
}
