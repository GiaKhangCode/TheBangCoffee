package View;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author SONY
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private JButton loginBtn;
    private JTextField userField;
    private JPasswordField passField;
    private JLabel signUpLabel;
    private JLabel forgotPassLabel;
    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    public LoginFrame() {
        setTitle("Quản Lý Cửa Hàng Đồ Uống - The Bang Coffee");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Căn giữa màn hình

        // 1. Tạo Background Panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Đảm bảo bạn có file ảnh này trong thư mục dự án
                ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/background3.jpeg"));
                Image image = imageIcon.getImage();
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout()); // Giúp căn giữa form đăng nhập

        // 2. Tạo Glass Panel (Form đăng nhập)
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

        // 3. Thêm các thành phần vào Form
        // Tiêu đề
        JLabel titleLabel = new JLabel("ĐĂNG NHẬP");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Trường nhập liệu Username
        userField = new JTextField();
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        userField.setBorder(BorderFactory.createTitledBorder("Tên đăng nhập"));

        // Trường nhập liệu Password
        passField = new JPasswordField();
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passField.setBorder(BorderFactory.createTitledBorder("Mật khẩu"));

        // Nút Đăng nhập (Custom để có màu xanh như thiết kế)
        loginBtn = createModernButton("ĐĂNG NHẬP", PRIMARY_COLOR, Color.WHITE);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginBtn.setBackground(new Color(67, 142, 104)); // Màu xanh lá
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setFocusPainted(false);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Links (Quên mật khẩu / Đăng ký)
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        linksPanel.setOpaque(false);
        forgotPassLabel = new JLabel("<html><u>Quên mật khẩu?</u></html>");
        signUpLabel = new JLabel("<html><u>Đăng ký</u></html>");
        forgotPassLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linksPanel.add(forgotPassLabel);
        linksPanel.add(signUpLabel);

        // Lắp ráp các thành phần vào Glass Panel
//        glassPanel.add(titleLabel);
//        glassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
//        glassPanel.add(Box.createRigidArea(new Dimension(0, 30)));
//        glassPanel.add(userField);
//        glassPanel.add(Box.createRigidArea(new Dimension(0, 20)));
//        glassPanel.add(passField);
//        glassPanel.add(Box.createRigidArea(new Dimension(0, 30)));
//        glassPanel.add(loginBtn);
//        glassPanel.add(Box.createRigidArea(new Dimension(0, 20)));
//        glassPanel.add(linksPanel);

          // 1. Thêm khoảng trống co giãn ở trên cùng để đẩy nội dung xuống giữa
        glassPanel.add(Box.createVerticalGlue()); 

        glassPanel.add(titleLabel);
        
        // Tăng khoảng cách từ tiêu đề xuống ô nhập liệu cho thoáng mắt hơn (đổi từ 30 -> 40)
        glassPanel.add(Box.createRigidArea(new Dimension(0, 40))); 
        
        glassPanel.add(userField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 20))); 
        
        glassPanel.add(passField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 30))); 
        
        glassPanel.add(loginBtn);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 20))); 
        
        glassPanel.add(linksPanel);

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
        getRootPane().setDefaultButton(loginBtn);
    }
    public void addLoginListener(ActionListener listener) {
        loginBtn.addActionListener(listener);
    }
    public void addSignUpListener(java.awt.event.MouseListener listener) {
        signUpLabel.addMouseListener(listener);
    }
    public void addForgotPasswordListener(java.awt.event.MouseListener listener) {
        forgotPassLabel.addMouseListener(listener);
    }
    public String getUsername() {
        return userField.getText();
    }

    public String getPassword() {
        return new String(passField.getPassword());
    }
    @Override
    public void setVisible(boolean b) {
        if (b) {
            resetFields();
        }
        super.setVisible(b);
    }

    public void resetFields() {
        userField.setText("");
        passField.setText("");
    }
    
    public static void main(String[] args) {
        // Chạy UI trong Event Dispatch Thread để đảm bảo an toàn
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
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
