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

/**
 *
 * @author Kiet
 */
public class WarehouseReceiptDAO {
    public List<WarehouseReceiptModel> getWarehouseReceipt(){
        List<WarehouseReceiptModel> danhSachPhieuNhap = new ArrayList<>();
        String query = "SELECT MaPhieuNhap, NgayNhap, TK.MaTaiKhoan, TongGiaTri, ND.HoTen "
                + "FROM PHIEU_NHAP_KHO PNK "
                + "JOIN TAI_KHOAN TK on PNK.MaTaiKhoan = TK.MaTaiKhoan "
                + "JOIN NGUOI_DUNG ND ON TK.MaNguoiDung = ND.MaNguoiDung ";
   
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            while(rs.next()){
                WarehouseReceiptModel t = new WarehouseReceiptModel(rs.getInt("MaPhieuNhap"),
                                    rs.getObject("NgayNhap", java.time.LocalDate.class),
                                    rs.getInt("MaTaiKhoan"),
                                    rs.getLong("TongGiaTri"),
                                    rs.getString("HoTen"));
                danhSachPhieuNhap.add(t);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        return danhSachPhieuNhap;
    }


    public int insertPhieuNhap(int accountID, List<WarehouseReceiptDetailModel> list) throws Exception {
        // Lấy Connection từ class quản lý DB của bạn (ví dụ: DatabaseHelper)
        Connection conn = ConnectionUtils.getMyConnection(); 
        int maPhieuMoi = -1;

        try {
            // 1. Tạo mảng Struct chứa các dòng chi tiết
            Struct[] structArray = new Struct[list.size()];

            for (int i = 0; i < list.size(); i++) {
                WarehouseReceiptDetailModel item = list.get(i);

                // THỨ TỰ BẮT BUỘC KHỚP VỚI 'T_CHI_TIET_PN_FULL' TRONG ORACLE:
                // (MaNguyenLieu, MaLoaiNguyenLieu, TenNguyenLieu, DonViTinh, NhaCungCap, SoLuong, DonGia)
                Object[] attributes = new Object[] {
                    item.getIngredientID(),       // Nếu là nguyên liệu mới, cái này mang giá trị null
                    item.getIngredientTypeID(),
                    item.getIngredientName(),
                    item.getUnit(),
                    item.getProviderName(),
                    item.getQuantity(),
                    item.getPrice()
                };

                // Ép kiểu dòng này thành Struct. Tên type phải VIẾT IN HOA
                structArray[i] = conn.createStruct("T_CHI_TIET_PN_FULL", attributes);
            }

            
            // 2. Gom Struct thành Array (Sử dụng chuẩn của riêng Oracle)
            
            // Ép kiểu Connection chung thành OracleConnection
            OracleConnection oracleConn;
            if (conn.isWrapperFor(OracleConnection.class)) {
                oracleConn = conn.unwrap(OracleConnection.class);
            } else {
                oracleConn = (OracleConnection) conn; 
            }

            // Dùng hàm createOracleArray thay vì createArrayOf
            Array oracleArray = oracleConn.createOracleArray("T_LIST_CHI_TIET_PN_FULL", structArray);

            // 3. Gọi Procedure (3 tham số IN, 1 tham số OUT)
            String sql = "{call SP_LAP_PHIEU_NHAP_FULL(?, ?, ?, ?)}";
            
            try (CallableStatement cstmt = conn.prepareCall(sql)) {
                // Set dữ liệu cho các tham số IN
                cstmt.setInt(1, accountID);
                cstmt.setArray(2, oracleArray); // Truyền nguyên mảng dữ liệu vào DB
                java.sql.Date sqlDate = java.sql.Date.valueOf(list.get(0).getImportingDate());
                cstmt.setDate(3, sqlDate);
                
                // Đăng ký tham số OUT để lấy Mã phiếu vừa được tự động sinh ra
                cstmt.registerOutParameter(4, Types.NUMERIC);

                // Thực thi Procedure
                cstmt.execute();

                // Lấy kết quả mã phiếu trả về
                maPhieuMoi = cstmt.getInt(4);
            }
            
            return maPhieuMoi; // Trả về mã phiếu để Controller hiển thị thông báo
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Lỗi khi lưu phiếu nhập: " + e.getMessage());
        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }
    
    public boolean deleteReceiptWithLog(int warehouseReceiptID, int accountID, String reason) {
        String sql = "{CALL SP_XOA_PHIEU_NHAP(?, ?, ?, ?)}"; 

        try (Connection conn = getMyConnection();
            CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, warehouseReceiptID);
            cs.setInt(2, accountID);
            cs.setString(3, reason);
            cs.registerOutParameter(4, java.sql.Types.NVARCHAR);

            cs.execute();

            String ketQua = cs.getString(4);

            if (ketQua.equals("Thành công")) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, ketQua, "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi kết nối: " + e.getMessage());
            return false;
        }
    }
    
    public String getReceiptDetail(int receiptID){
        String receiptDetail = "";
        String query = "select TenNguyenLieu, SoLuong, DonViTinh " +
                    "from PHIEU_NHAP_KHO pnk " +
                    "join chi_tiet_phieu_nhap ct on ct.MaPhieuNhap = pnk.MaPhieuNhap " +
                    "join nguyen_lieu nl on nl.MaNguyenLieu = ct.MaNguyenLieu " + 
                    "where ct.MaPhieuNhap = ?";
   
        try (Connection conn = getMyConnection();){ 
            
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, receiptID);
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                receiptDetail += "- " + rs.getString("TenNguyenLieu") 
                              + " nhập " 
                              + rs.getInt("SoLuong") 
                              + rs.getString("DonViTinh") 
                              + "\n";
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        return receiptDetail;
    }
}
