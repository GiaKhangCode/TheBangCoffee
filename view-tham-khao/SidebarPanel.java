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
import java.util.ArrayList;
import java.util.List;

public class SidebarPanel extends JPanel {
    private MainFrame mainFrame;
    private List<JButton> menuButtons;

    public SidebarPanel(MainFrame frame) {
        this.mainFrame = frame;
        this.menuButtons = new ArrayList<>();
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setPreferredSize(new Dimension(280, 0)); // Chiều rộng Sidebar
        setBorder(new EmptyBorder(30, 20, 30, 20)); // Căn lề trong (Trên, Trái, Dưới, Phải)

        // --- 1. PHẦN LOGO / TIÊU ĐỀ (TOP) ---
        JLabel brandLabel = new JLabel("<html><b>The Chill Drinks</b></html>");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandLabel.setForeground(new Color(40, 40, 40));
        brandLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // Căn lề trái
        add(brandLabel);

        JLabel subLabel = new JLabel("Management System");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(new Color(120, 120, 120));
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(subLabel);

        add(Box.createRigidArea(new Dimension(0, 40))); // Khoảng cách từ Logo tới Menu

        // --- 2. PHẦN CÁC NÚT MENU (MIDDLE) ---
        String[] menuItems = {"Bán hàng", "Menu đồ uống", "Quản lý kho", "Quản lý nhân viên", "Báo cáo & Thống kê", "Cài đặt"};
        String[] cardNames = {"SALES_CARD", "HOME_CARD", "INVENTORY_CARD", "HOME_CARD", "REPORT_CARD", "HOME_CARD"};

        for (int i = 0; i < menuItems.length; i++) {
            JButton btn = createMenuButton(menuItems[i], cardNames[i]);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT); // Ép nút căn lề trái
            add(btn);
            add(Box.createRigidArea(new Dimension(0, 10))); // Khoảng cách giữa các nút
            
            // Mặc định chọn nút "Bán hàng" lúc mới mở
            if (menuItems[i].equals("Bán hàng")) {
                btn.setForeground(new Color(67, 142, 104)); 
            }
        }

        // --- LÒ XO ĐẨY XUỐNG ĐÁY ---
        add(Box.createVerticalGlue()); // Nó sẽ chiếm toàn bộ khoảng trống thừa, đẩy phần User xuống đáy

        // --- 3. PHẦN THÔNG TIN USER & ĐĂNG XUẤT (BOTTOM) ---
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setOpaque(false);
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("Nguyễn Văn A (Thu ngân)");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(new Color(60, 60, 60));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton logoutBtn = new JButton("Đăng xuất");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutBtn.setForeground(new Color(220, 80, 80)); // Màu đỏ nhạt cho nút thoát
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setBorder(new EmptyBorder(5, 0, 0, 0)); // Xóa lề trái để thẳng hàng chữ

        // Hiệu ứng hover cho nút Đăng xuất
        logoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutBtn.setForeground(Color.RED);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutBtn.setForeground(new Color(220, 80, 80));
            }
        });

        userPanel.add(userLabel);
        userPanel.add(logoutBtn);
        add(userPanel);
    }

    // --- CÁC HÀM XỬ LÝ NÚT MENU (Đã fix lỗi bóng ma và bo góc) ---

    private JButton createMenuButton(String text, String cardName) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover() || isHighlighted(this)) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isHighlighted(this)) {
                        g2.setColor(new Color(67, 142, 104, 40)); 
                    } else {
                        g2.setColor(new Color(230, 240, 235)); 
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); 
                    g2.dispose();
                }
                super.paintComponent(g); 
            }
        };

        btn.setPreferredSize(new Dimension(240, 45)); 
        btn.setMaximumSize(new Dimension(240, 45));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(new Color(100, 100, 100));
        
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false); 
        btn.setBorder(new EmptyBorder(10, 20, 10, 20)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!isHighlighted(btn)) btn.setForeground(new Color(67, 142, 104));
                btn.repaint(); 
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!isHighlighted(btn)) btn.setForeground(new Color(100, 100, 100));
                btn.repaint();
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                highlightButton(btn);
                mainFrame.showCard(cardName);
            }
        });
        menuButtons.add(btn);
        return btn;
    }

    private void highlightButton(JButton btn) {
        for (JButton otherBtn : menuButtons) {
            unhighlightButton(otherBtn);
        }
        btn.setForeground(new Color(67, 142, 104)); 
        btn.repaint(); 
    }

    private void unhighlightButton(JButton btn) {
        btn.setForeground(new Color(100, 100, 100));
        btn.repaint();
    }

    private boolean isHighlighted(JButton btn) {
        return btn.getForeground().equals(new Color(67, 142, 104));
    }
}