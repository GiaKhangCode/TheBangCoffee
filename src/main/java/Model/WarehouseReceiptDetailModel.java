/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.time.LocalDate;

/**
 *
 * @author SONY
 */
public class WarehouseReceiptDetailModel {
    private Integer ingredientID;
    private Integer ingredientTypeID;
    private String ingredientName;
    private String unit;
    private int quantity;
    private long price;
    private int threshold;
    private String providerName;
    private LocalDate importingDate;

    public WarehouseReceiptDetailModel(String ingredientName, String unit, int quantity, long price, int threshold, String providerName, LocalDate importingDate) {
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.quantity = quantity;
        this.price = price;
        this.threshold = threshold;
        this.providerName = providerName;
        this.importingDate = importingDate;
    }

    public Integer getIngredientID() {
        return ingredientID;
    }

    public Integer getIngredientTypeID() {
        return ingredientTypeID;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public String getUnit() {
        return unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getPrice() {
        return price;
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

    
}


