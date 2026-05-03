package Controller;

/**
 * @author Kiet
 */
import Common.ValidationUtil;
import Model.WarehouseReceiptDetailModel;
import Model.IngredientModel;
import Model.IngredientTypeModel;
import Model.SessionManager;
import Model.WarehouseReceiptModel;
import Service.IngredientService;
import Service.WarehouseReceiptService;
import View.MainFrame;
import View.StockPanel;
import View.StockPanel.ActionButtonListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import Service.IngredientTypeService;

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
        
        ingredientTypeList = ingredientTypeService.getIngredientTypes();
        stockPanelView.loadIngredientTypesToComboBox(ingredientTypeList);
        
        loadIngredientToView();
        loadWarehouseReceiptToView();
    }
    
    private void initStockListeners() {
        
        this.stockPanelView.addSubmitReceiptListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    implementCreateReceipt(); 
                    loadIngredientToView();

                } catch (Exception ex) {
                    System.getLogger(StockPanelController.class.getName())
                          .log(System.Logger.Level.ERROR, "Lỗi nghiêm trọng khi lập phiếu nhập kho", ex);

                    javax.swing.JOptionPane.showMessageDialog(
                        null, 
                        "Đã xảy ra lỗi khi lưu phiếu nhập:\n" + ex.getMessage(),
                        "Lỗi hệ thống",
                        javax.swing.JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        
        this.stockPanelView.addHistoryButtonListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    loadWarehouseReceiptToView(); 
                } catch (Exception ex) { 
                    ex.printStackTrace(); 
                    JOptionPane.showMessageDialog(
                        null, 
                        "Không thể tải dữ liệu lịch sử!\nNguyên nhân: " + ex.toString(), 
                        "Lỗi Hệ Thống", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        
        stockPanelView.getReceiptItemModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                // Cập nhật lại Index cột Thành tiền (cột số 7)
                if (e.getType() == TableModelEvent.INSERT ||
                    e.getType() == TableModelEvent.DELETE ||
                   (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 7)) {
                    long total = warehouseReceiptService.calculateTotal(stockPanelView.getReceiptItemModel());
                    stockPanelView.setTotalAmountLabel(total);
                }
            }
        });
        
        this.stockPanelView.setInventoryActionListener(new View.StockPanel.ActionButtonListener() {
            
            @Override
            public void onDetail(int row) {
                String tenNL = stockPanelView.getInventoryTable().getValueAt(row, 1).toString();
                String detail = ingredientService.getIngredientDetail(tenNL);
                JOptionPane.showMessageDialog(null, detail, "Lịch sử nhập lô: " + tenNL, JOptionPane.INFORMATION_MESSAGE);
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
                    }
                } 
            }
        
            @Override
            public void onEdit(int row) {
                int maNL = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 0).toString());
                String tenCu = stockPanelView.getInventoryTable().getValueAt(row, 1).toString();
                String dvtCu = stockPanelView.getInventoryTable().getValueAt(row, 2).toString();
                int tonCu = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 3).toString());
                int nguongCu = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 4).toString());

                Object[] duLieuMoi = stockPanelView.showEditDialog(tenCu, dvtCu, tonCu, nguongCu);

                if (duLieuMoi != null) {
                    String tenMoi = (String) duLieuMoi[0];
                    String dvtMoi = (String) duLieuMoi[1];
                    int tonMoi = (int) duLieuMoi[2];
                    int nguongMoi = (int) duLieuMoi[3];
                    String lyDo = (String) duLieuMoi[4]; 

                    // Fix cứng Nhà Cung Cấp và Thuế cho luồng Edit nhanh (Bạn có thể thêm 2 field này vào showEditDialog sau)
                    String nhaCungCapMoi = "Đã cập nhật"; 
                    double thueMoi = 8.0; 

                    int currentUserID = SessionManager.getAccount().getAccountID(); 

                    boolean isSuccess = ingredientService.updateIngredient(maNL, tenMoi, dvtMoi, tonMoi, nguongMoi, nhaCungCapMoi, thueMoi, currentUserID, lyDo);

                    if (isSuccess) {
                        stockPanelView.getInventoryTable().setValueAt(tenMoi, row, 1);
                        stockPanelView.getInventoryTable().setValueAt(dvtMoi, row, 2);
                        stockPanelView.getInventoryTable().setValueAt(tonMoi, row, 3);
                        stockPanelView.getInventoryTable().setValueAt(nguongMoi, row, 4);

                        String trangThai = (tonMoi <= nguongMoi) ? "Sắp hết" : "Còn hàng";
                        if (tonMoi == 0) trangThai = "Hết hàng";
                        stockPanelView.getInventoryTable().setValueAt(trangThai, row, 5);

                        JOptionPane.showMessageDialog(null, "Cập nhật và lưu log thành công!");
                    }
                }
            }
        });
        
        this.stockPanelView.setHistoryActionListener(new View.StockPanel.ActionButtonListener() {
            @Override
            public void onDetail(int row){
                javax.swing.table.DefaultTableModel historyModel = stockPanelView.getHistoryModel();
                int receiptID = Integer.parseInt(String.valueOf(historyModel.getValueAt(row, 0)).trim());
                String detailReceipt = warehouseReceiptService.getDetailReceipt(receiptID);
                JOptionPane.showMessageDialog(stockPanelView, detailReceipt, "Chi tiết lô hàng phiếu nhập", JOptionPane.INFORMATION_MESSAGE);
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
        
        // --- LẮNG NGHE NÚT THÊM LOẠI NGUYÊN LIỆU MỚI ---
        this.stockPanelView.addCategoryButtonListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String newCategory = stockPanelView.showAddCategoryDialog();

                if (newCategory != null && !newCategory.trim().isEmpty()) {
                    // Cắt khoảng trắng dư thừa
                    newCategory = newCategory.trim();

                    // Gọi Service để lưu
                    boolean success = false;
                    try {
                        success = ingredientTypeService.addIngredientType(newCategory);
                    } catch (SQLException ex) {
                        System.getLogger(StockPanelController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    } catch (ClassNotFoundException ex) {
                        System.getLogger(StockPanelController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }

                    if (success) {
                        try {
                            // Reload lại danh sách vào ComboBox
                            ingredientTypeList = ingredientTypeService.getIngredientTypes();
                            stockPanelView.loadIngredientTypesToComboBox(ingredientTypeList);

                            // Tự động chọn (Select) cái loại nguyên liệu vừa mới thêm
                            for (int i = 0; i < stockPanelView.getCategoryComboBox().getItemCount(); i++) {
                                if (stockPanelView.getCategoryComboBox().getItemAt(i).getTypeName().equals(newCategory)) {
                                    stockPanelView.getCategoryComboBox().setSelectedIndex(i);
                                    break;
                                }
                            }

                            JOptionPane.showMessageDialog(null, "Đã thêm danh mục: " + newCategory);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }
        
    private void implementCreateReceipt() throws Exception {
        javax.swing.table.DefaultTableModel itemModel = stockPanelView.getReceiptItemModel();

        int rowCount = itemModel.getRowCount();
        List<WarehouseReceiptDetailModel> listDetails = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            // Đọc dữ liệu theo Index mới gồm 13 cột
            String category = String.valueOf(itemModel.getValueAt(i, 0)).trim();
            String name = String.valueOf(itemModel.getValueAt(i, 1)).trim();
            String unit = String.valueOf(itemModel.getValueAt(i, 2)).trim();
            double capacity = Double.parseDouble(String.valueOf(itemModel.getValueAt(i, 3)).trim()); 
            int quantity = Integer.parseInt(String.valueOf(itemModel.getValueAt(i, 4)).trim()); 
            
            long preTaxPrice = Long.parseLong(String.valueOf(itemModel.getValueAt(i, 5)).trim());
            double taxPercent = Double.parseDouble(String.valueOf(itemModel.getValueAt(i, 6)).trim());
            long totalPrice = Long.parseLong(String.valueOf(itemModel.getValueAt(i, 7)).trim());
            int threshold = Integer.parseInt(String.valueOf(itemModel.getValueAt(i, 8)).trim());
            
            String provider = String.valueOf(itemModel.getValueAt(i, 9)).trim();
            LocalDate importingDate = LocalDate.parse(String.valueOf(itemModel.getValueAt(i, 10)).trim()); 
            LocalDate expiryDate = LocalDate.parse(String.valueOf(itemModel.getValueAt(i, 11)).trim()); 

            // Tính tiền thuế
            long taxAmount = totalPrice - preTaxPrice;

            // Map ID động từ tên danh mục
            int typeID = 1; 
            for (IngredientTypeModel type : ingredientTypeList) {
                if (type.getTypeName().equals(category)) { 
                    typeID = type.getTypeID(); 
                    break; 
                }
            }

            // [ĐÃ SỬA] Truyền typeID vào tham số đầu tiên
            WarehouseReceiptDetailModel detail = new WarehouseReceiptDetailModel(
                    typeID, category, name, unit, capacity, quantity, 
                    preTaxPrice, taxPercent, taxAmount, totalPrice, 
                    threshold, provider, importingDate, expiryDate
            );
            
            listDetails.add(detail);
        }

        warehouseReceiptService.createReceipt(SessionManager.getAccount().getAccountID(), listDetails);
        JOptionPane.showMessageDialog(stockPanelView, "Lập phiếu nhập kho thành công!");
        stockPanelView.clearReceiptForm();
    }
    
    public void loadIngredientToView() throws SQLException {
        ingredientListModel = ingredientService.getIngredientList();
        stockPanelView.displayIngredientData(ingredientListModel);
    }
    
    public void loadWarehouseReceiptToView() throws SQLException {
        warehouseReceiptListModel = warehouseReceiptService.getWarehouseReceiptList();
        stockPanelView.displayWarehouseReceiptData(warehouseReceiptListModel);
    }
}