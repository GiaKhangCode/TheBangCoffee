/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import DatabaseAccessObject.WarehouseReceiptDAO;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Kiet
 */
public class WarehouseReceiptModel {
    private int receiptID;
    private int accountID;
    private LocalDate importingDate;
    private long total;
    //private List<WarehouseReceiptDetailModel> danhSach;
    //private WarehouseReceiptDAO phieuNhapKhoDAO;
    private String userName;
    
    public WarehouseReceiptModel(int accountID, List<WarehouseReceiptDetailModel> danhSach, LocalDate importingDate, long total){
        this.importingDate = importingDate;
        this.accountID = accountID;
        //this.danhSach = danhSach;
        this.total = total;
        //this.phieuNhapKhoDAO = new WarehouseReceiptDAO();
        this.userName = "";
    }
    
    public WarehouseReceiptModel(int receiptID, LocalDate importingDate, int accountID, long total, String userName){
        this.receiptID = receiptID;
        this.importingDate = importingDate;
        this.accountID = accountID;
        this.total = total;
        //this.phieuNhapKhoDAO = new WarehouseReceiptDAO();
        this.userName = userName;
    }
    

    public int getReceiptID() {
        return receiptID;
    }

    public int getAccountID() {
        return accountID;
    }

    public LocalDate getImportingDate() {
        return importingDate;
    }

    public String getUserName() {
        return userName;
    }
    
    

    public long getTotal() {
        return total;
    }
}
    
