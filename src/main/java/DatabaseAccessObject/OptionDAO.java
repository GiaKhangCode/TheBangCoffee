/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.OptionModel;
import Model.OptionGroupModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author FAKK
 */
public class OptionDAO {
    public HashMap<String, ArrayList<OptionModel>> getAllOption(){
        HashMap<String, ArrayList<OptionModel>> optionHashMap = new HashMap<>();
        String sql = "SELECT MaTuyChon, TenTuyChon, GiaPhuThu, TrangThai, TenNhomTuyChon "
                    + "FROM NHOM_TUY_CHON N "
                    + "JOIN TUY_CHON T "
                    + "ON T.MaNhomTuyChon = N.MaNhomTuyChon ";
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);){
            
            while(rs.next()){
                OptionModel optionModel = new OptionModel(rs.getInt("MaTuyChon"), rs.getString("TenTuyChon"), rs.getDouble("GiaPhuThu"), rs.getString("TrangThai"));
                optionHashMap.computeIfAbsent(rs.getString("TenNhomTuyChon"), k -> new ArrayList<>()).add(optionModel);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return optionHashMap;
    }
    public boolean insertOption(int groupID, String optionName, double extraPrice, String optionStatus) {
        String sql = "INSERT INTO TUY_CHON (MaNhomTuyChon, TenTuyChon, GiaPhuThu, TrangThai) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, groupID);
            ps.setString(2, optionName);
            ps.setDouble(3, extraPrice);
            ps.setString(4, optionStatus);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean insertOptionGroup(String groupName) {
        String sql = "INSERT INTO NHOM_TUY_CHON (TenNhomTuyChon) VALUES (?)";
        
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, groupName);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public ArrayList<OptionGroupModel> getAllOptionGroups() {
        String sql = "SELECT MaNhomTuyChon, TenNhomTuyChon FROM NHOM_TUY_CHON";
        ArrayList<OptionGroupModel> list = new ArrayList<>();
        
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while(rs.next()){
                list.add(new OptionGroupModel(
                    rs.getInt("MaNhomTuyChon"),
                    rs.getString("TenNhomTuyChon")
                ));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }
    public int getGroupIdByName(String groupName) {
        String sql = "SELECT MaNhomTuyChon FROM NHOM_TUY_CHON WHERE TenNhomTuyChon = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("MaNhomTuyChon");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Trả về -1 nếu không tìm thấy
    }
    public boolean deleteOptionGroup(int groupID) {
        String sql = "DELETE FROM NHOM_TUY_CHON WHERE MaNhomTuyChon = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, groupID);
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            System.out.println("Lỗi xóa Nhóm Tùy Chọn: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteOptionDetail(int optionId) {
        String sql = "DELETE FROM TUY_CHON WHERE MaTuyChon = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, optionId);
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            System.out.println("Lỗi xóa Tùy Chọn: " + e.getMessage());
            return false;
        }
    }
    public boolean updateOptionGroup(int groupId, String newName) {
        String sql = "UPDATE NHOM_TUY_CHON SET TenNhomTuyChon = ? WHERE MaNhomTuyChon = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, newName);
            ps.setInt(2, groupId);
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOptionDetail(int optionId, int newGroupId, String newName, double newPrice, String status) {
        String sql = "UPDATE TUY_CHON SET MaNhomTuyChon = ?, TenTuyChon = ?, GiaPhuThu = ?, TrangThai = ? WHERE MaTuyChon = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, newGroupId);
            ps.setString(2, newName);
            ps.setDouble(3, newPrice);
            ps.setString(4, status);
            ps.setInt(5, optionId);
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
