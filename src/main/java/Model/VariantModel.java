package Model;

public class VariantModel {
    private int variantID;
    private int productID;
    private String sizeName; 
    
    // 3 Types of Prices
    private long dineInPrice; 
    private long takeawayPrice;
    private long holidayPrice;

    public VariantModel(int variantID, int productID, String sizeName, long dineInPrice, long takeawayPrice, long holidayPrice) {
        this.variantID = variantID;
        this.productID = productID;
        this.sizeName = sizeName;
        this.dineInPrice = dineInPrice;
        this.takeawayPrice = takeawayPrice;
        this.holidayPrice = holidayPrice;
    }

    public int getVariantID() { return variantID; }
    public void setVariantID(int variantID) { this.variantID = variantID; }

    public int getProductID() { return productID; }
    public void setProductID(int productID) { this.productID = productID; }

    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }

    // --- GETTERS & SETTERS FOR PRICES ---
    public long getDineInPrice() { return dineInPrice; }
    public void setDineInPrice(long dineInPrice) { this.dineInPrice = dineInPrice; }

    public long getTakeawayPrice() { return takeawayPrice; }
    public void setTakeawayPrice(long takeawayPrice) { this.takeawayPrice = takeawayPrice; }

    public long getHolidayPrice() { return holidayPrice; }
    public void setHolidayPrice(long holidayPrice) { this.holidayPrice = holidayPrice; }
    // --------------------------------------
}