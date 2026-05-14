package DatabaseAccessObject;

import Model.CartItemModel;
import Model.OrderDetailModel;
import Model.OrderModel;
import Model.ToppingModel;
import Model.SessionManager; // <-- THÊM IMPORT NÀY
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public boolean createOrder(int accountId, Integer maKhachHang, List<CartItemModel> cart, long finalTotal, double totalVat, String prepStatus, String payStatus, boolean isTakeaway, boolean isHoliday, int pointsEarned, int pointsUsed) {
        // [QUAN TRỌNG] Đã thêm MaPhienCa vào câu INSERT
        String insertOrderSQL = "INSERT INTO DON_HANG (MaTaiKhoan, MaKhachHang, TongTien, TongTienThue, ThanhTien, TrangThaiPhaChe, TrangThaiThanhToan, GhiChu, MaPhienCa) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertOrderDetailSQL = "INSERT INTO CHI_TIET_DON_HANG (MaDonHang, MaBienThe, SoLuong, GiaTruocThue, TienThue, ThanhTien, GhiChuMon) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertToppingSQL = "INSERT INTO CHI_TIET_TOPPING (MaCTHD, MaTopping, SoLuong, GiaTruocThue, TienThue) VALUES (?, ?, ?, ?, ?)";
        
        String updateCustomerPointsSQL = "UPDATE KHACH_HANG SET DiemTichLuy = DiemTichLuy + ? - ? WHERE MaKhachHang = ?";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection()) {
            conn.setAutoCommit(false);

            try {
                int orderId = -1;
                long subTotal = finalTotal - Math.round(totalVat);
                String orderTypeNote = isHoliday ? "[LỄ] " : "";
                orderTypeNote += isTakeaway ? "Mua mang đi" : "Dùng tại quán";

                try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSQL, new String[]{"MADONHANG"})) {
                    psOrder.setInt(1, accountId);
                    if (maKhachHang != null) {
                        psOrder.setInt(2, maKhachHang);
                    } else {
                        psOrder.setNull(2, java.sql.Types.INTEGER);
                    }
                    psOrder.setLong(3, subTotal);
                    psOrder.setLong(4, Math.round(totalVat));
                    psOrder.setLong(5, finalTotal);
                    psOrder.setString(6, prepStatus);
                    psOrder.setString(7, payStatus);
                    psOrder.setString(8, orderTypeNote); 
                    
                    // [MỚI] Gán ID Ca đang làm việc lấy từ SessionManager
                    if (SessionManager.hasOpenShift()) {
                        psOrder.setInt(9, SessionManager.getCurrentMaPhienCa());
                    } else {
                        psOrder.setNull(9, java.sql.Types.INTEGER);
                    }
                    
                    psOrder.executeUpdate();

                    try (ResultSet rs = psOrder.getGeneratedKeys()) {
                        if (rs.next()) {
                            orderId = rs.getInt(1);
                        }
                    }
                }

                if (orderId == -1) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement psDetail = conn.prepareStatement(insertOrderDetailSQL, new String[]{"MACHITIETDON"});
                     PreparedStatement psTopping = conn.prepareStatement(insertToppingSQL)) {

                    for (CartItemModel item : cart) {
                        int variantId = item.getSelectedVariant() != null ? item.getSelectedVariant().getVariantID() : 0;
                        long mainSellingPrice = item.getMainSellingPrice(); 
                        double vatRate = item.getProduct().getVat();
                        double priceBeforeTax = mainSellingPrice / (1.0 + (vatRate / 100.0));
                        double taxAmount = mainSellingPrice - priceBeforeTax;
                        long totalRowPrice = mainSellingPrice * item.getQuantity();

                        psDetail.setInt(1, orderId);
                        psDetail.setInt(2, variantId);
                        psDetail.setInt(3, item.getQuantity());
                        psDetail.setLong(4, Math.round(priceBeforeTax)); 
                        psDetail.setLong(5, Math.round(taxAmount));     
                        psDetail.setLong(6, totalRowPrice);             
                        psDetail.setString(7, item.getNote()); 
                        psDetail.executeUpdate();

                        int detailId = -1;
                        try (ResultSet rs = psDetail.getGeneratedKeys()) {
                            if (rs.next()) {
                                detailId = rs.getInt(1);
                            }
                        }

                        if (item.getSelectedToppings() != null && !item.getSelectedToppings().isEmpty()) {
                            for (ToppingModel topping : item.getSelectedToppings()) {
                                double toppingVatRate = topping.getVat();
                                long toppingPrice = item.isReward() ? 0 : topping.getPrice();
                                double toppingPriceBeforeTax = toppingPrice / (1.0 + (toppingVatRate / 100.0));
                                double toppingTax = toppingPrice - toppingPriceBeforeTax;

                                psTopping.setInt(1, detailId);
                                psTopping.setInt(2, topping.getToppingID());
                                psTopping.setInt(3, item.getQuantity()); 
                                psTopping.setLong(4, Math.round(toppingPriceBeforeTax));
                                psTopping.setLong(5, Math.round(toppingTax));
                                psTopping.addBatch(); 
                            }
                            psTopping.executeBatch(); 
                        }
                    }
                }
                
                if (maKhachHang != null && (pointsEarned > 0 || pointsUsed > 0)) {
                    try (PreparedStatement psUpdatePoints = conn.prepareStatement(updateCustomerPointsSQL)) {
                        psUpdatePoints.setInt(1, pointsEarned);
                        psUpdatePoints.setInt(2, pointsUsed);
                        psUpdatePoints.setInt(3, maKhachHang);
                        psUpdatePoints.executeUpdate();
                    }
                }

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<OrderModel> getAllOrders(String statusFilter, String keyword) {
        List<OrderModel> list = new ArrayList<>();
        String sql = "SELECT MaDonHang, MaTaiKhoan, TO_CHAR(NgayDat, 'DD/MM/YYYY HH24:MI') as Ngay, "
                   + "TongTien, TongTienThue, ThanhTien, TrangThaiPhaChe, TrangThaiThanhToan, GhiChu "
                   + "FROM DON_HANG WHERE 1=1 ";
        
        if (!statusFilter.equals("Tất cả")) {
            if (statusFilter.equals("Chưa thanh toán") || statusFilter.equals("Đã thanh toán") || statusFilter.equals("Đã hoàn tiền")) {
                sql += " AND TrangThaiThanhToan = ? ";
            } else {
                sql += " AND TrangThaiPhaChe = ? "; 
            }
        }

        if (!keyword.isEmpty()) sql += " AND MaDonHang LIKE ? ";
        sql += " ORDER BY MaDonHang DESC";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            int paramIdx = 1;
            if (!statusFilter.equals("Tất cả")) ps.setString(paramIdx++, statusFilter);
            if (!keyword.isEmpty()) ps.setString(paramIdx++, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderModel(
                        rs.getInt("MaDonHang"), rs.getInt("MaTaiKhoan"), rs.getString("Ngay"),
                        rs.getLong("TongTien"), rs.getLong("TongTienThue"), rs.getLong("ThanhTien"),
                        rs.getString("TrangThaiPhaChe"), rs.getString("TrangThaiThanhToan"), rs.getString("GhiChu")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<OrderDetailModel> getOrderDetailsByOrderId(int orderId) {
        List<OrderDetailModel> list = new ArrayList<>();
        
        String sql = "SELECT ct.MaChiTietDon, sp.TenSanPham, bt.TenSize, ct.SoLuong, ct.ThanhTien, ct.GhiChuMon, "
                   + "(SELECT LISTAGG(t.TenTopping, ', ') WITHIN GROUP (ORDER BY t.TenTopping) "
                   + " FROM CHI_TIET_TOPPING ctt JOIN TOPPING t ON ctt.MaTopping = t.MaTopping "
                   + " WHERE ctt.MaCTHD = ct.MaChiTietDon) as Toppings "
                   + "FROM CHI_TIET_DON_HANG ct "
                   + "JOIN BIEN_THE bt ON ct.MaBienThe = bt.MaBienThe "
                   + "JOIN SAN_PHAM sp ON bt.MaSanPham = sp.MaSanPham "
                   + "WHERE ct.MaDonHang = ?";
        
        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String toppings = rs.getString("Toppings");
                    String note = rs.getString("GhiChuMon");
                    
                    String displayInfo = (toppings != null ? toppings : "");
                    if (note != null && !note.trim().isEmpty()) {
                        if (!displayInfo.isEmpty()) displayInfo += " | ";
                        displayInfo += note;
                    }

                    list.add(new OrderDetailModel(
                        rs.getInt("MaChiTietDon"), orderId, rs.getString("TenSanPham"),
                        rs.getString("TenSize"), displayInfo, 
                        rs.getInt("SoLuong"), rs.getLong("ThanhTien")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean updatePreparationStatus(int orderId, String newStatus) {
        String sql = "UPDATE DON_HANG SET TrangThaiPhaChe = ? WHERE MaDonHang = ?";
        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean updatePaymentStatus(int orderId, String newStatus) {
        String sql = "UPDATE DON_HANG SET TrangThaiThanhToan = ? WHERE MaDonHang = ?";
        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean completeAndDeductStock(int orderId) {
        Connection conn = null;
        try {
            conn = ConnectDatabase.ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false); 

            String sqlUpdateStatus = "UPDATE DON_HANG SET TrangThaiPhaChe = N'Đã hoàn thành' WHERE MaDonHang = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateStatus)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }

            String sqlGetItems = "SELECT MaBienThe, SoLuong FROM CHI_TIET_DON_HANG WHERE MaDonHang = ?";
            try (PreparedStatement psGetItems = conn.prepareStatement(sqlGetItems)) {
                psGetItems.setInt(1, orderId);
                try (ResultSet rsItems = psGetItems.executeQuery()) {
                    while (rsItems.next()) {
                        int variantId = rsItems.getInt("MaBienThe");
                        int itemQty = rsItems.getInt("SoLuong");

                        String sqlRecipe = "SELECT MaNguyenLieu, SoLuongCan FROM CONG_THUC WHERE MaBienThe = ?";
                        try (PreparedStatement psRecipe = conn.prepareStatement(sqlRecipe)) {
                            psRecipe.setInt(1, variantId);
                            try (ResultSet rsRec = psRecipe.executeQuery()) {
                                while (rsRec.next()) {
                                    int maNguyenLieu = rsRec.getInt("MaNguyenLieu");
                                    double soLuongCanTru = rsRec.getDouble("SoLuongCan") * itemQty;
                                    deductStockFEFO(conn, maNguyenLieu, soLuongCanTru);
                                }
                            }
                        }
                    }
                }
            }

            String sqlGetToppings = "SELECT tp.MaNguyenLieuTru, tp.DinhLuongHaoHut, ctt.SoLuong " +
                                    "FROM CHI_TIET_TOPPING ctt " +
                                    "JOIN TOPPING tp ON ctt.MaTopping = tp.MaTopping " +
                                    "JOIN CHI_TIET_DON_HANG ctdh ON ctt.MaCTHD = ctdh.MaChiTietDon " +
                                    "WHERE ctdh.MaDonHang = ?";
            
            try (PreparedStatement psGetTops = conn.prepareStatement(sqlGetToppings)) {
                psGetTops.setInt(1, orderId);
                try (ResultSet rsTops = psGetTops.executeQuery()) {
                    while (rsTops.next()) {
                        int ingId = rsTops.getInt("MaNguyenLieuTru");
                        double loss = rsTops.getDouble("DinhLuongHaoHut");
                        int qty = rsTops.getInt("SoLuong");
                        
                        if (ingId > 0) {
                            double soLuongToppingCanTru = loss * qty;
                            deductStockFEFO(conn, ingId, soLuongToppingCanTru);
                        }
                    }
                }
            }

            conn.commit(); 
            return true;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    private void deductStockFEFO(Connection conn, int maNguyenLieu, double tongSoLuongCanTru) throws SQLException {
        String sqlGetBatches = "SELECT MaLo, SoLuongConLai FROM LO_NGUYEN_LIEU " +
                               "WHERE MaNguyenLieu = ? AND SoLuongConLai > 0 " +
                               "ORDER BY HanSuDung ASC FOR UPDATE";
                               
        String sqlUpdateBatch = "UPDATE LO_NGUYEN_LIEU SET SoLuongConLai = ? WHERE MaLo = ?";
        
        double soLuongChuaTruHet = tongSoLuongCanTru;

        try (PreparedStatement psGetBatches = conn.prepareStatement(sqlGetBatches)) {
            psGetBatches.setInt(1, maNguyenLieu);
            
            try (ResultSet rsBatches = psGetBatches.executeQuery()) {
                while (rsBatches.next() && soLuongChuaTruHet > 0) {
                    int maLo = rsBatches.getInt("MaLo");
                    double tonKhoLo = rsBatches.getDouble("SoLuongConLai");
                    
                    double luongTruO_LoNay = Math.min(tonKhoLo, soLuongChuaTruHet);
                    double tonKhoMoiCuaLo = tonKhoLo - luongTruO_LoNay;
                    
                    try (PreparedStatement psUpdateBatch = conn.prepareStatement(sqlUpdateBatch)) {
                        psUpdateBatch.setDouble(1, tonKhoMoiCuaLo);
                        psUpdateBatch.setInt(2, maLo);
                        psUpdateBatch.executeUpdate();
                    }
                    
                    soLuongChuaTruHet -= luongTruO_LoNay;
                }
                
                if (soLuongChuaTruHet > 0) {
                    throw new SQLException("Nguyên liệu ID " + maNguyenLieu + " không đủ số lượng trong các lô để trừ!");
                }
            }
        }
    }
    
    public OrderModel getOrderById(int orderId) {
        String sql = "SELECT MaDonHang, MaTaiKhoan, TO_CHAR(NgayDat, 'DD/MM/YYYY HH24:MI') as Ngay, "
                   + "TongTien, TongTienThue, ThanhTien, TrangThaiPhaChe, TrangThaiThanhToan, GhiChu "
                   + "FROM DON_HANG WHERE MaDonHang = ?";
                   
        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new OrderModel(
                        rs.getInt("MaDonHang"), rs.getInt("MaTaiKhoan"), rs.getString("Ngay"),
                        rs.getLong("TongTien"), rs.getLong("TongTienThue"), rs.getLong("ThanhTien"),
                        rs.getString("TrangThaiPhaChe"), rs.getString("TrangThaiThanhToan"), rs.getString("GhiChu")
                    );
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return null;
    }
}