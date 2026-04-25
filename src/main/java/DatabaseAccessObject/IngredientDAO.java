/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

/**
 *
 * @author Kiet
 */
public class IngredientDAO {
    public List<IngredientModel> getIngredient() throws SQLException{
        ArrayList<IngredientModel> ingredientList = new ArrayList<>();
        String query = "SELECT MIN(nl.MaNguyenLieu) AS MaNguyenLieuDaiDien, "
                     + "UPPER(nl.TenNguyenLieu) AS TenNguyenLieu, "
                     + "CASE "
                     + "    WHEN nl.DonViTinh = 'kg' THEN 'gram' "
                     + "    WHEN nl.DonViTinh = 'lít' THEN 'ml' "
                     + "    ELSE nl.DonViTinh "
                     + "END AS DonViTinhQuyDoi, "
                     + "SUM("
                     + "    nl.SoLuongTon * "
                     + "    COALESCE((SELECT MAX(ct.TongDinhLuong / ct.SoLuong) FROM CHI_TIET_PHIEU_NHAP ct WHERE ct.MaNguyenLieu = nl.MaNguyenLieu), 1) * "
                     + "    CASE WHEN LOWER(nl.DonViTinh) IN ('kg', 'lít') THEN 1000 ELSE 1 END "
                     + ") AS TongDinhLuongHienTai, "
                     + "MAX("
                     + "    nl.NguongCanhBao * "
                     + "    COALESCE((SELECT MAX(ct.TongDinhLuong / ct.SoLuong) FROM CHI_TIET_PHIEU_NHAP ct WHERE ct.MaNguyenLieu = nl.MaNguyenLieu), 1) * "
                     + "    CASE WHEN LOWER(nl.DonViTinh) IN ('kg', 'lít') THEN 1000 ELSE 1 END "
                     + ") AS NguongQuyDoi "
                     + "FROM NGUYEN_LIEU nl "
                     + "GROUP BY UPPER(nl.TenNguyenLieu), "
                     + "CASE "
                     + "    WHEN nl.DonViTinh = 'kg' THEN 'gram' "
                     + "    WHEN nl.DonViTinh = 'lít' THEN 'ml' "
                     + "    ELSE nl.DonViTinh "
                     + "END "
                     + "ORDER BY UPPER(nl.TenNguyenLieu)";

        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);){ 

            while(rs.next()){
                IngredientModel t; 
                t = new IngredientModel(
                        rs.getInt("MaNguyenLieuDaiDien"),
                        rs.getString("TenNguyenLieu"),
                        rs.getString("DonViTinhQuyDoi"),
                        rs.getInt("TongDinhLuongHienTai"),
                        rs.getInt("NguongQuyDoi"));
                ingredientList.add(t);
            }
        }
        catch (Exception e){
            System.out.println("LỖI KHI LẤY DỮ LIỆU TỒN KHO: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ingredientList;
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
    
    public boolean updateIngredientWithLog(int maNL, String tenMoi, String dvtMoi, int tonKhoMoi, int nguongMoi, int maTaiKhoan, String lyDo) {
        String sql = "{CALL SP_SUA_NGUYEN_LIEU(?, ?, ?, ?, ?, ?, ?, ?)}"; // 8 tham số (7 IN, 1 OUT)
        
  
        try (Connection conn = getMyConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setInt(1, maNL);
            cs.setString(2, tenMoi);
            cs.setString(3, dvtMoi);
            cs.setInt(4, tonKhoMoi);
            cs.setInt(5, nguongMoi);
            cs.setInt(6, maTaiKhoan); // Truyền ID người dùng đang thao tác
            cs.setString(7, lyDo);
            
            // Khai báo tham số hứng kết quả (Tương ứng với OUT trong Procedure)
            cs.registerOutParameter(8, Types.NVARCHAR);
            
            cs.execute();
            
            String ketQua = cs.getString(8);
            
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
    
    public String getIngredientDetail(String tenNguyenLieu) {
        String detail = "";
        String query = "SELECT TO_CHAR(pnk.NgayNhap, 'DD/MM/YYYY') AS NgayNhapStr, ct.TongDinhLuong, nl.DonViTinh, TO_CHAR(nl.HanSuDung, 'DD/MM/YYYY') AS HSD " +
                       "FROM NGUYEN_LIEU nl " +
                       "JOIN CHI_TIET_PHIEU_NHAP ct ON nl.MaNguyenLieu = ct.MaNguyenLieu " +
                       "JOIN PHIEU_NHAP_KHO pnk ON pnk.MaPhieuNhap = ct.MaPhieuNhap " +
                       "WHERE UPPER(nl.TenNguyenLieu) = UPPER(?) " +
                       "ORDER BY pnk.NgayNhap DESC";
   
        try (Connection conn = getMyConnection();) { 
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, tenNguyenLieu); // Truyền tên NL vào
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String dateStr = rs.getString("NgayNhapStr");
                
                // Lắp ráp chuỗi hiển thị
                detail += "- Ngày " + dateStr 
                       + " nhập " + rs.getInt("TongDinhLuong") + " " + rs.getString("DonViTinh") 
                       + " (HSD: " + rs.getString("HSD") + ")\n";
            }
        } catch (Exception e){
            e.printStackTrace(); 
        }
        
        return detail.isEmpty() ? "Chưa có lịch sử chi tiết cho nguyên liệu này." : detail;
    }
    
    public ArrayList<String> getIngredientNames (){
        String sql = "SELECT DISTINCT TenNguyenLieu "
                + "FROM NGUYEN_LIEU ";
        ArrayList<String> ingredientNames = new ArrayList<>();
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);){
            while (rs.next()){
                ingredientNames.add(rs.getString("TenNguyenLieu"));
            }
        }catch (Exception e){
            e.printStackTrace(); 
        }
        return ingredientNames;
    }
    
    public int getIngredientIdByName (String name){
        int id = -1;
        String query = "SELECT MaNguyenLieu FROM NGUYEN_LIEU WHERE TenNguyenLieu = ?";
        try (Connection conn = getMyConnection();) { 
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, name); // Truyền tên NL vào
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                id = rs.getInt("MaNguyenLieu");
            }
        } catch (Exception e){
            e.printStackTrace(); 
        }
        return id;
    }
}
