package Model;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

public class ProductModel {
    private int productID;
    private String productName, imageName, imageType, productStatus, categoryName, categoryStatus, description;
    
    private double vat;
    private ImageIcon imageData;
    
    private List<VariantModel> variants; 

    public ProductModel(int productID, String productName, String imageName, String imageType, String productStatus, 
                        String categoryName, String categoryStatus, double vat, ImageIcon imageData, String description) {
        this.productID = productID;
        this.productName = productName;
        this.imageName = imageName;
        this.imageType = imageType;
        this.productStatus = productStatus;
        this.categoryName = categoryName;
        this.categoryStatus = categoryStatus;
        this.vat = vat;
        this.imageData = imageData;
        this.description = description;
        this.variants = new ArrayList<>();
    }

    public ProductModel() {
        this.productID = 0;
        this.productName = this.imageName = this.imageType = this.productStatus = this.categoryName = this.categoryStatus = this.description = "";
        this.vat = 8.0;
        this.imageData = null;
        this.variants = new ArrayList<>();
    }

    public int getProductID() { return productID; }
    public void setProductID(int productID) { this.productID = productID; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }

    public String getProductStatus() { return productStatus; }
    public void setProductStatus(String productStatus) { this.productStatus = productStatus; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryStatus() { return categoryStatus; }
    public void setCategoryStatus(String categoryStatus) { this.categoryStatus = categoryStatus; }

    public double getVat() { return vat; }
    public void setVat(double vat) { this.vat = vat; }

    public ImageIcon getImageData() { return imageData; }
    public void setImageData(ImageIcon imageData) { this.imageData = imageData; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<VariantModel> getVariants() { return variants; }
    public void setVariants(List<VariantModel> variants) { this.variants = variants; }
    
    public void addVariant(VariantModel variant) { this.variants.add(variant); }
}