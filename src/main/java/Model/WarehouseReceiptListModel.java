/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import DatabaseAccessObject.IngredientDAO;
import DatabaseAccessObject.WarehouseReceiptDAO;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author SONY
 */
public class WarehouseReceiptListModel {
    private List<WarehouseReceiptModel> warehouseReceiptList;

//    public List<PhieuNhapKho> getDanhSachPhieuNhapKho() {
//        this.danhSachPhieuNhap = phieuNhapKhoDAO.getAllsPhieuNhapKho();
//        return danhSachPhieuNhap;
//    }

    public List<WarehouseReceiptModel> getWarehouseReceiptList() {
        return warehouseReceiptList;
    }

   
    
    

 
    

    
}
