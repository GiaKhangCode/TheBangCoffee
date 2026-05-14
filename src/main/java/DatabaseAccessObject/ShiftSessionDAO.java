package DatabaseAccessObject;

import ConnectDatabase.ConnectionUtils;
import Model.ShiftSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ShiftSessionDAO {

    public ShiftSession getPhienCaDangMo() {
        String sql = "SELECT * FROM PHIEN_CA_LAM WHERE TrangThai = N'Đang mở' FETCH FIRST 1 ROWS ONLY";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                ShiftSession ca = new ShiftSession();
                ca.setMaPhienCa(rs.getInt("MaPhienCa"));
                ca.setMaLich(rs.getObject("MaLich") != null ? rs.getInt("MaLich") : null);
                ca.setMaTaiKhoanMo(rs.getInt("MaTaiKhoanMo"));
                ca.setThoiGianMo(rs.getTimestamp("ThoiGianMo"));
                ca.setTrangThai(rs.getString("TrangThai"));
                return ca;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public int moCa(ShiftSession phienCa, double tienMatDauCa) {
        String sqlMoCa = "INSERT INTO PHIEN_CA_LAM (MaLich, MaTaiKhoanMo, TrangThai) VALUES (?, ?, N'Đang mở')";
        String sqlTienMat = "INSERT INTO DOI_SOAT_DONG_TIEN (MaPhienCa, NguonTien, SoTienDauCa) VALUES (?, N'Tiền mặt', ?)";
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false); 
            int maPhienCaMoi = -1;
            try (PreparedStatement ps = con.prepareStatement(sqlMoCa, new String[]{"MaPhienCa"})) {
                ps.setObject(1, phienCa.getMaLich());
                ps.setInt(2, phienCa.getMaTaiKhoanMo());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) maPhienCaMoi = rs.getInt(1);
                }
            }
            if (maPhienCaMoi != -1) {
                try (PreparedStatement ps2 = con.prepareStatement(sqlTienMat)) {
                    ps2.setInt(1, maPhienCaMoi);
                    ps2.setDouble(2, tienMatDauCa);
                    ps2.executeUpdate();
                }
                con.commit(); 
                return maPhienCaMoi;
            } else {
                con.rollback();
            }
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (Exception ex) {}
        } finally {
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
        return -1;
    }

    public boolean dongCa(int maPhienCa, int maTaiKhoanNhan, String ghiChu) {
        String sql = "UPDATE PHIEN_CA_LAM SET ThoiGianDong = SYSDATE, TrangThai = N'Đã đóng', MaTaiKhoanNhan = ?, GhiChu = ? WHERE MaPhienCa = ?";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, maTaiKhoanNhan == 0 ? null : maTaiKhoanNhan);
            ps.setString(2, ghiChu);
            ps.setInt(3, maPhienCa);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public double getTienMatDauCa(int maPhienCa) {
        String sql = "SELECT NVL(SoTienDauCa, 0) FROM DOI_SOAT_DONG_TIEN WHERE MaPhienCa = ? AND NguonTien = N'Tiền mặt'";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPhienCa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public double getDoanhThuTienMat(int maPhienCa) {
        String sql = "SELECT NVL(SUM(ThanhTien), 0) FROM DON_HANG WHERE MaPhienCa = ? AND PhuongThucThanhToan = N'Tiền mặt' AND TrangThaiThanhToan = N'Đã thanh toán'";
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPhienCa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public boolean dongCaToanDien(int maPhienCa, Integer maTaiKhoanNhan, String ghiChu, double soTienHeThong, double soTienThucTe) {
        String sqlUpdatePhienCa = "UPDATE PHIEN_CA_LAM SET ThoiGianDong = SYSDATE, TrangThai = N'Đã đóng', MaTaiKhoanNhan = ?, GhiChu = ? WHERE MaPhienCa = ?";
        String sqlUpdateDoiSoat = "UPDATE DOI_SOAT_DONG_TIEN SET SoTienHeThong = ?, SoTienThucTe = ? WHERE MaPhienCa = ? AND NguonTien = N'Tiền mặt'";
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false); 
            try (PreparedStatement ps1 = con.prepareStatement(sqlUpdatePhienCa)) {
                if (maTaiKhoanNhan != null) ps1.setInt(1, maTaiKhoanNhan);
                else ps1.setNull(1, java.sql.Types.INTEGER);
                ps1.setString(2, ghiChu);
                ps1.setInt(3, maPhienCa);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = con.prepareStatement(sqlUpdateDoiSoat)) {
                ps2.setDouble(1, soTienHeThong);
                ps2.setDouble(2, soTienThucTe);
                ps2.setInt(3, maPhienCa);
                ps2.executeUpdate();
            }
            con.commit();
            return true;
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
    }

    // =========================================================
    // CÁC HÀM MỚI LẤY DỮ LIỆU ĐỔ LÊN GIAO DIỆN MỞ CA
    // =========================================================

    public Object[] getLastShiftHandoverInfo() {
        String sql = "SELECT ND.HoTen, DS.SoTienThucTe " +
                     "FROM PHIEN_CA_LAM PC " +
                     "JOIN TAI_KHOAN TK ON PC.MaTaiKhoanMo = TK.MaTaiKhoan " +
                     "JOIN NGUOI_DUNG ND ON TK.MaNguoiDung = ND.MaNguoiDung " +
                     "LEFT JOIN DOI_SOAT_DONG_TIEN DS ON PC.MaPhienCa = DS.MaPhienCa AND DS.NguonTien = N'Tiền mặt' " +
                     "WHERE PC.TrangThai = N'Đã đóng' " +
                     "ORDER BY PC.ThoiGianDong DESC FETCH FIRST 1 ROWS ONLY";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Object[]{rs.getString("HoTen"), rs.getDouble("SoTienThucTe")};
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public int countUnpaidOrders() {
        String sql = "SELECT COUNT(*) FROM DON_HANG WHERE TrangThaiThanhToan = N'Chưa thanh toán'";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public List<Object[]> getCurrentInventory() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TenNguyenLieu, SoLuongTon, DonViTinh FROM NGUYEN_LIEU ORDER BY TenNguyenLieu";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{ rs.getString("TenNguyenLieu"), rs.getDouble("SoLuongTon"), rs.getString("DonViTinh") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}