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

public class RegisterFrame extends JFrame {
    private JTextField nameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton signUpBtn;
    private JButton backBtn;
    private JTextField emailField;
    private JTextField phoneField;
    //private JLabel forgotPassLabel;
    private final Color PRIMARY_COLOR = new Color(67, 142, 104);

    public RegisterFrame() {
        setTitle("Đăng Ký - The Bang Coffee");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Background panel với ảnh
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

        // Glass panel (form đăng ký)
        JPanel glassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                g2.setColor(new Color(255, 255, 255, 200));
//                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(67, 142, 104, 25)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        glassPanel.setOpaque(false);
        glassPanel.setPreferredSize(new Dimension(350, 550));
        glassPanel.setLayout(new BoxLayout(glassPanel, BoxLayout.Y_AXIS));
        glassPanel.setBorder(new EmptyBorder(50, 60, 50, 60));

        // Tiêu đề
        JLabel titleLabel = new JLabel("ĐĂNG KÝ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Trường nhập liệu Họ Tên
        nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        nameField.setBorder(BorderFactory.createTitledBorder("Họ Tên"));

        // Trường nhập liệu Username
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setBorder(BorderFactory.createTitledBorder("Tên Đăng Nhập"));

        // Trường nhập liệu Password
        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setBorder(BorderFactory.createTitledBorder("Mật Khẩu"));

        // Nút Đăng ký
        signUpBtn = createModernButton("ĐĂNG KÝ", PRIMARY_COLOR, Color.WHITE);
        signUpBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        signUpBtn.setBackground(new Color(67, 142, 104));
        signUpBtn.setForeground(Color.WHITE);
        signUpBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        signUpBtn.setFocusPainted(false);
        signUpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nút quay về Login
        backBtn = createModernButton("QUAY LẠI", new Color(240, 240, 240), TEXT_DARK);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        backBtn.setBackground(new Color(200, 200, 200));
        backBtn.setForeground(Color.BLACK);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setFocusPainted(false);
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Email
        emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        emailField.setBorder(BorderFactory.createTitledBorder("Email"));

        // SĐT
        phoneField = new JTextField();
        phoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        phoneField.setBorder(BorderFactory.createTitledBorder("Số điện thoại"));

        // Thêm các thành phần vào glass panel
        glassPanel.add(titleLabel);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        glassPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        glassPanel.add(nameField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        glassPanel.add(emailField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        glassPanel.add(phoneField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        glassPanel.add(usernameField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        glassPanel.add(passwordField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        glassPanel.add(signUpBtn);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        glassPanel.add(backBtn);
        glassPanel.add(Box.createRigidArea(new Dimension(0, 20)));

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
        getRootPane().setDefaultButton(signUpBtn);
        //getRootPane().setDefaultButton(backBtn);
    }

    // Lắng nghe nút Sign Up
    public void addSignUpListener(ActionListener listener) {
        signUpBtn.addActionListener(listener);
    }

    // Lắng nghe nút Back
    public void addBackListener(ActionListener listener) {
        backBtn.addActionListener(listener);
    }
//    public void addForgotPasswordListener(java.awt.event.MouseListener listener) {
//        forgotPassLabel.addMouseListener(listener);
//    }
    public String getFullName() {
        return nameField.getText();
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }
    public String getEmail() {
        return emailField.getText();
    }

    public String getPhone() {
        return phoneField.getText();
    }
    
    @Override
    public void setVisible(boolean b) {
        if (b) {
            resetFields();
        }
        super.setVisible(b);
    }

    public void resetFields() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RegisterFrame().setVisible(true);
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
