package View;

import Model.CartItemModel;
import Model.ProductModel;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class PosPanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);
    private final Color BG_LIGHT = new Color(248, 249, 250);

    // --- LEFT PANEL (MENU) ---
    private JPanel menuContainer;
    private JTextField txtSearch;
    private JPanel categoryPanel;
    private JPanel productGridPanel;
    private ImageIcon defaultImage; 

    // --- RIGHT PANEL (CART) ---
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    
    // Các biến UI cho Khách hàng thành viên
    private JTextField txtCustomerPhone;
    private JTextField txtCustomerName;
    private JLabel lblCustomerInfo;
    private JButton btnCheckCustomer;
    private JButton btnRegisterCustomer; 
    private JButton btnClearCustomer;    
    private JPanel nameActionPanel;      

    // Các biến UI cho Tùy chọn đơn hàng
    private JRadioButton rbDineIn;
    private JRadioButton rbTakeaway;
    private JCheckBox chkHoliday;
    
    private JLabel lblSubTotal;
    private JLabel lblVat;
    private JLabel lblTotal;
    private JButton btnClearCart;
    private JButton btnCreateOrder;
    
    // Listeners
    private DeleteActionListener cartDeleteListener;

    public PosPanel() {
        loadDefaultImage(); 
        initComponents();
    }

    private void loadDefaultImage() {
        try {
            URL imgUrl = getClass().getResource("/images/backgroundLogin.jpg");
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image img = originalIcon.getImage().getScaledInstance(115, 145, Image.SCALE_SMOOTH);
                defaultImage = new ImageIcon(img);
            } else {
                defaultImage = createPlaceholderIcon(115, 145);
            }
        } catch (Exception e) {
            defaultImage = createPlaceholderIcon(115, 145);
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

    private void initComponents() {
        setLayout(new BorderLayout(15, 0));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int rightPanelWidth = (int) (screenSize.width * 0.28); 

        add(createLeftPanel(), BorderLayout.CENTER);
        add(createRightPanel(rightPanelWidth), BorderLayout.EAST);
    }

    // ==========================================================
    // KHU VỰC BÊN TRÁI: DANH SÁCH MÓN
    // ==========================================================
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        JPanel topFilterPanel = new JPanel(new BorderLayout(0, 10)); 
        topFilterPanel.setOpaque(false);
        topFilterPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        categoryPanel.setBackground(Color.WHITE); 
        categoryPanel.setOpaque(false);

        JScrollPane scrollCategory = new JScrollPane(categoryPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
            @Override
            public void setBorder(Border border) {} 
        };
        scrollCategory.setPreferredSize(new Dimension(0, 60)); 
        scrollCategory.getHorizontalScrollBar().setUnitIncrement(16);
        scrollCategory.setBackground(Color.WHITE); 
        scrollCategory.getViewport().setBackground(Color.WHITE); 

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(400, 40)); 
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(5, 15, 5, 15)));
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

        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchWrapper.setOpaque(false);
        searchWrapper.add(txtSearch);

        topFilterPanel.add(scrollCategory, BorderLayout.NORTH);
        topFilterPanel.add(searchWrapper, BorderLayout.CENTER);
        
        productGridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        productGridPanel.setBackground(Color.WHITE);

        JScrollPane scrollGrid = new JScrollPane(productGridPanel);
        scrollGrid.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
        scrollGrid.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(topFilterPanel, BorderLayout.NORTH);
        panel.add(scrollGrid, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================================
    // KHU VỰC KHÁCH HÀNG THÀNH VIÊN
    // ==========================================================
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(new Color(230, 230, 230)), "Khách Hàng Thành Viên",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), PRIMARY_COLOR
        ));

        JPanel phonePanel = new JPanel(new BorderLayout(5, 0));
        phonePanel.setOpaque(false);
        txtCustomerPhone = new JTextField();
        txtCustomerPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCustomerPhone.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(5, 10, 5, 10)));
        txtCustomerPhone.setText("Nhập SĐT...");
        txtCustomerPhone.setForeground(Color.GRAY);
        
        btnCheckCustomer = createModernButton("Tìm", PRIMARY_COLOR, Color.WHITE);
        btnCheckCustomer.setPreferredSize(new Dimension(60, 30));
        btnCheckCustomer.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        txtCustomerPhone.addActionListener(e -> btnCheckCustomer.doClick());

        txtCustomerPhone.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtCustomerPhone.getText().equals("Nhập SĐT...")) {
                    txtCustomerPhone.setText("");
                    txtCustomerPhone.setForeground(TEXT_DARK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtCustomerPhone.getText().isEmpty()) {
                    txtCustomerPhone.setForeground(Color.GRAY);
                    txtCustomerPhone.setText("Nhập SĐT...");
                }
            }
        });

        phonePanel.add(txtCustomerPhone, BorderLayout.CENTER);
        phonePanel.add(btnCheckCustomer, BorderLayout.EAST);

        JPanel infoPanel = new JPanel(new BorderLayout(5, 0));
        infoPanel.setOpaque(false);
        
        JPanel activeCustomerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        activeCustomerPanel.setOpaque(false);
        
        lblCustomerInfo = new JLabel("Khách vãng lai");
        lblCustomerInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCustomerInfo.setForeground(TEXT_MUTED);

        btnClearCustomer = createModernButton("X", new Color(231, 76, 60), Color.WHITE);
        btnClearCustomer.setPreferredSize(new Dimension(38, 22));
        btnClearCustomer.setBorder(new EmptyBorder(0, 0, 0, 0));
        btnClearCustomer.setVisible(false);
        
        activeCustomerPanel.add(lblCustomerInfo);
        activeCustomerPanel.add(btnClearCustomer);

        nameActionPanel = new JPanel(new BorderLayout(5, 0));
        nameActionPanel.setOpaque(false);

        txtCustomerName = new JTextField();
        txtCustomerName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCustomerName.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(5, 10, 5, 10)));
        txtCustomerName.setText("Nhập tên khách...");
        txtCustomerName.setForeground(Color.GRAY);
        
        txtCustomerName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtCustomerName.getText().equals("Nhập tên khách...")) {
                    txtCustomerName.setText("");
                    txtCustomerName.setForeground(TEXT_DARK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtCustomerName.getText().isEmpty()) {
                    txtCustomerName.setForeground(Color.GRAY);
                    txtCustomerName.setText("Nhập tên khách...");
                }
            }
        });

        btnRegisterCustomer = createModernButton("Đăng ký", new Color(41, 128, 185), Color.WHITE);
        btnRegisterCustomer.setPreferredSize(new Dimension(80, 30));
        btnRegisterCustomer.setBorder(new EmptyBorder(5, 10, 5, 10));

        nameActionPanel.add(txtCustomerName, BorderLayout.CENTER);
        nameActionPanel.add(btnRegisterCustomer, BorderLayout.EAST);
        nameActionPanel.setVisible(false);

        infoPanel.add(activeCustomerPanel, BorderLayout.NORTH); 
        infoPanel.add(nameActionPanel, BorderLayout.CENTER);

        panel.add(phonePanel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================================
    // KHU VỰC BÊN PHẢI: GIỎ HÀNG & THANH TOÁN
    // ==========================================================
    private JPanel createRightPanel(int width) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(width, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel lblTitle = new JLabel("HÓA ĐƠN HIỆN TẠI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblTitle, BorderLayout.NORTH);

        // [SỬA LẠI] Cột SL bị khóa lại, chỉ mở cột 3 (Xóa)
        String[] cols = {"Món", "SL", "Giá", "Xóa"};
        cartTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; 
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(70);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        cartTable.getTableHeader().setBackground(new Color(245, 245, 245));
        cartTable.setShowVerticalLines(false);
        cartTable.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30));

        TableColumnModel tcm = cartTable.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(170); 
        tcm.getColumn(1).setPreferredWidth(20);  
        tcm.getColumn(2).setPreferredWidth(80);  
        tcm.getColumn(3).setPreferredWidth(50);  
        
        TableColumn delCol = cartTable.getColumnModel().getColumn(3);
        delCol.setCellRenderer(new DeleteActionButtonRenderer(new DeleteActionPanel()));
        delCol.setCellEditor(new DeleteActionButtonEditor(row -> {
            if (cartTable.isEditing()) {
                cartTable.getCellEditor().stopCellEditing();
            }
            if (cartDeleteListener != null) {
                cartDeleteListener.onDelete(row);
            }
        }, new DeleteActionPanel()));

        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // --- KHU VỰC TỔNG KẾT ---
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setOpaque(false);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)), 
            new EmptyBorder(0, 0, 10, 0)
        ));

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        typePanel.setOpaque(false);
        rbDineIn = new JRadioButton("Tại quán", true);
        rbTakeaway = new JRadioButton("Mang đi");
        ButtonGroup bgOrderType = new ButtonGroup();
        bgOrderType.add(rbDineIn);
        bgOrderType.add(rbTakeaway);
        typePanel.add(new JLabel("Loại:"));
        typePanel.add(rbDineIn);
        typePanel.add(rbTakeaway);

        JPanel holidayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        holidayPanel.setOpaque(false);
        chkHoliday = new JCheckBox("Bật giá ngày lễ");
        chkHoliday.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkHoliday.setForeground(new Color(231, 76, 60)); 
        holidayPanel.add(chkHoliday);

        optionsPanel.add(typePanel);
        optionsPanel.add(holidayPanel);
        
        JPanel topBottomWrapper = new JPanel(new BorderLayout(0, 10));
        topBottomWrapper.setOpaque(false);
        topBottomWrapper.add(createCustomerPanel(), BorderLayout.NORTH);
        topBottomWrapper.add(optionsPanel, BorderLayout.CENTER);         

        bottomContainer.add(topBottomWrapper, BorderLayout.NORTH);

        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0;

        lblSubTotal = createSummaryLabel("Tạm tính:");
        lblVat = createSummaryLabel("VAT:");
        lblTotal = new JLabel("0 đ", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotal.setForeground(new Color(231, 76, 60)); 

        gbc.gridx = 0; gbc.gridy = 0; summaryPanel.add(new JLabel("Tạm tính:", SwingConstants.LEFT), gbc);
        gbc.gridx = 1; gbc.gridy = 0; summaryPanel.add(lblSubTotal, gbc);

        gbc.gridx = 0; gbc.gridy = 1; summaryPanel.add(new JLabel("VAT:", SwingConstants.LEFT), gbc);
        gbc.gridx = 1; gbc.gridy = 1; summaryPanel.add(lblVat, gbc);

        gbc.gridx = 0; gbc.gridy = 2; 
        JLabel lblTotalText = new JLabel("THÀNH TIỀN:", SwingConstants.LEFT);
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        summaryPanel.add(lblTotalText, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2; summaryPanel.add(lblTotal, gbc);

        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        btnClearCart = createModernButton("Hủy đơn", new Color(241, 243, 245), TEXT_DARK);
        btnCreateOrder = createModernButton("TẠO ĐƠN", PRIMARY_COLOR, Color.WHITE);
        btnCreateOrder.setFont(new Font("Segoe UI", Font.BOLD, 16));

        actionPanel.add(btnClearCart);
        actionPanel.add(btnCreateOrder);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        summaryPanel.add(actionPanel, gbc);

        bottomContainer.add(summaryPanel, BorderLayout.CENTER);
        panel.add(bottomContainer, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================================
    // UTILS & API
    // ==========================================================
    private JLabel createSummaryLabel(String text) {
        JLabel lbl = new JLabel("0 đ", SwingConstants.RIGHT);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return lbl;
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

    private JPanel createProductCard(ProductModel product, ActionListener onClick) {
        JPanel card = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(175, 230));

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true)); }
            public void mouseExited(MouseEvent e) { card.setBorder(new EmptyBorder(15, 15, 15, 15)); }
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) {
                    onClick.actionPerformed(null);
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

        JLabel lblPrice = new JLabel(String.format("%,d đ", product.getDineInPrice()));
        lblPrice.setForeground(PRIMARY_COLOR);

        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        info.add(lblName);
        info.add(lblPrice);

        card.add(lblImage, BorderLayout.NORTH);
        card.add(info, BorderLayout.CENTER);

        return card;
    }

    public void addCategoryButton(String name, boolean isActive, ActionListener onClick) {
        JButton btn = createModernButton(name, isActive ? PRIMARY_COLOR : Color.WHITE, isActive ? Color.WHITE : TEXT_DARK);
        
        if (!isActive) {
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 200), 1, true),
                    new EmptyBorder(9, 19, 9, 19) 
            ));
        } else {
            btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        }
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 38)); 
        btn.addActionListener(onClick);
        
        categoryPanel.add(btn);
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    public void clearProducts() {
        productGridPanel.removeAll();
        productGridPanel.revalidate();
        productGridPanel.repaint();
    }

    public void addProductCard(ProductModel product, ActionListener onClick) {
        if (product.getProductStatus().equals("Ngừng bán")) return;
        productGridPanel.add(createProductCard(product, onClick));
        productGridPanel.revalidate();
        productGridPanel.repaint();
    }

    public void clearCategories() {
        categoryPanel.removeAll();
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    public void updateCartTable(List<CartItemModel> cartItems) {
        cartTableModel.setRowCount(0); 
        for (int i = 0; i < cartItems.size(); i++) {
            CartItemModel item = cartItems.get(i);
            cartTableModel.addRow(new Object[]{
                item.getDisplayName(), 
                item.getQuantity(), 
                String.format("%,d", item.getTotalPrice()), 
                "Xóa" 
            });
        }
    }

    public void updateSummary(long subTotal, double vat, long total) {
        lblSubTotal.setText(String.format("%,d đ", subTotal));
        lblVat.setText(String.format("%,.0f đ", vat));
        lblTotal.setText(String.format("%,d đ", total));
    }

    public boolean isTakeaway() { return rbTakeaway.isSelected(); }
    public boolean isHoliday() { return chkHoliday.isSelected(); }
    
    public void addOrderOptionListener(ActionListener listener) {
        rbDineIn.addActionListener(listener);
        rbTakeaway.addActionListener(listener);
        chkHoliday.addActionListener(listener);
    }

    public void addSearchListener(java.awt.event.KeyListener listener) { txtSearch.addKeyListener(listener); }
    public String getSearchText() { return txtSearch.getText().trim(); }
    public void addClearCartListener(ActionListener listener) { btnClearCart.addActionListener(listener); }
    public void addCreateOrderListener(ActionListener listener) { btnCreateOrder.addActionListener(listener); }

    // ==========================================================
    // API KHÁCH HÀNG THÀNH VIÊN
    // ==========================================================
    public void addCheckCustomerListener(ActionListener listener) {
        btnCheckCustomer.addActionListener(listener);
    }

    public void addRegisterCustomerListener(ActionListener listener) {
        btnRegisterCustomer.addActionListener(listener);
    }

    public void addClearCustomerListener(ActionListener listener) {
        btnClearCustomer.addActionListener(listener);
    }

    public String getCustomerPhone() {
        String phone = txtCustomerPhone.getText().trim();
        return phone.equals("Nhập SĐT...") ? "" : phone;
    }

    public String getCustomerName() {
        String name = txtCustomerName.getText().trim();
        return name.equals("Nhập tên khách...") ? "" : name;
    }

    public void setCustomerStatus(String info, boolean isNewCustomer) {
        lblCustomerInfo.setText(info);
        
        if (isNewCustomer) {
            lblCustomerInfo.setForeground(new Color(231, 76, 60)); 
            nameActionPanel.setVisible(true); 
            btnClearCustomer.setVisible(false); 
            txtCustomerName.requestFocus(); 
            
            if (txtCustomerName.getText().equals("Nhập tên khách...")) {
                txtCustomerName.setText("");
                txtCustomerName.setForeground(TEXT_DARK);
            }
        } else {
            lblCustomerInfo.setForeground(PRIMARY_COLOR); 
            nameActionPanel.setVisible(false); 
            btnClearCustomer.setVisible(true); 
            txtCustomerName.setText("");
        }
        this.revalidate();
        this.repaint();
    }

    public void clearCustomerInfo() {
        txtCustomerPhone.setText("Nhập SĐT...");
        txtCustomerPhone.setForeground(Color.GRAY);
        txtCustomerName.setText("Nhập tên khách...");
        txtCustomerName.setForeground(Color.GRAY);
        
        nameActionPanel.setVisible(false); 
        btnClearCustomer.setVisible(false); 
        
        lblCustomerInfo.setText("Khách vãng lai");
        lblCustomerInfo.setForeground(TEXT_MUTED);
        
        this.revalidate();
        this.repaint();
    }

    // ==========================================================
    // INNER CLASSES (GIAO DIỆN NÚT VÀ CÁC THÀNH PHẦN)
    // ==========================================================
    class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) { Dimension minimum = layoutSize(target, false); minimum.width -= (getHgap() + 1); return minimum; }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getParent().getWidth(); if (targetWidth == 0) targetWidth = 400; 
                int hgap = getHgap(); int vgap = getVgap(); Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2); int maxWidth = targetWidth - horizontalInsetsAndGap;
                Dimension dim = new Dimension(0, 0); int rowWidth = 0; int rowHeight = 0; int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) { dim.width = Math.max(dim.width, rowWidth); dim.height += rowHeight + vgap; rowWidth = 0; rowHeight = 0; }
                        rowWidth += d.width + hgap; rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rowWidth); dim.height += rowHeight + insets.top + insets.bottom + vgap * 2; return dim;
            }
        }
    }
    
    // --- GIAO DIỆN XÓA ---
    public void setCartDeleteListener(DeleteActionListener listener) {
        this.cartDeleteListener = listener;
    }

    public interface DeleteActionListener { void onDelete(int row); }

    class DeleteActionPanel extends JPanel {
        protected JButton btnDelete = new JButton("Xóa");
        public DeleteActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 4)); setOpaque(true);
            btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 11)); 
            btnDelete.setForeground(new Color(255, 59, 48));
            btnDelete.setBackground(Color.WHITE);
            btnDelete.setBorder(BorderFactory.createLineBorder(new Color(255, 59, 48), 1));
            btnDelete.setFocusPainted(false); btnDelete.setPreferredSize(new Dimension(45, 25)); 
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            add(btnDelete);
        }
    }

    class DeleteActionButtonRenderer implements TableCellRenderer {
        protected DeleteActionPanel panel; 
        public DeleteActionButtonRenderer(DeleteActionPanel panel) { this.panel = panel; }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE); return panel;
        }
    }

    class DeleteActionButtonEditor extends DefaultCellEditor {
        protected DeleteActionPanel panel; protected DeleteActionListener listener; protected int currentRow;
        public DeleteActionButtonEditor(DeleteActionListener listener, DeleteActionPanel panel) {
            super(new JCheckBox()); this.listener = listener; this.panel = panel;
            this.panel.btnDelete.addActionListener(e -> { stopCellEditing(); listener.onDelete(currentRow); });
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row; panel.setBackground(table.getSelectionBackground()); return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
}