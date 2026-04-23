package Model;

import java.time.LocalDate;

public class WarehouseReceiptDetailModel {
    private Integer ingredientID;
    private int ingredientTypeID;
    private String ingredientType;
    private String ingredientName;
    private String unit;
    private int totalCapacity; 
    private int quantity;     
    private long totalPrice;  
    private int threshold;
    private String providerName;
    private LocalDate importingDate;
    private LocalDate expiryDate;

    public WarehouseReceiptDetailModel(int ingredientTypeID, String ingredientType, String ingredientName, String unit, int totalCapacity, int quantity, long totalPrice, int threshold, String providerName, LocalDate importingDate, LocalDate expiryDate) {
        this.ingredientTypeID = ingredientTypeID;
        this.ingredientType = ingredientType;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.totalCapacity = totalCapacity;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.threshold = threshold;
        this.providerName = providerName;
        this.importingDate = importingDate;
        this.expiryDate = expiryDate;
    }

    public Integer getIngredientID() {
        return ingredientID;
    }

    public int getIngredientTypeID() {
        return ingredientTypeID;
    }

    public String getIngredientType() {
        return ingredientType;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public String getUnit() {
        return unit;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getProviderName() {
        return providerName;
    }

    public LocalDate getImportingDate() {
        return importingDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    
}