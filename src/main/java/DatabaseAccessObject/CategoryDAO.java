package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.CategoryModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    
    // Lấy tất cả danh mục
    public List<CategoryModel> getAllCategories() throws SQLException, ClassNotFoundException {
        List<CategoryModel> list = new ArrayList<>();
        String sql = "SELECT MaLoaiSanPham, TenLoaiSanPham, Thue_GTGT_MacDinh, TrangThai FROM LOAI_SAN_PHAM ORDER BY MaLoaiSanPham DESC";
        
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new CategoryModel(
                    rs.getInt("MaLoaiSanPham"),
                    rs.getNString("TenLoaiSanPham"), // Quan trọng: dùng getNString
                    rs.getNString("TrangThai"),      // Quan trọng: dùng getNString
                    rs.getDouble("Thue_GTGT_MacDinh")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Kiểm tra tên trùng lặp
    public boolean checkDuplicateName(String name, int excludeId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM LOAI_SAN_PHAM WHERE TenLoaiSanPham = ? AND MaLoaiSanPham != ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, name); // Quan trọng: dùng setNString
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Thêm mới danh mục 
    public boolean addCategory(CategoryModel category) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO LOAI_SAN_PHAM (TenLoaiSanPham, Thue_GTGT_MacDinh, TrangThai) VALUES (?, ?, N'Đang sử dụng')";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, category.getCategoryName()); 
            ps.setDouble(2, category.getDefaultVat());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật thông tin danh mục 
    public boolean updateCategory(CategoryModel category) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE LOAI_SAN_PHAM SET TenLoaiSanPham = ?, Thue_GTGT_MacDinh = ? WHERE MaLoaiSanPham = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, category.getCategoryName()); 
            ps.setDouble(2, category.getDefaultVat());
            ps.setInt(3, category.getCategoryID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Thay đổi trạng thái (Ẩn / Sử dụng lại)
    public boolean updateCategoryStatus(int id, String newStatus) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE LOAI_SAN_PHAM SET TrangThai = ? WHERE MaLoaiSanPham = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, newStatus); 
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}