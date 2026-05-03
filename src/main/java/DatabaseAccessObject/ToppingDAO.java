/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.ToppingModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class ToppingDAO {
    // Trả về thẳng ArrayList vì không còn Group nữa
    public ArrayList<ToppingModel> getAllToppings(){
        ArrayList<ToppingModel> toppingList = new ArrayList<>();
        String sql = "SELECT MaTopping, TenTopping, GiaBan, MaNguyenLieuTru, DinhLuongHaoHut, Thue_GTGT "
                   + "FROM TOPPING ORDER BY MaTopping";
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){
            
            while(rs.next()){
                ToppingModel t = new ToppingModel(
                    rs.getInt("MaTopping"), 
                    rs.getString("TenTopping"), 
                    rs.getLong("GiaBan"), 
                    rs.getInt("MaNguyenLieuTru"),
                    rs.getDouble("DinhLuongHaoHut"),
                    rs.getDouble("Thue_GTGT")
                );
                toppingList.add(t);
            }
        }
        catch (Exception e){
            e.printStackTrace(); 
        }
        return toppingList;
    }
    
    public boolean insertTopping(String toppingName, long price, int ingredientID, double lossAmount, double vat) {
        String sql = "INSERT INTO TOPPING (TenTopping, GiaBan, MaNguyenLieuTru, DinhLuongHaoHut, Thue_GTGT) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, toppingName);
            ps.setLong(2, price);
            ps.setInt(3, ingredientID);
            ps.setDouble(4, lossAmount);
            ps.setDouble(5, vat);
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTopping(int toppingId) {
        String sql = "DELETE FROM TOPPING WHERE MaTopping = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, toppingId);
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            System.out.println("Lỗi xóa Topping: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTopping(int toppingId, String newName, long newPrice, int ingredientID, double lossAmount, double vat) {
        String sql = "UPDATE TOPPING SET TenTopping = ?, GiaBan = ?, MaNguyenLieuTru = ?, DinhLuongHaoHut = ?, Thue_GTGT = ? WHERE MaTopping = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, newName);
            ps.setLong(2, newPrice);
            ps.setInt(3, ingredientID);
            ps.setDouble(4, lossAmount);
            ps.setDouble(5, vat);
            ps.setInt(6, toppingId);
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Lấy Topping theo Sản Phẩm (Dùng khi hiển thị form bán hàng)
    public ArrayList<ToppingModel> getToppingsByProductID(int productID) {
        ArrayList<ToppingModel> list = new ArrayList<>();
        String sql = "SELECT T.MaTopping, T.TenTopping, T.GiaBan, T.MaNguyenLieuTru, T.DinhLuongHaoHut, T.Thue_GTGT "
                   + "FROM SAN_PHAM_TOPPING SPT "
                   + "JOIN TOPPING T ON SPT.MaTopping = T.MaTopping "
                   + "WHERE SPT.MaSanPham = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, productID);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    list.add(new ToppingModel(
                        rs.getInt("MaTopping"), rs.getString("TenTopping"), 
                        rs.getLong("GiaBan"), rs.getInt("MaNguyenLieuTru"),
                        rs.getDouble("DinhLuongHaoHut"), rs.getDouble("Thue_GTGT")
                    ));
                }
            }
        } catch (Exception e){
            e.printStackTrace(); 
        }
        return list;
    }
}
