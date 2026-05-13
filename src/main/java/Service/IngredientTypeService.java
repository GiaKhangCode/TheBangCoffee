package Service;

import DatabaseAccessObject.IngredientTypeDAO;
import Model.IngredientTypeModel;
import java.sql.SQLException;
import java.util.List;

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
    
    // [MỚI] Gọi hàm cập nhật từ DAO
    public boolean updateIngredientType(int id, String newName) {
        return ingredientTypeDAO.updateIngredientType(id, newName);
    }
}