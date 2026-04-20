/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.OptionModel;
import java.sql.Connection;
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
}
