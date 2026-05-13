package Model;

public class RecipeModel {
    private int variantID; // [SỬA LỚN] Ánh xạ vào MaBienThe
    private int ingredientID;
    private String ingredientName;
    private String unit;
    private double quantityRequired; // SoLuongCan

    public RecipeModel() {
        this.ingredientName = this.unit = "";
        this.variantID = -1;
        this.ingredientID = -1;
        this.quantityRequired = 0.0;
    }
    
    public RecipeModel(int variantID, int ingredientID, double quantityRequired) {
        this.variantID = variantID;
        this.ingredientID = ingredientID;
        this.quantityRequired = quantityRequired;
        this.ingredientName = "";
        this.unit = "";
    }

    public RecipeModel(int variantID, int ingredientID, String ingredientName, String unit, double quantityRequired) {
        this.variantID = variantID;
        this.ingredientID = ingredientID;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.quantityRequired = quantityRequired;
    }

    public int getVariantID() { return variantID; }
    public void setVariantID(int variantID) { this.variantID = variantID; }

    public int getIngredientID() { return ingredientID; }
    public void setIngredientID(int ingredientID) { this.ingredientID = ingredientID; }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getQuantityRequired() { return quantityRequired; }
    public void setQuantityRequired(double quantityRequired) { this.quantityRequired = quantityRequired; }
}