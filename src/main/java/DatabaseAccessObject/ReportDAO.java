package DatabaseAccessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    // =========================================================================
    // TAB 1: THỐNG KÊ DOANH THU 
    // =========================================================================
    public Object[] getRevenueStats(String filterType) {
        Object[] result = {0L, 0}; 
        String dateCondition = "";
        
        switch (filterType) {
            case "Hôm nay": dateCondition = "TRUNC(NgayDat) = TRUNC(SYSDATE)"; break;
            case "Tuần này": dateCondition = "NgayDat >= TRUNC(SYSDATE, 'IW')"; break;
            case "Tháng này": dateCondition = "TRUNC(NgayDat, 'MM') = TRUNC(SYSDATE, 'MM')"; break;
            default: dateCondition = "TRUNC(NgayDat) = TRUNC(SYSDATE)"; 
        }

        String sql = "SELECT SUM(ThanhTien) AS TongTien, COUNT(MaDonHang) AS TongDon " +
                     "FROM DON_HANG WHERE TrangThaiThanhToan = N'Đã thanh toán' AND " + dateCondition;

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                result[0] = rs.getLong("TongTien");
                result[1] = rs.getInt("TongDon");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getRevenueChartData(String filterType) {
        List<Object[]> list = new ArrayList<>();
        String dateCondition = "";
        String groupBy = "TO_CHAR(NgayDat, 'DD/MM')"; 
        String orderBy = "TRUNC(NgayDat)";

        switch (filterType) {
            case "Hôm nay":
                dateCondition = "TRUNC(NgayDat) = TRUNC(SYSDATE)";
                groupBy = "TO_CHAR(NgayDat, 'HH24') || ':00'"; 
                orderBy = "TO_CHAR(NgayDat, 'HH24')"; 
                break;
            case "Tuần này":
                dateCondition = "NgayDat >= TRUNC(SYSDATE, 'IW')"; 
                break;
            case "Tháng này":
                dateCondition = "TRUNC(NgayDat, 'MM') = TRUNC(SYSDATE, 'MM')"; 
                break;
            default:
                dateCondition = "NgayDat >= TRUNC(SYSDATE) - 6"; 
        }

        String sql = "SELECT " + groupBy + " AS NhanX, SUM(ThanhTien) AS DoanhThu " +
                     "FROM DON_HANG " +
                     "WHERE TrangThaiThanhToan = N'Đã thanh toán' AND " + dateCondition + " " +
                     "GROUP BY " + groupBy + ", " + orderBy + " " +
                     "ORDER BY " + orderBy + " ASC";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{ rs.getString("NhanX"), rs.getLong("DoanhThu") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    
    public Object[] getCustomRevenueStats(java.util.Date startDate, java.util.Date endDate) {
        Object[] result = {0L, 0}; 
        String sql = "SELECT SUM(ThanhTien) AS TongTien, COUNT(MaDonHang) AS TongDon " +
                     "FROM DON_HANG WHERE TrangThaiThanhToan = N'Đã thanh toán' " +
                     "AND TRUNC(NgayDat) >= ? AND TRUNC(NgayDat) <= ?";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getLong("TongTien");
                    result[1] = rs.getInt("TongDon");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getCustomRevenueChartData(java.util.Date startDate, java.util.Date endDate) {
        List<Object[]> list = new ArrayList<>();
        String groupBy = "TO_CHAR(NgayDat, 'DD/MM')";
        String orderBy = "TRUNC(NgayDat)";
        
        String sql = "SELECT " + groupBy + " AS NhanX, SUM(ThanhTien) AS DoanhThu " +
                     "FROM DON_HANG " +
                     "WHERE TrangThaiThanhToan = N'Đã thanh toán' " +
                     "AND TRUNC(NgayDat) >= ? AND TRUNC(NgayDat) <= ? " +
                     "GROUP BY " + groupBy + ", " + orderBy + " " +
                     "ORDER BY " + orderBy + " ASC";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{ rs.getString("NhanX"), rs.getLong("DoanhThu") });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================================
    // TAB 2: THỐNG KÊ MÓN BÁN CHẠY VÀ DANH MỤC
    // =========================================================================
    public List<Object[]> getTopSellingProducts() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT sp.TenSanPham, lsp.TenLoaiSanPham, SUM(ct.SoLuong) AS SoLuongBan, SUM(ct.ThanhTien) AS DoanhThu " +
                     "FROM CHI_TIET_DON_HANG ct " +
                     "JOIN DON_HANG dh ON ct.MaDonHang = dh.MaDonHang " +
                     "JOIN BIEN_THE bt ON ct.MaBienThe = bt.MaBienThe " +
                     "JOIN SAN_PHAM sp ON bt.MaSanPham = sp.MaSanPham " +
                     "JOIN LOAI_SAN_PHAM lsp ON sp.MaLoaiSanPham = lsp.MaLoaiSanPham " +
                     "WHERE dh.TrangThaiThanhToan = N'Đã thanh toán' " +
                     "GROUP BY sp.TenSanPham, lsp.TenLoaiSanPham " +
                     "ORDER BY SoLuongBan DESC " +
                     "FETCH FIRST 10 ROWS ONLY"; 

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("TenSanPham"),
                    rs.getString("TenLoaiSanPham"),
                    rs.getInt("SoLuongBan"),
                    rs.getLong("DoanhThu")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> getSalesByCategory() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT lsp.TenLoaiSanPham, SUM(ct.SoLuong) AS TongSoLuong " +
                     "FROM CHI_TIET_DON_HANG ct " +
                     "JOIN DON_HANG dh ON ct.MaDonHang = dh.MaDonHang " +
                     "JOIN BIEN_THE bt ON ct.MaBienThe = bt.MaBienThe " +
                     "JOIN SAN_PHAM sp ON bt.MaSanPham = sp.MaSanPham " +
                     "JOIN LOAI_SAN_PHAM lsp ON sp.MaLoaiSanPham = lsp.MaLoaiSanPham " +
                     "WHERE dh.TrangThaiThanhToan = N'Đã thanh toán' " +
                     "GROUP BY lsp.TenLoaiSanPham";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("TenLoaiSanPham"),
                    rs.getInt("TongSoLuong")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================================
    // TAB 3: BÁO CÁO CA LÀM VIỆC (MỚI)
    // =========================================================================
    
    // =========================================================================
    // HÀM BỔ TRỢ: HIỆU SUẤT NHÂN VIÊN (Dành cho Xuất File Excel)
    // =========================================================================
    public List<Object[]> getEmployeePerformance() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT tk.MaTaiKhoan, nd.HoTen, COUNT(dh.MaDonHang) AS SoDonTao, SUM(dh.ThanhTien) AS DoanhThuMangVe " +
                     "FROM DON_HANG dh " +
                     "JOIN TAI_KHOAN tk ON dh.MaTaiKhoan = tk.MaTaiKhoan " +
                     "JOIN NGUOI_DUNG nd ON tk.MaNguoiDung = nd.MaNguoiDung " +
                     "WHERE dh.TrangThaiThanhToan = N'Đã thanh toán' " +
                     "GROUP BY tk.MaTaiKhoan, nd.HoTen " +
                     "ORDER BY DoanhThuMangVe DESC";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("MaTaiKhoan"),
                    rs.getString("HoTen"),
                    rs.getInt("SoDonTao"),
                    rs.getLong("DoanhThuMangVe")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Trả về mảng: [0] = Số ca (int), [1] = Số giờ làm (double), [2] = Doanh thu TB (long), [3] = Đơn hủy (int)
    public Object[] getShiftOverviewStats(String filterType) {
        Object[] result = {0, 0.0, 0L, 0}; 
        String dateConditionLLV = "";
        String dateConditionDH = "";

        switch (filterType) {
            case "Tuần này": 
                dateConditionLLV = "NgayLamViec >= TRUNC(SYSDATE, 'IW')"; 
                dateConditionDH = "NgayDat >= TRUNC(SYSDATE, 'IW')"; 
                break;
            case "Tháng này": 
                dateConditionLLV = "TRUNC(NgayLamViec, 'MM') = TRUNC(SYSDATE, 'MM')"; 
                dateConditionDH = "TRUNC(NgayDat, 'MM') = TRUNC(SYSDATE, 'MM')"; 
                break;
            default: 
                dateConditionLLV = "NgayLamViec >= TRUNC(SYSDATE) - 6"; 
                dateConditionDH = "NgayDat >= TRUNC(SYSDATE) - 6"; 
        }

        String sqlShifts = 
            "SELECT COUNT(l.MaLich) AS SoCa, " +
            "NVL(SUM((TO_DATE(TO_CHAR(c.GioKetThuc, 'HH24:MI'), 'HH24:MI') - TO_DATE(TO_CHAR(c.GioBatDau, 'HH24:MI'), 'HH24:MI') " +
            "+ CASE WHEN TO_CHAR(c.GioKetThuc, 'HH24:MI') < TO_CHAR(c.GioBatDau, 'HH24:MI') THEN 1 ELSE 0 END) * 24), 0) AS TongGio " +
            "FROM LICH_LAM_VIEC l JOIN CA_LAM_VIEC c ON l.MaCa = c.MaCa " +
            "WHERE " + dateConditionLLV;

        String sqlRev = "SELECT NVL(SUM(ThanhTien), 0) FROM DON_HANG WHERE TrangThaiThanhToan = N'Đã thanh toán' AND " + dateConditionDH;
        
        String sqlCanceled = "SELECT COUNT(MaDonHang) FROM DON_HANG WHERE " +
                             "(TrangThaiPhaChe = N'Đã hủy' OR TrangThaiThanhToan IN (N'Đã hủy', N'Đã hoàn tiền')) AND " + dateConditionDH;

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection()) {
            int totalShifts = 0;
            long totalRevenue = 0;
            
            try(PreparedStatement ps1 = conn.prepareStatement(sqlShifts); ResultSet rs1 = ps1.executeQuery()) {
                if(rs1.next()) {
                    totalShifts = rs1.getInt("SoCa");
                    result[0] = totalShifts;
                    result[1] = rs1.getDouble("TongGio");
                }
            }
            try(PreparedStatement ps2 = conn.prepareStatement(sqlRev); ResultSet rs2 = ps2.executeQuery()) {
                if(rs2.next()) totalRevenue = rs2.getLong(1);
            }
            try(PreparedStatement ps3 = conn.prepareStatement(sqlCanceled); ResultSet rs3 = ps3.executeQuery()) {
                if(rs3.next()) result[3] = rs3.getInt(1);
            }
            
            result[2] = totalShifts > 0 ? (totalRevenue / totalShifts) : 0L;
            
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public Object[] getCustomShiftOverviewStats(java.util.Date startDate, java.util.Date endDate) {
        Object[] result = {0, 0.0, 0L, 0}; 

        String sqlShifts = 
            "SELECT COUNT(l.MaLich) AS SoCa, " +
            "NVL(SUM((TO_DATE(TO_CHAR(c.GioKetThuc, 'HH24:MI'), 'HH24:MI') - TO_DATE(TO_CHAR(c.GioBatDau, 'HH24:MI'), 'HH24:MI') " +
            "+ CASE WHEN TO_CHAR(c.GioKetThuc, 'HH24:MI') < TO_CHAR(c.GioBatDau, 'HH24:MI') THEN 1 ELSE 0 END) * 24), 0) AS TongGio " +
            "FROM LICH_LAM_VIEC l JOIN CA_LAM_VIEC c ON l.MaCa = c.MaCa " +
            "WHERE TRUNC(l.NgayLamViec) >= ? AND TRUNC(l.NgayLamViec) <= ?";

        String sqlRev = "SELECT NVL(SUM(ThanhTien), 0) FROM DON_HANG WHERE TrangThaiThanhToan = N'Đã thanh toán' AND TRUNC(NgayDat) >= ? AND TRUNC(NgayDat) <= ?";
        
        String sqlCanceled = "SELECT COUNT(MaDonHang) FROM DON_HANG WHERE (TrangThaiPhaChe = N'Đã hủy' OR TrangThaiThanhToan IN (N'Đã hủy', N'Đã hoàn tiền')) AND TRUNC(NgayDat) >= ? AND TRUNC(NgayDat) <= ?";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection()) {
            int totalShifts = 0;
            long totalRevenue = 0;
            java.sql.Date sDate = new java.sql.Date(startDate.getTime());
            java.sql.Date eDate = new java.sql.Date(endDate.getTime());
            
            try(PreparedStatement ps1 = conn.prepareStatement(sqlShifts)) {
                ps1.setDate(1, sDate); ps1.setDate(2, eDate);
                try(ResultSet rs1 = ps1.executeQuery()) {
                    if(rs1.next()) {
                        totalShifts = rs1.getInt("SoCa");
                        result[0] = totalShifts;
                        result[1] = rs1.getDouble("TongGio");
                    }
                }
            }
            try(PreparedStatement ps2 = conn.prepareStatement(sqlRev)) {
                ps2.setDate(1, sDate); ps2.setDate(2, eDate);
                try(ResultSet rs2 = ps2.executeQuery()) {
                    if(rs2.next()) totalRevenue = rs2.getLong(1);
                }
            }
            try(PreparedStatement ps3 = conn.prepareStatement(sqlCanceled)) {
                ps3.setDate(1, sDate); ps3.setDate(2, eDate);
                try(ResultSet rs3 = ps3.executeQuery()) {
                    if(rs3.next()) result[3] = rs3.getInt(1);
                }
            }
            
            result[2] = totalShifts > 0 ? (totalRevenue / totalShifts) : 0L;
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Biểu đồ Cột: Doanh thu theo mẫu Ca (Ca Sáng / Chiều / Tối) [ĐÃ CẬP NHẬT LOGIC SO SÁNH GIỜ]
    public List<Object[]> getShiftRevenueChartData(String filterType) {
        List<Object[]> list = new ArrayList<>();
        String dateConditionLLV = "";

        switch (filterType) {
            case "Tuần này": dateConditionLLV = "l.NgayLamViec >= TRUNC(SYSDATE, 'IW')"; break;
            case "Tháng này": dateConditionLLV = "TRUNC(l.NgayLamViec, 'MM') = TRUNC(SYSDATE, 'MM')"; break;
            default: dateConditionLLV = "l.NgayLamViec >= TRUNC(SYSDATE) - 6"; 
        }

        // Logic check giờ bao phủ cả trường hợp Ca Đêm (Ví dụ: 22:00 hôm nay đến 06:00 sáng hôm sau)
        String joinLogic = "LEFT JOIN DON_HANG dh ON l.MaTaiKhoan = dh.MaTaiKhoan AND dh.TrangThaiThanhToan = N'Đã thanh toán' " +
                           "AND ( " +
                           "  (TO_CHAR(c.GioBatDau, 'HH24:MI') <= TO_CHAR(c.GioKetThuc, 'HH24:MI') " +
                           "   AND TRUNC(dh.NgayDat) = TRUNC(l.NgayLamViec) " +
                           "   AND TO_CHAR(dh.NgayDat, 'HH24:MI') >= TO_CHAR(c.GioBatDau, 'HH24:MI') " +
                           "   AND TO_CHAR(dh.NgayDat, 'HH24:MI') <= TO_CHAR(c.GioKetThuc, 'HH24:MI')) " +
                           "  OR " +
                           "  (TO_CHAR(c.GioBatDau, 'HH24:MI') > TO_CHAR(c.GioKetThuc, 'HH24:MI') " +
                           "   AND ( " +
                           "     (TRUNC(dh.NgayDat) = TRUNC(l.NgayLamViec) AND TO_CHAR(dh.NgayDat, 'HH24:MI') >= TO_CHAR(c.GioBatDau, 'HH24:MI')) " +
                           "     OR " +
                           "     (TRUNC(dh.NgayDat) = TRUNC(l.NgayLamViec) + 1 AND TO_CHAR(dh.NgayDat, 'HH24:MI') <= TO_CHAR(c.GioKetThuc, 'HH24:MI')) " +
                           "   ) " +
                           "  ) " +
                           ") ";

        String sql = "SELECT c.TenCa, NVL(SUM(dh.ThanhTien), 0) AS DoanhThu " +
                     "FROM CA_LAM_VIEC c " +
                     "JOIN LICH_LAM_VIEC l ON c.MaCa = l.MaCa " +
                     joinLogic +
                     "WHERE " + dateConditionLLV + " " +
                     "GROUP BY c.TenCa";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{ rs.getString("TenCa"), rs.getLong("DoanhThu") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // [ĐÃ CẬP NHẬT LOGIC SO SÁNH GIỜ]
    public List<Object[]> getCustomShiftRevenueChartData(java.util.Date startDate, java.util.Date endDate) {
        List<Object[]> list = new ArrayList<>();
        
        String joinLogic = "LEFT JOIN DON_HANG dh ON l.MaTaiKhoan = dh.MaTaiKhoan AND dh.TrangThaiThanhToan = N'Đã thanh toán' " +
                           "AND ( " +
                           "  (TO_CHAR(c.GioBatDau, 'HH24:MI') <= TO_CHAR(c.GioKetThuc, 'HH24:MI') " +
                           "   AND TRUNC(dh.NgayDat) = TRUNC(l.NgayLamViec) " +
                           "   AND TO_CHAR(dh.NgayDat, 'HH24:MI') >= TO_CHAR(c.GioBatDau, 'HH24:MI') " +
                           "   AND TO_CHAR(dh.NgayDat, 'HH24:MI') <= TO_CHAR(c.GioKetThuc, 'HH24:MI')) " +
                           "  OR " +
                           "  (TO_CHAR(c.GioBatDau, 'HH24:MI') > TO_CHAR(c.GioKetThuc, 'HH24:MI') " +
                           "   AND ( " +
                           "     (TRUNC(dh.NgayDat) = TRUNC(l.NgayLamViec) AND TO_CHAR(dh.NgayDat, 'HH24:MI') >= TO_CHAR(c.GioBatDau, 'HH24:MI')) " +
                           "     OR " +
                           "     (TRUNC(dh.NgayDat) = TRUNC(l.NgayLamViec) + 1 AND TO_CHAR(dh.NgayDat, 'HH24:MI') <= TO_CHAR(c.GioKetThuc, 'HH24:MI')) " +
                           "   ) " +
                           "  ) " +
                           ") ";

        String sql = "SELECT c.TenCa, NVL(SUM(dh.ThanhTien), 0) AS DoanhThu " +
                     "FROM CA_LAM_VIEC c " +
                     "JOIN LICH_LAM_VIEC l ON c.MaCa = l.MaCa " +
                     joinLogic +
                     "WHERE TRUNC(l.NgayLamViec) >= ? AND TRUNC(l.NgayLamViec) <= ? " +
                     "GROUP BY c.TenCa";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{ rs.getString("TenCa"), rs.getLong("DoanhThu") });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Biểu đồ Đường: Xu hướng tổng giờ làm việc của cả quán theo ngày
    public List<Object[]> getWorkingHoursChartData(String filterType) {
        List<Object[]> list = new ArrayList<>();
        String dateConditionLLV = "";
        String groupBy = "TO_CHAR(l.NgayLamViec, 'DD/MM')"; 
        String orderBy = "TRUNC(l.NgayLamViec)";

        switch (filterType) {
            case "Tuần này": dateConditionLLV = "l.NgayLamViec >= TRUNC(SYSDATE, 'IW')"; break;
            case "Tháng này": dateConditionLLV = "TRUNC(l.NgayLamViec, 'MM') = TRUNC(SYSDATE, 'MM')"; break;
            default: dateConditionLLV = "l.NgayLamViec >= TRUNC(SYSDATE) - 6"; 
        }

        String sql = "SELECT " + groupBy + " AS NhanX, " +
                     "NVL(SUM((TO_DATE(TO_CHAR(c.GioKetThuc, 'HH24:MI'), 'HH24:MI') - TO_DATE(TO_CHAR(c.GioBatDau, 'HH24:MI'), 'HH24:MI') + CASE WHEN TO_CHAR(c.GioKetThuc, 'HH24:MI') < TO_CHAR(c.GioBatDau, 'HH24:MI') THEN 1 ELSE 0 END) * 24), 0) AS TongGio " +
                     "FROM LICH_LAM_VIEC l JOIN CA_LAM_VIEC c ON l.MaCa = c.MaCa " +
                     "WHERE " + dateConditionLLV + " " +
                     "GROUP BY " + groupBy + ", " + orderBy + " " +
                     "ORDER BY " + orderBy + " ASC";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{ rs.getString("NhanX"), rs.getDouble("TongGio") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> getCustomWorkingHoursChartData(java.util.Date startDate, java.util.Date endDate) {
        List<Object[]> list = new ArrayList<>();
        String groupBy = "TO_CHAR(l.NgayLamViec, 'DD/MM')"; 
        String orderBy = "TRUNC(l.NgayLamViec)";
        
        String sql = "SELECT " + groupBy + " AS NhanX, " +
                     "NVL(SUM((TO_DATE(TO_CHAR(c.GioKetThuc, 'HH24:MI'), 'HH24:MI') - TO_DATE(TO_CHAR(c.GioBatDau, 'HH24:MI'), 'HH24:MI') + CASE WHEN TO_CHAR(c.GioKetThuc, 'HH24:MI') < TO_CHAR(c.GioBatDau, 'HH24:MI') THEN 1 ELSE 0 END) * 24), 0) AS TongGio " +
                     "FROM LICH_LAM_VIEC l JOIN CA_LAM_VIEC c ON l.MaCa = c.MaCa " +
                     "WHERE TRUNC(l.NgayLamViec) >= ? AND TRUNC(l.NgayLamViec) <= ? " +
                     "GROUP BY " + groupBy + ", " + orderBy + " " +
                     "ORDER BY " + orderBy + " ASC";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{ rs.getString("NhanX"), rs.getDouble("TongGio") });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================================
    // TAB 4: BÁO CÁO KHO 
    // =========================================================================
    public Object[] getInventoryOverviewStats() {
        Object[] result = {0L, 0, 0L}; 
        String sql1 = "SELECT NVL(SUM(SoLuongTon * NVL(DonGiaBinhQuan, 0)), 0) FROM NGUYEN_LIEU";
        String sql2 = "SELECT COUNT(*) FROM NGUYEN_LIEU WHERE SoLuongTon <= NguongCanhBao";
        String sql3 = "SELECT NVL(SUM(TongGiaTri), 0) FROM PHIEU_NHAP_KHO " +
                      "WHERE EXTRACT(MONTH FROM NgayNhap) = EXTRACT(MONTH FROM SYSDATE) " +
                      "AND EXTRACT(YEAR FROM NgayNhap) = EXTRACT(YEAR FROM SYSDATE)";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection()) {
            try(PreparedStatement ps1 = conn.prepareStatement(sql1); ResultSet rs1 = ps1.executeQuery()) {
                if(rs1.next()) result[0] = rs1.getLong(1);
            }
            try(PreparedStatement ps2 = conn.prepareStatement(sql2); ResultSet rs2 = ps2.executeQuery()) {
                if(rs2.next()) result[1] = rs2.getInt(1);
            }
            try(PreparedStatement ps3 = conn.prepareStatement(sql3); ResultSet rs3 = ps3.executeQuery()) {
                if(rs3.next()) result[2] = rs3.getLong(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getExpiringIngredients() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT l.MaLo, nl.TenNguyenLieu, nl.DonViTinh, l.SoLuongConLai, TO_CHAR(l.HanSuDung, 'DD/MM/YYYY') AS HSD " +
                     "FROM LO_NGUYEN_LIEU l " +
                     "JOIN NGUYEN_LIEU nl ON l.MaNguyenLieu = nl.MaNguyenLieu " +
                     "WHERE l.HanSuDung <= (SYSDATE + 7) " +
                     "AND l.SoLuongConLai > 0 " +
                     "ORDER BY l.HanSuDung ASC";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("MaLo"),
                    rs.getString("TenNguyenLieu"),
                    rs.getDouble("SoLuongConLai") + " " + rs.getString("DonViTinh"),
                    rs.getString("HSD")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> getMostUsedIngredients() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TenNguyenLieu, DonViTinh, SUM(TongSuDung) AS TongTieuHao FROM ( " +
                     "    SELECT nl.TenNguyenLieu, nl.DonViTinh, (ctdh.SoLuong * ct.SoLuongCan) AS TongSuDung " +
                     "    FROM DON_HANG dh " +
                     "    JOIN CHI_TIET_DON_HANG ctdh ON dh.MaDonHang = ctdh.MaDonHang " +
                     "    JOIN CONG_THUC ct ON ctdh.MaBienThe = ct.MaBienThe " +
                     "    JOIN NGUYEN_LIEU nl ON ct.MaNguyenLieu = nl.MaNguyenLieu " +
                     "    WHERE dh.TrangThaiPhaChe = N'Đã hoàn thành' " +
                     "      AND EXTRACT(MONTH FROM dh.NgayDat) = EXTRACT(MONTH FROM SYSDATE) " +
                     "    UNION ALL " +
                     "    SELECT nl.TenNguyenLieu, nl.DonViTinh, (ctdh.SoLuong * tp.DinhLuongHaoHut) AS TongSuDung " +
                     "    FROM DON_HANG dh " +
                     "    JOIN CHI_TIET_DON_HANG ctdh ON dh.MaDonHang = ctdh.MaDonHang " +
                     "    JOIN CHI_TIET_TOPPING ctt ON ctdh.MaChiTietDon = ctt.MaCTHD " +
                     "    JOIN TOPPING tp ON ctt.MaTopping = tp.MaTopping " +
                     "    JOIN NGUYEN_LIEU nl ON tp.MaNguyenLieuTru = nl.MaNguyenLieu " +
                     "    WHERE dh.TrangThaiPhaChe = N'Đã hoàn thành' AND tp.MaNguyenLieuTru IS NOT NULL " +
                     "      AND EXTRACT(MONTH FROM dh.NgayDat) = EXTRACT(MONTH FROM SYSDATE) " +
                     ") " +
                     "GROUP BY TenNguyenLieu, DonViTinh " +
                     "ORDER BY TongTieuHao DESC " +
                     "FETCH FIRST 10 ROWS ONLY";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("TenNguyenLieu"),
                    rs.getDouble("TongTieuHao") + " " + rs.getString("DonViTinh")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================================
    // TAB 5: BÁO CÁO KHÁCH HÀNG
    // =========================================================================
    public Object[] getCustomerOverviewStats(String filterType) {
        Object[] result = {0, 0.0, 0L, 0L}; 
        String dateConditionKH = "";
        String dateConditionDH = "";

        switch (filterType) {
            case "Tuần này":
                dateConditionKH = "NgayDangKy >= TRUNC(SYSDATE, 'IW')";
                dateConditionDH = "NgayDat >= TRUNC(SYSDATE, 'IW')";
                break;
            case "Tháng này":
                dateConditionKH = "TRUNC(NgayDangKy, 'MM') = TRUNC(SYSDATE, 'MM')";
                dateConditionDH = "TRUNC(NgayDat, 'MM') = TRUNC(SYSDATE, 'MM')";
                break;
            default: 
                dateConditionKH = "NgayDangKy >= TRUNC(SYSDATE) - 6";
                dateConditionDH = "NgayDat >= TRUNC(SYSDATE) - 6";
        }

        String sqlNewCus = "SELECT COUNT(MaKhachHang) FROM KHACH_HANG WHERE " + dateConditionKH;
        
        String sqlAnalytics = 
            "WITH KhachMua AS ( " +
            "    SELECT MaKhachHang, COUNT(MaDonHang) as SoDon, SUM(ThanhTien) as TongTien " +
            "    FROM DON_HANG " +
            "    WHERE MaKhachHang IS NOT NULL AND TrangThaiThanhToan = N'Đã thanh toán' AND " + dateConditionDH + " " +
            "    GROUP BY MaKhachHang " +
            ") " +
            "SELECT " +
            "    NVL(SUM(CASE WHEN SoDon >= 2 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(MaKhachHang), 0), 0) AS RetentionRate, " +
            "    NVL(SUM(TongTien) / NULLIF(COUNT(MaKhachHang), 0), 0) AS ARPU " +
            "FROM KhachMua";

        String sqlTotalPoints = "SELECT NVL(SUM(DiemTichLuy), 0) FROM KHACH_HANG";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection()) {
            try(PreparedStatement ps1 = conn.prepareStatement(sqlNewCus); ResultSet rs1 = ps1.executeQuery()) {
                if(rs1.next()) result[0] = rs1.getInt(1);
            }
            try(PreparedStatement ps2 = conn.prepareStatement(sqlAnalytics); ResultSet rs2 = ps2.executeQuery()) {
                if(rs2.next()) {
                    result[1] = rs2.getDouble("RetentionRate");
                    result[2] = rs2.getLong("ARPU");
                }
            }
            try(PreparedStatement ps3 = conn.prepareStatement(sqlTotalPoints); ResultSet rs3 = ps3.executeQuery()) {
                if(rs3.next()) result[3] = rs3.getLong(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public Object[] getCustomCustomerOverviewStats(java.util.Date startDate, java.util.Date endDate) {
        Object[] result = {0, 0.0, 0L, 0L}; 
        String sqlNewCus = "SELECT COUNT(MaKhachHang) FROM KHACH_HANG WHERE TRUNC(NgayDangKy) >= ? AND TRUNC(NgayDangKy) <= ?";
        
        String sqlAnalytics = 
            "WITH KhachMua AS ( " +
            "    SELECT MaKhachHang, COUNT(MaDonHang) as SoDon, SUM(ThanhTien) as TongTien " +
            "    FROM DON_HANG " +
            "    WHERE MaKhachHang IS NOT NULL AND TrangThaiThanhToan = N'Đã thanh toán' " +
            "      AND TRUNC(NgayDat) >= ? AND TRUNC(NgayDat) <= ? " +
            "    GROUP BY MaKhachHang " +
            ") " +
            "SELECT " +
            "    NVL(SUM(CASE WHEN SoDon >= 2 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(MaKhachHang), 0), 0) AS RetentionRate, " +
            "    NVL(SUM(TongTien) / NULLIF(COUNT(MaKhachHang), 0), 0) AS ARPU " +
            "FROM KhachMua";

        String sqlTotalPoints = "SELECT NVL(SUM(DiemTichLuy), 0) FROM KHACH_HANG";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection()) {
            try(PreparedStatement ps1 = conn.prepareStatement(sqlNewCus)) {
                ps1.setDate(1, new java.sql.Date(startDate.getTime()));
                ps1.setDate(2, new java.sql.Date(endDate.getTime()));
                try(ResultSet rs1 = ps1.executeQuery()) {
                    if(rs1.next()) result[0] = rs1.getInt(1);
                }
            }
            try(PreparedStatement ps2 = conn.prepareStatement(sqlAnalytics)) {
                ps2.setDate(1, new java.sql.Date(startDate.getTime()));
                ps2.setDate(2, new java.sql.Date(endDate.getTime()));
                try(ResultSet rs2 = ps2.executeQuery()) {
                    if(rs2.next()) {
                        result[1] = rs2.getDouble("RetentionRate");
                        result[2] = rs2.getLong("ARPU");
                    }
                }
            }
            try(PreparedStatement ps3 = conn.prepareStatement(sqlTotalPoints); ResultSet rs3 = ps3.executeQuery()) {
                if(rs3.next()) result[3] = rs3.getLong(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getCustomerGrowthChartData(String filterType) {
        List<Object[]> list = new ArrayList<>();
        String dateCondition = "";
        String groupBy = "TO_CHAR(NgayDangKy, 'DD/MM')"; 
        String orderBy = "TRUNC(NgayDangKy)";

        switch (filterType) {
            case "Tuần này": dateCondition = "NgayDangKy >= TRUNC(SYSDATE, 'IW')"; break;
            case "Tháng này": dateCondition = "TRUNC(NgayDangKy, 'MM') = TRUNC(SYSDATE, 'MM')"; break;
            default: dateCondition = "NgayDangKy >= TRUNC(SYSDATE) - 6"; 
        }

        String sql = "SELECT " + groupBy + " AS NhanX, COUNT(MaKhachHang) AS SoKhach " +
                     "FROM KHACH_HANG " +
                     "WHERE " + dateCondition + " " +
                     "GROUP BY " + groupBy + ", " + orderBy + " " +
                     "ORDER BY " + orderBy + " ASC";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{ rs.getString("NhanX"), rs.getInt("SoKhach") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> getCustomCustomerGrowthChartData(java.util.Date startDate, java.util.Date endDate) {
        List<Object[]> list = new ArrayList<>();
        String groupBy = "TO_CHAR(NgayDangKy, 'DD/MM')";
        String orderBy = "TRUNC(NgayDangKy)";
        
        String sql = "SELECT " + groupBy + " AS NhanX, COUNT(MaKhachHang) AS SoKhach " +
                     "FROM KHACH_HANG " +
                     "WHERE TRUNC(NgayDangKy) >= ? AND TRUNC(NgayDangKy) <= ? " +
                     "GROUP BY " + groupBy + ", " + orderBy + " " +
                     "ORDER BY " + orderBy + " ASC";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{ rs.getString("NhanX"), rs.getInt("SoKhach") });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}