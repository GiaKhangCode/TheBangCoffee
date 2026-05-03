package DatabaseAccessObject;

import Model.CartItemModel;
import Model.OrderDetailModel;
import Model.OrderModel;
import Model.ToppingModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public boolean createOrder(int accountId, List<CartItemModel> cart, long finalTotal, double totalVat, String status, boolean isTakeaway, boolean isHoliday) {
        String insertOrderSQL = "INSERT INTO DON_HANG (MaTaiKhoan, TongTien, TongTienThue, ThanhTien, TrangThai, GhiChu) VALUES (?, ?, ?, ?, ?, ?)";
        String insertOrderDetailSQL = "INSERT INTO CHI_TIET_DON_HANG (MaDonHang, MaBienThe, SoLuong, GiaTruocThue, TienThue, ThanhTien, GhiChuMon) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertToppingSQL = "INSERT INTO CHI_TIET_TOPPING (MaCTHD, MaTopping, SoLuong, GiaTruocThue, TienThue) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection()) {
            conn.setAutoCommit(false); 

            try {
                int orderId = -1;
                long subTotal = finalTotal - Math.round(totalVat);
                
                String orderTypeNote = isHoliday ? "[LỄ] " : "";
                orderTypeNote += isTakeaway ? "Mua mang đi" : "Dùng tại quán";

                try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSQL, new String[]{"MADONHANG"})) {
                    psOrder.setInt(1, accountId);
                    psOrder.setLong(2, subTotal);
                    psOrder.setLong(3, Math.round(totalVat));
                    psOrder.setLong(4, finalTotal);
                    psOrder.setString(5, status);
                    psOrder.setString(6, orderTypeNote); 
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
                        
                        // [ĐÃ SỬA LẠI]: Lấy ghi chú (Đá, Đường...) từ CartItemModel để truyền vào cột GhiChuMon
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
                                double toppingPriceBeforeTax = topping.getPrice() / (1.0 + (toppingVatRate / 100.0));
                                double toppingTax = topping.getPrice() - toppingPriceBeforeTax;

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
                   + "TongTien, TongTienThue, ThanhTien, TrangThai, GhiChu "
                   + "FROM DON_HANG WHERE 1=1 ";
        
        if (!statusFilter.equals("Tất cả")) sql += " AND TrangThai = ? ";
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
                        rs.getString("TrangThai"), rs.getString("GhiChu")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<OrderDetailModel> getOrderDetailsByOrderId(int orderId) {
        List<OrderDetailModel> list = new ArrayList<>();
        
        // [ĐÃ SỬA LẠI]: Thêm ct.GhiChuMon vào câu lệnh SELECT
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
                    
                    // [ĐÃ SỬA LẠI]: Nối Topping và Ghi chú lại với nhau để hiển thị lên UI Order Tracking
                    String displayInfo = (toppings != null ? toppings : "");
                    if (note != null && !note.trim().isEmpty()) {
                        if (!displayInfo.isEmpty()) displayInfo += " | ";
                        displayInfo += note;
                    }

                    list.add(new OrderDetailModel(
                        rs.getInt("MaChiTietDon"), orderId, rs.getString("TenSanPham"),
                        rs.getString("TenSize"), displayInfo, // Truyền cái chuỗi đã nối vào đây
                        rs.getInt("SoLuong"), rs.getLong("ThanhTien")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateStatus(int orderId, String newStatus) {
        String sql = "UPDATE DON_HANG SET TrangThai = ? WHERE MaDonHang = ?";
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

            // Cập nhật trạng thái (Bạn đang set là "Chờ thanh toán", mình giữ nguyên nhé)
            String sqlUpdateStatus = "UPDATE DON_HANG SET TrangThai = N'Chờ thanh toán' WHERE MaDonHang = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateStatus)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }

            String sqlGetItems = "SELECT MaBienThe, SoLuong FROM CHI_TIET_DON_HANG WHERE MaDonHang = ?";
            String sqlDeductRecipe = "UPDATE NGUYEN_LIEU SET SoLuongTon = SoLuongTon - (? * ?) WHERE MaNguyenLieu = ?";
            
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
                                    try (PreparedStatement psDeduct = conn.prepareStatement(sqlDeductRecipe)) {
                                        psDeduct.setDouble(1, rsRec.getDouble("SoLuongCan"));
                                        psDeduct.setInt(2, itemQty);
                                        psDeduct.setInt(3, rsRec.getInt("MaNguyenLieu"));
                                        psDeduct.executeUpdate();
                                    }
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
                            try (PreparedStatement psDeduct = conn.prepareStatement(sqlDeductRecipe)) {
                                psDeduct.setDouble(1, loss);
                                psDeduct.setInt(2, qty);
                                psDeduct.setInt(3, ingId);
                                psDeduct.executeUpdate();
                            }
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
    
    public OrderModel getOrderById(int orderId) {
        String sql = "SELECT MaDonHang, MaTaiKhoan, TO_CHAR(NgayDat, 'DD/MM/YYYY HH24:MI') as Ngay, "
                   + "TongTien, TongTienThue, ThanhTien, TrangThai, GhiChu "
                   + "FROM DON_HANG WHERE MaDonHang = ?";
                   
        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new OrderModel(
                        rs.getInt("MaDonHang"), rs.getInt("MaTaiKhoan"), rs.getString("Ngay"),
                        rs.getLong("TongTien"), rs.getLong("TongTienThue"), rs.getLong("ThanhTien"),
                        rs.getString("TrangThai"), rs.getString("GhiChu")
                    );
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return null;
    }
}