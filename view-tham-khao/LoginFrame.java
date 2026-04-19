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
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("Quản Lý Cửa Hàng Đồ Uống - The Bang Coffee");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Căn giữa màn hình

        // 1. Tạo Background Panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Đảm bảo bạn có file ảnh này trong thư mục dự án
                ImageIcon imageIcon = new ImageIcon("C:\\Users\\SONY\\Documents\\NetBeansProjects\\DevCoffeeGUI\\images\\backgroundLogin.jpg"); 
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
                // Màu trắng với độ trong suốt (alpha = 200/255)
                g2.setColor(new Color(255, 255, 255, 200)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); // Bo góc 30px
                g2.dispose();
            }
        };
        glassPanel.setOpaque(false);
        glassPanel.setPreferredSize(new Dimension(350, 450));
        glassPanel.setLayout(new BoxLayout(glassPanel, BoxLayout.Y_AXIS));
        glassPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // 3. Thêm các thành phần vào Form
        // Tiêu đề
        JLabel titleLabel = new JLabel("LOGIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("The Bang Coffee");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Trường nhập liệu Username
        JTextField userField = new JTextField();
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        userField.setBorder(BorderFactory.createTitledBorder("Username"));

        // Trường nhập liệu Password
        JPasswordField passField = new JPasswordField();
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passField.setBorder(BorderFactory.createTitledBorder("Password"));

        // Nút Đăng nhập (Custom để có màu xanh như thiết kế)
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginBtn.setBackground(new Color(67, 142, 104)); // Màu xanh lá
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setFocusPainted(false);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Links (Quên mật khẩu / Đăng ký)
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        linksPanel.setOpaque(false);
        JLabel forgotPassLabel = new JLabel("<html><u>Forgot Password?</u></html>");
        JLabel signUpLabel = new JLabel("<html><u>Sign Up</u></html>");
        forgotPassLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linksPanel.add(forgotPassLabel);
        linksPanel.add(signUpLabel);

        // Lắp ráp các thành phần vào Glass Panel
        glassPanel.add(titleLabel);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        glassPanel.add(subtitleLabel);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        glassPanel.add(userField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        glassPanel.add(passField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        glassPanel.add(loginBtn);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        glassPanel.add(linksPanel);

        // Thêm Glass Panel vào Background, rồi thêm vào Frame
        backgroundPanel.add(glassPanel);
        add(backgroundPanel);
    }

    public static void main(String[] args) {
        // Chạy UI trong Event Dispatch Thread để đảm bảo an toàn
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
