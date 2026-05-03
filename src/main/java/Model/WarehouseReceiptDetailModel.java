package Model;

import java.time.LocalDate;

public class WarehouseReceiptDetailModel {
    private Integer ingredientID; 
    private int ingredientTypeID; 
    private String ingredientType;
    private String ingredientName;
    private String unit;
    
    private double capacityPerUnit; 
    private int quantity; 
    
    private long preTaxPrice;     
    private double taxPercentage; 
    private long taxAmount;       
    private long totalPrice;      
    
    private int threshold;
    private String providerName;
    private LocalDate importingDate;
    private LocalDate expiryDate;

    public WarehouseReceiptDetailModel(int ingredientTypeID, String ingredientType, String ingredientName, String unit, 
                                       double capacityPerUnit, int quantity, long preTaxPrice, double taxPercentage, 
                                       long taxAmount, long totalPrice, int threshold, String providerName, 
                                       LocalDate importingDate, LocalDate expiryDate) {
        this.ingredientTypeID = ingredientTypeID;
        this.ingredientType = ingredientType;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.capacityPerUnit = capacityPerUnit;
        this.quantity = quantity;
        this.preTaxPrice = preTaxPrice;
        this.taxPercentage = taxPercentage;
        this.taxAmount = taxAmount;
        this.totalPrice = totalPrice;
        this.threshold = threshold;
        this.providerName = providerName;
        this.importingDate = importingDate;
        this.expiryDate = expiryDate;
    }

    public Integer getIngredientID() { return ingredientID; }
    public void setIngredientID(Integer ingredientID) { this.ingredientID = ingredientID; }

    public int getIngredientTypeID() { return ingredientTypeID; }
    public void setIngredientTypeID(int ingredientTypeID) { this.ingredientTypeID = ingredientTypeID; }

    public String getIngredientType() { return ingredientType; }
    public String getIngredientName() { return ingredientName; }
    public String getUnit() { return unit; }
    public double getCapacityPerUnit() { return capacityPerUnit; }
    public int getQuantity() { return quantity; }
    
    public long getPreTaxPrice() { return preTaxPrice; }
    public double getTaxPercentage() { return taxPercentage; }
    public long getTaxAmount() { return taxAmount; }
    public long getTotalPrice() { return totalPrice; }
    
    public int getThreshold() { return threshold; }
    public String getProviderName() { return providerName; }
    public LocalDate getImportingDate() { return importingDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
}