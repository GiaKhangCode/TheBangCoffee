/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DatabaseAccessObject.IngredientDAO;
import Model.IngredientModel;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Kiet
 */
public class IngredientService {
    private IngredientDAO ingredientDAO;
    
    public IngredientService(){
        this.ingredientDAO = new IngredientDAO();
    }
    
    public List<IngredientModel> getIngredientList() throws SQLException{
            return ingredientDAO.getIngredient();
    }
    
    // Thêm hàm này vào class IngredientService
    public boolean updateIngredient(int maNL, String tenMoi, String dvtMoi, int tonKhoMoi, int nguongMoi, int maTaiKhoan, String lyDo) {
        return ingredientDAO.updateIngredientWithLog(maNL, tenMoi, dvtMoi, tonKhoMoi, nguongMoi, maTaiKhoan, lyDo);
    }
    
    public boolean deleteIngredient(int maNL, int currentUserID, String lyDo) {
        return ingredientDAO.deleteIngredientWithLog(maNL, currentUserID, lyDo);
    }
}



