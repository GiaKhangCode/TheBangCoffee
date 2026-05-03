package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.ProductListModel;
import Model.ProductModel;
import Model.VariantModel;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public class ProductDAO {
    public ProductListModel getAllProduct() {
        ProductListModel productList = new ProductListModel();
        
        // [SỬA] Cập nhật truy vấn 3 cột giá
        String sqlProduct = "SELECT SP.MaSanPham, SP.TenSanPham, SP.TenAnhSanPham, SP.KieuDuLieuAnh, " +
                            "SP.TrangThai AS TrangThaiSP, LSP.TenLoaiSanPham, LSP.TrangThai AS TrangThaiLoai, " +
                            "SP.GiaTaiQuan, SP.GiaMangVe, SP.GiaNgayLe, SP.DuLieuAnh, SP.MoTa, SP.Thue_GTGT " +
                            "FROM SAN_PHAM SP " +
                            "JOIN LOAI_SAN_PHAM LSP on SP.MaLoaiSanPham = LSP.MaLoaiSanPham " +
                            "ORDER BY SP.MaSanPham DESC";

        try (Connection conn = getMyConnection();
             Statement stmtProd = conn.createStatement();
             ResultSet rsProd = stmtProd.executeQuery(sqlProduct)) { 
            
            while(rsProd.next()){
                ImageIcon imageIcon = null;
                try {
                    InputStream is = rsProd.getBinaryStream("DuLieuAnh");
                    if (is != null) {
                        byte[] bytes = is.readAllBytes();
                        ImageIcon original = new ImageIcon(bytes);
                        Image img = original.getImage().getScaledInstance(180, 150, Image.SCALE_SMOOTH);
                        imageIcon = new ImageIcon(img);
                    }
                } catch (Exception e) { e.printStackTrace(); }
                
                ProductModel prod = new ProductModel(
                        rsProd.getInt("MaSanPham"), rsProd.getString("TenSanPham"),
                        rsProd.getString("TenAnhSanPham"), rsProd.getString("KieuDuLieuAnh"),
                        rsProd.getString("TrangThaiSP"), rsProd.getString("TenLoaiSanPham"),
                        rsProd.getString("TrangThaiLoai"), 
                        rsProd.getLong("GiaTaiQuan"), rsProd.getLong("GiaMangVe"), rsProd.getLong("GiaNgayLe"),
                        rsProd.getDouble("Thue_GTGT"), imageIcon, rsProd.getString("MoTa")
                );
                
                // [SỬA] Cập nhật truy vấn Size lấy 3 cột giá
                try (PreparedStatement psVariant = conn.prepareStatement("SELECT MaBienThe, TenSize, GiaTaiQuan, GiaMangVe, GiaNgayLe FROM BIEN_THE WHERE MaSanPham = ?")) {
                    psVariant.setInt(1, prod.getProductID());
                    try (ResultSet rsVar = psVariant.executeQuery()) {
                        while (rsVar.next()) {
                            prod.addVariant(new VariantModel(
                                rsVar.getInt("MaBienThe"), prod.getProductID(),
                                rsVar.getString("TenSize"), 
                                rsVar.getLong("GiaTaiQuan"), rsVar.getLong("GiaMangVe"), rsVar.getLong("GiaNgayLe")
                            ));
                        }
                    }
                }
                
                productList.addProductList(prod);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return productList;
    }
    
    // [LƯU Ý]: Bạn phải sửa lại SP_INSERT_SAN_PHAM trong Database để nhận 12 tham số!
    public void insertProduct(String categoryName, String productName, long dineInPrice, long takeawayPrice, long holidayPrice, double vat, File imageFile, 
                              String status, String description, List<VariantModel> listVariants, List<Integer> listToppingIds) {
        
        String sqlProduct = "{call SP_INSERT_SAN_PHAM(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}"; // 12 tham số (bao gồm cả OUT_MaSanPham)
        // [SỬA] Câu lệnh Insert Size
        String sqlVariant = "INSERT INTO BIEN_THE (MaSanPham, TenSize, GiaTaiQuan, GiaMangVe, GiaNgayLe) VALUES (?, ?, ?, ?, ?)";
        String sqlTopping = "INSERT INTO SAN_PHAM_TOPPING (MaSanPham, MaTopping) VALUES (?, ?)";

        try (Connection conn = getMyConnection()) {
            conn.setAutoCommit(false);
            int newProductId = -1;
            
            // 1. Thêm Sản Phẩm
            try (CallableStatement cs = conn.prepareCall(sqlProduct)){
                cs.setString(1, productName);
                cs.setLong(2, dineInPrice);
                cs.setLong(3, takeawayPrice);
                cs.setLong(4, holidayPrice);
                cs.setDouble(5, vat);
                cs.setString(6, status);
                cs.setString(7, categoryName);
                cs.setString(8, description);

                if (imageFile != null) {
                    FileInputStream fis = new FileInputStream(imageFile);
                    String fileName = imageFile.getName();
                    String mimeType = "image/" + fileName.substring(fileName.lastIndexOf(".") + 1);

                    cs.setString(9, fileName);
                    cs.setString(10, mimeType);
                    cs.setBinaryStream(11, fis, imageFile.length());
                } else {
                    cs.setString(9, null);
                    cs.setString(10, null);
                    cs.setNull(11, java.sql.Types.BLOB);
                }

                cs.registerOutParameter(12, Types.NUMERIC);
                cs.execute();
                newProductId = cs.getInt(12);
            }
            
            if (newProductId > 0) {
                // 2. Thêm Size
                if (listVariants != null && !listVariants.isEmpty()) {
                    try (PreparedStatement psVar = conn.prepareStatement(sqlVariant)) {
                        for (VariantModel var : listVariants) {
                            psVar.setInt(1, newProductId);
                            psVar.setString(2, var.getSizeName());
                            psVar.setLong(3, var.getDineInPrice());
                            psVar.setLong(4, var.getTakeawayPrice());
                            psVar.setLong(5, var.getHolidayPrice());
                            psVar.addBatch();
                        }
                        psVar.executeBatch(); 
                    } catch (SQLException ex) {
                        System.out.println("LỖI KHI INSERT BIẾN THỂ: " + ex.getMessage());
                        throw ex; 
                    }
                }
                
                // 3. Link Topping
                if (listToppingIds != null && !listToppingIds.isEmpty()){
                    try (PreparedStatement psTop = conn.prepareStatement(sqlTopping)) {
                        for (Integer toppingId : listToppingIds) {
                            psTop.setInt(1, newProductId);
                            psTop.setInt(2, toppingId);
                            psTop.addBatch();
                        }
                        psTop.executeBatch();
                    }
                }
            } else {
                 System.out.println("LỖI: Mã Sản Phẩm trả về là " + newProductId);
            }
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // [LƯU Ý]: Bạn phải sửa lại SP_UPDATE_SAN_PHAM trong Database để nhận đủ 11 tham số!
    public void updateProduct(int productId, String categoryName, String productName, long dineInPrice, long takeawayPrice, long holidayPrice, double vat, File imageFile, 
                              String status, String description, List<VariantModel> listVariants, List<Integer> listToppingIds) {
        
        String sqlProduct = "{call SP_UPDATE_SAN_PHAM(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}"; // 11 tham số
        String sqlDeleteToppings = "DELETE FROM SAN_PHAM_TOPPING WHERE MaSanPham = ?";
        String sqlInsertTopping = "INSERT INTO SAN_PHAM_TOPPING (MaSanPham, MaTopping) VALUES (?, ?)";
        
        try (Connection conn = getMyConnection()) {
            conn.setAutoCommit(false);

            // 1. Cập nhật bảng SAN_PHAM qua Stored Procedure
            try (CallableStatement cs = conn.prepareCall(sqlProduct)) {
                cs.setInt(1, productId);
                cs.setString(2, productName);
                cs.setLong(3, dineInPrice);
                cs.setLong(4, takeawayPrice);
                cs.setLong(5, holidayPrice);
                cs.setDouble(6, vat);

                if (imageFile != null) {
                    FileInputStream fis = new FileInputStream(imageFile);
                    String fileName = imageFile.getName();
                    String mimeType = "image/" + fileName.substring(fileName.lastIndexOf(".") + 1);

                    cs.setString(7, fileName);
                    cs.setString(8, mimeType);
                    cs.setBinaryStream(9, fis, imageFile.length());
                } else {
                    cs.setString(7, null);
                    cs.setString(8, null);
                    cs.setNull(9, java.sql.Types.BLOB);
                }

                cs.setString(10, status);
                cs.setString(11, categoryName);
                cs.execute();
            }

            // ============================================
            // 2. ĐỒNG BỘ BIẾN THỂ (SIZE) VÀ CÔNG THỨC
            // ============================================
            if (listVariants != null) {
                // A. Lọc ra danh sách các ID Size mà người dùng còn giữ lại trên giao diện
                List<Integer> keepVariantIds = new ArrayList<>();
                for (VariantModel var : listVariants) {
                    if (var.getVariantID() > 0) keepVariantIds.add(var.getVariantID());
                }

                // B. Quét Database, nếu Size cũ nào không nằm trong danh sách giữ lại -> XÓA
                String sqlGetOldVars = "SELECT MaBienThe FROM BIEN_THE WHERE MaSanPham = ?";
                String sqlDelRecipe = "DELETE FROM CONG_THUC WHERE MaBienThe = ?";
                String sqlDelVar = "DELETE FROM BIEN_THE WHERE MaBienThe = ?";

                try (PreparedStatement psGet = conn.prepareStatement(sqlGetOldVars)) {
                    psGet.setInt(1, productId);
                    try (ResultSet rsOld = psGet.executeQuery();
                         PreparedStatement psDelRec = conn.prepareStatement(sqlDelRecipe);
                         PreparedStatement psDelV = conn.prepareStatement(sqlDelVar)) {
                        
                        while (rsOld.next()) {
                            int oldId = rsOld.getInt(1);
                            if (!keepVariantIds.contains(oldId)) {
                                psDelRec.setInt(1, oldId); psDelRec.executeUpdate();
                                psDelV.setInt(1, oldId); psDelV.executeUpdate();
                            }
                        }
                    }
                }

                // C. Cập nhật Size cũ đã sửa và Thêm Size mới
                // [SỬA] Đổi GiaBan thành 3 cột giá
                String sqlUpdateVar = "UPDATE BIEN_THE SET TenSize = ?, GiaTaiQuan = ?, GiaMangVe = ?, GiaNgayLe = ? WHERE MaBienThe = ?";
                String sqlInsertVar = "INSERT INTO BIEN_THE (MaSanPham, TenSize, GiaTaiQuan, GiaMangVe, GiaNgayLe) VALUES (?, ?, ?, ?, ?)";
                
                try (PreparedStatement psUpd = conn.prepareStatement(sqlUpdateVar);
                     PreparedStatement psIns = conn.prepareStatement(sqlInsertVar)) {
                     
                    for (VariantModel var : listVariants) {
                        if (var.getVariantID() > 0) {
                            psUpd.setString(1, var.getSizeName());
                            psUpd.setLong(2, var.getDineInPrice());
                            psUpd.setLong(3, var.getTakeawayPrice());
                            psUpd.setLong(4, var.getHolidayPrice());
                            psUpd.setInt(5, var.getVariantID());
                            psUpd.addBatch();
                        } else {
                            psIns.setInt(1, productId);
                            psIns.setString(2, var.getSizeName());
                            psIns.setLong(3, var.getDineInPrice());
                            psIns.setLong(4, var.getTakeawayPrice());
                            psIns.setLong(5, var.getHolidayPrice());
                            psIns.addBatch();
                        }
                    }
                    psUpd.executeBatch();
                    psIns.executeBatch();
                }
            }

            // 3. Cập nhật Topping (Xóa cũ - Thêm mới)
            try (PreparedStatement psDelete = conn.prepareStatement(sqlDeleteToppings)) {
                psDelete.setInt(1, productId);
                psDelete.executeUpdate();
            }
            if (listToppingIds != null && !listToppingIds.isEmpty()) {
                try (PreparedStatement psTop = conn.prepareStatement(sqlInsertTopping)) {
                    for (Integer toppingId : listToppingIds) {
                        psTop.setInt(1, productId);
                        psTop.setInt(2, toppingId);
                        psTop.addBatch();
                    }
                    psTop.executeBatch();
                }
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void deleteProduct(int id) throws SQLException, ClassNotFoundException {
        // Thứ tự xóa rất quan trọng để không dính khóa ngoại (Foreign Key)
        String sqlDeleteRecipe = "DELETE FROM CONG_THUC WHERE MaBienThe IN (SELECT MaBienThe FROM BIEN_THE WHERE MaSanPham = ?)";
        String sqlDeleteVariant = "DELETE FROM BIEN_THE WHERE MaSanPham = ?";
        String sqlDeleteToppingLinks = "DELETE FROM SAN_PHAM_TOPPING WHERE MaSanPham = ?";
        String sqlDeleteProduct = "DELETE FROM SAN_PHAM WHERE MaSanPham = ?";
        
        try (Connection conn = getMyConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // 1. Xóa Công Thức của các Size thuộc Sản phẩm này
                try (PreparedStatement ps = conn.prepareStatement(sqlDeleteRecipe)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                
                // 2. Xóa các Size (Biến thể)
                try (PreparedStatement ps = conn.prepareStatement(sqlDeleteVariant)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                
                // 3. Xóa liên kết Topping
                try (PreparedStatement ps = conn.prepareStatement(sqlDeleteToppingLinks)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                
                // 4. Cuối cùng mới xóa Sản Phẩm
                try (PreparedStatement ps = conn.prepareStatement(sqlDeleteProduct)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback(); // Nếu có lỗi thì rollback lại toàn bộ
                throw e; 
            }
        }
    }
}