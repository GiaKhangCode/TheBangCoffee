/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.FunctionModel;
import Model.RoleGroupModel;
import Model.RoleModel;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author SONY
 */
public class RoleDAO {
    public List<FunctionModel> getFunctionList() throws SQLException{
        ArrayList<FunctionModel> functionList = new ArrayList<>();
        String query = "SELECT * "
                + "FROM CHUC_NANG "
                + "ORDER BY MaChucNang";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            while(rs.next()){
                FunctionModel t = new FunctionModel(rs.getInt("MaChucNang"),
                                  rs.getString("TenChucNang"));
                functionList.add(t);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return functionList;
    }
    
    public List<RoleModel> getRoleList() throws SQLException{
        ArrayList<RoleModel> roleList = new ArrayList<>();
        String query = "SELECT * "
                + "FROM PHAM_VI_QUYEN "
                + "ORDER BY MaPhamVi";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            while(rs.next()){
                RoleModel t = new RoleModel(rs.getInt("MaPhamVi"),
                                    rs.getString("TenPhamVi"),
                                    rs.getInt("MaChucNang"),
                                    rs.getInt("Them"),
                                    rs.getInt("Sua"),
                                    rs.getInt("Xoa"),
                                    rs.getInt("Xem"),
                                    rs.getInt("XuatFile"));
                roleList.add(t);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return roleList;
    }
    
    public boolean createRole(RoleModel role) throws SQLException{
        String query = "INSERT INTO PHAM_VI_QUYEN (tenphamvi, machucnang, them, sua, xoa, xem, xuatfile) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            
            ps.setString(1, role.getRoleName());
            ps.setInt(2, role.getFunctionId());
            ps.setInt(3, role.getAdd());
            ps.setInt(4, role.getEdit());
            ps.setInt(5, role.getDelete());
            ps.setInt(6, role.getView());
            ps.setInt(7, role.getExportFile());
           
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Có lỗi hệ thống", "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return false;
    }
    
    public boolean updateRole(RoleModel role) throws SQLException{
        String query = "update PHAM_VI_QUYEN " +
                        "set TenPhamVi = ?, MaChucNang = ?, Them = ?, Sua = ?, Xoa = ?, Xem = ?, XuatFile = ? " +
                        "where MaPhamVi = ?";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            
            ps.setString(1, role.getRoleName());
            ps.setInt(2, role.getFunctionId());
            ps.setInt(3, role.getAdd());
            ps.setInt(4, role.getEdit());
            ps.setInt(5, role.getDelete());
            ps.setInt(6, role.getView());
            ps.setInt(7, role.getExportFile());
            ps.setInt(8, role.getRoleId());
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Có lỗi hệ thống", "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return false;
    }
    
    public boolean deleteRole(int roleId) throws SQLException{
        String query = "delete from PHAM_VI_QUYEN " +
                        "where MaPhamVi = ?";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            
            ps.setInt(1, roleId);
           
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Có lỗi hệ thống", "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return false;
    }
    
    public List<RoleGroupModel> getRoleGroupList(){
        ArrayList<RoleGroupModel> roleGroupList = new ArrayList<>();
        String query = "SELECT * "
                + "FROM NHOM_QUYEN "
                + "ORDER BY MaNhomQuyen";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            while(rs.next()){
                RoleGroupModel t = new RoleGroupModel(rs.getInt("MaNhomQuyen"),
                                    rs.getString("TenNhomQuyen"));
                roleGroupList.add(t);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return roleGroupList;
    }
    
    public boolean createRoleGroup(RoleGroupModel roleGroup) throws SQLException{
        String query = "INSERT INTO NHOM_QUYEN (TenNhomQuyen) " +
                       "VALUES (?)";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            ps.setString(1, roleGroup.getRoleGroupName());
           
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Có lỗi hệ thống", "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return false;
    }
    
    public boolean updateRoleGroup(RoleGroupModel roleGroup) throws SQLException{
        String query = "UPDATE NHOM_QUYEN SET TenNhomQuyen = ? WHERE MaNhomQuyen = ?";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            ps.setString(1, roleGroup.getRoleGroupName());
            ps.setInt(2, roleGroup.getRoleGroupId());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Có lỗi hệ thống", "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return false;
    }
    
    public boolean deleteRoleGroup(int roleGroupId) throws SQLException{
        String query = "DELETE NHOM_QUYEN WHERE MaNhomQuyen = ?";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            ps.setInt(1, roleGroupId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Có lỗi hệ thống", "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return false;
    }
    
    public boolean assignRoleToRoleGroup(int roleGroupId, int roleId){
        String query = "INSERT INTO CAU_HINH_NHOM_QUYEN (MaNhomQuyen, MaPhamVi) " +
                       "VALUES (?, ?)";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            
            ps.setInt(1, roleGroupId);
            ps.setInt(2, roleId);
           
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Có lỗi hệ thống", "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return false;
    }
    
    public boolean deleteRoleFromRoleGroup(int roleGroupId, int roleId){
        String query = "DELETE FROM CAU_HINH_NHOM_QUYEN WHERE MaNhomQuyen = ? AND MaPhamVi = ?";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            
            ps.setInt(1, roleGroupId);
            ps.setInt(2, roleId);
           
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Có lỗi hệ thống", "Lỗi Database", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return false;
    }
    
    public List<RoleModel> getConfiguredRolesByGroupId(int groupId) throws SQLException{
        ArrayList<RoleModel> roleList = new ArrayList<>();
        String query = "SELECT pv.MaPhamVi, pv.TenPhamVi "
                + "FROM PHAM_VI_QUYEN pv "
                + "JOIN CAU_HINH_NHOM_QUYEN ch on pv.MaPhamVi = ch.MaPhamVi "
                + "JOIN NHOM_QUYEN nq on ch.MaNhomQuyen = nq.MaNhomQuyen "
                + "WHERE nq.MaNhomQuyen = ? "
                + "ORDER BY pv.MaPhamVi";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, groupId);    
             
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                RoleModel roleModel = new RoleModel();
                roleModel.setRoleId(rs.getInt("MaPhamVi"));
                roleModel.setRoleName(rs.getString("TenPhamVi"));
                
                roleList.add(roleModel);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return roleList;
    }
    
    public List<RoleModel> getUnconfiguredRolesByGroupId(int groupId) throws SQLException{
        ArrayList<RoleModel> roleList = new ArrayList<>();
        String query = "select MaPhamVi, TenPhamVi " +
                        "from PHAM_VI_QUYEN " +
                        "minus " +
                        "select pvq.MaPhamVi, TenPhamVi " +
                        "from PHAM_VI_QUYEN pvq " +
                        "join cau_hinh_nhom_quyen ch on pvq.MaPhamVi = ch.MaPhamVi " +
                        "where MaNhomQuyen = ?";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, groupId);    
             
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                RoleModel roleModel = new RoleModel();
                roleModel.setRoleId(rs.getInt("MaPhamVi"));
                roleModel.setRoleName(rs.getString("TenPhamVi"));
                
                roleList.add(roleModel);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return roleList;
    }
    
    public int isPermissed(String operationName, int accountId, int functionId) throws SQLException {
        // 1. Chốt chặn an toàn (Chống SQL Injection do cộng chuỗi tên cột)
        if (!operationName.matches("^(Them|Sua|Xoa|Xem|XuatFile)$")) {
            System.err.println("Tên quyền không hợp lệ: " + operationName);
            return 0;
        }

        // 2. Cộng chuỗi tên cột vào thẳng câu SQL
        String query = "SELECT MAX(" + operationName + ") AS ISPERMISSED\n" +
                       "FROM (\n" +
                       "    SELECT PVQ." + operationName + "\n" +
                       "    FROM PHAN_QUYEN_TAI_KHOAN PQTK\n" +
                       "    JOIN PHAM_VI_QUYEN PVQ ON PVQ.MaPhamVi = PQTK.MaPhamVi\n" +
                       "    WHERE PQTK.MaTaiKhoan = ? AND PVQ.MaChucNang = ?\n" +
                       "    UNION\n" +
                       "    SELECT PVQ." + operationName + "\n" +
                       "    FROM PHAN_QUYEN_NHOM PQN\n" +
                       "    JOIN CAU_HINH_NHOM_QUYEN CH ON CH.MaNhomQuyen = PQN.MaNhomQuyen\n" +
                       "    JOIN PHAM_VI_QUYEN PVQ ON PVQ.MaPhamVi = CH.MaPhamVi\n" +
                       "    WHERE PQN.MaTaiKhoan = ? AND PVQ.MaChucNang = ?\n" +
                       ")";
        
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
         
            // Bây giờ chỉ còn 4 dấu ? cho Giá Trị (AccountID và FunctionID)
            ps.setInt(1, accountId);
            ps.setInt(2, functionId);
            ps.setInt(3, accountId);
            ps.setInt(4, functionId);
           
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ISPERMISSED");
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); 
        }
        
        return 0;
    }
    
}
