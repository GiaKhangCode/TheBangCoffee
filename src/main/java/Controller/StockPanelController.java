/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author Kiet
 */
import Common.ValidationUtil;
import Model.WarehouseReceiptDetailModel;
import Model.IngredientListModel;
import Model.WarehouseReceiptListModel;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import Service.IngredientTypeService;
import javax.swing.JComboBox;

public class StockPanelController {
    private List<IngredientModel> ingredientListModel;
    private List<WarehouseReceiptModel> warehouseReceiptListModel;
    private StockPanel stockPanelView; // đại diện cho View
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
                } catch (Exception ex) { 

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
                    e.getType() == TableModelEvent.DELETE ||
                   (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 5)) {
                    long total = warehouseReceiptService.calculateTotal(stockPanelView.getReceiptItemModel());
                    stockPanelView.setTotalAmountLabel(total);
                }
            }
        });
        
        // --- LẮNG NGHE SỰ KIỆN TRÊN BẢNG TỒN KHO ---
        this.stockPanelView.setInventoryActionListener(new View.StockPanel.ActionButtonListener() {
            
            // ĐÃ SỬA: Đưa hàm onDetail vào ĐÚNG BÊN TRONG khối ngoặc của Listener
            @Override
            public void onDetail(int row) {
                // 1. Lấy tên nguyên liệu đang được chọn
                String tenNL = stockPanelView.getInventoryTable().getValueAt(row, 1).toString();
                // 2. Xuống Database lấy lịch sử
                String detail = ingredientService.getIngredientDetail(tenNL);
                // 3. In ra màn hình
                JOptionPane.showMessageDialog(null, detail, "Lịch sử nhập: " + tenNL, JOptionPane.INFORMATION_MESSAGE);
            }
            
            @Override
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
        
            @Override
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
        
        
        // --- LẮNG NGHE SỰ KIỆN TRÊN BẢNG LỊCH SỬ NHẬP HÀNG ---
        this.stockPanelView.setHistoryActionListener(new View.StockPanel.ActionButtonListener() {
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
        
        // --- LOGIC THÊM LOẠI NGUYÊN LIỆU MỚI (POPUP) ---
        this.stockPanelView.addAddCategoryListener(e -> {
            // 1. Hiển thị Popup nhập liệu giống hệt ảnh của bạn
            String newCategoryName = JOptionPane.showInputDialog(
                null, 
                "Nhập tên loại nguyên liệu mới:", 
                "Loại nguyên liệu",
                JOptionPane.PLAIN_MESSAGE
            );

            // 2. Kiểm tra nếu người dùng bấm OK và có nhập chữ
            if (newCategoryName != null && !newCategoryName.trim().isEmpty()) {
                newCategoryName = newCategoryName.trim();
                
                // 3. Gọi Service đẩy xuống Database
                boolean success = false;
                try {
                    success = ingredientTypeService.addIngredientType(newCategoryName);
                   
                } catch (SQLException ex) {
                    System.getLogger(StockPanelController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                } catch (ClassNotFoundException ex) {
                    System.getLogger(StockPanelController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                
                if (success) {
                    try {
                        // 4. Tải lại danh sách mới từ DB
                        ingredientTypeList = ingredientTypeService.getIngredientTypes();
                        stockPanelView.loadIngredientTypesToComboBox(ingredientTypeList);
                        
                        // 5. Tự động Set lựa chọn (Select) vào đúng cái loại vừa mới tạo
                        JComboBox<IngredientTypeModel> cb = stockPanelView.getCategoryComboBox();
                        for (int i = 0; i < cb.getItemCount(); i++) {
                            if (cb.getItemAt(i).getTypeName().equalsIgnoreCase(newCategoryName)) {
                                cb.setSelectedIndex(i);
                                break;
                            }
                        }
                        
                        JOptionPane.showMessageDialog(null, "Đã thêm loại nguyên liệu mới thành công!");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Thêm thất bại! Tên loại này có thể đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
        
    private void implementCreateReceipt() throws Exception {
        javax.swing.table.DefaultTableModel itemModel = stockPanelView.getReceiptItemModel();
        if (!ValidationUtil.validateAttributesOfWarehouseReceipt(itemModel, stockPanelView)) 
            return; 

        int rowCount = itemModel.getRowCount();
        List<WarehouseReceiptDetailModel> listDetails = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            String category = String.valueOf(itemModel.getValueAt(i, 0)).trim();
            String name = String.valueOf(itemModel.getValueAt(i, 1)).trim();
            String unit = String.valueOf(itemModel.getValueAt(i, 2)).trim();
            int capacity = Integer.parseInt(String.valueOf(itemModel.getValueAt(i, 3)).trim()); 
            int quantity = Integer.parseInt(String.valueOf(itemModel.getValueAt(i, 4)).trim()); 
            long totalPrice = Long.parseLong(String.valueOf(itemModel.getValueAt(i, 5)).trim());
            int threshold = Integer.parseInt(String.valueOf(itemModel.getValueAt(i, 6)).trim());
            String provider = String.valueOf(itemModel.getValueAt(i, 7)).trim();
            LocalDate importingDate = LocalDate.parse(String.valueOf(itemModel.getValueAt(i, 8)).trim()); 
            LocalDate expiryDate = LocalDate.parse(String.valueOf(itemModel.getValueAt(i, 9)).trim()); 

            // Map ID động từ tên danh mục
            int typeID = 1; 
            for (IngredientTypeModel type : ingredientTypeList) {
                if (type.getTypeName().equals(category)) { 
                    typeID = type.getTypeID(); 
                    break; 
                }
            }

            WarehouseReceiptDetailModel detail = new WarehouseReceiptDetailModel(typeID, category, name, unit, capacity, quantity, totalPrice, threshold, provider, importingDate, expiryDate);
            listDetails.add(detail);
        }

        warehouseReceiptService.createReceipt(SessionManager.getAccount().getAccountID(), listDetails);
        JOptionPane.showMessageDialog(stockPanelView, "Lập phiếu nhập thành công!");
        stockPanelView.clearReceiptForm();
    }
    
    // Hàm load dữ liệu lần đầu khi vừa mở app
    
    public void loadIngredientToView() throws SQLException {
        ingredientListModel = ingredientService.getIngredientList();
        stockPanelView.displayIngredientData(ingredientListModel);
        
        int totalTypes = ingredientListModel.size();
        int warningCount = 0;
        
        // Duyệt qua danh sách xem có bao nhiêu món "Hết hàng" hoặc "Sắp hết"
        for (IngredientModel item : ingredientListModel) {
            String trangThai = item.getStatus(); // Lấy trạng thái từ Model
            // Chỗ này bạn tự điền chữ cho khớp với chữ sinh ra từ Model của bạn nhé
            if (trangThai.equalsIgnoreCase("Hết hàng")) {
                warningCount++;
            }
        }
        

        // 3. Đẩy 3 con số vừa tính được ngược lại cho View hiển thị
        stockPanelView.updateDashboardStats(totalTypes, warningCount);
    }
    
    public void loadWarehouseReceiptToView() throws SQLException {
        warehouseReceiptListModel = warehouseReceiptService.getWarehouseReceiptList();
        stockPanelView.displayWarehouseReceiptData(warehouseReceiptListModel);
    }
    
}