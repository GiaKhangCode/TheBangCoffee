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
    private ArrayList<CategoryModel> productCategoryList;

    public ProductCategoryListModel(ArrayList<CategoryModel> productCategoryList) {
        this.productCategoryList = productCategoryList;
    }

    public ProductCategoryListModel() {
        productCategoryList = new ArrayList<>();
    }

    public ArrayList<CategoryModel> getProductCategoryList() {
        return productCategoryList;
    }
    
    public ArrayList<String> getCategoryNames() {
        ArrayList<String> categoryNames = new ArrayList<>();
        for(CategoryModel categoryModel : productCategoryList){
            categoryNames.add(categoryModel.getCategoryName());
        }
        return categoryNames;
    }

    public void setProductCategoryList(ArrayList<CategoryModel> productCategoryList) {
        this.productCategoryList = productCategoryList;
    }
}
