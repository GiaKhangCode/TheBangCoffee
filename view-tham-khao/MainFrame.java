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

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContentCardPanel;
    private SidebarPanel sidebarPanel;
    private SalesPanel salesPanel;

    public MainFrame() {
        setTitle("The Chill Drinks & Cafe - Hệ Thống Quản Lý");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Background Panel (3D Background)
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon imageIcon = new ImageIcon("images/background.jpg");
                if (imageIcon.getImage() != null) {
                    g.drawImage(imageIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // 2. Glass Panel (Main Translucent Container)
        JPanel glassPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 220)); // White translucent
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        glassPanel.setOpaque(false);
        glassPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Outer margin

        // 3. Sidebar (West)
        sidebarPanel = new SidebarPanel(this);
        glassPanel.add(sidebarPanel, BorderLayout.WEST);

        // 4. Main Content (CardLayout) (Center)
        cardLayout = new CardLayout();
        mainContentCardPanel = new JPanel(cardLayout);
        mainContentCardPanel.setOpaque(false);
        // Khởi tạo ReportPanel
        ReportPanel reportPanel = new ReportPanel();

        // Thêm vào CardLayout với tên nhận diện là "REPORT_CARD"
        mainContentCardPanel.add(reportPanel, "REPORT_CARD");
        
        // Khởi tạo InventoryPanel
        InventoryPanel inventoryPanel = new InventoryPanel();

        // Thêm vào CardLayout với tên nhận diện là "INVENTORY_CARD"
        mainContentCardPanel.add(inventoryPanel, "INVENTORY_CARD");

        // 5. Initialize SalesPanel and add to CardLayout
        salesPanel = new SalesPanel();
        mainContentCardPanel.add(salesPanel, "SALES_CARD");

        // (Add other panels for other menu items here)
        mainContentCardPanel.add(new JPanel(), "HOME_CARD");

        glassPanel.add(mainContentCardPanel, BorderLayout.CENTER);

        backgroundPanel.add(glassPanel);
        add(backgroundPanel);
    }

    public void showCard(String cardName) {
        cardLayout.show(mainContentCardPanel, cardName);
    }
}
