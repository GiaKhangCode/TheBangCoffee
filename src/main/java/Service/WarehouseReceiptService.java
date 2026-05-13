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
        int rowAffected = warehouseReceiptDAO.insertWarehouseReceipt(accountID, listDetails);
        
        if (rowAffected > 0){
            return true;
        }
        return false;
    }
    
    public long calculateTotal(DefaultTableModel model){
        long total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                // [ĐÃ SỬA] Đổi index thành 6 vì cột Thành tiền ở Model Table mới là cột số 6
                String strTotal = model.getValueAt(i, 6).toString().trim(); 
                if (strTotal.isEmpty()) 
                    continue;
                total += Long.parseLong(strTotal);
            } catch (Exception ex) { 
                continue; 
            }
        }
        return total;
    }
    
    public boolean deleteWarehouseReceipt(int maPhieuNhap, int maTaiKhoan, String lyDo) {
        return warehouseReceiptDAO.deleteReceiptWithLog(maPhieuNhap, maTaiKhoan, lyDo);
    }
    
    public String getDetailReceipt(int receiptID){
        return warehouseReceiptDAO.getReceiptDetail(receiptID);
    }
    
    public List<Object[]> getReceiptDetailList(int receiptID){
        return warehouseReceiptDAO.getReceiptDetailList(receiptID);
    }
}