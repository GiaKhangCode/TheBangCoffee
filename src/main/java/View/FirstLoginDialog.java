package View;

import Common.ComponentUI;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialog bắt buộc đổi mật khẩu khi đăng nhập lần đầu tiên.
 * Không thể đóng bằng nút X — người dùng phải hoàn tất đổi mật khẩu.
 */
public class FirstLoginDialog extends JDialog {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color BG_COLOR = new Color(245, 248, 245);

    // --- Các field nhập liệu ---
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtOtp;

    // --- Các nút bấm ---
    private JButton btnSendOtp;
    private JButton btnConfirm;

    // --- Trạng thái ---
    private boolean otpSent = false;
    private boolean confirmed = false;

    public FirstLoginDialog(Frame parent) {
        super(parent, "Đăng nhập lần đầu - Vui lòng đổi mật khẩu", true);

        // Không cho phép đóng bằng nút X
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Không làm gì — bắt buộc đổi mật khẩu
            }
        });

        initUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_COLOR);

        // ---- HEADER ----
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel("Đổi Mật Khẩu Lần Đầu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Đây là lần đăng nhập đầu tiên của bạn. Vui lòng đổi mật khẩu để bảo mật tài khoản.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(200, 235, 215));

        JPanel headerTextPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        headerTextPanel.setOpaque(false);
        headerTextPanel.add(lblTitle);
        headerTextPanel.add(lblSubtitle);
        headerPanel.add(headerTextPanel, BorderLayout.CENTER);

        // ---- FORM ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        formPanel.setBorder(new EmptyBorder(25, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // Mật khẩu mới
        JLabel lblNew = new JLabel("Mật khẩu mới:");
        lblNew.setFont(labelFont);
        txtNewPassword = new JPasswordField(22);
        txtNewPassword.setFont(fieldFont);
        txtNewPassword.setPreferredSize(new Dimension(280, 36));

        // Xác nhận mật khẩu
        JLabel lblConfirm = new JLabel("Xác nhận mật khẩu:");
        lblConfirm.setFont(labelFont);
        txtConfirmPassword = new JPasswordField(22);
        txtConfirmPassword.setFont(fieldFont);
        txtConfirmPassword.setPreferredSize(new Dimension(280, 36));

        // Nút gửi OTP
        btnSendOtp = ComponentUI.createModernButton("Gửi OTP về email", PRIMARY_COLOR, Color.WHITE);
        btnSendOtp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSendOtp.setPreferredSize(new Dimension(280, 38));

        // OTP
        JLabel lblOtp = new JLabel("Nhập mã OTP:");
        lblOtp.setFont(labelFont);
        txtOtp = new JTextField(22);
        txtOtp.setFont(fieldFont);
        txtOtp.setPreferredSize(new Dimension(280, 36));
        txtOtp.setEnabled(false);

        // Nút xác nhận đổi mật khẩu
        btnConfirm = ComponentUI.createModernButton("Xác nhận đổi mật khẩu", PRIMARY_COLOR, Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirm.setPreferredSize(new Dimension(280, 38));
        btnConfirm.setEnabled(false);

        // Layout
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        formPanel.add(lblNew, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        formPanel.add(txtNewPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        formPanel.add(lblConfirm, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        formPanel.add(txtConfirmPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
        formPanel.add(btnSendOtp, gbc);

        gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0.35;
        formPanel.add(lblOtp, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        formPanel.add(txtOtp, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0;
        formPanel.add(btnConfirm, gbc);

        // ---- FOOTER ----
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(BG_COLOR);
        footerPanel.setBorder(new EmptyBorder(5, 0, 15, 0));
        JLabel lblNote = new JLabel("<html><i>* Sau khi đổi mật khẩu, bạn cần đăng nhập lại bằng mật khẩu mới.</i></html>");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNote.setForeground(Color.GRAY);
        footerPanel.add(lblNote);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setPreferredSize(new Dimension(530, 420));
    }

    // =============================================
    // CÁC PHƯƠNG THỨC LẤY DỮ LIỆU
    // =============================================

    public String getNewPassword() {
        return new String(txtNewPassword.getPassword());
    }

    public String getConfirmPassword() {
        return new String(txtConfirmPassword.getPassword());
    }

    public String getOtp() {
        return txtOtp.getText().trim();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    // =============================================
    // CÁC PHƯƠNG THỨC ĐIỀU KHIỂN TRẠNG THÁI
    // =============================================

    /** Bật ô nhập OTP và nút xác nhận sau khi OTP đã được gửi */
    public void onOtpSent() {
        otpSent = true;
        txtOtp.setEnabled(true);
        btnConfirm.setEnabled(true);
        btnSendOtp.setText("Gửi lại OTP");
        txtOtp.requestFocus();
    }

    /** Gọi khi đổi mật khẩu thành công để đóng dialog */
    public void onSuccess() {
        confirmed = true;
        dispose();
    }

    // =============================================
    // ĐĂNG KÝ LISTENER
    // =============================================

    public void addSendOtpListener(ActionListener listener) {
        btnSendOtp.addActionListener(listener);
    }

    public void addConfirmListener(ActionListener listener) {
        btnConfirm.addActionListener(listener);
    }
}
