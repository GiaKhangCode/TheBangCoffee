/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.util.List;

/**
 *
 * @author Kiet
 */

public class IngredientListModel {
   private List<IngredientModel> ingredientList;

    public IngredientListModel(List<IngredientModel> ingredientList) {
        this.ingredientList = ingredientList;
    }

    public IngredientListModel() {}
   

    public List<IngredientModel> getIngredientList() {
        return ingredientList;
    }
   
   
}
