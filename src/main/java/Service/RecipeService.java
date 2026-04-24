/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DatabaseAccessObject.RecipeDAO;
import java.util.List;

/**
 *
 * @author FAKK
 */
public class RecipeService {
    private RecipeDAO recipeDAO;
    
    public RecipeService(){
        recipeDAO = new RecipeDAO();
    }
    
    public List<Model.RecipeModel> getRecipeByProductId(int productId){
        return recipeDAO.getRecipeByProductId(productId);
    }
    
    public boolean upsertRecipe(int productId, int ingredientId, String unit, double quantitative) {
        return recipeDAO.upsertRecipe(productId, ingredientId, unit, quantitative);
    }
    
}
