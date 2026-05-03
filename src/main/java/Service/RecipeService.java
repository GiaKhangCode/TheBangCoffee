/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DatabaseAccessObject.RecipeDAO;
import Model.RecipeModel;
import java.sql.SQLException;
import java.util.List;

/**
 * @author FAKK
 */
public class RecipeService {
    private RecipeDAO recipeDAO;
    
    public RecipeService(){
        recipeDAO = new RecipeDAO();
    }
    
    // [SỬA] Đổi tham số thành VariantID
    public List<RecipeModel> getRecipeByVariantId(int variantId){
        return recipeDAO.getRecipeByVariantId(variantId);
    }
    
    // [SỬA] Đổi tham số thành VariantID và bỏ tham số unit
    public boolean upsertRecipe(int variantId, int ingredientId, double quantityRequired) throws SQLException, ClassNotFoundException {
        return recipeDAO.upsertRecipe(variantId, ingredientId, quantityRequired);
    }
    
    public boolean deleteRecipe(int variantId, int ingredientId) {
        return recipeDAO.deleteRecipe(variantId, ingredientId);
    }
}