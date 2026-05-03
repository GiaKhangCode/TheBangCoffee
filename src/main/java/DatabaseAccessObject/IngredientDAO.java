package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.IngredientModel;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import javax.swing.JOptionPane;

public class IngredientDAO {
    
    public List<IngredientModel> getIngredient() throws SQLException {
        ArrayList<IngredientModel> ingredientList = new ArrayList<>();
        // Lấy trực tiếp vì NGUYEN_LIEU giờ là bảng danh mục chuẩn
        String query = "SELECT MaNguyenLieu, TenNguyenLieu, DonViTinh, SoLuongTon, NguongCanhBao, NhaCungCap, Thue_GTGT, DonGiaBinhQuan "
                     + "FROM NGUYEN_LIEU "
                     + "ORDER BY MaNguyenLieu";
        
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);) { 
        
            while(rs.next()) {
                IngredientModel t = new IngredientModel(
                        rs.getInt("MaNguyenLieu"),
                        rs.getString("TenNguyenLieu"),
                        rs.getString("DonViTinh"),
                        rs.getInt("SoLuongTon"),
                        rs.getInt("NguongCanhBao"),
                        rs.getString("NhaCungCap"),
                        rs.getDouble("Thue_GTGT"),
                        rs.getDouble("DonGiaBinhQuan")
                );
                ingredientList.add(t);
            }
        } catch (Exception e) {
            System.out.println("LỖI KHI KÉO TỒN KHO: " + e.getMessage());
            e.printStackTrace();
        }
        return ingredientList;
    }
    
    public boolean deleteIngredient(int maNL) {
        try {
            Connection conn = getMyConnection();
            String sql = "DELETE FROM NGUYEN_LIEU WHERE MaNguyenLieu = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, maNL);
            int rowAffected = ps.executeUpdate();
            conn.close();
            return rowAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateIngredientWithLog(int maNL, String tenMoi, String dvtMoi, int tonKhoMoi, int nguongMoi, String nhaCungCapMoi, double thueMoi, int maTaiKhoan, String lyDo) {
        // Procedure mới cần 10 tham số (9 IN, 1 OUT)
        String sql = "{CALL SP_SUA_NGUYEN_LIEU(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}"; 
        
        try (Connection conn = getMyConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, maNL);
            cs.setString(2, tenMoi);
            cs.setString(3, dvtMoi);
            cs.setInt(4, tonKhoMoi);
            cs.setInt(5, nguongMoi);
            cs.setString(6, nhaCungCapMoi); // Cột mới
            cs.setDouble(7, thueMoi);       // Cột mới
            cs.setInt(8, maTaiKhoan); 
            cs.setString(9, lyDo);
            
            cs.registerOutParameter(10, Types.NVARCHAR);
            cs.execute();
            
            String ketQua = cs.getString(10);
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

    public boolean deleteIngredientWithLog(int maNL, int maTaiKhoan, String lyDo) {
        String sql = "{CALL SP_XOA_NGUYEN_LIEU(?, ?, ?, ?)}";
        try (Connection conn = getMyConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, maNL);
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
    
    public String getIngredientDetail(String tenNguyenLieu) {
        String detail = "";
        // Truy xuất HSD từ bảng LO_NGUYEN_LIEU
        String query = "SELECT TO_CHAR(pnk.NgayNhap, 'DD/MM/YYYY') AS NgayNhapStr, ct.SoLuong, ct.DinhLuong, nl.DonViTinh, TO_CHAR(lo.HanSuDung, 'DD/MM/YYYY') AS HSD " +
                       "FROM NGUYEN_LIEU nl " +
                       "JOIN LO_NGUYEN_LIEU lo ON nl.MaNguyenLieu = lo.MaNguyenLieu " +
                       "JOIN CHI_TIET_PHIEU_NHAP ct ON (lo.MaPhieuNhap = ct.MaPhieuNhap AND lo.MaNguyenLieu = ct.MaNguyenLieu) " +
                       "JOIN PHIEU_NHAP_KHO pnk ON pnk.MaPhieuNhap = ct.MaPhieuNhap " +
                       "WHERE nl.TenNguyenLieu = ? " +
                       "ORDER BY pnk.NgayNhap DESC";
   
        try (Connection conn = getMyConnection();) { 
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, tenNguyenLieu); 
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String dateStr = rs.getString("NgayNhapStr");
                detail += "- Ngày " + dateStr 
                       + " nhập lô " + rs.getInt("SoLuong") + " gói/hộp " + rs.getDouble("DinhLuong")
                       + rs.getString("DonViTinh") 
                       + " (HSD: " + rs.getString("HSD") + ")\n";
            }
        } catch (Exception e){
            e.printStackTrace(); 
        }
        return detail.isEmpty() ? "Chưa có lịch sử nhập lô cho nguyên liệu này." : detail;
    }
    
    public ArrayList<String> getIngredientNames() {
        String sql = "SELECT DISTINCT TenNguyenLieu FROM NGUYEN_LIEU";
        ArrayList<String> ingredientNames = new ArrayList<>();
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);) {
            while (rs.next()){
                ingredientNames.add(rs.getString("TenNguyenLieu"));
            }
        } catch (Exception e){
            e.printStackTrace(); 
        }
        return ingredientNames;
    }
    
    public int getIngredientIdByName (String name){
        int id = -1;
        String query = "SELECT MaNguyenLieu FROM NGUYEN_LIEU WHERE TenNguyenLieu = ?";
        try (Connection conn = getMyConnection();) { 
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                id = rs.getInt("MaNguyenLieu");
            }
        } catch (Exception e){
            e.printStackTrace(); 
        }
        return id;
    }
    
    public String getUnitByName(String ingredientName) {
        String sql = "SELECT DonViTinh FROM NGUYEN_LIEU WHERE TenNguyenLieu = ?";
        try (java.sql.Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ingredientName);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("DonViTinh");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ""; // Trả về rỗng nếu không tìm thấy
    }
    
    public double getAveragePrice(int ingredientId) {
        double averagePrice = 0;
        String sql = "SELECT DonGiaBinhQuan FROM NGUYEN_LIEU WHERE MaNguyenLieu = ?";
        
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, ingredientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    averagePrice = rs.getDouble("DonGiaBinhQuan");
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy Đơn giá bình quân: " + e.getMessage());
            e.printStackTrace();
        }
        return averagePrice;
    }
    
    // Lấy thông tin 1 nguyên liệu cụ thể để đối chiếu tồn kho
    public IngredientModel getIngredientById(int id) {
        String query = "SELECT MaNguyenLieu, TenNguyenLieu, DonViTinh, SoLuongTon, NguongCanhBao, NhaCungCap, Thue_GTGT, DonGiaBinhQuan FROM NGUYEN_LIEU WHERE MaNguyenLieu = ?";
        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new IngredientModel(
                        rs.getInt("MaNguyenLieu"), 
                        rs.getString("TenNguyenLieu"), 
                        rs.getString("DonViTinh"),
                        rs.getInt("SoLuongTon"), 
                        rs.getInt("NguongCanhBao"), 
                        rs.getString("NhaCungCap"), 
                        rs.getDouble("Thue_GTGT"),
                        rs.getDouble("DonGiaBinhQuan")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}