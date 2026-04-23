/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Kiet
 */
public class IngredientModel {
    private int ingredientID;
    private String ingredientName;
    private String unit;
    private int inStock;
    private int threshold;
    
    public IngredientModel(int ingredientID, String ingredientName, String unit, int inStock, int threshold) {
        this.ingredientID = ingredientID;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.inStock = inStock;
        this.threshold = threshold;
    }

    public int getIngredientID() {
        return ingredientID;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public String getUnit() {
        return unit;
    }

    public int getInStock() {
        return inStock;
    }

    public int getThreshold() {
        return threshold;
    }
    
    public String getStatus() {
        if (this.inStock > this.threshold){
            return "Còn hàng";
        }
        else
            return "Hết hàng";
    }
    
    
}
