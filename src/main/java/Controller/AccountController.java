/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import Common.EmailUtil;
import Common.ValidationUtil;
import Service.OtpService;
import Model.AccountModel;
import Model.SessionManager;
import Service.AccountService;
import Service.SessionService;
import View.ForgotPasswordFrame;
import View.LoginFrame;
import View.MainFrame;
import View.OtpDialog;
import View.RegisterFrame;
import java.sql.SQLException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author FAKK
 */
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
    
    public void setRoleController(RoleController roleController) {
        this.roleController = roleController;
    }
    // ----------------------------------------------------------
    
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
                JOptionPane optionPane = new JOptionPane("Đăng nhập thành công", JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog("Thành công");
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
                
                String token = sessionService.loginAndCreateToken(accountModel);
                SessionManager.setSession(token, accountModel);
                
                loginFrame.setVisible(false);
                try {
                    openMainFrame();
                } catch (SQLException ex) {
                    System.getLogger(AccountController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                
            }
            else {
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
                
                // --> GỌI LỆNH LÀM MỚI COMBOBOX BÊN PHÂN QUYỀN <--
                if (this.roleController != null) {
                    this.roleController.refreshAccountList();
                }
                // ------------------------------------------------
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
        
//        this.registerFrame.addForgotPasswordListener(new java.awt.event.MouseAdapter() {
//            @Override
//            public void mouseClicked(java.awt.event.MouseEvent e) {
//                registerFrame.setVisible(false);
//                forgotPasswordFrame.setVisible(true);
//            }
//        });
        
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
            new ShiftController(mainFrame);
            this.setRoleController(roleController);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
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
