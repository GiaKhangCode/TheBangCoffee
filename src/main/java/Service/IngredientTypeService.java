/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DatabaseAccessObject.IngredientTypeDAO;
import Model.IngredientTypeModel;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Kiet
 */
public class IngredientTypeService {
    private IngredientTypeDAO ingredientTypeDAO;
    
    public IngredientTypeService() { 
        this.ingredientTypeDAO = new IngredientTypeDAO(); 
    }
    
    public List<IngredientTypeModel> getIngredientTypes() throws SQLException {
        return ingredientTypeDAO.getIngredientTypes();
    }
    
    public boolean addIngredientType(String typeName) throws SQLException, ClassNotFoundException {
        return ingredientTypeDAO.insertIngredientType(typeName);
    }
}