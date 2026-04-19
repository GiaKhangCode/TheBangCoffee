package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * Giao diện Quản lý Thực đơn (Menu Đồ uống) - Dạng lưới (Card Layout)
 */
public class MenuPanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);

    private JPanel gridPanel;
    private JButton btnAddProduct;
    // Dùng backgroundLogin làm ảnh mẫu
    private ImageIcon defaultImage;

    public MenuPanel() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        loadDefaultImage();
        initHeader();
        initProductGrid();
        loadMockData();
    }

    private void loadDefaultImage() {
        try {
            // Lấy ảnh từ src/main/resources/images/ (Sử dụng backgroundLogin làm mockup)
            URL imgUrl = getClass().getResource("/images/backgroundLogin.jpg");
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image img = originalIcon.getImage().getScaledInstance(180, 150, Image.SCALE_SMOOTH);
                defaultImage = new ImageIcon(img);
            } else {
                defaultImage = createPlaceholderIcon(180, 150);
            }
        } catch (Exception e) {
            defaultImage = createPlaceholderIcon(180, 150);
        }
    }

    private ImageIcon createPlaceholderIcon(int width, int height) {
        // Tạo 1 ảnh xám giữ chỗ nếu không lẫy được hình gốc
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(230, 230, 230));
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.GRAY);
        g2.drawString("No Image", width / 2 - 25, height / 2);
        g2.dispose();
        return new ImageIcon(img);
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Bên trái: Ô tìm kiếm
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        
        JTextField txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm đồ uống...");
        leftPanel.add(txtSearch);

        // Bên phải: Nút Thêm Mới
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);

        btnAddProduct = new JButton("+ Thêm Món Mới");
        btnAddProduct.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddProduct.setForeground(Color.WHITE);
        btnAddProduct.setBackground(PRIMARY_COLOR);
        btnAddProduct.setPreferredSize(new Dimension(160, 40));
        btnAddProduct.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddProduct.setFocusPainted(false);
        btnAddProduct.setBorderPainted(false);
        
        btnAddProduct.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            ProductDetailDialog dialog = new ProductDetailDialog(parentFrame);
            dialog.setVisible(true);
        });

        rightPanel.add(btnAddProduct);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void initProductGrid() {
        // Dùng GridLayout(0, 4) và ScrollablePanel để ép chiều rộng vừa khít màn hình, không bị tràn (Không có thanh cuộn ngang)
        gridPanel = new ScrollablePanel(new GridLayout(0, 4, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // Tắt thanh cuộn ngang

        add(scrollPane, BorderLayout.CENTER);
    }

    // Class con cài đặt trạng thái cuộn để tắt cuộn ngang, giúp các thẻ tự động thu phóng lại nếu màn hình hẹp
    private class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel(LayoutManager layout) {
            super(layout);
        }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return visibleRect.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; } // Bắt buộc panel thu lại vừa JScrollPane
        @Override public boolean getScrollableTracksViewportHeight() { return false; } // Vẫn cho phép cuộn dọc
    }

    private void loadMockData() {
        gridPanel.removeAll();
        
        Object[][] data = {
            {"Cà phê sữa đá", "29,000", "Truyền thống"},
            {"Bạc xỉu 3 tầng", "35,000", "Truyền thống"},
            {"Trà đào cam sả", "45,000", "Trà trái cây"},
            {"Trà vải kiều mạch", "45,000", "Trà trái cây"},
            {"Đá xay việt quất", "55,000", "Đá xay"},
            {"Trà ổi hồng hồng", "40,000", "Trà trái cây"},
            {"Cà phê đen đá", "25,000", "Truyền thống"},
            {"Matcha đá xay", "49,000", "Đá xay"},
            {"Trà hạt sen", "39,000", "Trà trái cây"},
            {"Cacao nóng", "35,000", "Cổ điển"}
        };

        for (Object[] row : data) {
            String name = (String) row[0];
            String price = (String) row[1];
            String category = (String) row[2];
            
            gridPanel.add(createProductCard(name, price, category));
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createProductCard(String name, String price, String category) {
        JPanel card = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setPreferredSize(new Dimension(220, 280));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover Effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true),
                    new EmptyBorder(13, 13, 13, 13)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(new EmptyBorder(15, 15, 15, 15));
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Ví dụ khi click vào món thì cũng mở detail lên (edit mode)
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(card);
                ProductDetailDialog dialog = new ProductDetailDialog(parentFrame);
                dialog.setVisible(true);
            }
        });

        // Ảnh món nước (đã bo tròn một phần nếu có thể, tạm dùng icon phẳng)
        JLabel lblImage = new JLabel(defaultImage);
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);

        // Cụm Thông tin (Tên, Thể loại, Giá)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblName.setForeground(TEXT_DARK);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblCategory = new JLabel(category);
        lblCategory.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblCategory.setForeground(TEXT_MUTED);
        lblCategory.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPrice = new JLabel(price + " đ");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPrice.setForeground(PRIMARY_COLOR);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPrice.setBorder(new EmptyBorder(8, 0, 0, 0));

        infoPanel.add(lblName);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(lblCategory);
        infoPanel.add(lblPrice);

        card.add(lblImage, BorderLayout.NORTH);
        card.add(infoPanel, BorderLayout.CENTER);

        return card;
    }
}
