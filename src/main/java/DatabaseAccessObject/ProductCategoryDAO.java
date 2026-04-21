/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.CategoryModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author FAKK
 */
public class ProductCategoryDAO {
    public ArrayList<CategoryModel> getAllCategoriesFull() {
        String sql = "SELECT MaLoaiSanPham, TenLoaiSanPham, TrangThai FROM LOAI_SAN_PHAM";
        ArrayList<Model.CategoryModel> list = new ArrayList<>();
        
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while(rs.next()){
                list.add(new Model.CategoryModel(
                    rs.getInt("MaLoaiSanPham"),
                    rs.getString("TenLoaiSanPham"),
                    rs.getString("TrangThai")
                ));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }
    public boolean insertCategory(String categoryName, String status) {
        String sql = "INSERT INTO LOAI_SAN_PHAM (TenLoaiSanPham, TrangThai) VALUES (?, ?)";
        
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, categoryName);
            ps.setString(2, status);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM LOAI_SAN_PHAM WHERE MaLoaiSanPham = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            System.out.println("Lỗi xóa Loại SP (Có thể do dính khóa ngoại): " + e.getMessage());
            return false;
        }
    }
}
