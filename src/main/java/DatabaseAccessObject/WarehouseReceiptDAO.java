/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import ConnectDatabase.ConnectionUtils;
import static ConnectDatabase.ConnectionUtils.getMyConnection;
import oracle.jdbc.OracleConnection;
import Model.WarehouseReceiptDetailModel;
import Model.WarehouseReceiptModel;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class WarehouseReceiptDAO {
    
    public List<WarehouseReceiptModel> getWarehouseReceipt() {
        List<WarehouseReceiptModel> danhSachPhieuNhap = new ArrayList<>();
        String query = "SELECT MaPhieuNhap, NgayNhap, TK.MaTaiKhoan, TongGiaTri, ND.HoTen, PNK.GhiChu "
                     + "FROM PHIEU_NHAP_KHO PNK "
                     + "JOIN TAI_KHOAN TK on PNK.MaTaiKhoan = TK.MaTaiKhoan "
                     + "JOIN NGUOI_DUNG ND ON TK.MaNguoiDung = ND.MaNguoiDung "
                     + "ORDER BY MaPhieuNhap DESC";
   
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);) { 
        
            while(rs.next()){
                WarehouseReceiptModel t = new WarehouseReceiptModel(
                        rs.getInt("MaPhieuNhap"),
                        rs.getObject("NgayNhap", java.time.LocalDate.class),
                        rs.getInt("MaTaiKhoan"),
                        rs.getLong("TongGiaTri"),
                        rs.getString("HoTen"),
                        rs.getString("GhiChu")
                );
                danhSachPhieuNhap.add(t);
            }
        } catch (Exception e){
            e.printStackTrace(); 
        }
        return danhSachPhieuNhap;
    }

    public int insertWarehouseReceipt(int maTaiKhoan, List<WarehouseReceiptDetailModel> danhSach) throws Exception {
        Connection conn = ConnectionUtils.getMyConnection(); 
        int maPhieuMoi = -1;

        try {
            Struct[] structArray = new Struct[danhSach.size()];

            for (int i = 0; i < danhSach.size(); i++) {
                WarehouseReceiptDetailModel item = danhSach.get(i);

                // MAP CẤU TRÚC ĐÚNG VỚI T_CHI_TIET_PN_FULL BÊN ORACLE
                Object[] attributes = new Object[] {
                    item.getIngredientID(),         // 1. MaNguyenLieu (có thể null)
                    item.getIngredientTypeID(),     // 2. MaLoaiNguyenLieu
                    item.getIngredientName(),       // 3. TenNguyenLieu
                    item.getUnit(),                 // 4. DonViTinh
                    item.getProviderName(),         // 5. NhaCungCap
                    item.getCapacityPerUnit(),      // 6. DinhLuong
                    item.getQuantity(),             // 7. SoLuong
                    item.getPreTaxPrice(),          // 8. GiaTruocThue
                    item.getTaxPercentage(),        // 9. Thue_GTGT
                    item.getTaxAmount(),            // 10. TienThue
                    item.getTotalPrice(),           // 11. ThanhTien
                    item.getThreshold(),            // 12. NguongCanhBao
                    item.getExpiryDate().toString() // 13. HanSuDung
                };            
                
                structArray[i] = conn.createStruct("T_CHI_TIET_PN_FULL", attributes);
            }

            OracleConnection oracleConn;
            if (conn.isWrapperFor(OracleConnection.class)) {
                oracleConn = conn.unwrap(OracleConnection.class);
            } else {
                oracleConn = (OracleConnection) conn; 
            }

            Array oracleArray = oracleConn.createOracleArray("T_LIST_CHI_TIET_PN_FULL", structArray);

            // Bổ sung tham số GhiChu vào Procedure (4 IN, 1 OUT = 5 parameters)
            String sql = "{call SP_LAP_PHIEU_NHAP_FULL(?, ?, ?, ?, ?)}";
            
            try (CallableStatement cstmt = conn.prepareCall(sql)) {
                cstmt.setInt(1, maTaiKhoan);
                cstmt.setArray(2, oracleArray); 
                java.sql.Date sqlDate = java.sql.Date.valueOf(danhSach.get(0).getImportingDate());
                cstmt.setDate(3, sqlDate);
                cstmt.setString(4, "Nhập hàng từ hệ thống"); // Ghi chú mặc định (Có thể lấy từ UI)
                
                cstmt.registerOutParameter(5, Types.NUMERIC);
                cstmt.execute();

                maPhieuMoi = cstmt.getInt(5);
            }
            return maPhieuMoi; 
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Lỗi khi lưu phiếu nhập: " + e.getMessage());
        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }
    
    public boolean deleteReceiptWithLog(int maPhieuNhap, int maTaiKhoan, String lyDo) {
        String sql = "{CALL SP_XOA_PHIEU_NHAP(?, ?, ?, ?)}"; 
        try (Connection conn = getMyConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, maPhieuNhap);
            cs.setInt(2, maTaiKhoan);
            cs.setString(3, lyDo);
            cs.registerOutParameter(4, java.sql.Types.NVARCHAR);

            cs.execute();
            String ketQua = cs.getString(4);

            if (ketQua.equals("Thành công")) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, ketQua, "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi kết nối: " + e.getMessage());
            return false;
        }
    }
    
    public String getReceiptDetail(int receiptID) {
        String receiptDetail = "";
        String query = "SELECT nl.TenNguyenLieu, ct.SoLuong, ct.DinhLuong, nl.DonViTinh, TO_CHAR(lo.HanSuDung, 'DD/MM/YYYY') AS HSD " +
                       "FROM PHIEU_NHAP_KHO pnk " +
                       "JOIN CHI_TIET_PHIEU_NHAP ct ON ct.MaPhieuNhap = pnk.MaPhieuNhap " +
                       "JOIN LO_NGUYEN_LIEU lo ON (lo.MaPhieuNhap = ct.MaPhieuNhap AND lo.MaNguyenLieu = ct.MaNguyenLieu) " +
                       "JOIN NGUYEN_LIEU nl ON nl.MaNguyenLieu = ct.MaNguyenLieu " +
                       "WHERE ct.MaPhieuNhap = ?";
   
        try (Connection conn = getMyConnection();) { 
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, receiptID);
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                receiptDetail += "- " + rs.getString("TenNguyenLieu") 
                              + " nhập " + rs.getInt("SoLuong") + " gói/hộp " + rs.getDouble("DinhLuong") 
                              + rs.getString("DonViTinh") 
                              + " (HSD Lô: " + rs.getString("HSD") + ")\n";
            }
        } catch (Exception e){
            e.printStackTrace(); 
        }
        
        return receiptDetail.isEmpty() ? "Không có chi tiết nguyên liệu cho phiếu nhập này." : receiptDetail;
    }
}


