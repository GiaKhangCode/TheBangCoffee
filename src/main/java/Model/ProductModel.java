/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.io.File;
import javax.swing.ImageIcon;

/**
 *
 * @author FAKK
 */
public class ProductModel {
    private int productID;
    private String productName, imageName, imageType, productStatus, categoryName, categoryStatus;
    private double basicPrice;
    private ImageIcon imageData;

    public ProductModel(int productID, String productName, String imageName, String imageType, String productStatus, String categoryName, String categoryStatus, double basicPrice, ImageIcon imageData) {
        this.productID = productID;
        this.productName = productName;
        this.imageName = imageName;
        this.imageType = imageType;
        this.productStatus = productStatus;
        this.categoryName = categoryName;
        this.categoryStatus = categoryStatus;
        this.basicPrice = basicPrice;
        this.imageData = imageData;
    }

    public ProductModel() {
        this.productID = 0;
        this.productName = this.imageName = this.imageType = this.productStatus = this.categoryName = this.categoryStatus = "";
        this.basicPrice = 0.0;
        this.imageData = null;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryStatus() {
        return categoryStatus;
    }

    public void setCategoryStatus(String categoryStatus) {
        this.categoryStatus = categoryStatus;
    }

    public double getBasicPrice() {
        return basicPrice;
    }

    public void setBasicPrice(double basicPrice) {
        this.basicPrice = basicPrice;
    }

    public ImageIcon getImageData() {
        return imageData;
    }

    public void setImageData(ImageIcon imageData) {
        this.imageData = imageData;
    }
}
