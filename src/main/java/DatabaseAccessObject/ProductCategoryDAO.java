/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author FAKK
 */
public class ProductCategoryDAO {
    public ArrayList<String> getProductCategory(){
        String sql = "SELECT * " +
                     "FROM LOAI_SAN_PHAM ";
        ArrayList<String> productCategoryList = new ArrayList<>();
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);){ //Chỉ phục vụ đọc/lấy dữ liệu
            
            while(rs.next()){
                productCategoryList.add(rs.getString("TenLoaiSanPham"));
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return productCategoryList;
    }
}
