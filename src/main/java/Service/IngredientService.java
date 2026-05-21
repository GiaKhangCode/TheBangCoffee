package Service;

import DatabaseAccessObject.IngredientDAO;
import Model.IngredientModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
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
    
    // [SỬA] Thêm tham số nhaCungCapMoi và thueMoi để khớp với DB mới
    public boolean updateIngredient(int maNL, String tenMoi, String dvtMoi, int tonKhoMoi, int nguongMoi, String nhaCungCapMoi, double thueMoi, int maTaiKhoan, String lyDo) {
        return ingredientDAO.updateIngredientWithLog(maNL, tenMoi, dvtMoi, tonKhoMoi, nguongMoi, nhaCungCapMoi, thueMoi, maTaiKhoan, lyDo);
    }
    
    public boolean deleteIngredient(int maNL, int currentUserID, String lyDo) {
        return ingredientDAO.deleteIngredientWithLog(maNL, currentUserID, lyDo);
    }
    
    public String getIngredientDetail(String ingredientName) {
        return ingredientDAO.getIngredientDetail(ingredientName);
    }
    
    public ArrayList<String> getIngredientNames(){
        return ingredientDAO.getIngredientNames();
    }
    
    public int getIngredientIdByName (String name){
        return ingredientDAO.getIngredientIdByName(name);
    }
    
    public String getUnitByName(String ingredientName) {
        return ingredientDAO.getUnitByName(ingredientName);
    }
    
    public double getAveragePrice(int ingredientId) {
        return ingredientDAO.getAveragePrice(ingredientId);
    }
    
    public boolean addIngredientMasterData(String categoryName, String ingredientName, String unit, int threshold) {
        return ingredientDAO.addIngredientMasterData(categoryName, ingredientName, unit, threshold);
    }
    
    public List<Object[]> getIngredientBatches(int maNL) {
        return ingredientDAO.getIngredientBatches(maNL);
    }
    
    public String disposeBatch(int maLo, double soLuong, String lyDo) {
        return ingredientDAO.disposeBatch(maLo, soLuong, lyDo);
    }
    
    public boolean updateProvider(int maNguyenLieu, String nhaCungCap) {
    return ingredientDAO.updateProvider(maNguyenLieu, nhaCungCap);
}
}