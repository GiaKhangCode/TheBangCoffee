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
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class HomeFrame extends JFrame {

    public HomeFrame() {
        setTitle("The Chill Drinks & Cafe - Trang Chủ");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Tạo Background Panel (Nền ảnh 3D)
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon imageIcon = new ImageIcon("background.jpg"); // Nhớ thay bằng ảnh của bạn
                Image image = imageIcon.getImage();
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        // 2. Tạo Glass Panel (Khung kính mờ tổng)
        JPanel glassPanel = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 220)); // Kính trắng mờ
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        glassPanel.setOpaque(false);
        glassPanel.setPreferredSize(new Dimension(1100, 650));
        glassPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 3. Xây dựng Thanh điều hướng (Sidebar) bên trái
        JPanel sidebar = createSidebar();
        glassPanel.add(sidebar, BorderLayout.WEST);

        // 4. Xây dựng Khu vực nội dung chính (Main Dashboard) ở giữa
        JPanel mainContent = createMainContent();
        glassPanel.add(mainContent, BorderLayout.CENTER);

        // Lắp ráp
        backgroundPanel.add(glassPanel);
        add(backgroundPanel);
    }

    // --- CÁC HÀM HỖ TRỢ TẠO GIAO DIỆN ---

    // 1. Hàm tạo Sidebar mới
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        
        // SỬA Ở ĐÂY: Tăng chiều rộng lên 260 (trước đó là 220)
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10)); 

        // Tiêu đề Sidebar
        JLabel brandLabel = new JLabel("The Chill Drinks");
        // SỬA Ở ĐÂY: Có thể giảm cỡ chữ xuống 18 hoặc 19 để an toàn không bị cắt
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 19)); 
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandLabel.setForeground(new Color(40, 40, 40)); 
        
        sidebar.add(brandLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        // Các nút Menu
        String[] menuItems = {"Bán hàng", "Menu đồ uống", "Quản lý kho", "Quản lý nhân viên", "Báo cáo & Thống kê", "Cài đặt"};
        for (String item : menuItems) {
            JButton btn = createMenuButton(item);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 8))); 
        }
        return sidebar;
    }
    
    // 2. Hàm thiết kế nút Menu phẳng và có hiệu ứng (Hover)
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setHorizontalAlignment(SwingConstants.LEFT); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(new Color(80, 80, 80)); 
        
        // FIX LỖI CHỒNG CHỮ TẠI ĐÂY:
        btn.setFocusPainted(false);  
        btn.setBorderPainted(false); 
        btn.setContentAreaFilled(false); 
        btn.setOpaque(false); // Mặc định phải là FALSE (trong suốt)
        
        btn.setBorder(new EmptyBorder(10, 15, 10, 10)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Sự kiện chuột (Hover) đã được sửa lại logic
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Biến thành khối đặc để hiện màu nền
                btn.setOpaque(true); 
                btn.setBackground(new Color(230, 240, 235)); 
                btn.setForeground(new Color(67, 142, 104));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Trả lại trạng thái trong suốt ban đầu
                btn.setOpaque(false); 
                btn.setForeground(new Color(80, 80, 80)); 
            }
        });

        return btn;
    }

    private JPanel createMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setOpaque(false);

        // Header: Nút tác vụ nhanh & Info người dùng
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Tổng quan hôm nay");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        quickActions.setOpaque(false);
        JButton btnNewOrder = new JButton("Tạo đơn hàng mới +");
        btnNewOrder.setBackground(new Color(67, 142, 104));
        btnNewOrder.setForeground(Color.WHITE);
        quickActions.add(btnNewOrder);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(quickActions, BorderLayout.EAST);

        // Center: Thẻ thống kê & Biểu đồ
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        // Thẻ thống kê (Stats Cards)
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatCard("Đơn hàng mới", "125"));
        statsPanel.add(createStatCard("Doanh thu", "15,400,000 VND"));
        statsPanel.add(createStatCard("Bán chạy nhất", "Cà phê cốt dừa"));
        centerPanel.add(statsPanel, BorderLayout.NORTH);

        // Placeholder cho Biểu đồ
        JPanel chartPanel = new JPanel();
        chartPanel.setBackground(new Color(230, 240, 235));
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        chartPanel.add(new JLabel("Khu vực hiển thị Biểu đồ (Dùng JFreeChart để tích hợp)"));
        centerPanel.add(chartPanel, BorderLayout.CENTER);

        // Bottom: Bảng đơn hàng gần đây
        String[] columnNames = {"ID", "Khách hàng", "Thời gian", "Trạng thái", "Tổng tiền"};
        Object[][] data = {
                {"484", "Khách lẻ", "30/03/2026 14:30", "Hoàn thành", "150,000"},
                {"485", "Nguyễn Văn B", "30/03/2026 15:00", "Đang pha chế", "85,000"}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        JTable table = new JTable(model);
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(0, 150));

        // Lắp ráp Main Content
        mainContent.add(headerPanel, BorderLayout.NORTH);
        mainContent.add(centerPanel, BorderLayout.CENTER);
        mainContent.add(scrollPane, BorderLayout.SOUTH);

        return mainContent;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(Color.GRAY);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));

        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(lblValue);
        return card;
    }

    public static void main(String[] args) {
        // FlatLightLaf.setup(); // BỎ COMMENT DÒNG NÀY NẾU BẠN ĐÃ CÀI FLATLAF
        SwingUtilities.invokeLater(() -> {
            new HomeFrame().setVisible(true);
        });
    }
}
