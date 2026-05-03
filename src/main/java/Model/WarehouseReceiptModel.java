/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.time.LocalDate;
import java.util.List;

public class WarehouseReceiptModel {
    private int receiptID;
    private int accountID;
    private LocalDate importingDate;
    private long total;
    private String userName;
    
    // [MỚI] Thêm Ghi chú
    private String note;
    
    // Constructor cho lúc TẠO PHIẾU
    public WarehouseReceiptModel(int accountID, LocalDate importingDate, long total, String note){
        this.accountID = accountID;
        this.importingDate = importingDate;
        this.total = total;
        this.note = note;
        this.userName = "";
    }
    
    // Constructor cho lúc LẤY LỊCH SỬ TỪ DB LÊN
    public WarehouseReceiptModel(int receiptID, LocalDate importingDate, int accountID, long total, String userName, String note){
        this.receiptID = receiptID;
        this.importingDate = importingDate;
        this.accountID = accountID;
        this.total = total;
        this.userName = userName;
        this.note = note;
    }

    public int getReceiptID() { return receiptID; }
    public int getAccountID() { return accountID; }
    public LocalDate getImportingDate() { return importingDate; }
    public long getTotal() { return total; }
    public String getUserName() { return userName; }
    public String getNote() { return note; }
}