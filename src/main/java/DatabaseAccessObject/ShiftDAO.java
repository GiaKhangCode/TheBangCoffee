package DatabaseAccessObject;

import ConnectDatabase.ConnectionUtils;
import Model.ShiftModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShiftDAO {
    
    // 1. THÊM CA LÀM VIỆC
    public boolean insertShift(ShiftModel shift) {
        String sql = "INSERT INTO CA_LAM_VIEC (MaTaiKhoan, BuoiLamViec, NgayLam, SoGioLamViec) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, shift.getMaTaiKhoan());
            ps.setString(2, shift.getBuoiLamViec());
            ps.setDate(3, Date.valueOf(shift.getNgayLam())); // Chuyển LocalDate sang java.sql.Date
            ps.setDouble(4, shift.getSoGioLamViec());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 2. CẬP NHẬT CA LÀM VIỆC (Đổi nhân viên, đổi giờ)
    public boolean updateShift(ShiftModel shift) {
        String sql = "UPDATE CA_LAM_VIEC SET MaTaiKhoan = ?, SoGioLamViec = ? WHERE MaCa = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, shift.getMaTaiKhoan());
            ps.setDouble(2, shift.getSoGioLamViec());
            ps.setInt(3, shift.getMaCa());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 3. XÓA CA LÀM VIỆC
    public boolean deleteShift(int maCa) {
        String sql = "DELETE FROM CA_LAM_VIEC WHERE MaCa = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, maCa);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4. LẤY DANH SÁCH CA TRONG 1 THÁNG (Để hiển thị lên View Lịch)
    public List<ShiftModel> getShiftsByMonthYear(int month, int year) {
        List<ShiftModel> list = new ArrayList<>();
        // Truy vấn JOIN để lấy được tên tài khoản/nhân viên (kết với tài khoản và người dùng để lấy Họ tên người trực ca đó)
        String sql = "SELECT C.MaCa, C.MaTaiKhoan, ND.HoTen AS TenNhanVien, C.BuoiLamViec, C.NgayLam, C.SoGioLamViec " +
                     "FROM CA_LAM_VIEC C " +
                     "JOIN TAI_KHOAN T ON C.MaTaiKhoan = T.MaTaiKhoan " +
                     "JOIN NGUOI_DUNG ND ON T.MaNguoiDung = ND.MaNguoiDung " +
                     "WHERE EXTRACT(MONTH FROM C.NgayLam) = ? AND EXTRACT(YEAR FROM C.NgayLam) = ?";
                     
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, month);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                ShiftModel shift = new ShiftModel(
                    rs.getInt("MaCa"),
                    rs.getInt("MaTaiKhoan"),
                    rs.getString("TenNhanVien"),
                    rs.getString("BuoiLamViec"),
                    rs.getDate("NgayLam").toLocalDate(),
                    rs.getDouble("SoGioLamViec")
                );
                list.add(shift);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // 5. Lấy dữ liệu ca để hiển thị lên view
   
}