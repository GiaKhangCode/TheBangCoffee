/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.RecipeModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecipeDAO {
    // [SỬA LỚN] Lấy công thức theo Mã Biến Thể (Size) chứ không lấy theo SP
    public List<RecipeModel> getRecipeByVariantId(int variantId) {
        List<RecipeModel> recipeList = new ArrayList<>();
        // Query trực tiếp, không cần dùng Procedure Cursor cho phức tạp
        String sql = "SELECT C.MaNguyenLieu, N.TenNguyenLieu, N.DonViTinh, C.SoLuongCan "
                   + "FROM CONG_THUC C "
                   + "JOIN NGUYEN_LIEU N ON C.MaNguyenLieu = N.MaNguyenLieu "
                   + "WHERE C.MaBienThe = ?";

        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, variantId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RecipeModel recipe = new RecipeModel(
                        variantId,
                        rs.getInt("MaNguyenLieu"),
                        rs.getString("TenNguyenLieu"),
                        rs.getString("DonViTinh"),
                        rs.getDouble("SoLuongCan")
                    );
                    recipeList.add(recipe);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recipeList;
    }
    
    // Dùng MERGE để thêm hoặc cập nhật công thức
    public boolean upsertRecipe(int variantId, int ingredientId, double quantityRequired) throws SQLException, ClassNotFoundException {
        String sql = "MERGE INTO CONG_THUC C " +
                     "USING (SELECT ? AS MaBienThe, ? AS MaNguyenLieu, ? AS SoLuongCan FROM dual) input " +
                     "ON (C.MaBienThe = input.MaBienThe AND C.MaNguyenLieu = input.MaNguyenLieu) " +
                     "WHEN MATCHED THEN " +
                     "    UPDATE SET C.SoLuongCan = input.SoLuongCan " +
                     "WHEN NOT MATCHED THEN " +
                     "    INSERT (MaBienThe, MaNguyenLieu, SoLuongCan) " +
                     "    VALUES (input.MaBienThe, input.MaNguyenLieu, input.SoLuongCan)";

        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, variantId);
            ps.setInt(2, ingredientId);
            ps.setDouble(3, quantityRequired);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } 
    }
    
    public boolean deleteRecipe(int variantId, int ingredientId) {
        String sql = "DELETE FROM CONG_THUC WHERE MaBienThe = ? AND MaNguyenLieu = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, variantId);
            ps.setInt(2, ingredientId);
            
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}