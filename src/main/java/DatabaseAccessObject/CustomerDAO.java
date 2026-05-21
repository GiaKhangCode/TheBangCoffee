package DatabaseAccessObject;

import Model.CustomerModel;
import static ConnectDatabase.ConnectionUtils.getMyConnection;
import java.sql.*;

public class CustomerDAO {
    
    public CustomerModel findCustomerByPhone(String phone) {
        String sql = "SELECT k.MaKhachHang, k.SoDienThoai, k.HoTen, k.DiemHienTai, k.DiemTichLuy, h.TenHang AS HangThanhVien, h.PhanTramChietKhau " +
                     "FROM KHACH_HANG k " +
                     "LEFT JOIN HANG_THANH_VIEN h ON k.MaHang = h.MaHang " +
                     "WHERE k.SoDienThoai = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CustomerModel(
                        rs.getInt("MaKhachHang"), rs.getString("SoDienThoai"),
                        rs.getString("HoTen"), rs.getInt("DiemHienTai"), rs.getInt("DiemTichLuy"),
                        rs.getString("HangThanhVien"), rs.getDouble("PhanTramChietKhau")
                    );
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean updateCustomer(int id, String phone, String name) {
        String sql = "UPDATE KHACH_HANG SET SoDienThoai = ?, HoTen = ? WHERE MaKhachHang = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setString(2, name);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { 
            e.printStackTrace(); 
            return false;
        }
    }

    public int insertAndGetId(CustomerModel customer) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO KHACH_HANG (SoDienThoai, HoTen, DiemHienTai, DiemTichLuy, MaHang) " +
                     "VALUES (?, ?, 0, 0, (SELECT MaHang FROM HANG_THANH_VIEN WHERE LaMacDinh = 1 AND ROWNUM = 1))";
        
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"MAKHACHHANG"})) {
            
            ps.setString(1, customer.getSoDienThoai());
            ps.setString(2, customer.getTenKH()); 
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); 
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return -1;
    }

    public void addPointsToCustomerByOrderId(int orderId, int pointsToAdd) {
        String sql = "UPDATE KHACH_HANG SET DiemHienTai = DiemHienTai + ?, DiemTichLuy = DiemTichLuy + ? " +
                     "WHERE MaKhachHang = (SELECT MaKhachHang FROM DON_HANG WHERE MaDonHang = ? AND MaKhachHang IS NOT NULL)";

        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pointsToAdd); 
            ps.setInt(2, pointsToAdd);
            ps.setInt(3, orderId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refundPointsToCustomerByOrderId(int orderId, int pointsToRefund) {
        String sql = "UPDATE KHACH_HANG SET DiemHienTai = DiemHienTai + ? " +
                     "WHERE MaKhachHang = (SELECT MaKhachHang FROM DON_HANG WHERE MaDonHang = ? AND MaKhachHang IS NOT NULL)";

        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pointsToRefund);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet getAllTiers() throws SQLException, ClassNotFoundException {
        Connection conn = getMyConnection();
        String sql = "SELECT * FROM HANG_THANH_VIEN ORDER BY DiemYeuCau ASC";
        return conn.createStatement().executeQuery(sql);
    }

    public void saveTier(int maHang, String tenHang, int diemYeuCau, double phanTramChietKhau) throws SQLException, ClassNotFoundException {
        try (Connection conn = getMyConnection()) {
            if (maHang == 0) { 
                String sql = "INSERT INTO HANG_THANH_VIEN (TenHang, DiemYeuCau, PhanTramChietKhau) VALUES (?, ?, ?)";
                try(PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, tenHang);
                    ps.setInt(2, diemYeuCau);
                    ps.setDouble(3, phanTramChietKhau);
                    ps.executeUpdate();
                }
            } else { 
                String sql = "UPDATE HANG_THANH_VIEN SET TenHang = ?, DiemYeuCau = ?, PhanTramChietKhau = ? WHERE MaHang = ?";
                try(PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, tenHang);
                    ps.setInt(2, diemYeuCau);
                    ps.setDouble(3, phanTramChietKhau);
                    ps.setInt(4, maHang);
                    ps.executeUpdate();
                }
            }
        }
    }

    public String deleteTier(int maHang) throws SQLException, ClassNotFoundException {
        try (Connection conn = getMyConnection();
             CallableStatement cs = conn.prepareCall("{call SP_XOA_HANG_THANH_VIEN(?, ?)}")) {
            cs.setInt(1, maHang);
            cs.registerOutParameter(2, Types.NVARCHAR);
            cs.execute();
            return cs.getString(2);
        }
    }

    public String syncTiers() throws SQLException, ClassNotFoundException {
        try (Connection conn = getMyConnection();
             CallableStatement cs = conn.prepareCall("{call SP_DONG_BO_HANG_THANH_VIEN(?)}")) {
            cs.registerOutParameter(1, Types.NVARCHAR);
            cs.execute();
            return cs.getString(1);
        }
    }

    // [CẬP NHẬT] Đã sửa mảng 3 tham số để lấy thêm DiemDoiMotLy
    public int[] getPointRule() throws SQLException, ClassNotFoundException {
        String sql = "SELECT TienTichMotDiem, GiaTriMotDiem, DiemDoiMotLy FROM CAU_HINH_QUY_DOI WHERE ROWNUM = 1";
        try (Connection conn = getMyConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            if (rs.next()) {
                return new int[]{
                    rs.getInt("TienTichMotDiem"), 
                    rs.getInt("GiaTriMotDiem"), 
                    rs.getInt("DiemDoiMotLy")
                };
            }
        }
        return new int[]{10000, 100, 50}; 
    }

    // [CẬP NHẬT] Nhận 3 tham số từ Service để update 
    public void updatePointRule(int tienTich, int giaTri, int diemDoiLy) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE CAU_HINH_QUY_DOI SET TienTichMotDiem = ?, GiaTriMotDiem = ?, DiemDoiMotLy = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tienTich);
            ps.setInt(2, giaTri);
            ps.setInt(3, diemDoiLy);
            ps.executeUpdate();
        }
    }
    
    public ResultSet getAllCustomers() throws SQLException, ClassNotFoundException {
        Connection conn = getMyConnection();
        String sql = "SELECT k.MaKhachHang, k.SoDienThoai, k.HoTen, k.NgayDangKy, k.DiemHienTai, k.DiemTichLuy, h.TenHang AS HangThanhVien " +
                     "FROM KHACH_HANG k " +
                     "LEFT JOIN HANG_THANH_VIEN h ON k.MaHang = h.MaHang " +
                     "ORDER BY k.MaKhachHang DESC";
        return conn.createStatement().executeQuery(sql);
    }
}