/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package View;

/**
 *
 * @author FAKK
 */

import static View.AppColor.TEXT_DARK;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class ForgotPasswordFrame extends JFrame {
    private JTextField emailField;
    private JTextField otpField;
    private JPasswordField newPassField;
    private JButton sendOtpBtn;
    private JButton confirmBtn;
    private JButton backBtn;
    private final Color PRIMARY_COLOR = new Color(67, 142, 104);

    public ForgotPasswordFrame() {
        setTitle("Quên Mật Khẩu - The Bang Coffee");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // full screen
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Background
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/background3.jpeg"));
                Image image = imageIcon.getImage();
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        // Glass panel
        JPanel glassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(67, 142, 104, 25)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };

        glassPanel.setOpaque(false);
        glassPanel.setPreferredSize(new Dimension(350, 550));
        glassPanel.setLayout(new BoxLayout(glassPanel, BoxLayout.Y_AXIS));
        glassPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Title
        JLabel title = new JLabel("QUÊN MẬT KHẨU");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email
        emailField = new JTextField();
        emailField.setBorder(BorderFactory.createTitledBorder("Email"));
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // OTP
        otpField = new JTextField();
        otpField.setBorder(BorderFactory.createTitledBorder("Mã xác minh"));
        otpField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // New Password
        newPassField = new JPasswordField();
        newPassField.setBorder(BorderFactory.createTitledBorder("Mật khẩu mới"));
        newPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Buttons
        sendOtpBtn = createModernButton("GỬI MÃ", PRIMARY_COLOR, Color.WHITE);
        sendOtpBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        sendOtpBtn.setBackground(new Color(67, 142, 104));
        sendOtpBtn.setForeground(Color.WHITE);
        sendOtpBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendOtpBtn.setFocusPainted(false);
        sendOtpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
         

        confirmBtn = createModernButton("XÁC NHẬN", PRIMARY_COLOR, Color.WHITE);
        confirmBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        confirmBtn.setBackground(new Color(67, 142, 104));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        backBtn = createModernButton("QUAY LẠI", new Color(240, 240, 240), TEXT_DARK);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        backBtn.setBackground(new Color(200, 200, 200));
        backBtn.setForeground(Color.BLACK);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setFocusPainted(false);
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Align buttons
        sendOtpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components
        glassPanel.add(title);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        glassPanel.add(emailField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        glassPanel.add(sendOtpBtn);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        glassPanel.add(otpField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        glassPanel.add(newPassField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        glassPanel.add(confirmBtn);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        glassPanel.add(backBtn);

         // 2. Thêm khoảng trống co giãn ở dưới cùng để giữ nội dung ở giữa
        glassPanel.add(Box.createVerticalGlue());

       // Cài đặt ràng buộc để đẩy form sang phải
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0; // Yêu cầu lấy toàn bộ không gian chiều ngang còn trống
        gbc.weighty = 1.0; // Yêu cầu lấy toàn bộ không gian chiều dọc còn trống
        gbc.anchor = GridBagConstraints.EAST; // Căn lề về phía Đông (bên phải màn hình)
        
        // Căn lề: trên, trái, dưới, phải. 
        // Đặt lề phải là 150px (bạn có thể thay đổi số này cho vừa vặn với vùng màu trắng)
        gbc.insets = new Insets(0, 0, 0, 150); 
        
         // Thêm Glass Panel vào Background với ràng buộc (gbc)
        backgroundPanel.add(glassPanel, gbc);
        add(backgroundPanel);
        getRootPane().setDefaultButton(confirmBtn);
    }

    // ===== Getter =====
    public String getEmail() {
        return emailField.getText();
    }

    public String getOtp() {
        return otpField.getText();
    }

    public String getNewPassword() {
        return new String(newPassField.getPassword());
    }

    // ===== Listener =====
    public void addSendOtpListener(ActionListener l) {
        sendOtpBtn.addActionListener(l);
    }

    public void addConfirmListener(ActionListener l) {
        confirmBtn.addActionListener(l);
    }

    public void addBackListener(ActionListener l) {
        backBtn.addActionListener(l);
    }

    // Reset
    public void resetFields() {
        emailField.setText("");
        otpField.setText("");
        newPassField.setText("");
    }

    @Override
    public void setVisible(boolean b) {
        if (b) resetFields();
        super.setVisible(b);
    }
    
         private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}