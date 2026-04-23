/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.IngredientTypeModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kiet
 */
public class IngredientTypeDAO {
    public List<IngredientTypeModel> getIngredientTypes() throws SQLException {
        List<IngredientTypeModel> list = new ArrayList<>();
        String query = "SELECT MaLoaiNguyenLieu, TenLoaiNguyenLieu "
                + "FROM LOAI_NGUYEN_LIEU "
                + "ORDER BY MaLoaiNguyenLieu";
        
        try (Connection conn = getMyConnection(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(query)) {
             
            while (rs.next()) {
                list.add(new IngredientTypeModel(rs.getInt("MaLoaiNguyenLieu"), 
                        rs.getString("TenLoaiNguyenLieu")));
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return list;
    }
}
