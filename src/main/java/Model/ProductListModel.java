/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.util.ArrayList;

/**
 *
 * @author FAKK
 */
public class ProductListModel {
    private ArrayList<ProductModel> productList;

    public ProductListModel() {
        productList = new ArrayList<>();
    }

    public ProductListModel(ArrayList<ProductModel> productList) {
        this.productList = productList;
    }

    public ArrayList<ProductModel> getProductList() {
        return productList;
    }

    public void setProductList(ArrayList<ProductModel> productList) {
        this.productList = productList;
    }
    
    public void addProductList(ProductModel product){
        this.productList.add(product);
    }
    
    public void addProduct(ProductModel product){
        this.productList.add(product);
    }
    
}
