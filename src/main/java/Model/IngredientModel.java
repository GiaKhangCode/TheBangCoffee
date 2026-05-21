package Model;

public class IngredientModel {
    private int ingredientID;
    private String ingredientName;
    private String unit;
    private int inStock;
    private int threshold;
    private double averagePrice;
    
    // [MỚI] Thêm Thuế theo DB mới
    private double vat; 

    public IngredientModel(int ingredientID, String ingredientName, String unit, int inStock, int threshold, double vat, double averagePrice) {
        this.ingredientID = ingredientID;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.inStock = inStock;
        this.threshold = threshold;
        this.vat = vat;
        this.averagePrice = averagePrice;
    }

    public int getIngredientID() { return ingredientID; }
    public String getIngredientName() { return ingredientName; }
    public String getUnit() { return unit; }
    public int getInStock() { return inStock; }
    public int getThreshold() { return threshold; }
    public double getVat() { return vat; }
    public double getAveragePrice() { return averagePrice; }
    
    public String getStatus() {
        if (this.inStock > this.threshold) {
            return "Còn hàng";
        } else if (this.inStock > 0) {
            return "Sắp hết"; // Bạn có thể thêm trạng thái này cho hay
        } else {
            return "Hết hàng";
        }
    }
}