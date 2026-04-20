/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import ConnectDatabase.ConnectionUtils;
import Model.AccountModel;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
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
}
