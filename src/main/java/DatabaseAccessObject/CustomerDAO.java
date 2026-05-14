package DatabaseAccessObject;

import Model.CustomerModel;
import static ConnectDatabase.ConnectionUtils.getMyConnection;
import java.sql.*;

public class CustomerDAO {
    
    // 1. Tìm khách hàng theo Số điện thoại
    public CustomerModel findCustomerByPhone(String phone) {
        String sql = "SELECT MaKhachHang, SoDienThoai, HoTen, DiemTichLuy, HangThanhVien " +
                     "FROM KHACH_HANG WHERE SoDienThoai = ?";
        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CustomerModel(
                        rs.getInt("MaKhachHang"), rs.getString("SoDienThoai"),
                        rs.getString("HoTen"), rs.getInt("DiemTichLuy"),
                        rs.getString("HangThanhVien") // Lấy thêm ở đây
                    );
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // 2. Thêm khách hàng mới và trả về Mã Khách Hàng (ID tự tăng)
    public int insertAndGetId(CustomerModel customer) throws SQLException, ClassNotFoundException {
        // Đổi tên bảng và tên cột khớp với CSDL Oracle
        String sql = "INSERT INTO KHACH_HANG (SoDienThoai, HoTen, DiemTichLuy) VALUES (?, ?, 0)";
        
        try (Connection conn = getMyConnection();
             // Chỉ định rõ cột MAKHACHHANG để lấy ID tự tăng trong Oracle
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"MAKHACHHANG"})) {
            
            ps.setString(1, customer.getSoDienThoai());
            ps.setString(2, customer.getTenKH()); 
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); // Trả về MaKhachHang vừa tạo
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return -1; // Thất bại
    }

    // 3. [SỬA LỖI ORA-00904] Cập nhật lại câu lệnh SQL đơn giản và an toàn tuyệt đối
    public void addPointsToCustomerByOrderId(int orderId, int pointsToAdd) {
        String sql = "UPDATE KHACH_HANG SET DiemTichLuy = DiemTichLuy + ? " +
                     "WHERE MaKhachHang = (SELECT MaKhachHang FROM DON_HANG WHERE MaDonHang = ? AND MaKhachHang IS NOT NULL)";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pointsToAdd); // Truyền trực tiếp số điểm đã được tính toán từ Controller
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}