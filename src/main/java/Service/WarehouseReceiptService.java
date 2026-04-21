/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DatabaseAccessObject.WarehouseReceiptDAO;
import Model.WarehouseReceiptDetailModel;
import Model.WarehouseReceiptModel;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Kiet
 */
public class WarehouseReceiptService {
    private WarehouseReceiptDAO warehouseReceiptDAO;
    
    public WarehouseReceiptService(){
        this.warehouseReceiptDAO = new WarehouseReceiptDAO();
    }
    
    public List<WarehouseReceiptModel> getWarehouseReceiptList(){
        return warehouseReceiptDAO.getWarehouseReceipt();
    }
    
    
    public boolean createReceipt(int accountID, List<WarehouseReceiptDetailModel> listDetails) throws Exception{
        int rowAffected = warehouseReceiptDAO.insertPhieuNhap(accountID, listDetails);
        
        if (rowAffected > 0){
            return true;
        }
        return false;
    }
    
    public long calculateTotal(DefaultTableModel model){
        long total = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                // Lấy giá trị ở ô Số lượng và Đơn giá
                String strQuantity = model.getValueAt(i, 2).toString().trim();
                String strPrice = model.getValueAt(i, 3).toString().trim();
                
                // Nếu rỗng thì bỏ qua
                if (strQuantity.isEmpty() || strPrice.isEmpty()) continue;
                
                // Nhân lại và cộng dồn
                int quantity = Integer.parseInt(strQuantity);
                long price = Long.parseLong(strPrice);
                total += (quantity * price);
                
            } catch (Exception ex) {
                // Lỗi ép kiểu (gõ chữ cái vào ô số) thì bỏ qua
                continue; 
            }
        }
        return total;
    }
    
    public boolean deleteWarehouseReceipt(int warehouseReceiptID, int accountID, String reason) {
        return warehouseReceiptDAO.deleteReceiptWithLog(warehouseReceiptID, accountID, reason);
    }
    
    public String getDetailReceipt(int receiptID){
        return warehouseReceiptDAO.getReceiptDetail(receiptID);
    }
  
}
