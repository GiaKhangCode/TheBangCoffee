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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Lấy kích thước màn hình
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Tính toán tỉ lệ chuẩn (giả sử màn hình thiết kế là 1536x864)
        int panelWidth = (int) (screenWidth * 0.28); 
        int panelHeight = (int) (screenHeight * 0.72); 
        int rightInset = (int) (screenWidth * 0.13); 
        int fieldHeight = (int) (screenHeight * 0.06); 
        int btnHeight = (int) (screenHeight * 0.065); 
        
        // Cỡ chữ theo tỉ lệ
        int titleFontSize = Math.max(28, (int) (screenWidth * 0.026)); 
        int buttonFontSize = Math.max(16, (int) (screenWidth * 0.012)); 
        int labelFontSize = Math.max(14, (int) (screenWidth * 0.011)); 
        int fieldFontSize = Math.max(14, (int) (screenWidth * 0.011));
        
        // Khoảng cách theo tỉ lệ
        int padding = (int) (screenWidth * 0.03); 
        int gapLarge = (int) (screenHeight * 0.05); 
        int gapMedium = (int) (screenHeight * 0.04); 
        int gapSmall = (int) (screenHeight * 0.025); 
        int gapTiny = (int) (screenHeight * 0.012);
        
        Font titleFont = new Font("Segoe UI", Font.BOLD, titleFontSize);
        Font btnFont = new Font("Segoe UI", Font.BOLD, buttonFontSize);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, fieldFontSize);
        Font linkFont = new Font("Segoe UI", Font.PLAIN, labelFontSize);

        setSize(screenWidth, screenHeight);
        setLocationRelativeTo(null);

        // Background
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/background3.png"));
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
        glassPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));
        glassPanel.setLayout(new BoxLayout(glassPanel, BoxLayout.Y_AXIS));
        glassPanel.setBorder(new EmptyBorder(padding, padding, padding, padding));

        // Title
        JLabel title = new JLabel("QUÊN MẬT KHẨU");
        title.setFont(titleFont);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email
        emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldHeight));
        emailField.setFont(fieldFont);
        javax.swing.border.TitledBorder emailBorder = BorderFactory.createTitledBorder("Email");
        emailBorder.setTitleFont(linkFont);
        emailField.setBorder(emailBorder);

        // OTP
        otpField = new JTextField();
        otpField.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldHeight));
        otpField.setFont(fieldFont);
        javax.swing.border.TitledBorder otpBorder = BorderFactory.createTitledBorder("Mã xác minh");
        otpBorder.setTitleFont(linkFont);
        otpField.setBorder(otpBorder);

        // New Password
        newPassField = new JPasswordField();
        newPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldHeight));
        newPassField.setFont(fieldFont);
        javax.swing.border.TitledBorder passBorder = BorderFactory.createTitledBorder("Mật khẩu mới");
        passBorder.setTitleFont(linkFont);
        newPassField.setBorder(passBorder);

        // Buttons
        sendOtpBtn = createModernButton("GỬI MÃ", PRIMARY_COLOR, Color.WHITE);
        sendOtpBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnHeight));
        sendOtpBtn.setBackground(new Color(67, 142, 104));
        sendOtpBtn.setForeground(Color.WHITE);
        sendOtpBtn.setFont(btnFont);
        sendOtpBtn.setFocusPainted(false);
        sendOtpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
         

        confirmBtn = createModernButton("XÁC NHẬN", PRIMARY_COLOR, Color.WHITE);
        confirmBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnHeight));
        confirmBtn.setBackground(new Color(67, 142, 104));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(btnFont);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        backBtn = createModernButton("QUAY LẠI", new Color(240, 240, 240), TEXT_DARK);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnHeight));
        backBtn.setBackground(new Color(200, 200, 200));
        backBtn.setForeground(Color.BLACK);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, buttonFontSize));
        backBtn.setFocusPainted(false);
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components
        glassPanel.add(Box.createVerticalGlue());
        glassPanel.add(title);
        glassPanel.add(Box.createRigidArea(new Dimension(0, gapLarge)));
        
        glassPanel.add(emailField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, gapTiny)));
        
        glassPanel.add(sendOtpBtn);
        glassPanel.add(Box.createRigidArea(new Dimension(0, gapMedium)));
        
        glassPanel.add(otpField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, gapTiny)));
        
        glassPanel.add(newPassField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, gapMedium)));
        
        glassPanel.add(confirmBtn);
        glassPanel.add(Box.createRigidArea(new Dimension(0, gapTiny)));
        
        glassPanel.add(backBtn);

         // 2. Thêm khoảng trống co giãn ở dưới cùng để giữ nội dung ở giữa
        glassPanel.add(Box.createVerticalGlue());

        // 4. Tạo Panel chứa chữ ở bên trái (dưới ly cà phê)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel brandLabelLeft = new JLabel("THE BANG COFFEE");
        brandLabelLeft.setFont(new Font("Segoe UI", Font.BOLD, titleFontSize + 16)); 
        brandLabelLeft.setForeground(Color.WHITE);
        brandLabelLeft.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeLabelLeft = new JLabel("CHÀO MỪNG TRỞ LẠI!");
        welcomeLabelLeft.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(18, titleFontSize - 2)));
        welcomeLabelLeft.setForeground(Color.WHITE);
        welcomeLabelLeft.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(brandLabelLeft);
        textPanel.add(Box.createRigidArea(new Dimension(0, gapSmall)));
        textPanel.add(welcomeLabelLeft);

        GridBagConstraints gbcText = new GridBagConstraints();
        gbcText.gridx = 0;
        gbcText.gridy = 0;
        gbcText.weightx = 1.0;
        gbcText.weighty = 1.0;
        gbcText.anchor = GridBagConstraints.CENTER;
        // Đẩy panel xuống một chút để nằm dưới hình ly cà phê, và lệch nhẹ sang trái cho cân với ảnh
        gbcText.insets = new Insets((int)(screenHeight * 0.15), 0, 0, (int)(screenWidth * 0.05)); 
        backgroundPanel.add(textPanel, gbcText);

        // Cài đặt ràng buộc để đẩy form sang phải
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.0; // Không chiếm khoảng trắng thừa để giữ đúng kích thước form
        gbc.weighty = 1.0; 
        gbc.anchor = GridBagConstraints.EAST; // Căn lề về phía Đông (bên phải màn hình)
        
        // Căn lề phải theo tỉ lệ màn hình
        gbc.insets = new Insets(0, 0, 0, rightInset); 
        
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