/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import ConnectDatabase.ConnectionUtils;
import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.AccountModel;
import Model.RoleGroupModel;
import Model.RoleModel;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.swing.JOptionPane;
/**
 *
 * @author FAKK
 */
public class AccountDAO {
    public AccountModel loginProcess(String name, String password){
        try(Connection con = ConnectionUtils.getMyConnection()){
            String sql = "SELECT * "
                    + "FROM TAI_KHOAN TK "
                    + "JOIN NGUOI_DUNG ND ON TK.MaNguoiDung = ND.MaNguoiDung "
                    + "WHERE TenDangNhap = ? "
                    + "AND MatKhauDaMaHoa = ? ";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
//            ResultSetMetaData meta = rs.getMetaData();
//            for (int i = 1; i <= meta.getColumnCount(); i++) {
//                System.out.println(meta.getColumnName(i));
//            }
            if(rs.next()){
                AccountModel account = new AccountModel();
                account.setAccountID(rs.getInt("MaTaiKhoan"));
                account.setUsername(rs.getString("TenDangNhap"));
                account.setPassword(rs.getString("MatKhauDaMaHoa"));
                account.setFullName(rs.getString("HoTen"));
                account.setPhoneNumber(rs.getString("SoDienThoai"));
                account.setEmail(rs.getString("Email"));
                
                return account;
            }
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }
    
    public String signUpProcess(String fullName, String username, String password, String phoneNumber, String email){
        String result;
        String sql = "{call SP_DANG_KY_TAI_KHOAN(?, ?, ?, ?, ?, ?)}";
        try(Connection con = ConnectionUtils.getMyConnection()){
            CallableStatement cs = con.prepareCall(sql);
            
            cs.setString(1, fullName);
            cs.setString(2, email);
            cs.setString(3, phoneNumber);
            cs.setString(4, username);
            cs.setString(5, password);
            cs.registerOutParameter(6, Types.NVARCHAR);
            
            cs.execute();
            
            result = cs.getString(6);
        }
        catch (Exception ex) {
            System.out.println(ex);
            result = "Lỗi hệ thống: " + ex.getMessage();
        }
        return result;
    }
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE TAI_KHOAN " +
                     "SET MatKhauDaMaHoa = ? " +
                     "WHERE MaNguoiDung = (" +
                     "   SELECT nd.MaNguoiDung " +
                     "   FROM NGUOI_DUNG nd " +
                     "   WHERE nd.Email = ?)";
        try (Connection con = ConnectionUtils.getMyConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, newPassword);
            ps.setString(2, email);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public String createToken(int accountountID){
        String token = UUID.randomUUID().toString().replace("-", "");
        String sql = "INSERT INTO TOKEN_TAI_KHOAN (MaTaiKhoan, GiaTriToken, ThoiDiemHetHan) VALUES (?, ?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, accountountID);
            ps.setString(2, token);
            ps.setObject(3, LocalDate.now().plusDays(60));
            
            ps.executeUpdate();
            return token;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean revokeToken(String token){
        String sql = "UPDATE TOKEN_TAI_KHOAN "
                + "SET DaThuHoi = 1 "
                + "WHERE GiaTriToken = ? ";
        try(Connection con = ConnectionUtils.getMyConnection()){
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, token);
            
            return ps.executeUpdate() > 0;
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean revokeAllTokensByEmail(String email){
        String sql = "UPDATE TOKEN_TAI_KHOAN "
                + "SET DaThuHoi = 1 "
                + "WHERE MaTaiKhoan = ( "
                + "SELECT TK.MaTaiKhoan "
                + "FROM TAI_KHOAN TK "
                + "JOIN NGUOI_DUNG ND ON TK.MaNguoiDung = ND.MaNguoiDung "
                + "WHERE ND.Email = ? )";
        
        try(Connection con = ConnectionUtils.getMyConnection()){
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);

            return ps.executeUpdate() > 0;
        } catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean isEmailExists(String email){
        String sql = "SELECT 1 FROM NGUOI_DUNG WHERE Email = ?";

        try(Connection con = ConnectionUtils.getMyConnection()){
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    public List<AccountModel> getAccountList(){
        ArrayList<AccountModel> accountList = new ArrayList<>();
        String query = "SELECT tk.MaTaiKhoan, nd.HoTen "
                + "FROM TAI_KHOAN tk "
                + "JOIN NGUOI_DUNG nd on tk.MaNguoiDung = nd.MaNguoiDung "
                + "ORDER BY MaTaiKhoan";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            while(rs.next()){
                AccountModel account = new AccountModel();
                account.setAccountID(rs.getInt("MaTaiKhoan"));
                account.setUsername(rs.getString("HoTen"));
                
                accountList.add(account);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return accountList;
    }
    
    public boolean assignRoleGroupToAccount(int accountId, int roleGroupId){
        String query = "INSERT INTO PHAN_QUYEN_NHOM (MaTaiKhoan, MaNhomQuyen) " +
                       "VALUES (?, ?)";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            
            ps.setInt(1, accountId);
            ps.setInt(2, roleGroupId);
           
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
    
    public boolean deleteRoleGroupFromAccount(int accountId, int roleGroupId){
        String query = "DELETE FROM PHAN_QUYEN_NHOM WHERE MaTaiKhoan = ? AND MaNhomQuyen = ?";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            
            ps.setInt(1, accountId);
            ps.setInt(2, roleGroupId);
           
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
    
    public boolean assignRoleToAccount(int accountId, int roleId){
        String query = "INSERT INTO PHAN_QUYEN_TAI_KHOAN (MaTaiKhoan, MaPhamVi) " +
                       "VALUES (?, ?)";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            
            ps.setInt(1, accountId);
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
    
    public boolean deleteRoleFromAccount(int accountId, int roleId){
        String query = "DELETE FROM PHAN_QUYEN_TAI_KHOAN WHERE MaTaiKhoan = ? AND MaPhamVi = ?";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();
            PreparedStatement ps = conn.prepareStatement(query);){
         
            
            ps.setInt(1, accountId);
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
    
    public List<RoleGroupModel> getAssignedRoleGroupsByAccountId(int accountId) throws SQLException{
        ArrayList<RoleGroupModel> roleGroupList = new ArrayList<>();
        String query = "SELECT nq.MaNhomQuyen, nq.TenNhomQuyen "
                + "FROM PHAN_QUYEN_NHOM pqn "
                + "JOIN NHOM_QUYEN nq on pqn.MaNhomQuyen = nq.MaNhomQuyen "
                + "WHERE pqn.MaTaiKhoan = ? "
                + "ORDER BY nq.MaNhomQuyen";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, accountId);    
             
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                RoleGroupModel roleGroupModel = new RoleGroupModel();
                roleGroupModel.setRoleGroupId(rs.getInt("MaNhomQuyen"));
                roleGroupModel.setRoleGroupName(rs.getString("TenNhomQuyen"));
                
                roleGroupList.add(roleGroupModel);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return roleGroupList;
    }
    
    public List<RoleModel> getAssignedRolesByAccountId(int accountId) throws SQLException{
        ArrayList<RoleModel> roleList = new ArrayList<>();
        String query = "SELECT PQTK.MAPHAMVI, PVQ.TenPhamVi " +
                        "FROM TAI_KHOAN TK " +
                        "JOIN PHAN_QUYEN_TAI_KHOAN PQTK ON TK.MATAIKHOAN = PQTK.MATAIKHOAN " +
                        "JOIN PHAM_VI_QUYEN PVQ ON PVQ.MaPhamVi = PQTK.MaPhamVi " +
                        "WHERE TK.MaTaiKhoan = 1 " +
                        "UNION " +
                        "SELECT PVQ.MaPhamVi, PVQ.TenPhamVi " +
                        "FROM TAI_KHOAN TK " +
                        "JOIN PHAN_QUYEN_NHOM PQN ON TK.MaTaiKhoan = PQN.MaTaiKhoan " +
                        "JOIN CAU_HINH_NHOM_QUYEN CH ON CH.MaNhomQuyen = PQN.MaNhomQuyen " +
                        "JOIN PHAM_VI_QUYEN PVQ ON PVQ.MaPhamVi = CH.MaPhamVi " +
                        "WHERE TK.MATAIKHOAN = ?";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, accountId);    
             
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
    
    // còn phải sửa
    public List<RoleModel> getUnassignedRolesByAccountId(int accountId) throws SQLException{
        ArrayList<RoleModel> roleList = new ArrayList<>();
        String query = "SELECT MAPHAMVI, TENPHAMVI " +
                        "FROM PHAM_VI_QUYEN " +
                        "MINUS " +
                        "(SELECT PQTK.MAPHAMVI, PVQ.TenPhamVi " +
                        "FROM TAI_KHOAN TK " +
                        "JOIN PHAN_QUYEN_TAI_KHOAN PQTK ON TK.MATAIKHOAN = PQTK.MATAIKHOAN " +
                        "JOIN PHAM_VI_QUYEN PVQ ON PVQ.MaPhamVi = PQTK.MaPhamVi " +
                        "WHERE TK.MaTaiKhoan = 1 " +
                        "UNION " +
                        "SELECT PVQ.MaPhamVi, PVQ.TenPhamVi " +
                        "FROM TAI_KHOAN TK " +
                        "JOIN PHAN_QUYEN_NHOM PQN ON TK.MaTaiKhoan = PQN.MaTaiKhoan " +
                        "JOIN CAU_HINH_NHOM_QUYEN CH ON CH.MaNhomQuyen = PQN.MaNhomQuyen " +
                        "JOIN PHAM_VI_QUYEN PVQ ON PVQ.MaPhamVi = CH.MaPhamVi " +
                        "WHERE TK.MATAIKHOAN = ?)";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, accountId);    
             
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
    
    public List<RoleGroupModel> getUnassignedRoleGroupsByAccountId(int accountId) throws SQLException{
        ArrayList<RoleGroupModel> roleGroupList = new ArrayList<>();
        String query = "select MaNhomQuyen, TenNhomQuyen " +
                        "from NHOM_QUYEN " +
                        "minus " +
                        "select nq.MaNhomQuyen, TenNhomQuyen " +
                        "from NHOM_QUYEN nq " +
                        "join PHAN_QUYEN_NHOM pqn on nq.MaNhomQuyen = pqn.MaNhomQuyen " +
                        "where MaTaiKhoan = ?";
        
        //try(): Tự động đóng tài nguyên
        try (Connection conn = getMyConnection();){ //Chỉ phục vụ đọc/lấy dữ liệu
        
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, accountId);    
             
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                RoleGroupModel roleGroupModel = new RoleGroupModel();
                roleGroupModel.setRoleGroupId(rs.getInt("MaNhomQuyen"));
                roleGroupModel.setRoleGroupName(rs.getString("TenNhomQuyen"));
                
                roleGroupList.add(roleGroupModel);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return roleGroupList;
    }
}
