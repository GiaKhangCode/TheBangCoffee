package Controller;

import Common.EmailUtil;
import Common.ValidationUtil;
import DatabaseAccessObject.ShiftSessionDAO;
import DatabaseAccessObject.ShiftDAO;
import Service.OtpService;
import Model.AccountModel;
import Model.SessionManager;
import Model.ShiftModel;
import Model.ShiftSession;
import Service.AccountService;
import Service.SessionService;
import View.ForgotPasswordFrame;
import View.LoginFrame;
import View.MainFrame;
import View.OtpDialog;
import View.RegisterFrame;
import View.ShiftSessionOpenDialog;
import View.ShiftSessionCloseDialog;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class AccountController {
    private AccountModel accountModel;
    private LoginFrame loginFrame;
    private RegisterFrame registerFrame;
    private ForgotPasswordFrame forgotPasswordFrame;
    private MainFrame mainFrame;
    private AccountService accountService;
    private OtpDialog otpDialog;
    private SessionService sessionService;
    private RoleController roleController;
    private ShiftController shiftController; 
    
    public void setRoleController(RoleController roleController) {
        this.roleController = roleController;
    }

    public void setShiftController(ShiftController shiftController) {
        this.shiftController = shiftController;
    }
    
    public AccountController() throws SQLException{
        accountModel = new AccountModel();
        accountService = new AccountService();
        sessionService = new SessionService();
        
        loginFrame = new LoginFrame();
        registerFrame = new RegisterFrame();
        forgotPasswordFrame = new ForgotPasswordFrame();
        otpDialog = new OtpDialog(registerFrame);
        
        initListeners();
        usingUnrevokedToken();
    }
    
    private void initListeners() {
        this.loginFrame.addLoginListener(e -> {
            String username = loginFrame.getUsername();
            String password = loginFrame.getPassword();
            accountModel = accountService.login(username, password);
            
            if(accountModel != null) {
                String token = sessionService.loginAndCreateToken(accountModel);
                SessionManager.setSession(token, accountModel);
                loginFrame.setVisible(false);
                try {
                    openMainFrame();
                } catch (SQLException ex) {
                    System.getLogger(AccountController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            } else {
                JOptionPane optionPane = new JOptionPane("Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
                JDialog dialog = optionPane.createDialog("Thất Bại");
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
            }
        });
        
        loginFrame.addSignUpListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                loginFrame.setVisible(false);
                registerFrame.setVisible(true);
                registerFrame.addBackListener(ev -> {
                    registerFrame.setVisible(false);
                    loginFrame.setVisible(true);
                });
            }
        });
        
        this.registerFrame.addSignUpListener(ev -> {
            String fullName = registerFrame.getFullName();
            String email = registerFrame.getEmail();
            String phoneNumber = registerFrame.getPhone();
            String username = registerFrame.getUsername();
            String password = registerFrame.getPassword();
            String isValidateLogin = ValidationUtil.checkValidateLogin(fullName, username, password, phoneNumber, email);
            
            if(!isValidateLogin.equalsIgnoreCase("Đăng nhập hợp lệ")){
                JOptionPane optionPane = new JOptionPane( isValidateLogin, JOptionPane.ERROR_MESSAGE);
                JDialog dialog = optionPane.createDialog("Thất Bại");
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
                return;
            }

            try {
                String otp = OtpService.generateOTP(email, OtpService.OtpType.REGISTER);
                EmailUtil.sendOTP(email, otp, "đăng ký tài khoản");
            } catch (Exception ex){
                JOptionPane.showMessageDialog(null, ex.getMessage());
                return;
            }
            
            otpDialog.setVisible(true);
            
            if(!OtpService.verifyOTP(email, otpDialog.getOtp(), OtpService.OtpType.REGISTER)){
                JOptionPane.showMessageDialog(null, "OTP không đúng hoặc đã hết hạn!");
                return;
            }
            
            String result = accountService.signUp(fullName, username, password, phoneNumber, email);
            JOptionPane optionPane = new JOptionPane(result, JOptionPane.INFORMATION_MESSAGE);
            JDialog dialog = optionPane.createDialog("Đăng ký");
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
            
            if(result.equals("Thành công")){
                registerFrame.setVisible(false);
                loginFrame.setVisible(true);
                if (this.roleController != null) this.roleController.refreshAccountList();
                if (this.shiftController != null) this.shiftController.refreshEmployeeList();
            }
        });
        
        this.loginFrame.addForgotPasswordListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                loginFrame.setVisible(false);
                forgotPasswordFrame.setVisible(true);
                forgotPasswordFrame.addBackListener(ev -> {
                    forgotPasswordFrame.setVisible(false);
                    loginFrame.setVisible(true);
                });
            }
        });
        
        this.forgotPasswordFrame.addSendOtpListener(e -> {
            String email = forgotPasswordFrame.getEmail();
            if(!accountService.isEmailExists(email)){
                JOptionPane optionPane = new JOptionPane("Email không tồn tại", JOptionPane.ERROR_MESSAGE);
                JDialog dialog = optionPane.createDialog("Thất Bại");
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
                return;
            }
            try {
                String otp = OtpService.generateOTP(email, OtpService.OtpType.RESET_PASSWORD);
                EmailUtil.sendOTP(email, otp, "khôi phục mật khẩu");
            } catch (Exception ex){
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
            JOptionPane.showMessageDialog(null, "OTP đã được gửi về email!");
        });
        
        this.forgotPasswordFrame.addConfirmListener(e -> {
            String email = forgotPasswordFrame.getEmail();
            String otp = forgotPasswordFrame.getOtp();
            String newPass = forgotPasswordFrame.getNewPassword();
            
            if (!OtpService.verifyOTP(email, otp, OtpService.OtpType.RESET_PASSWORD)) {
                JOptionPane optionPane = new JOptionPane("OTP không đúng", JOptionPane.ERROR_MESSAGE);
                JDialog dialog = optionPane.createDialog("Thất Bại");
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
                return;
            }
            boolean success = accountService.resetPassword(email, newPass);

            if (success) {
                sessionService.revokeAllTokens(email);
                JOptionPane.showMessageDialog(null, "Đổi mật khẩu thành công!");
                forgotPasswordFrame.setVisible(false);
                loginFrame.setVisible(true);
            } 
            else {
                JOptionPane optionPane = new JOptionPane("Lỗi cập nhật mật khẩu", JOptionPane.ERROR_MESSAGE);
                JDialog dialog = optionPane.createDialog("Thất Bại");
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
                return;
            }
        });
        
        this.forgotPasswordFrame.addBackListener(e -> {
            forgotPasswordFrame.setVisible(false);
            loginFrame.setVisible(true);
        });
    }

    private void openMainFrame() throws SQLException {
        this.mainFrame = new MainFrame();
        
        this.mainFrame.addLogoutListener(e -> {
            sessionService.logout(SessionManager.getToken());
            SessionManager.clear();
            mainFrame.setVisible(false);
            mainFrame.dispose();
            this.mainFrame = null;
            loginFrame.setVisible(true);
        });

        try {
            new StockPanelController(mainFrame);
            roleController = new RoleController(this.mainFrame);
            roleController.hiddenButton();
            new ProductController(mainFrame);
            new PosController(mainFrame);
            new DashboardController(mainFrame);
            
            ShiftController newShiftCtrl = new ShiftController(mainFrame);
            this.setShiftController(newShiftCtrl);
            this.setRoleController(roleController);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        ShiftSessionDAO shiftSessionDAO = new ShiftSessionDAO();
        ShiftSession caDangMo = shiftSessionDAO.getPhienCaDangMo();

        if (caDangMo != null) {
            SessionManager.setCurrentMaPhienCa(caDangMo.getMaPhienCa());
            mainFrame.setShiftButtonState(true); 
            
            if (caDangMo.getMaTaiKhoanMo() != SessionManager.getAccountId()) {
                JOptionPane.showMessageDialog(mainFrame, 
                    "Hệ thống đang sử dụng két tiền của ca trước do chưa được đóng.\nBạn đang dùng chung ca.", 
                    "Thông báo Ca làm việc", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            mainFrame.setShiftButtonState(false); 
        }

        this.mainFrame.addShiftToggleListener(e -> {
            if (!SessionManager.hasOpenShift()) {
                // LẤY DỮ LIỆU ĐỔ LÊN POPUP
                ShiftDAO shiftDAO = new ShiftDAO();
                List<ShiftModel> activeShifts = new ArrayList<>();
                try { activeShifts = shiftDAO.getActiveShift(); } catch (Exception ex) {}
                
                Object[] handoverInfo = shiftSessionDAO.getLastShiftHandoverInfo();
                int unpaidCount = shiftSessionDAO.countUnpaidOrders();
                List<Object[]> inventory = shiftSessionDAO.getCurrentInventory();

                ShiftSessionOpenDialog dialog = new ShiftSessionOpenDialog(
                        mainFrame, 
                        new ShiftSession(SessionManager.getAccountId(), null),
                        activeShifts, handoverInfo, unpaidCount, inventory
                );
                dialog.setVisible(true); 

                if (dialog.isConfirmed()) {
                    int newShiftId = shiftSessionDAO.moCa(dialog.getShiftSessionModel(), dialog.getTienMatDauCa());
                    if (newShiftId != -1) {
                        SessionManager.setCurrentMaPhienCa(newShiftId);
                        mainFrame.setShiftButtonState(true); 
                        JOptionPane.showMessageDialog(mainFrame, "Mở ca thành công! Đã có thể bắt đầu bán hàng.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Lỗi hệ thống: Không thể khởi tạo ca làm việc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } 
            // Tìm đến phần logic addShiftToggleListener trong hàm openMainFrame và cập nhật đoạn này:
            else {
                // TẬP HỢP DỮ LIỆU ĐỔ VÀO FORM ĐÓNG CA
                int maPhienCa = SessionManager.getCurrentMaPhienCa();
                double tienDauCa = shiftSessionDAO.getTienMatDauCa(maPhienCa);
                double doanhThu = shiftSessionDAO.getDoanhThuTienMat(maPhienCa);
                int unpaidCount = shiftSessionDAO.countUnpaidOrders();

                // Lấy danh sách nhân viên thực tế từ DB để bàn giao
                List<AccountModel> allAccounts = accountService.getAccountList();

                ShiftSessionCloseDialog dialog = new ShiftSessionCloseDialog(
                        mainFrame, tienDauCa, doanhThu, 0, unpaidCount, allAccounts
                );
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    double tienThucTe = dialog.getTienMatThucTe();
                    String ghiChu = dialog.getGhiChu();
                    Integer maNhanBanGiao = dialog.getMaTaiKhoanNhan();

                    double tienHeThong = tienDauCa + doanhThu;
                    double chenhLech = tienThucTe - tienHeThong;

                    if (chenhLech != 0 && ghiChu.isEmpty()) {
                        JOptionPane.showMessageDialog(mainFrame, "Tiền đang bị lệch! Bạn phải nhập Ghi chú để giải trình.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    boolean isSuccess = shiftSessionDAO.dongCaToanDien(maPhienCa, maNhanBanGiao, ghiChu, tienHeThong, tienThucTe);

                    if (isSuccess) {
                        JOptionPane.showMessageDialog(mainFrame, "Chốt ca thành công!");
                        SessionManager.setCurrentMaPhienCa(-1); 
                        mainFrame.setShiftButtonState(false); 
                        try { mainFrame.setPageActive("Stats"); } catch (SQLException ex) { ex.printStackTrace(); }
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Lỗi khi chốt ca!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        this.mainFrame.setVisible(true);
    }
    
    private void usingUnrevokedToken() throws SQLException{
        accountModel = accountService.getAccountFromToken();
        String token = accountService.getUnrevokedToken();
        SessionManager.setSession(token, accountModel);

        if(accountModel != null){
            openMainFrame();
        }
        else{
            loginFrame.setVisible(true);
        }
    }
}