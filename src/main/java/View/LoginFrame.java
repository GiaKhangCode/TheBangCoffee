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
    private JLabel forgotPassLabel;
    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    public LoginFrame() {
        setTitle("Quản Lý Cửa Hàng Đồ Uống - The Bang Coffee");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Lấy kích thước màn hình
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Tính toán tỉ lệ chuẩn (giả sử màn hình thiết kế là 1536x864)
        int panelWidth = (int) (screenWidth * 0.28); // Tăng độ rộng form lên 28% màn hình
        int panelHeight = (int) (screenHeight * 0.72); // Tăng chiều cao form lên 72%
        int rightInset = (int) (screenWidth * 0.13); // Tăng lề phải (15%) để đẩy form vào gần giữa hơn
        int fieldHeight = (int) (screenHeight * 0.06); // Tăng chiều cao ô nhập liệu
        int btnHeight = (int) (screenHeight * 0.065); // Tăng chiều cao nút bấm
        
        // Cỡ chữ theo tỉ lệ
        int titleFontSize = Math.max(28, (int) (screenWidth * 0.026)); 
        int buttonFontSize = Math.max(16, (int) (screenWidth * 0.012)); 
        int labelFontSize = Math.max(14, (int) (screenWidth * 0.011)); 
        int fieldFontSize = Math.max(14, (int) (screenWidth * 0.011));
        
        // Khoảng cách theo tỉ lệ
        int padding = (int) (screenWidth * 0.03); // Tăng padding viền
        int gapLarge = (int) (screenHeight * 0.05); // Khoảng cách tiêu đề
        int gapMedium = (int) (screenHeight * 0.04); // Khoảng cách giữa pass và nút
        int gapSmall = (int) (screenHeight * 0.025); // Khoảng cách các textfield
        
        Font titleFont = new Font("Segoe UI", Font.BOLD, titleFontSize);
        Font btnFont = new Font("Segoe UI", Font.BOLD, buttonFontSize);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, fieldFontSize);
        Font linkFont = new Font("Segoe UI", Font.PLAIN, labelFontSize);

        setSize(screenWidth, screenHeight);
        setLocationRelativeTo(null); // Căn giữa màn hình

        // 1. Tạo Background Panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Đảm bảo bạn có file ảnh này trong thư mục dự án
                ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/background3.png"));
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
        glassPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));
        glassPanel.setLayout(new BoxLayout(glassPanel, BoxLayout.Y_AXIS));
        glassPanel.setBorder(new EmptyBorder(padding, padding, padding, padding));

        // 3. Thêm các thành phần vào Form
        // Tiêu đề
        JLabel titleLabel = new JLabel("ĐĂNG NHẬP");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, Math.max(16, titleFontSize - 4)));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Trường nhập liệu Username
        userField = new JTextField();
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldHeight));
        userField.setFont(fieldFont);
        javax.swing.border.TitledBorder userBorder = BorderFactory.createTitledBorder("Tên đăng nhập");
        userBorder.setTitleFont(linkFont);
        userField.setBorder(userBorder);

        // Trường nhập liệu Password
        passField = new JPasswordField();
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldHeight));
        passField.setFont(fieldFont);
        javax.swing.border.TitledBorder passBorder = BorderFactory.createTitledBorder("Mật khẩu");
        passBorder.setTitleFont(linkFont);
        passField.setBorder(passBorder);

        // Nút Đăng nhập (Custom để có màu xanh như thiết kế)
        loginBtn = createModernButton("ĐĂNG NHẬP", PRIMARY_COLOR, Color.WHITE);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnHeight));
        loginBtn.setBackground(new Color(67, 142, 104)); // Màu xanh lá
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(btnFont);
        loginBtn.setFocusPainted(false);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Links (chỉ để lại Quên mật khẩu, đã xóa chức năng tự đăng ký)
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, gapSmall, 0));
        linksPanel.setOpaque(false);
        forgotPassLabel = new JLabel("<html><u>Quên mật khẩu?</u></html>");
        forgotPassLabel.setFont(linkFont);
        forgotPassLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linksPanel.add(forgotPassLabel);

        // Lắp ráp các thành phần vào Glass Panel
        // 1. Thêm khoảng trống co giãn ở trên cùng để đẩy nội dung xuống giữa
        glassPanel.add(Box.createVerticalGlue()); 

        glassPanel.add(titleLabel);
        
        // Tăng khoảng cách từ tiêu đề xuống ô nhập liệu cho thoáng mắt hơn
        glassPanel.add(Box.createRigidArea(new Dimension(0, gapMedium)));  
        
        glassPanel.add(userField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, gapSmall))); 
        
        glassPanel.add(passField);
        glassPanel.add(Box.createRigidArea(new Dimension(0, gapMedium))); 
        
        glassPanel.add(loginBtn);
        glassPanel.add(Box.createRigidArea(new Dimension(0, gapSmall))); 
        
        glassPanel.add(linksPanel);

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
        getRootPane().setDefaultButton(loginBtn);
    }
    public void addLoginListener(ActionListener listener) {
        loginBtn.addActionListener(listener);
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
