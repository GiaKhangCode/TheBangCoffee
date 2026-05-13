package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.IngredientTypeModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

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
    
    public boolean insertIngredientType(String typeName) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO LOAI_NGUYEN_LIEU (TenLoaiNguyenLieu) VALUES (?)";
        try (Connection conn = getMyConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, typeName);
            int rowAffected = ps.executeUpdate();
            return rowAffected > 0;

        } catch (SQLException e) {
            // Bắt lỗi Duplicate nếu tên bị trùng (Do bảng có ràng buộc UNIQUE)
            if (e.getErrorCode() == 1) { 
                JOptionPane.showMessageDialog(null, "Tên loại nguyên liệu này đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }
    
    // [MỚI] Hàm cập nhật tên loại nguyên liệu
    public boolean updateIngredientType(int id, String newName) {
        String sql = "UPDATE LOAI_NGUYEN_LIEU SET TenLoaiNguyenLieu = ? WHERE MaLoaiNguyenLieu = ?";
        try (Connection conn = getMyConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setInt(2, id);
            
            int rowAffected = ps.executeUpdate();
            return rowAffected > 0;

        } catch (SQLException e) {
            // Bắt lỗi Duplicate tên khi sửa
            if (e.getErrorCode() == 1) { 
                JOptionPane.showMessageDialog(null, "Tên loại nguyên liệu này đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}