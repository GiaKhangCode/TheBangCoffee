/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author Kiet
 */
import Model.SessionManager;
import Common.ValidationUtil;
import Model.WarehouseReceiptDetailModel;
import Model.IngredientModel;
import Model.WarehouseReceiptModel;
import Service.IngredientService;
import Service.WarehouseReceiptService;
import View.MainFrame;
import View.StockPanel;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;


public class StockPanelController {
    private List<IngredientModel> ingredientListModel;
    private List<WarehouseReceiptModel> warehouseReceiptListModel;
    private StockPanel stockPanelView; // đại diện cho View
    private MainFrame mainFrame;
    private IngredientService ingredientService;
    private WarehouseReceiptService warehouseReceiptService;

    public StockPanelController(MainFrame sharedMainFrame) throws SQLException {
        this.mainFrame = sharedMainFrame;
        
        ingredientService = new IngredientService();
        warehouseReceiptService = new WarehouseReceiptService();
        
        this.stockPanelView = mainFrame.getStockPanel();

        initStockListeners();
        loadIngredientToView();
        loadWarehouseReceiptToView();
    }
    
    private void initStockListeners() {
        
        this.stockPanelView.addSubmitReceiptListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    // Thực thi logic chính
                    implementCreateReceipt(); 
                    loadIngredientToView();

                } catch (Exception ex) {
                    // 1. Ghi log đầy đủ, rõ ràng để Dev đọc
                    System.getLogger(StockPanelController.class.getName())
                          .log(System.Logger.Level.ERROR, "Lỗi nghiêm trọng khi lập phiếu nhập kho", ex);

                    // 2. Hiện thông báo cho User biết (BẮT BUỘC ĐỐI VỚI APP GIAO DIỆN)
                    javax.swing.JOptionPane.showMessageDialog(
                        null, // Hoặc truyền this.stockPanelView vào đây để hộp thoại canh giữa màn hình
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
                    loadWarehouseReceiptToView(); // Gọi hàm kéo data từ DB
                } catch (Exception ex) { // <-- ĐÃ SỬA: Dùng Exception để tóm gọn mọi loại lỗi

                    // 1. In chi tiết lỗi chữ đỏ ra tab Console/Output trong IDE để bạn rà soát
                    ex.printStackTrace(); 

                    // 2. Hiện popup cho biết lỗi gì, thay vì để màn hình đứng im
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
                // Nếu là sự kiện THÊM DÒNG mới, hoặc CẬP NHẬT cột 2 (Số lượng), cột 3 (Đơn giá)
                if (e.getType() == TableModelEvent.INSERT || 
                   (e.getType() == TableModelEvent.UPDATE && (e.getColumn() == 2 || e.getColumn() == 3))) {
                    long total = warehouseReceiptService.calculateTotal(stockPanelView.getReceiptItemModel());
                    stockPanelView.setTotalAmountLabel(total);
                }
            }
        });
        
        // Bên trong hàm initStockListeners(), phần setInventoryActionListener:
    
        this.stockPanelView.setInventoryActionListener(new View.StockPanel.ActionButtonListener() {
            public void onDelete(int row) {
                // 1. Lấy thông tin cơ bản để hiển thị xác nhận
                int maNL = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 0).toString());
                String tenNL = stockPanelView.getInventoryTable().getValueAt(row, 1).toString();

                // 2. Tạo Popup yêu cầu nhập lý do xóa
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

                    // 3. Gọi Service/DAO xử lý
                    int currentUserID = SessionManager.getAccount().getAccountID();
                    boolean success = ingredientService.deleteIngredient(maNL, currentUserID, lyDo);

                    if (success) {
                        stockPanelView.getInventoryModel().removeRow(row);
                        JOptionPane.showMessageDialog(null, "Đã xóa nguyên liệu và lưu log thành công.");
                    }
                } 
        }
        
        public void onEdit(int row) {
            // 1. Lấy dữ liệu cũ từ dòng đang chọn
            int maNL = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 0).toString());
            String tenCu = stockPanelView.getInventoryTable().getValueAt(row, 1).toString();
            String dvtCu = stockPanelView.getInventoryTable().getValueAt(row, 2).toString();
            int tonCu = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 3).toString());
            int nguongCu = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 4).toString());

            // 2. Gọi View hiện Form nhập liệu mới (kèm ô Lý do)
            Object[] duLieuMoi = stockPanelView.showEditDialog(tenCu, dvtCu, tonCu, nguongCu);

            // 3. Xử lý khi người dùng bấm OK
            if (duLieuMoi != null) {
                String tenMoi = (String) duLieuMoi[0];
                String dvtMoi = (String) duLieuMoi[1];
                int tonMoi = (int) duLieuMoi[2];
                int nguongMoi = (int) duLieuMoi[3];
                String lyDo = (String) duLieuMoi[4]; // Nhận thêm lý do
                
                // Lấy ID tài khoản người đang đăng nhập
                int currentUserID = SessionManager.getAccount().getAccountID(); 

                // 4. Gọi Service để đẩy xuống Database
                boolean isSuccess = ingredientService.updateIngredient(maNL, tenMoi, dvtMoi, tonMoi, nguongMoi, currentUserID, lyDo);

                if (isSuccess) {
                    // 5. Cập nhật UI ngay lập tức
                    stockPanelView.getInventoryTable().setValueAt(tenMoi, row, 1);
                    stockPanelView.getInventoryTable().setValueAt(dvtMoi, row, 2);
                    stockPanelView.getInventoryTable().setValueAt(tonMoi, row, 3);
                    stockPanelView.getInventoryTable().setValueAt(nguongMoi, row, 4);
                    
                    // Logic tính lại trạng thái "Còn hàng / Hết hàng" trên bảng
                    String trangThai = (tonMoi < nguongMoi) ? "Hết hàng" : "Còn hàng";
                    stockPanelView.getInventoryTable().setValueAt(trangThai, row, 5);

                    JOptionPane.showMessageDialog(null, "Cập nhật và lưu log thành công!");
                }
            }
        }
    });
        // Lắng nghe sự kiện Sửa/Xóa trên bảng Lịch sử nhập hàng
        this.stockPanelView.setHistoryActionListener(new View.StockPanel.HistoryActionButtonListener() {
            @Override
            public void onDetail(int row){
                javax.swing.table.DefaultTableModel historyModel = stockPanelView.getHistoryModel();
                int receiptID = Integer.parseInt(String.valueOf(historyModel.getValueAt(row, 0)).trim());
                System.out.println(receiptID);
                String detailReceipt = warehouseReceiptService.getDetailReceipt(receiptID);
                JOptionPane.showMessageDialog(stockPanelView, detailReceipt, "Chi tiết phiếu nhập", JOptionPane.INFORMATION_MESSAGE);
            }
            
            @Override
            public void onEdit(int row) {
                // Tạm thời hiển thị thông báo (Bạn có thể phát triển tính năng Xem chi tiết sau)
                JOptionPane.showMessageDialog(null, "Chức năng Xem/Sửa chi tiết phiếu nhập đang được cập nhật!");
            }

            @Override
            public void onDelete(int row) {
                // 1. Lấy thông tin cơ bản từ bảng lịch sử
                int maPhieuNhap = Integer.parseInt(stockPanelView.getHistoryTable().getValueAt(row, 0).toString());
                String ngayNhap = stockPanelView.getHistoryTable().getValueAt(row, 1).toString();
                
                try {
                    // Đọc ngày từ chuỗi gốc
                    java.time.LocalDate parsedDate = java.time.LocalDate.parse(ngayNhap);
                    // Tạo bộ định dạng mới "ngày/tháng/năm"
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    // Ép ngày thành chuỗi mới
                    ngayNhap = parsedDate.format(formatter);
                } catch (Exception ex) {
                    // Nếu ngày trên bảng bị sai định dạng nào đó không parse được, thì cứ giữ nguyên ngày gốc để khỏi lỗi app
                    System.out.println("Không thể parse ngày: " + ngayNhap);
                }

                // 2. Tạo Popup yêu cầu nhập lý do xóa kèm cảnh báo hoàn kho
                JTextField txtLyDo = new JTextField();
                Object[] message = {
                    "CẢNH BÁO: Bạn sắp xóa Phiếu nhập #" + maPhieuNhap + " ngày " + ngayNhap,
                    "Số lượng tồn kho của các nguyên liệu trong phiếu này sẽ bị trừ đi tương ứng.",
                    "Lý do hủy/xóa phiếu nhập (Bắt buộc):", txtLyDo
                };

                int option = JOptionPane.showConfirmDialog(null, message, "Xác nhận xóa phiếu nhập", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                if (option == JOptionPane.OK_OPTION) {
                    String lyDo = txtLyDo.getText().trim();
                    if (lyDo.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Bạn phải nhập lý do để hệ thống ghi nhận log!");
                        return;
                    }

                    // 3. Gọi Service xử lý (DAO sẽ gọi Oracle Procedure như đã thống nhất)
                    int currentUserID = SessionManager.getAccount().getAccountID();
                    boolean success = warehouseReceiptService.deleteWarehouseReceipt(maPhieuNhap, currentUserID, lyDo);

                    if (success) {
                        // 4. Xóa dòng khỏi giao diện bảng lịch sử
                        javax.swing.table.DefaultTableModel historyModel = (javax.swing.table.DefaultTableModel) stockPanelView.getHistoryTable().getModel();
                        historyModel.removeRow(row);
                        
                        // 5. RẤT QUAN TRỌNG: Load lại bảng Tồn kho (Vì số lượng vừa bị trừ đi)
                        try {
                            loadIngredientToView(); 
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                        JOptionPane.showMessageDialog(null, "Đã xóa phiếu nhập, lưu log và hoàn trả tồn kho thành công!");
                    }
                }
            }
        });
    }
        
    private void implementCreateReceipt() throws Exception {
        javax.swing.table.DefaultTableModel itemModel = stockPanelView.getReceiptItemModel();

        boolean isValid = ValidationUtil.validateAttributesOfWarehouseReceipt(itemModel, stockPanelView);
        if (!isValid) {
            return; 
        }

        int rowCount = itemModel.getRowCount();
        List<WarehouseReceiptDetailModel> warehouseReceiptListDetail = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            String tenNguyenLieu = String.valueOf(itemModel.getValueAt(i, 0)).trim();
            String donViTinh = String.valueOf(itemModel.getValueAt(i, 1)).trim();
            int soLuong = Integer.parseInt(String.valueOf(itemModel.getValueAt(i, 2)).trim()); // Thoải mái ép kiểu vì đã validate ở trên
            long donGia = Long.parseLong(String.valueOf(itemModel.getValueAt(i, 3)).trim());
            int nguong = Integer.parseInt(String.valueOf(itemModel.getValueAt(i, 4)).trim());
            String nhaCungCap = String.valueOf(itemModel.getValueAt(i, 5)).trim();
            LocalDate ngayNhap = LocalDate.parse(String.valueOf(itemModel.getValueAt(i, 6)).trim()); 

            // Đóng gói nó thành đối tượng
            WarehouseReceiptDetailModel detail = new WarehouseReceiptDetailModel(tenNguyenLieu, donViTinh, soLuong, donGia, nguong, nhaCungCap, ngayNhap);

            warehouseReceiptListDetail.add(detail);
        }

        // 3. THÊM VÀO DATABASE
        warehouseReceiptService.createReceipt(SessionManager.getAccount().getAccountID(), warehouseReceiptListDetail);

        JOptionPane.showMessageDialog(stockPanelView, "Đã nhập hàng thành công!");
        stockPanelView.clearReceiptForm();
    }
    
        // Hàm load dữ liệu lần đầu khi vừa mở app
    
    private void loadIngredientToView() throws SQLException {
        ingredientListModel = ingredientService.getIngredientList();
        stockPanelView.displayIngredientData(ingredientListModel);
    }
    
    private void loadWarehouseReceiptToView() throws SQLException {
        warehouseReceiptListModel = warehouseReceiptService.getWarehouseReceiptList();
        stockPanelView.displayWarehouseReceiptData(warehouseReceiptListModel);
    }
}
