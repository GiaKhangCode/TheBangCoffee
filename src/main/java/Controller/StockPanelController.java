package Controller;

/**
 * @author Kiet
 */
import Model.IngredientModel;
import Model.IngredientTypeModel;
import Model.SessionManager;
import Model.WarehouseReceiptDetailModel;
import Model.WarehouseReceiptModel;
import Service.IngredientService;
import Service.IngredientTypeService;
import Service.WarehouseReceiptService;
import View.MainFrame;
import View.StockPanel;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockPanelController {
    private List<IngredientModel> ingredientListModel;
    private List<WarehouseReceiptModel> warehouseReceiptListModel;
    private StockPanel stockPanelView; 
    private MainFrame mainFrame;
    private IngredientService ingredientService;
    private WarehouseReceiptService warehouseReceiptService;
    private IngredientTypeService ingredientTypeService;
    private List<IngredientTypeModel> ingredientTypeList;
    
    public StockPanelController(MainFrame sharedMainFrame) throws SQLException {
        this.mainFrame = sharedMainFrame;
        
        ingredientService = new IngredientService();
        warehouseReceiptService = new WarehouseReceiptService();
        ingredientTypeService = new IngredientTypeService();
        
        this.stockPanelView = mainFrame.getStockPanel();
        
        initStockListeners();
        
        loadCategoriesToView(); // [MỚI] Gọi hàm load danh mục
        loadIngredientToView();
        loadWarehouseReceiptToView();
    }
    
    private void initStockListeners() {
        
        // 1. LẮNG NGHE NÚT THÊM NGUYÊN LIỆU GỐC
        this.stockPanelView.addAddNewIngredientListener(e -> {
            Object[] data = stockPanelView.showAddIngredientDialog(ingredientTypeList);
            if (data != null) {
                IngredientTypeModel type = (IngredientTypeModel) data[0];
                String name = (String) data[1];
                String unit = (String) data[2];
                int threshold = (int) data[3];

                // Gọi Service để tạo dữ liệu gốc (Tồn kho = 0)
                boolean success = ingredientService.addIngredientMasterData(type.getTypeName(), name, unit, threshold);
                
                if (success) {
                    JOptionPane.showMessageDialog(mainFrame, "Thêm nguyên liệu gốc thành công!");
                    try {
                        loadIngredientToView(); // Tải lại bảng & ComboBox
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Thêm thất bại. Tên nguyên liệu có thể đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 2. AUTO-FILL ĐƠN VỊ TÍNH KHI CHỌN NGUYÊN LIỆU TRONG COMBOBOX
        this.stockPanelView.getIngredientComboBox().addActionListener(e -> {
            Object selectedItem = stockPanelView.getIngredientComboBox().getSelectedItem();
            if (selectedItem != null && ingredientListModel != null) {
                String selectedName = selectedItem.toString();
                boolean found = false;
                for (IngredientModel ing : ingredientListModel) {
                    if (ing.getIngredientName().equals(selectedName)) {
                        stockPanelView.setUnitText(ing.getUnit());
                        found = true;
                        break;
                    }
                }
                if (!found) stockPanelView.setUnitText(""); // Xóa trắng nếu không khớp
            } else {
                stockPanelView.setUnitText(""); // Xóa trắng nếu null
            }
        });
        
        // 3. LẬP PHIẾU NHẬP
        this.stockPanelView.addSubmitReceiptListener(e -> {
            try {
                implementCreateReceipt(); 
                loadIngredientToView();
            } catch (Exception ex) {
                System.getLogger(StockPanelController.class.getName()).log(System.Logger.Level.ERROR, "Lỗi lập phiếu", ex);
                JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi khi lưu phiếu nhập:\n" + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        this.stockPanelView.addHistoryButtonListener(e -> {
            try {
                loadWarehouseReceiptToView(); 
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(null, "Không thể tải dữ liệu lịch sử!\n" + ex.toString(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        stockPanelView.getReceiptItemModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.INSERT ||
                    e.getType() == TableModelEvent.DELETE ||
                   (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 6)) {
                    long total = warehouseReceiptService.calculateTotal(stockPanelView.getReceiptItemModel());
                    stockPanelView.setTotalAmountLabel(total);
                }
            }
        });
        
        // =========================================================
        // SỰ KIỆN TRÊN BẢNG TỒN KHO
        // =========================================================
        this.stockPanelView.setInventoryActionListener(new View.StockPanel.ActionButtonListener() {
            @Override
            public void onDetail(int row) {
                int maNL = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 0).toString());
                String tenNL = stockPanelView.getInventoryTable().getValueAt(row, 1).toString();

                List<Object[]> batches = ingredientService.getIngredientBatches(maNL);

                stockPanelView.showBatchDetailDialog(tenNL, batches, (maLo, qty, reason) -> {
                    String result = ingredientService.disposeBatch(maLo, qty, reason);
                    if (result.equals("Thành công")) {
                        JOptionPane.showMessageDialog(null, "Xuất hủy lô thành công!");
                        try {
                            loadIngredientToView(); 
                        } catch (SQLException ex) { ex.printStackTrace(); }
                    } else {
                        JOptionPane.showMessageDialog(null, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
            
            @Override
            public void onDelete(int row) {
                int maNL = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 0).toString());
                String tenNL = stockPanelView.getInventoryTable().getValueAt(row, 1).toString();

                JTextField txtLyDo = new JTextField();
                Object[] message = {
                    "Xác nhận xóa nguyên liệu: " + tenNL,
                    "Lý do xóa (Bắt buộc):", txtLyDo
                };

                int option = JOptionPane.showConfirmDialog(null, message, "Xác nhận xóa", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (option == JOptionPane.OK_OPTION) {
                    String lyDo = txtLyDo.getText().trim();
                    if (lyDo.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Bạn phải nhập lý do để thực hiện xóa!");
                        return;
                    }
                    int currentUserID = SessionManager.getAccount().getAccountID();
                    boolean success = ingredientService.deleteIngredient(maNL, currentUserID, lyDo);
                    if (success) {
                        stockPanelView.getInventoryModel().removeRow(row);
                        JOptionPane.showMessageDialog(null, "Đã xóa nguyên liệu và lưu log thành công.");
                        try { loadIngredientToView(); } catch (SQLException ex) {} 
                    }
                } 
            }
        
            @Override
            public void onEdit(int row) {
                int maNL = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 0).toString());
                String tenCu = stockPanelView.getInventoryTable().getValueAt(row, 1).toString();
                String dvtCu = stockPanelView.getInventoryTable().getValueAt(row, 2).toString(); 
                int tonCu = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 3).toString().replace(",", ""));
                int nguongCu = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 4).toString().replace(",", ""));

                Object[] duLieuMoi = stockPanelView.showEditDialog(tenCu, dvtCu, tonCu, nguongCu);

                if (duLieuMoi != null) {
                    String tenMoi = (String) duLieuMoi[0];
                    String dvtMoi = (String) duLieuMoi[1];
                    int tonMoi = (int) duLieuMoi[2];
                    int nguongMoi = (int) duLieuMoi[3];
                    String lyDo = (String) duLieuMoi[4]; 

                    String nhaCungCapMoi = "Đã cập nhật"; 
                    double thueMoi = 8.0; 

                    int currentUserID = SessionManager.getAccount().getAccountID(); 
                    boolean isSuccess = ingredientService.updateIngredient(maNL, tenMoi, dvtMoi, tonMoi, nguongMoi, nhaCungCapMoi, thueMoi, currentUserID, lyDo);

                    if (isSuccess) {
                        try { loadIngredientToView(); } catch (SQLException ex) {}
                        JOptionPane.showMessageDialog(null, "Cập nhật và lưu log thành công!");
                    }
                }
            }
        });
        
        // =========================================================
        // SỰ KIỆN TRÊN BẢNG LỊCH SỬ NHẬP
        // =========================================================
        this.stockPanelView.setHistoryActionListener(new View.StockPanel.ActionButtonListener() {
            @Override
            public void onDetail(int row) {
                int receiptID = Integer.parseInt(stockPanelView.getHistoryTable().getValueAt(row, 0).toString().replace("#", "").trim());
                List<Object[]> detailData = warehouseReceiptService.getReceiptDetailList(receiptID);

                if (detailData != null && !detailData.isEmpty()) {
                    stockPanelView.showReceiptDetailTableDialog(receiptID, detailData, (idToPrint) -> {
                    // [GỌI HÀM IN Ở ĐÂY]
                    Service.InvoiceService invoiceService = new Service.InvoiceService();
                    invoiceService.printWarehouseReceipt(idToPrint);
                    });
                } else {
                    JOptionPane.showMessageDialog(null, "Không tìm thấy dữ liệu chi tiết cho phiếu này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                }
            }
            
            @Override
            public void onEdit(int row) {
                JOptionPane.showMessageDialog(null, "Chức năng Xem/Sửa chi tiết phiếu nhập đang được cập nhật!");
            }

            @Override
            public void onDelete(int row) {
                int maPhieuNhap = Integer.parseInt(stockPanelView.getHistoryTable().getValueAt(row, 0).toString());
                String ngayNhap = stockPanelView.getHistoryTable().getValueAt(row, 1).toString();
                
                try {
                    java.time.LocalDate parsedDate = java.time.LocalDate.parse(ngayNhap);
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    ngayNhap = parsedDate.format(formatter);
                } catch (Exception ex) {
                    System.out.println("Không thể parse ngày: " + ngayNhap);
                }

                JTextField txtLyDo = new JTextField();
                Object[] message = {
                    "CẢNH BÁO: Bạn sắp xóa Phiếu nhập #" + maPhieuNhap + " ngày " + ngayNhap,
                    "Số lượng tồn kho của các lô thuộc phiếu này sẽ bị trừ đi tương ứng.",
                    "Lý do hủy/xóa phiếu nhập (Bắt buộc):", txtLyDo
                };

                int option = JOptionPane.showConfirmDialog(null, message, "Xác nhận xóa phiếu nhập", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                if (option == JOptionPane.OK_OPTION) {
                    String lyDo = txtLyDo.getText().trim();
                    if (lyDo.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Bạn phải nhập lý do để hệ thống ghi nhận log!");
                        return;
                    }

                    int currentUserID = SessionManager.getAccount().getAccountID();
                    boolean success = warehouseReceiptService.deleteWarehouseReceipt(maPhieuNhap, currentUserID, lyDo);

                    if (success) {
                        javax.swing.table.DefaultTableModel historyModel = (javax.swing.table.DefaultTableModel) stockPanelView.getHistoryTable().getModel();
                        historyModel.removeRow(row);
                        
                        try {
                            loadIngredientToView(); 
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        JOptionPane.showMessageDialog(null, "Đã xóa phiếu nhập, lưu log và hoàn trả tồn kho lô thành công!");
                    }
                }
            }
        });
        
        // =========================================================
        // [MỚI] SỰ KIỆN QUẢN LÝ LOẠI NGUYÊN LIỆU (CATEGORY)
        // =========================================================
        
        // Lắng nghe nút Thêm Loại NL
        this.stockPanelView.addCategoryButtonListener(e -> {
            String newCategory = stockPanelView.showAddCategoryDialog();
            if (newCategory != null && !newCategory.trim().isEmpty()) {
                newCategory = newCategory.trim();
                boolean success = false;
                try {
                    success = ingredientTypeService.addIngredientType(newCategory);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (success) {
                    loadCategoriesToView(); // Cập nhật lại Bảng và ComboBox
                    JOptionPane.showMessageDialog(null, "Đã thêm danh mục: " + newCategory);
                }
            }
        });
        
        // Lắng nghe nút Sửa Loại NL trên Bảng
        this.stockPanelView.setCategoryActionListener(new View.StockPanel.ActionButtonListener() {
            @Override
            public void onEdit(int row) {
                int id = Integer.parseInt(stockPanelView.getCategoryTable().getValueAt(row, 0).toString());
                String currentName = stockPanelView.getCategoryTable().getValueAt(row, 1).toString();
                
                String newName = stockPanelView.showEditCategoryDialog(currentName);
                if (newName != null && !newName.trim().isEmpty() && !newName.equals(currentName)) {
                    boolean success = ingredientTypeService.updateIngredientType(id, newName.trim());
                    if (success) {
                        JOptionPane.showMessageDialog(mainFrame, "Cập nhật loại nguyên liệu thành công!");
                        loadCategoriesToView(); // Tải lại danh sách
                    }
                }
            }
            @Override public void onDelete(int row) {}
            @Override public void onDetail(int row) {}
        });
    }
        
    private void implementCreateReceipt() throws Exception {
        javax.swing.table.DefaultTableModel itemModel = stockPanelView.getReceiptItemModel();
        int rowCount = itemModel.getRowCount();
        
        if (rowCount == 0) {
            JOptionPane.showMessageDialog(stockPanelView, "Không có chi tiết nào trong phiếu nhập!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        List<WarehouseReceiptDetailModel> listDetails = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            String name = String.valueOf(itemModel.getValueAt(i, 0)).trim();
            String unit = String.valueOf(itemModel.getValueAt(i, 1)).trim();
            double capacity = Double.parseDouble(String.valueOf(itemModel.getValueAt(i, 2)).trim()); 
            int quantity = Integer.parseInt(String.valueOf(itemModel.getValueAt(i, 3)).trim()); 
            long preTaxPrice = Long.parseLong(String.valueOf(itemModel.getValueAt(i, 4)).trim());
            double taxPercent = Double.parseDouble(String.valueOf(itemModel.getValueAt(i, 5)).trim());
            long totalPrice = Long.parseLong(String.valueOf(itemModel.getValueAt(i, 6)).trim());
            String provider = String.valueOf(itemModel.getValueAt(i, 7)).trim();
            LocalDate importingDate = LocalDate.parse(String.valueOf(itemModel.getValueAt(i, 8)).trim()); 
            LocalDate expiryDate = LocalDate.parse(String.valueOf(itemModel.getValueAt(i, 9)).trim()); 

            long taxAmount = totalPrice - preTaxPrice;

            WarehouseReceiptDetailModel detail = new WarehouseReceiptDetailModel(
                    1, "", name, unit, capacity, quantity, 
                    preTaxPrice, taxPercent, taxAmount, totalPrice, 
                    0, provider, importingDate, expiryDate
            );
            
            listDetails.add(detail);
        }

        warehouseReceiptService.createReceipt(SessionManager.getAccount().getAccountID(), listDetails);
        JOptionPane.showMessageDialog(stockPanelView, "Lập phiếu nhập kho thành công!");
        stockPanelView.clearReceiptForm();
    }
    
    // [MỚI] Hàm tải dữ liệu Category vào View
    private void loadCategoriesToView() {
        try {
            ingredientTypeList = ingredientTypeService.getIngredientTypes();
            // Đổ vào Bảng Loại NL
            stockPanelView.displayCategoryData(ingredientTypeList);
            // Đổ vào ComboBox Phiếu nhập
            stockPanelView.loadIngredientTypesToComboBox(ingredientTypeList);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void loadIngredientToView() throws SQLException {
        ingredientListModel = ingredientService.getIngredientList();
        stockPanelView.displayIngredientData(ingredientListModel);
        stockPanelView.loadIngredientsToComboBox(ingredientListModel);
    }
    
    public void loadWarehouseReceiptToView() throws SQLException {
        warehouseReceiptListModel = warehouseReceiptService.getWarehouseReceiptList();
        stockPanelView.displayWarehouseReceiptData(warehouseReceiptListModel);
    }
}