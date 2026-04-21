/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.IngredientModel;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import javax.swing.JOptionPane;

/**
 *
 * @author Kiet
 */
public class IngredientDAO {
    public List<IngredientModel> getIngredient() throws SQLException{
        ArrayList<IngredientModel> ingredientList = new ArrayList<>();
        String query = "SELECT *"
                + "FROM NGUYEN_LIEU "
                + "ORDER BY MaNguyenLieu";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            while(rs.next()){
                IngredientModel t = new IngredientModel(rs.getInt("MaNguyenLieu"),
                                  rs.getString("TenNguyenLieu"),
                                  rs.getString("DonViTinh"),
                                  rs.getInt("SoLuongTon"),
                                  rs.getInt("NguongCanhBao"));
                ingredientList.add(t);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return ingredientList;
    }
    
    public boolean deleteIngredient(int ingredientID) {
        try {
            Connection conn = getMyConnection();
            String sql = "DELETE FROM NGUYEN_LIEU WHERE MaNguyenLieu = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, ingredientID);

            int rowAffected = ps.executeUpdate(); // Thực hiện lệnh xóa
            conn.close();
            return rowAffected > 0; // Trả về true nếu có ít nhất 1 dòng bị xóa
        } 
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateIngredientWithLog(int ingredientID, String newName, String newUnit, int newInventory, int newThreshold, int accountID, String reason) {
        String sql = "{CALL SP_SUA_NGUYEN_LIEU(?, ?, ?, ?, ?, ?, ?, ?)}"; // 8 tham số (7 IN, 1 OUT)
        
  
        try (Connection conn = getMyConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, ingredientID);
            cs.setString(2, newName);
            cs.setString(3, newUnit);
            cs.setInt(4, newInventory);
            cs.setInt(5, newThreshold);
            cs.setInt(6, accountID); // Truyền ID người dùng đang thao tác
            cs.setString(7, reason);
            
            // Khai báo tham số hứng kết quả (Tương ứng với OUT trong Procedure)
            cs.registerOutParameter(8, Types.NVARCHAR);
            
            cs.execute();
            
            String ketQua = cs.getString(8);
            
            if (ketQua.equals("Thành công")) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, ketQua, "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi kết nối: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteIngredientWithLog(int ingredientID, int accountID, String reason) {
    String sql = "{CALL SP_XOA_NGUYEN_LIEU(?, ?, ?, ?)}";
    try (Connection conn = getMyConnection();
         CallableStatement cs = conn.prepareCall(sql)) {
        
        cs.setInt(1, ingredientID);
        cs.setInt(2, accountID);
        cs.setString(3, reason);
        cs.registerOutParameter(4, java.sql.Types.NVARCHAR);
        
        cs.execute();
        String ketQua = cs.getString(4);
        
        if (ketQua.equals("Thành công")) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null, ketQua, "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Lỗi kết nối: " + e.getMessage());
        return false;
    }
}
}
