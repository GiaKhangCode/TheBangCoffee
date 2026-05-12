package DatabaseAccessObject;

import Model.CustomerModel;
import static ConnectDatabase.ConnectionUtils.getMyConnection;
import java.sql.*;

public class CustomerDAO {
    
    // 1. Tìm khách hàng theo Số điện thoại
    public CustomerModel findByPhone(String phone) throws SQLException, ClassNotFoundException {
        // [SỬA]: Đổi tên bảng thành KHACH_HANG
        String sql = "SELECT * FROM KHACH_HANG WHERE SoDienThoai = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new CustomerModel(
                    rs.getInt("MaKhachHang"),      // Đổi từ MaKH -> MaKhachHang
                    rs.getString("SoDienThoai"),
                    rs.getString("HoTen"),         // Đổi từ TenKH -> HoTen
                    rs.getInt("DiemTichLuy"),
                    rs.getString("HangThanhVien")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Ném lỗi lên để dễ debug
        }
        return null;
    }

    // 2. Thêm khách hàng mới và trả về Mã Khách Hàng (ID tự tăng)
    public int insertAndGetId(CustomerModel customer) throws SQLException, ClassNotFoundException {
        // [SỬA]: Đổi tên bảng và tên cột khớp với CSDL Oracle của bạn
        String sql = "INSERT INTO KHACH_HANG (SoDienThoai, HoTen, DiemTichLuy, HangThanhVien) VALUES (?, ?, 0, N'Mới')";
        
        try (Connection conn = getMyConnection();
             // [SỬA]: Chỉ định rõ cột MAKHACHHANG để lấy ID tự tăng trong Oracle
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"MAKHACHHANG"})) {
            
            ps.setString(1, customer.getSoDienThoai());
            ps.setString(2, customer.getTenKH()); // Thuộc tính TenKH trong Model map với cột HoTen trong CSDL
            
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
}