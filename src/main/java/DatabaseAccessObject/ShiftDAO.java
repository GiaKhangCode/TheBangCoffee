package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.ShiftModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShiftDAO {

    public List<ShiftModel> getAllShifts() throws SQLException, ClassNotFoundException {
        List<ShiftModel> list = new ArrayList<>();
        // [CẬP NHẬT] Dùng TO_CHAR để lấy giờ phút từ TIMESTAMP
        String sql = "SELECT MaCa, TenCa, TO_CHAR(GioBatDau, 'HH24:MI') AS GioBatDau, TO_CHAR(GioKetThuc, 'HH24:MI') AS GioKetThuc, TrangThai FROM CA_LAM_VIEC " +
                     "ORDER BY " +
                     "  CASE WHEN TrangThai = N'Đang sử dụng' THEN 1 ELSE 2 END ASC, " +
                     "  MaCa ASC";
        
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                ShiftModel shift = new ShiftModel(
                    rs.getInt("MaCa"),
                    rs.getString("TenCa"),
                    rs.getString("GioBatDau"),
                    rs.getString("GioKetThuc"),
                    rs.getString("TrangThai")
                );
                list.add(shift);
            }
        }
        return list;
    }

    public boolean isShiftNameExists(String tenCa) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM CA_LAM_VIEC WHERE UPPER(TenCa) = UPPER(?)";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenCa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    public boolean isShiftNameExistsExcludeCurrent(String tenCa, int maCaHienTai) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM CA_LAM_VIEC WHERE UPPER(TenCa) = UPPER(?) AND MaCa != ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenCa);
            ps.setInt(2, maCaHienTai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean checkUpdateConflict(int maCa, String newGioBatDau, String newGioKetThuc) throws SQLException, ClassNotFoundException {
        // [CẬP NHẬT] Chuyển tham số string thành TIMESTAMP để so sánh chính xác thời gian
        String sql = "SELECT COUNT(*) " +
                     "FROM LICH_LAM_VIEC l1 " +
                     "JOIN LICH_LAM_VIEC l2 ON l1.NgayLamViec = l2.NgayLamViec AND l1.MaTaiKhoan = l2.MaTaiKhoan " +
                     "JOIN CA_LAM_VIEC c2 ON l2.MaCa = c2.MaCa " +
                     "WHERE l1.MaCa = ? " +
                     "  AND l2.MaCa != ? " +
                     "  AND (TO_TIMESTAMP(?, 'HH24:MI') < c2.GioKetThuc AND c2.GioBatDau < TO_TIMESTAMP(?, 'HH24:MI')) " +
                     "  AND c2.TrangThai = N'Đang sử dụng'";
                     
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maCa);          
            ps.setInt(2, maCa);          
            ps.setString(3, newGioBatDau); 
            ps.setString(4, newGioKetThuc);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; 
                }
            }
        }
        return false; 
    }

    public boolean insertShift(ShiftModel shift) throws SQLException, ClassNotFoundException {
        // [CẬP NHẬT] Insert chuỗi thời gian dưới dạng TIMESTAMP
        String sql = "INSERT INTO CA_LAM_VIEC (TenCa, GioBatDau, GioKetThuc, TrangThai) VALUES (?, TO_TIMESTAMP(?, 'HH24:MI'), TO_TIMESTAMP(?, 'HH24:MI'), N'Đang sử dụng')";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shift.getTenCa());
            ps.setString(2, shift.getGioBatDau());
            ps.setString(3, shift.getGioKetThuc());
            
            int result = ps.executeUpdate();
            if(result > 0) {
                try { conn.commit(); } catch (SQLException e) { }
                return true;
            }
            return false;
        }
    }

    public boolean updateShift(ShiftModel shift) throws SQLException, ClassNotFoundException {
        // [CẬP NHẬT] Update chuỗi thời gian dưới dạng TIMESTAMP
        String sql = "UPDATE CA_LAM_VIEC SET TenCa=?, GioBatDau=TO_TIMESTAMP(?, 'HH24:MI'), GioKetThuc=TO_TIMESTAMP(?, 'HH24:MI') WHERE MaCa=?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shift.getTenCa());
            ps.setString(2, shift.getGioBatDau());
            ps.setString(3, shift.getGioKetThuc());
            ps.setInt(4, shift.getMaCa());
            
            int result = ps.executeUpdate();
            if(result > 0) {
                try { conn.commit(); } catch (SQLException e) { }
                return true;
            }
            return false;
        }
    }
    
    public List<ShiftModel> getActiveShift() throws SQLException, ClassNotFoundException{
        List<ShiftModel> list = new ArrayList<>();
        // [CẬP NHẬT] Dùng TO_CHAR để format lại giờ
        String sql = "SELECT MaCa, TenCa, TO_CHAR(GioBatDau, 'HH24:MI') AS GioBatDau, TO_CHAR(GioKetThuc, 'HH24:MI') AS GioKetThuc, TrangThai FROM CA_LAM_VIEC "
                + "WHERE TRANGTHAI = N'Đang sử dụng' ORDER BY MaCa ASC";
        
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                ShiftModel shift = new ShiftModel(
                    rs.getInt("MaCa"),
                    rs.getString("TenCa"),
                    rs.getString("GioBatDau"),
                    rs.getString("GioKetThuc"),
                    rs.getString("TrangThai")
                );
                list.add(shift);
            }
        }
        return list;
    }

    public boolean disableShift(int maCa) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE CA_LAM_VIEC SET TrangThai = N'Ngừng sử dụng' WHERE MaCa = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maCa);
            
            int result = ps.executeUpdate();
            if(result > 0) {
                try { conn.commit(); } catch (SQLException e) { }
                return true;
            }
            return false;
        }
    }

    public List<String[]> getWeeklySchedules(java.time.LocalDate startDate, java.time.LocalDate endDate) throws SQLException, ClassNotFoundException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT l.MaTaiKhoan, TO_CHAR(l.NgayLamViec, 'YYYY-MM-DD') AS Ngay, c.TenCa " +
                     "FROM LICH_LAM_VIEC l JOIN CA_LAM_VIEC c ON l.MaCa = c.MaCa " +
                     "WHERE l.NgayLamViec >= ? AND l.NgayLamViec <= ? " +
                     "AND (c.TrangThai = N'Đang sử dụng' OR l.NgayLamViec < TRUNC(SYSDATE))";
                     
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    list.add(new String[]{
                        String.valueOf(rs.getInt("MaTaiKhoan")),
                        rs.getString("Ngay"),
                        rs.getString("TenCa")
                    });
                }
            }
        }
        return list;
    }

    public void deleteSchedules(java.time.LocalDate startDate, java.time.LocalDate endDate, List<Integer> accountIds) throws SQLException, ClassNotFoundException {
        if (accountIds == null || accountIds.isEmpty()) return;
        
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < accountIds.size(); i++) {
            placeholders.append("?");
            if (i < accountIds.size() - 1) placeholders.append(",");
        }
        
        String sql = "DELETE FROM LICH_LAM_VIEC WHERE NgayLamViec >= ? AND NgayLamViec <= ? AND MaTaiKhoan IN (" + placeholders + ")";
        
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            
            int index = 3;
            for (Integer id : accountIds) {
                ps.setInt(index++, id);
            }
            
            ps.executeUpdate();
            try { conn.commit(); } catch (SQLException e) {} 
        }
    }

    public void insertSchedule(java.time.LocalDate workDate, int maCa, int maTaiKhoan) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO LICH_LAM_VIEC (NgayLamViec, MaCa, MaTaiKhoan) VALUES (?, ?, ?)";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setDate(1, java.sql.Date.valueOf(workDate));
            ps.setInt(2, maCa);
            ps.setInt(3, maTaiKhoan);
            
            ps.executeUpdate();
            try { conn.commit(); } catch (SQLException e) {}
            
        } catch (SQLException ex) {
            if (!ex.getMessage().contains("ORA-00001") && !ex.getMessage().contains("unique constraint")) { 
                throw ex;
            }
        }
    }
}