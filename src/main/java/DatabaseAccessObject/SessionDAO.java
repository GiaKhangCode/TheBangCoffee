/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import ConnectDatabase.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.UUID;

/**
 *
 * @author FAKK
 */
public class SessionDAO {
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
    
    public boolean isTokenValid(String token) {
        String sql = "SELECT 1 FROM TOKEN_TAI_KHOAN " +
                     "WHERE GiaTriToken = ? " +
                     "AND DaThuHoi = 0 " +
                     "AND ThoiDiemHetHan >= SYSDATE ";

        try(Connection con = ConnectionUtils.getMyConnection()){
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, token);

            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    
}
