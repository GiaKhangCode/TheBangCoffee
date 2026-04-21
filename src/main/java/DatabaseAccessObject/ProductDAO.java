/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.ProductListModel;
import Model.ProductModel;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 *
 * @author FAKK
 */
public class ProductDAO {
    public ProductListModel getAllProduct (){
        String sql = "SELECT SP.MaSanPham, " +
                     "        SP.TenSanPham, " +
                     "        SP.TenAnhSanPham, " +
                     "        SP.KieuDuLieuAnh, " +
                     "        SP.TrangThai AS TrangThaiSP, " +
                     "        LSP.TenLoaiSanPham, " +
                     "        LSP.TrangThai AS TrangThaiLoai, " +
                     "        SP.GiaCoBan, " + 
                     "        SP.DuLieuAnh " +
                     "FROM SAN_PHAM SP " +
                     "JOIN LOAI_SAN_PHAM LSP on SP.MaLoaiSanPham = LSP.MaLoaiSanPham";
        ProductListModel productList = new ProductListModel();
        try (Connection conn = getMyConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);)
        { //Chỉ phục vụ đọc/lấy dữ liệu
            while(rs.next()){
                ImageIcon imageIcon = null;
                try {
                    InputStream is = rs.getBinaryStream("DuLieuAnh");
                    if (is != null) {
                        byte[] bytes = is.readAllBytes();
                        ImageIcon original = new ImageIcon(bytes);

                        // scale ảnh cho UI đẹp hơn
                        Image img = original.getImage().getScaledInstance(180, 150, Image.SCALE_SMOOTH);
                        imageIcon = new ImageIcon(img);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ProductModel t = new ProductModel(rs.getInt("MaSanPham"),
                                  rs.getString("TenSanPham"),
                                  rs.getString("TenAnhSanPham"),
                                  rs.getString("KieuDuLieuAnh"),
                                  rs.getString("TrangThaiSP"),
                                  rs.getString("TenLoaiSanPham"),
                                  rs.getString("TrangThaiLoai"),
                                  rs.getDouble("GiaCoBan"),
                                  imageIcon );
                productList.addProductList(t);
            }
        }
        catch (Exception e){
            e.printStackTrace(); //In ra dấu vết bắt lỗi
        }
        
        return productList;
    }
    
    public void insertProduct(String categoryName, String productName, double basicPrice, File imageFile, String status, HashMap<String, List<String>> selectedOptions) {
        String sqlProduct = "{call SP_INSERT_SAN_PHAM(?, ?, ?, ?, ?, ?, ?, ?)}";
        String sqlOption = "{call SP_INSERT_CT_TUY_CHON(?, ?, ?)}";

        try (Connection conn = getMyConnection()) {
            conn.setAutoCommit(false);
            int newProductId = -1;
            try (CallableStatement cs = conn.prepareCall(sqlProduct)){
                cs.setString(1, productName);
                cs.setDouble(2, basicPrice);

                if (imageFile != null) {
                    FileInputStream fis = new FileInputStream(imageFile);

                    String fileName = imageFile.getName();
                    String mimeType = "image/" + fileName.substring(fileName.lastIndexOf(".") + 1);

                    cs.setString(3, fileName);
                    cs.setString(4, mimeType);
                    cs.setBinaryStream(5, fis, imageFile.length());
                } else {
                    cs.setString(3, null);
                    cs.setString(4, null);
                    cs.setNull(5, java.sql.Types.BLOB);
                }

                cs.setString(6, status);
                cs.setString(7, categoryName);
                cs.registerOutParameter(8, Types.NUMERIC);

                cs.execute();
                newProductId = cs.getInt(8);
            }
            if (newProductId > 0 && selectedOptions != null && !selectedOptions.isEmpty()){
                try (CallableStatement csOpt = conn.prepareCall(sqlOption)) {
                    for (Map.Entry<String, List<String>> entry : selectedOptions.entrySet()) {
                        String groupName = entry.getKey();

                        for (String optionName : entry.getValue()) {
                            csOpt.setInt(1, newProductId);
                            csOpt.setString(2, optionName);
                            csOpt.setString(3, groupName);
                            csOpt.addBatch();
                        }
                    }
                    csOpt.executeBatch();
                }
            }
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
