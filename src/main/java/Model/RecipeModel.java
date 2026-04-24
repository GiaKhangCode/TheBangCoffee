package Model;

public class RecipeModel {
    private int ingredientID;
    private String ingredientName;
    private String unit;
    private double quantitative;
    private double price;

    public RecipeModel() {
        this.ingredientName = this.unit = "";
        this.ingredientID = -1;
        this.quantitative = this.price = 0.0;
    }

    public RecipeModel(int ingredientID, String ingredientName, String unit, double quantitative, double price) {
        this.ingredientID = ingredientID;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.quantitative = quantitative;
        this.price = price;
    }

    public int getIngredientID() { 
        return ingredientID; 
    }
    
    public void setIngredientID(int ingredientID) { 
        this.ingredientID = ingredientID; 
    }

    public String getIngredientName() { 
        return ingredientName; 
    }
    
    public void setIngredientName(String ingredientName) { 
        this.ingredientName = ingredientName; 
    }

    public String getUnit() { 
        return unit; 
    }
    
    public void setUnit(String unit) { 
        this.unit = unit; 
    }

    public double getQuantitative() { 
        return quantitative; 
    }
    
    public void setQuantitative(double quantitative) { 
        this.quantitative = quantitative; 
    }

    public double getPrice() { 
        return price; 
    }
    
    public void setPrice(double price) { 
        this.price = price; 
    }
}