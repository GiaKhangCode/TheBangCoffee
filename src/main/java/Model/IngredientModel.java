/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;


/**
 *
 * @author Kiet
 */
public class IngredientModel {
    private int ingredientID;
    private String ingredientName;
    private String unit;
    private int inventory;
    private int threshold;

    public IngredientModel(int ingredientID, String ingredientName, String unit, int inventory, int threshold) {
        this.ingredientID = ingredientID;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.inventory = inventory;
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

    public int getInventory() {
        return inventory;
    }

    public int getThreshold() {
        return threshold;
    }
    
    public String getTrangThai(){
        if (this.inventory < this.threshold){
            return "Hết hàng";
        }
        return "Còn hàng";
    }
    
}
