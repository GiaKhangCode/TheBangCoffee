package Model;

public class VariantModel {
    private int variantID;
    private int productID;
    private String sizeName; 
    
    // 3 Types of Prices
    private long dineInPrice; 
    private long takeawayPrice;
    private long holidayPrice;

    private boolean hasRecipe; // [MỚI] Đánh dấu size này đã có công thức chưa

    public VariantModel(int variantID, int productID, String sizeName, long dineInPrice, long takeawayPrice, long holidayPrice) {
        this(variantID, productID, sizeName, dineInPrice, takeawayPrice, holidayPrice, false);
    }
    
    public VariantModel(int variantID, int productID, String sizeName, long dineInPrice, long takeawayPrice, long holidayPrice, boolean hasRecipe) {
        this.variantID = variantID;
        this.productID = productID;
        this.sizeName = sizeName;
        this.dineInPrice = dineInPrice;
        this.takeawayPrice = takeawayPrice;
        this.holidayPrice = holidayPrice;
        this.hasRecipe = hasRecipe;
    }

    public int getVariantID() { return variantID; }
    public void setVariantID(int variantID) { this.variantID = variantID; }

    public int getProductID() { return productID; }
    public void setProductID(int productID) { this.productID = productID; }

    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }
    
    // Thêm hàm này để AutoCompleteComboBox biết cách hiển thị tên Size lên giao diện
    @Override
    public String toString() {
        // Trả về tên biến chứa tên Size của bạn (ở đây mình ví dụ là sizeName)
        return this.sizeName; 
    }

    // --- GETTERS & SETTERS FOR PRICES ---
    public long getDineInPrice() { return dineInPrice; }
    public void setDineInPrice(long dineInPrice) { this.dineInPrice = dineInPrice; }

    public long getTakeawayPrice() { return takeawayPrice; }
    public void setTakeawayPrice(long takeawayPrice) { this.takeawayPrice = takeawayPrice; }

    public long getHolidayPrice() { return holidayPrice; }
    public void setHolidayPrice(long holidayPrice) { this.holidayPrice = holidayPrice; }
    // --------------------------------------

    public boolean isHasRecipe() { return hasRecipe; }
    public void setHasRecipe(boolean hasRecipe) { this.hasRecipe = hasRecipe; }
}