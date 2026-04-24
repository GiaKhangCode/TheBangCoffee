/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author FAKK
 */
public class RecipeDAO {
    public List<Model.RecipeModel> getRecipeByProductId(int productId) {
        List<Model.RecipeModel> recipeList = new ArrayList<>();
        String sql = "{call CT_LAY_CONG_THUC_SAN_PHAM(?, ?)}";

        try (Connection conn = getMyConnection();
             java.sql.CallableStatement cs = conn.prepareCall(sql)) {
             
            cs.setInt(1, productId);
            
            cs.registerOutParameter(2, java.sql.Types.REF_CURSOR);
            
            cs.execute();
            
            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                while (rs.next()) {
                    Model.RecipeModel recipe = new Model.RecipeModel();
                    recipe.setIngredientID(rs.getInt("MaNguyenLieu"));
                    recipe.setIngredientName(rs.getString("TenNguyenLieu"));
                    recipe.setUnit(rs.getString("DonViCongThuc"));
                    recipe.setQuantitative(rs.getDouble("DinhLuong"));
                    recipe.setPrice(rs.getDouble("ThanhTien"));
                    
                    recipeList.add(recipe);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return recipeList;
    }
    
    public boolean upsertRecipe(int productId, int ingredientId, String unit, double quantitative) {
        String sql = "MERGE INTO CONG_THUC C " +
                     "USING (SELECT ? AS MaSanPham, ? AS MaNguyenLieu, ? AS DinhLuong, ? AS DonViTinh FROM dual) input " +
                     "ON (C.MaSanPham = input.MaSanPham AND C.MaNguyenLieu = input.MaNguyenLieu) " +
                     "WHEN MATCHED THEN " +
                     "    UPDATE SET C.DinhLuong = input.DinhLuong, C.DonViTinh = input.DonViTinh " +
                     "WHEN NOT MATCHED THEN " +
                     "    INSERT (MaSanPham, MaNguyenLieu, DinhLuong, DonViTinh) " +
                     "    VALUES (input.MaSanPham, input.MaNguyenLieu, input.DinhLuong, input.DonViTinh)";

        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, productId);
            ps.setInt(2, ingredientId);
            ps.setDouble(3, quantitative);
            ps.setString(4, unit);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch(ClassNotFoundException c){
            c.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteRecipe(int productId, int ingredientId) {
        String sql = "DELETE FROM CONG_THUC WHERE MaSanPham = ? AND MaNguyenLieu = ?";
        
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, productId);
            ps.setInt(2, ingredientId);
            
            return ps.executeUpdate() > 0;
            
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        } 
        catch (ClassNotFoundException c) {
            c.printStackTrace();
            return false;
        }
    }
}
