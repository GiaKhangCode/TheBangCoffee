package View;

import Model.ProductListModel;
import Model.ProductModel;
import Controller.CategoryController;
import Controller.ToppingController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class MenuPanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);

    private JTabbedPane tabbedPane;
    private JPanel productTab;
    private JPanel gridPanel;
    private JButton btnAddProduct;
    private JTextField txtSearch;
    private ImageIcon defaultImage;
    private ProductClickListener productClickListener;

    private CategoryManagementPanel categoryPanel;
    private ToppingManagementPanel toppingPanel;
    
    // [MỚI] Thêm 2 Panel mới
    private CustomerManagementPanel customerPanel;
    private LoyaltyManagementPanel loyaltyPanel;
    
    private MainFrame mainFrame;

    public MenuPanel() {
        // Khởi tạo trống
    }
    
    public void setupPanels(MainFrame frame) {
        this.mainFrame = frame;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE); 
        setOpaque(true);
        setBorder(new EmptyBorder(10, 10, 10, 10)); 
        loadDefaultImage();

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(TEXT_DARK);
        
        // Khởi tạo Tab 1: Đồ uống
        initProductTab();
        
        // Khởi tạo Tab 2: Danh mục
        categoryPanel = new CategoryManagementPanel();
        try {
            new CategoryController(categoryPanel, this.mainFrame);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Khởi tạo Tab 3: Topping
        toppingPanel = new ToppingManagementPanel();
        try {
            new ToppingController(toppingPanel, this.mainFrame);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // [MỚI] Khởi tạo Tab 4 & 5: Khách hàng và Tích điểm
        customerPanel = new CustomerManagementPanel();
        loyaltyPanel = new LoyaltyManagementPanel();

        tabbedPane.addTab("Quản Lý Đồ Uống", productTab);
        tabbedPane.addTab("Quản Lý Danh Mục", categoryPanel);
        tabbedPane.addTab("Quản Lý Topping", toppingPanel);

        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void initProductTab() {
        productTab = new JPanel(new BorderLayout(0, 10));
        productTab.setBackground(Color.WHITE);
        productTab.setOpaque(true);
        productTab.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setOpaque(true);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(Color.WHITE);

        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setText("Tìm kiếm tên món...");
        txtSearch.setForeground(Color.GRAY);

        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Tìm kiếm tên món...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(TEXT_DARK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setText("Tìm kiếm tên món...");
                }
            }
        });
        leftPanel.add(txtSearch);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(Color.WHITE);

        btnAddProduct = Common.ComponentUI.createModernButton("+ Thêm Món Mới", PRIMARY_COLOR, Color.WHITE);
        btnAddProduct.setPreferredSize(new Dimension(160, 40));

        rightPanel.add(btnAddProduct);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        gridPanel = new ScrollablePanel(new GridLayout(0, 4, 15, 15)); 
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setOpaque(true);
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        productTab.add(headerPanel, BorderLayout.NORTH);
        productTab.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadDefaultImage() {
        try {
            URL imgUrl = getClass().getResource("/images/backgroundLogin.jpg");
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image img = originalIcon.getImage().getScaledInstance(90, 135, Image.SCALE_SMOOTH);
                defaultImage = new ImageIcon(img);
            } else {
                defaultImage = createPlaceholderIcon(90, 135);
            }
        } catch (Exception e) {
            defaultImage = createPlaceholderIcon(90, 135);
        }
    }

    private ImageIcon createPlaceholderIcon(int width, int height) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(230, 230, 230));
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.GRAY);
        g2.drawString("No Image", width / 2 - 25, height / 2);
        g2.dispose();
        return new ImageIcon(img);
    }

    private class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel(LayoutManager layout) { super(layout); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return visibleRect.height; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    public void displayProductList(ProductListModel list) {
        gridPanel.removeAll();
        for (ProductModel p : list.getProductList()) {
            gridPanel.add(createProductCard(p));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createProductCard(ProductModel product) {
        JPanel card = new JPanel(new BorderLayout(0, 15)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };

        card.setBackground(Color.WHITE);
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true)); }
            public void mouseExited(MouseEvent e) { card.setBorder(new EmptyBorder(10, 10, 10, 10)); }
            public void mouseClicked(MouseEvent e) {
                if (productClickListener != null) {
                    productClickListener.onClick(product);
                }
            }
        });

        ImageIcon icon = defaultImage;
        if (product.getImageData() != null) {
            Image originalImg = product.getImageData().getImage();
            Image scaledImg = originalImg.getScaledInstance(115, 145, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImg);
        }
        JLabel lblImage = new JLabel(icon);
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblName = new JLabel(product.getProductName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel lblCategory = new JLabel(product.getCategoryName());
        lblCategory.setForeground(TEXT_MUTED);

        JLabel lblPrice = new JLabel(String.format("Tại quán: %,d đ", product.getDineInPrice()));
        lblPrice.setForeground(PRIMARY_COLOR);

        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblCategory.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        info.add(lblName);
        info.add(lblCategory);
        info.add(lblPrice);

        card.add(lblImage, BorderLayout.NORTH);
        card.add(info, BorderLayout.CENTER);

        return card;
    }

    public void addAddProductListener(java.awt.event.ActionListener listener) { btnAddProduct.addActionListener(listener); }
    public void setProductClickListener(ProductClickListener listener) { this.productClickListener = listener; }
    public String getSearchText() { return txtSearch.getText(); }
    public void addSearchListener(java.awt.event.KeyListener listener) { txtSearch.addKeyListener(listener); }

    public CategoryManagementPanel getCategoryPanel() { return categoryPanel; }
    public ToppingManagementPanel getToppingPanel() { return toppingPanel; }
    public CustomerManagementPanel getCustomerPanel() { return customerPanel; }
    public LoyaltyManagementPanel getLoyaltyPanel() { return loyaltyPanel; }
    public JButton getBtnAddProduct() { return btnAddProduct; }

    public interface ProductClickListener {
        void onClick(ProductModel product);
    }
}