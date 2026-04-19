///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package Controller;
//
//import Common.EmailUtil;
//import Common.ValidationUtil;
//import Service.OtpService;
//import Model.AccountModel;
//import Model.WarehouseReceiptDetailModel;
//import Model.IngredientListModel;
//import Model.WarehouseReceiptListModel;
//import Model.IngredientModel;
//import Model.WarehouseReceiptModel;
//import Service.AccountService;
//import Service.IngredientService;
//import Service.WarehouseReceiptService;
//import View.ForgotPasswordFrame;
//import View.LoginFrame;
//import View.MainFrame;
//import View.OtpDialog;
//import View.RegisterFrame;
//import View.StockPanel;
//import View.StockPanel.ActionButtonListener;
//import java.sql.SQLException;
//import java.time.LocalDate;
//import java.time.format.DateTimeParseException;
//import java.util.ArrayList;
//import java.util.List;
//import javax.swing.JDialog;
//import javax.swing.JOptionPane;
//import javax.swing.JTextField;
//import javax.swing.event.TableModelEvent;
//import javax.swing.event.TableModelListener;
//import javax.swing.table.DefaultTableModel;
//
//
//public class TheBangCoffeeController {
//    
//    private AccountModel accountModel;
//    private LoginFrame loginFrame;
//    private RegisterFrame registerFrame;
//    private ForgotPasswordFrame forgotPasswordFrame;
//    private List<IngredientModel> ingredientListModel;
//    private List<WarehouseReceiptModel> warehouseReceiptListModel;
//    private StockPanel stockPanelView; // đại diện cho View
//    private MainFrame mainFrame;
//    private AccountService accountService;
//    private OtpDialog otpDialog;
//    public static String currentToken;
//    private IngredientService ingredientService;
//    private WarehouseReceiptService warehouseReceiptService;
//
//    
//    
//
//    public TheBangCoffeeController() throws SQLException{
//        accountModel = new AccountModel();
//        loginFrame = new LoginFrame();
//        registerFrame = new RegisterFrame();
//        forgotPasswordFrame = new ForgotPasswordFrame();
//        //ingredientListModel = new IngredientListModel();
//        //warehouseReceiptListModel = new WarehouseReceiptListModel();
//        mainFrame = new MainFrame();
//        accountService = new AccountService();
//        otpDialog = new OtpDialog(registerFrame);
//        ingredientService = new IngredientService();
//        warehouseReceiptService = new WarehouseReceiptService();
//        
////        // Gán Panel thực tế vào biến, KHÔNG DÙNG "new StockPanel()" NỮA
//        this.stockPanelView = mainFrame.getStockPanel(); 
//        
//        loginFrame.setVisible(true);
//        initListeners();
//        initStockListeners();
//        
//        loadIngredientToView();
//        loadWarehouseReceiptToView();
//    }
//    
//    private void initListeners() {
//        this.loginFrame.addLoginListener(e -> {
//            String username = loginFrame.getUsername();
//            String password = loginFrame.getPassword();
//            accountModel = accountService.login(username, password);
//            
//            if(accountModel != null) {
//                JOptionPane optionPane = new JOptionPane("Đăng nhập thành công", JOptionPane.INFORMATION_MESSAGE);
//                JDialog dialog = optionPane.createDialog("Thành công");
//                dialog.setAlwaysOnTop(true);
//                dialog.setVisible(true);
//                
//                String token = accountService.loginAndCreateToken(accountModel);
//                TheBangCoffeeController.currentToken = token; 
//                
//                loginFrame.setVisible(false);
//                mainFrame.setVisible(true);
//            }
//            else {
//                JOptionPane optionPane = new JOptionPane("Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
//                JDialog dialog = optionPane.createDialog("Thất Bại");
//                dialog.setAlwaysOnTop(true);
//                dialog.setVisible(true);
//            }
//        });
//        
//        loginFrame.addSignUpListener(new java.awt.event.MouseAdapter() {
//            @Override
//            public void mouseClicked(java.awt.event.MouseEvent e) {
//                loginFrame.setVisible(false);
//                registerFrame.setVisible(true);
//                
//                registerFrame.addBackListener(ev -> {
//                    registerFrame.setVisible(false);
//                    loginFrame.setVisible(true);
//                });
//            }
//        });
//        
//        this.registerFrame.addSignUpListener(ev -> {
//            String fullName = registerFrame.getFullName();
//            String email = registerFrame.getEmail();
//            String phoneNumber = registerFrame.getPhone();
//            String username = registerFrame.getUsername();
//            String password = registerFrame.getPassword();
//            String isValidateLogin = ValidationUtil.checkValidateLogin(fullName, username, password, phoneNumber, email);
//            
//            if(!isValidateLogin.equalsIgnoreCase("Đăng nhập hợp lệ")){
//                JOptionPane optionPane = new JOptionPane( isValidateLogin, JOptionPane.ERROR_MESSAGE);
//                JDialog dialog = optionPane.createDialog("Thất Bại");
//                dialog.setAlwaysOnTop(true);
//                dialog.setVisible(true);
//                return;
//            }
//            
//            
//            
//            try {
//                String otp = OtpService.generateOTP(email, OtpService.OtpType.REGISTER);
//                EmailUtil.sendOTP(email, otp, "đăng ký tài khoản");
//            } catch (Exception ex){
//                JOptionPane.showMessageDialog(null, ex.getMessage());
//                return;
//            }
//            
//            otpDialog.setVisible(true);
//            
//            if(!OtpService.verifyOTP(email, otpDialog.getOtp(), OtpService.OtpType.REGISTER)){
//                JOptionPane.showMessageDialog(null, "OTP không đúng hoặc đã hết hạn!");
//                return;
//            }
//            
//            String result = accountService.signUp(fullName, username, password, phoneNumber, email);
//            JOptionPane optionPane = new JOptionPane(result, JOptionPane.INFORMATION_MESSAGE);
//            JDialog dialog = optionPane.createDialog("Đăng ký");
//            dialog.setAlwaysOnTop(true);
//            dialog.setVisible(true);
//            if(result.equals("Thành công")){
//                registerFrame.setVisible(false);
//                loginFrame.setVisible(true);
//            }
//        });
//        
//        this.loginFrame.addForgotPasswordListener(new java.awt.event.MouseAdapter() {
//            @Override
//            public void mouseClicked(java.awt.event.MouseEvent e) {
//                loginFrame.setVisible(false);
//                forgotPasswordFrame.setVisible(true);
//                
//                forgotPasswordFrame.addBackListener(ev -> {
//                    forgotPasswordFrame.setVisible(false);
//                    loginFrame.setVisible(true);
//                });
//               
//                
//            }
//        });
//        
////        this.registerFrame.addForgotPasswordListener(new java.awt.event.MouseAdapter() {
////            @Override
////            public void mouseClicked(java.awt.event.MouseEvent e) {
////                registerFrame.setVisible(false);
////                forgotPasswordFrame.setVisible(true);
////            }
////        });
//        
//        this.forgotPasswordFrame.addSendOtpListener(e -> {
//            String email = forgotPasswordFrame.getEmail();
//            
//            if(!accountService.isEmailExists(email)){
//                JOptionPane optionPane = new JOptionPane("Email không tồn tại", JOptionPane.ERROR_MESSAGE);
//                JDialog dialog = optionPane.createDialog("Thất Bại");
//                dialog.setAlwaysOnTop(true);
//                dialog.setVisible(true);
//                return;
//            }
//            
//            try {
//                String otp = OtpService.generateOTP(email, OtpService.OtpType.RESET_PASSWORD);
//                EmailUtil.sendOTP(email, otp, "khôi phục mật khẩu");
//            } catch (Exception ex){
//                JOptionPane.showMessageDialog(null, ex.getMessage());
//            }
//
//            JOptionPane.showMessageDialog(null, "OTP đã được gửi về email!");
//        });
//        
//        this.forgotPasswordFrame.addConfirmListener(e -> {
//            String email = forgotPasswordFrame.getEmail();
//            String otp = forgotPasswordFrame.getOtp();
//            String newPass = forgotPasswordFrame.getNewPassword();
//            
//            
//            if (!OtpService.verifyOTP(email, otp, OtpService.OtpType.RESET_PASSWORD)) {
//                JOptionPane optionPane = new JOptionPane("OTP không đúng", JOptionPane.ERROR_MESSAGE);
//                JDialog dialog = optionPane.createDialog("Thất Bại");
//                dialog.setAlwaysOnTop(true);
//                dialog.setVisible(true);
//                return;
//            }
//            boolean success = accountService.resetPassword(email, newPass);
//
//            if (success) {
//                accountService.revokeAllTokens(email);
//                
//                JOptionPane.showMessageDialog(null, "Đổi mật khẩu thành công!");
//                forgotPasswordFrame.setVisible(false);
//                loginFrame.setVisible(true);
//            } 
//            else {
//                JOptionPane optionPane = new JOptionPane("Lỗi cập nhật mật khẩu", JOptionPane.ERROR_MESSAGE);
//                JDialog dialog = optionPane.createDialog("Thất Bại");
//                dialog.setAlwaysOnTop(true);
//                dialog.setVisible(true);
//                return;
//            }
//        });
//        
//        this.forgotPasswordFrame.addBackListener(e -> {
//            forgotPasswordFrame.setVisible(false);
//            loginFrame.setVisible(true);
//        });
//        
//        this.mainFrame.addLogoutListener(e -> {
//            accountService.logout(currentToken);
//            TheBangCoffeeController.currentToken = null;
//            mainFrame.setVisible(false);
//            loginFrame.setVisible(true);
//        });
//    }
//    
//    private void initStockListeners() {
//        
//        this.stockPanelView.addSubmitReceiptListener(new java.awt.event.ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent e) {
//                try {
//                    // Thực thi logic chính
//                    implementCreateReceipt(); 
//                    loadIngredientToView();
//
//                } catch (Exception ex) {
//                    // 1. Ghi log đầy đủ, rõ ràng để Dev đọc
//                    System.getLogger(TheBangCoffeeController.class.getName())
//                          .log(System.Logger.Level.ERROR, "Lỗi nghiêm trọng khi lập phiếu nhập kho", ex);
//
//                    // 2. Hiện thông báo cho User biết (BẮT BUỘC ĐỐI VỚI APP GIAO DIỆN)
//                    javax.swing.JOptionPane.showMessageDialog(
//                        null, // Hoặc truyền this.stockPanelView vào đây để hộp thoại canh giữa màn hình
//                        "Đã xảy ra lỗi khi lưu phiếu nhập:\n" + ex.getMessage(),
//                        "Lỗi hệ thống",
//                        javax.swing.JOptionPane.ERROR_MESSAGE
//                    );
//                }
//            }
//        });
//        
//        this.stockPanelView.addHistoryButtonListener(new java.awt.event.ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent e) {
//                try {
//                    loadWarehouseReceiptToView(); // Gọi hàm kéo data từ DB
//                } catch (Exception ex) { // <-- ĐÃ SỬA: Dùng Exception để tóm gọn mọi loại lỗi
//
//                    // 1. In chi tiết lỗi chữ đỏ ra tab Console/Output trong IDE để bạn rà soát
//                    ex.printStackTrace(); 
//
//                    // 2. Hiện popup cho biết lỗi gì, thay vì để màn hình đứng im
//                    JOptionPane.showMessageDialog(
//                        null, 
//                        "Không thể tải dữ liệu lịch sử!\nNguyên nhân: " + ex.toString(), 
//                        "Lỗi Hệ Thống", 
//                        JOptionPane.ERROR_MESSAGE
//                    );
//                }
//            }
//        });
//        
//        stockPanelView.getReceiptItemModel().addTableModelListener(new TableModelListener() {
//            @Override
//            public void tableChanged(TableModelEvent e) {
//                // Nếu là sự kiện THÊM DÒNG mới, hoặc CẬP NHẬT cột 2 (Số lượng), cột 3 (Đơn giá)
//                if (e.getType() == TableModelEvent.INSERT || 
//                   (e.getType() == TableModelEvent.UPDATE && (e.getColumn() == 2 || e.getColumn() == 3))) {
//                    long total = warehouseReceiptService.calculateTotal(stockPanelView.getReceiptItemModel());
//                    stockPanelView.setTotalAmountLabel(total);
//                }
//            }
//        });
//        
//        // Bên trong hàm initStockListeners(), phần setInventoryActionListener:
//    
//        this.stockPanelView.setInventoryActionListener(new View.StockPanel.ActionButtonListener() {
//            public void onDelete(int row) {
//                // 1. Lấy thông tin cơ bản để hiển thị xác nhận
//                int maNL = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 0).toString());
//                String tenNL = stockPanelView.getInventoryTable().getValueAt(row, 1).toString();
//
//                // 2. Tạo Popup yêu cầu nhập lý do xóa
//                JTextField txtLyDo = new JTextField();
//                Object[] message = {
//                    "Xác nhận xóa nguyên liệu: " + tenNL,
//                    "Lý do xóa (Bắt buộc):", txtLyDo
//                };
//
//                int option = JOptionPane.showConfirmDialog(null, message, "Xác nhận xóa", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
//
//                if (option == JOptionPane.OK_OPTION) {
//                    String lyDo = txtLyDo.getText().trim();
//                    if (lyDo.isEmpty()) {
//                        JOptionPane.showMessageDialog(null, "Bạn phải nhập lý do để thực hiện xóa!");
//                        return;
//                    }
//
//                    // 3. Gọi Service/DAO xử lý
//                    int currentUserID = accountModel.getAccountID();
//                    boolean success = ingredientService.deleteIngredient(maNL, currentUserID, lyDo);
//
//                    if (success) {
//                        stockPanelView.getInventoryModel().removeRow(row);
//                        JOptionPane.showMessageDialog(null, "Đã xóa nguyên liệu và lưu log thành công.");
//                    }
//                } 
//        }
//        
//        public void onEdit(int row) {
//            // 1. Lấy dữ liệu cũ từ dòng đang chọn
//            int maNL = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 0).toString());
//            String tenCu = stockPanelView.getInventoryTable().getValueAt(row, 1).toString();
//            String dvtCu = stockPanelView.getInventoryTable().getValueAt(row, 2).toString();
//            int tonCu = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 3).toString());
//            int nguongCu = Integer.parseInt(stockPanelView.getInventoryTable().getValueAt(row, 4).toString());
//
//            // 2. Gọi View hiện Form nhập liệu mới (kèm ô Lý do)
//            Object[] duLieuMoi = stockPanelView.showEditDialog(tenCu, dvtCu, tonCu, nguongCu);
//
//            // 3. Xử lý khi người dùng bấm OK
//            if (duLieuMoi != null) {
//                String tenMoi = (String) duLieuMoi[0];
//                String dvtMoi = (String) duLieuMoi[1];
//                int tonMoi = (int) duLieuMoi[2];
//                int nguongMoi = (int) duLieuMoi[3];
//                String lyDo = (String) duLieuMoi[4]; // Nhận thêm lý do
//                
//                // Lấy ID tài khoản người đang đăng nhập
//                int currentUserID = accountModel.getAccountID(); 
//
//                // 4. Gọi Service để đẩy xuống Database
//                boolean isSuccess = ingredientService.updateIngredient(maNL, tenMoi, dvtMoi, tonMoi, nguongMoi, currentUserID, lyDo);
//
//                if (isSuccess) {
//                    // 5. Cập nhật UI ngay lập tức
//                    stockPanelView.getInventoryTable().setValueAt(tenMoi, row, 1);
//                    stockPanelView.getInventoryTable().setValueAt(dvtMoi, row, 2);
//                    stockPanelView.getInventoryTable().setValueAt(tonMoi, row, 3);
//                    stockPanelView.getInventoryTable().setValueAt(nguongMoi, row, 4);
//                    
//                    // Logic tính lại trạng thái "Còn hàng / Hết hàng" trên bảng
//                    String trangThai = (tonMoi < nguongMoi) ? "Hết hàng" : "Còn hàng";
//                    stockPanelView.getInventoryTable().setValueAt(trangThai, row, 5);
//
//                    JOptionPane.showMessageDialog(null, "Cập nhật và lưu log thành công!");
//                }
//            }
//        }
//    });
//        // Lắng nghe sự kiện Sửa/Xóa trên bảng Lịch sử nhập hàng
//        this.stockPanelView.setHistoryActionListener(new View.StockPanel.ActionButtonListener() {
//            @Override
//            public void onEdit(int row) {
//                // Tạm thời hiển thị thông báo (Bạn có thể phát triển tính năng Xem chi tiết sau)
//                JOptionPane.showMessageDialog(null, "Chức năng Xem/Sửa chi tiết phiếu nhập đang được cập nhật!");
//            }
//
//            @Override
//            public void onDelete(int row) {
//                // 1. Lấy thông tin cơ bản từ bảng lịch sử
//                int maPhieuNhap = Integer.parseInt(stockPanelView.getHistoryTable().getValueAt(row, 0).toString());
//                String ngayNhap = stockPanelView.getHistoryTable().getValueAt(row, 1).toString();
//                
//                try {
//                    // Đọc ngày từ chuỗi gốc
//                    java.time.LocalDate parsedDate = java.time.LocalDate.parse(ngayNhap);
//                    // Tạo bộ định dạng mới "ngày/tháng/năm"
//                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
//                    // Ép ngày thành chuỗi mới
//                    ngayNhap = parsedDate.format(formatter);
//                } catch (Exception ex) {
//                    // Nếu ngày trên bảng bị sai định dạng nào đó không parse được, thì cứ giữ nguyên ngày gốc để khỏi lỗi app
//                    System.out.println("Không thể parse ngày: " + ngayNhap);
//                }
//
//                // 2. Tạo Popup yêu cầu nhập lý do xóa kèm cảnh báo hoàn kho
//                JTextField txtLyDo = new JTextField();
//                Object[] message = {
//                    "CẢNH BÁO: Bạn sắp xóa Phiếu nhập #" + maPhieuNhap + " ngày " + ngayNhap,
//                    "Số lượng tồn kho của các nguyên liệu trong phiếu này sẽ bị trừ đi tương ứng.",
//                    "Lý do hủy/xóa phiếu nhập (Bắt buộc):", txtLyDo
//                };
//
//                int option = JOptionPane.showConfirmDialog(null, message, "Xác nhận xóa phiếu nhập", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
//
//                if (option == JOptionPane.OK_OPTION) {
//                    String lyDo = txtLyDo.getText().trim();
//                    if (lyDo.isEmpty()) {
//                        JOptionPane.showMessageDialog(null, "Bạn phải nhập lý do để hệ thống ghi nhận log!");
//                        return;
//                    }
//
//                    // 3. Gọi Service xử lý (DAO sẽ gọi Oracle Procedure như đã thống nhất)
//                    int currentUserID = accountModel.getAccountID();
//                    boolean success = warehouseReceiptService.deleteWarehouseReceipt(maPhieuNhap, currentUserID, lyDo);
//
//                    if (success) {
//                        // 4. Xóa dòng khỏi giao diện bảng lịch sử
//                        javax.swing.table.DefaultTableModel historyModel = (javax.swing.table.DefaultTableModel) stockPanelView.getHistoryTable().getModel();
//                        historyModel.removeRow(row);
//                        
//                        // 5. RẤT QUAN TRỌNG: Load lại bảng Tồn kho (Vì số lượng vừa bị trừ đi)
//                        try {
//                            loadIngredientToView(); 
//                        } catch (SQLException ex) {
//                            ex.printStackTrace();
//                        }
//
//                        JOptionPane.showMessageDialog(null, "Đã xóa phiếu nhập, lưu log và hoàn trả tồn kho thành công!");
//                    }
//                }
//            }
//        });
//    }
//        
//    private void implementCreateReceipt() throws Exception {
//        javax.swing.table.DefaultTableModel itemModel = stockPanelView.getReceiptItemModel();
//
//        boolean isValid = ValidationUtil.validateAttributesOfWarehouseReceipt(itemModel, stockPanelView);
//        if (!isValid) {
//            return; 
//        }
//
//        int rowCount = itemModel.getRowCount();
//        List<WarehouseReceiptDetailModel> warehouseReceiptListDetail = new ArrayList<>();
//
//        for (int i = 0; i < rowCount; i++) {
//            String tenNguyenLieu = String.valueOf(itemModel.getValueAt(i, 0)).trim();
//            String donViTinh = String.valueOf(itemModel.getValueAt(i, 1)).trim();
//            int soLuong = Integer.parseInt(String.valueOf(itemModel.getValueAt(i, 2)).trim()); // Thoải mái ép kiểu vì đã validate ở trên
//            long donGia = Long.parseLong(String.valueOf(itemModel.getValueAt(i, 3)).trim());
//            int nguong = Integer.parseInt(String.valueOf(itemModel.getValueAt(i, 4)).trim());
//            String nhaCungCap = String.valueOf(itemModel.getValueAt(i, 5)).trim();
//            LocalDate ngayNhap = LocalDate.parse(String.valueOf(itemModel.getValueAt(i, 6)).trim()); 
//
//            // Đóng gói nó thành đối tượng
//            WarehouseReceiptDetailModel detail = new WarehouseReceiptDetailModel(tenNguyenLieu, donViTinh, soLuong, donGia, nguong, nhaCungCap, ngayNhap);
//
//            warehouseReceiptListDetail.add(detail);
//        }
//
//        // 3. THÊM VÀO DATABASE
//        warehouseReceiptService.createReceipt(accountModel.getAccountID(), warehouseReceiptListDetail);
//
//        JOptionPane.showMessageDialog(stockPanelView, "Đã nhập hàng thành công!");
//        stockPanelView.clearReceiptForm();
//    }
//    
//        // Hàm load dữ liệu lần đầu khi vừa mở app
//    
//    public void loadIngredientToView() throws SQLException {
//        ingredientListModel = ingredientService.getIngredientList();
//        stockPanelView.displayIngredientData(ingredientListModel);
//    }
//    
//    // cbi sửa
//    public void loadWarehouseReceiptToView() throws SQLException {
//        warehouseReceiptListModel = warehouseReceiptService.getWarehouseReceiptList();
//        stockPanelView.displayWarehouseReceiptData(warehouseReceiptListModel);
//    }
//    
//    
//}
