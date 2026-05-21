package DatabaseAccessObject;

import static ConnectDatabase.ConnectionUtils.getMyConnection;
import Model.VariantModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class VariantDAO {
    public boolean deleteVariant(int variantId) {
        String sqlDelRecipe = "DELETE FROM CONG_THUC WHERE MaBienThe = ?";
        String sqlDelVar = "DELETE FROM BIEN_THE WHERE MaBienThe = ?";
        
        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection()) {
            conn.setAutoCommit(false); // Bật Transaction an toàn
            
            try (PreparedStatement psRec = conn.prepareStatement(sqlDelRecipe);
                 PreparedStatement psVar = conn.prepareStatement(sqlDelVar)) {
                
                // 1. Xóa công thức trước
                psRec.setInt(1, variantId);
                psRec.executeUpdate();
                
                // 2. Xóa Size sau
                psVar.setInt(1, variantId);
                int row = psVar.executeUpdate();
                
                conn.commit(); // Chốt giao dịch
                return row > 0;
                
            } catch (Exception e) {
                conn.rollback(); // Có lỗi là quay xe ngay
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<VariantModel> getVariantsByProductId(int productID){
        ArrayList<VariantModel> list = new ArrayList<>();
        // [SỬA] Đổi GiaBan thành 3 cột giá mới
        String sql = "SELECT B.MaBienThe, B.TenSize, B.GiaTaiQuan, B.GiaMangVe, B.GiaNgayLe, "
                   + "(CASE WHEN EXISTS (SELECT 1 FROM CONG_THUC C WHERE C.MaBienThe = B.MaBienThe) THEN 1 ELSE 0 END) AS HasRecipe "
                   + "FROM BIEN_THE B "
                   + "WHERE B.MaSanPham = ?";
        try (Connection conn = getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, productID);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    boolean hasRecipe = rs.getInt("HasRecipe") == 1;
                    list.add(new VariantModel(
                        rs.getInt("MaBienThe"), 
                        productID, 
                        rs.getString("TenSize"), 
                        rs.getLong("GiaTaiQuan"),
                        rs.getLong("GiaMangVe"),
                        rs.getLong("GiaNgayLe"),
                        hasRecipe
                    ));
                }
            }
        } catch (Exception e){
            e.printStackTrace(); 
        }
        return list;
    }
}