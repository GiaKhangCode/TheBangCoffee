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
public class ProductCategoryListModel {
    private ArrayList<String> productCategoryList;

    public ProductCategoryListModel(ArrayList<String> productCategoryList) {
        this.productCategoryList = productCategoryList;
    }

    public ProductCategoryListModel() {
        productCategoryList = new ArrayList<>();
    }

    public ArrayList<String> getProductCategoryList() {
        return productCategoryList;
    }

    public void setProductCategoryList(ArrayList<String> productCategoryList) {
        this.productCategoryList = productCategoryList;
    }
}
