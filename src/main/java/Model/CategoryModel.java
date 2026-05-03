package Model;

public class CategoryModel {
    private int categoryID;
    private String categoryName;
    private String categoryStatus;
    private double defaultVat; // [MỚI] Thêm Thuế mặc định từ DB

    public CategoryModel(int categoryID, String categoryName, String categoryStatus, double defaultVat) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
        this.categoryStatus = categoryStatus;
        this.defaultVat = defaultVat;
    }

    public int getCategoryID() { return categoryID; }
    public void setCategoryID(int categoryID) { this.categoryID = categoryID; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryStatus() { return categoryStatus; }
    public void setCategoryStatus(String status) { this.categoryStatus = status; }
    
    public double getDefaultVat() { return defaultVat; }
    public void setDefaultVat(double defaultVat) { this.defaultVat = defaultVat; }
}