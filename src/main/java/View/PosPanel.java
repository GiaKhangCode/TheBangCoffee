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
    

    // Các biến UI cho Tùy chọn đơn hàng
    private JRadioButton rbDineIn;
    private JRadioButton rbTakeaway;
    private JRadioButton rbHoliday;
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

        String[] cols = {"Món", "SL", "Giá", "Xóa"};
        cartTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 3; 
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(95);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        cartTable.getTableHeader().setBackground(new Color(245, 245, 245));
        cartTable.setShowVerticalLines(false);
        cartTable.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30));

        TableColumnModel tcm = cartTable.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(140); 
        tcm.getColumn(1).setPreferredWidth(80);  
        tcm.getColumn(2).setPreferredWidth(60);  
        tcm.getColumn(3).setPreferredWidth(55);  
        
        TableColumn qtyCol = cartTable.getColumnModel().getColumn(1);
        qtyCol.setCellRenderer(new QuantityButtonRenderer(new QuantityPanel()));
        qtyCol.setCellEditor(new QuantityButtonEditor(new QuantityActionListener() {
            @Override
            public void onIncrease(int row) {
                if (cartTable.isEditing()) {
                    cartTable.getCellEditor().stopCellEditing();
                }
                if (cartQuantityListener != null) {
                    cartQuantityListener.onIncrease(row);
                }
            }
            @Override
            public void onDecrease(int row) {
                if (cartTable.isEditing()) {
                    cartTable.getCellEditor().stopCellEditing();
                }
                if (cartQuantityListener != null) {
                    cartQuantityListener.onDecrease(row);
                }
            }
        }, new QuantityPanel()));

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

        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)), 
            new EmptyBorder(0, 0, 10, 0)
        ));

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        typePanel.setOpaque(false);
        rbDineIn = new JRadioButton("Tại quán", true);
        rbTakeaway = new JRadioButton("Mang đi");
        rbHoliday = new JRadioButton("Ngày lễ");
        rbHoliday.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rbHoliday.setForeground(new Color(231, 76, 60)); 
        
        ButtonGroup bgOrderType = new ButtonGroup();
        bgOrderType.add(rbDineIn);
        bgOrderType.add(rbTakeaway);
        bgOrderType.add(rbHoliday);
        
        typePanel.add(new JLabel("Loại:"));
        typePanel.add(rbDineIn);
        typePanel.add(rbTakeaway);
        typePanel.add(rbHoliday);

        optionsPanel.add(typePanel);
        bottomContainer.add(optionsPanel, BorderLayout.NORTH);



        // Panel 3 nút bấm
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        btnClearCart = createModernButton("Hủy đơn", new Color(241, 243, 245), TEXT_DARK);
        btnCreateOrder = createModernButton("TẠO ĐƠN", PRIMARY_COLOR, Color.WHITE);
        
        btnClearCart.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCreateOrder.setFont(new Font("Segoe UI", Font.BOLD, 14));

        actionPanel.add(btnClearCart);
        actionPanel.add(btnCreateOrder);

        bottomContainer.add(actionPanel, BorderLayout.CENTER);
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
        btn.setBorder(new EmptyBorder(10, 10, 10, 10)); // Giảm padding để fit 3 nút
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createProductCard(ProductModel product, ActionListener onClick) {
        boolean isOutOfStock = product.getProductStatus() != null && product.getProductStatus().equalsIgnoreCase("Tạm hết");

        JPanel card = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g); 

                if (isOutOfStock) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    g2.setColor(new Color(230, 230, 230, 180)); 
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                    int rectHeight = 35;
                    int rectY = (getHeight() - rectHeight) / 2;
                    g2.setColor(new Color(50, 50, 50, 200)); 
                    g2.fillRect(0, rectY, getWidth(), rectHeight);

                    g2.setColor(Color.WHITE); 
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    
                    String text = "TẠM HẾT";
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    int textAscent = fm.getAscent();
                    
                    int textX = (getWidth() - textWidth) / 2;
                    int textY = rectY + ((rectHeight - fm.getHeight()) / 2) + textAscent;
                    
                    g2.drawString(text, textX, textY);
                    g2.dispose();
                }
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        card.setCursor(isOutOfStock ? new Cursor(Cursor.DEFAULT_CURSOR) : new Cursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(175, 230));

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                if (!isOutOfStock) card.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2, true)); 
            }
            public void mouseExited(MouseEvent e) { 
                card.setBorder(new EmptyBorder(15, 15, 15, 15)); 
            }
            public void mouseClicked(MouseEvent e) {
                if (!isOutOfStock && onClick != null) {
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

        // Lấy giá từ biến thể đầu tiên (giá không còn lưu ở cấp sản phẩm nữa)
        String priceText = "Chưa có giá";
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            priceText = String.format("Từ: %,d đ", product.getVariants().get(0).getDineInPrice());
        }
        JLabel lblPrice = new JLabel(priceText);
        
        if (isOutOfStock) {
            lblName.setForeground(Color.GRAY);
            lblPrice.setForeground(Color.GRAY);
        } else {
            lblName.setForeground(Color.BLACK);
            lblPrice.setForeground(PRIMARY_COLOR);
        }

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
        if (product.getProductStatus() != null && product.getProductStatus().equalsIgnoreCase("Ngừng bán")) {
            return;
        }
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



    public boolean isTakeaway() { return rbTakeaway.isSelected(); }
    public boolean isHoliday() { return rbHoliday.isSelected(); }
    
    public void addOrderOptionListener(ActionListener listener) {
        rbDineIn.addActionListener(listener);
        rbTakeaway.addActionListener(listener);
        rbHoliday.addActionListener(listener);
    }

    public void addSearchListener(java.awt.event.KeyListener listener) { txtSearch.addKeyListener(listener); }
    public String getSearchText() { return txtSearch.getText().trim(); }
    public void addClearCartListener(ActionListener listener) { btnClearCart.addActionListener(listener); }
    public void addCreateOrderListener(ActionListener listener) { btnCreateOrder.addActionListener(listener); }


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
    
    public void setCartDeleteListener(DeleteActionListener listener) {
        this.cartDeleteListener = listener;
    }

    public interface DeleteActionListener { void onDelete(int row); }

    class DeleteActionPanel extends JPanel {
        URL deleteIconUrl = getClass().getResource("/images/delete-icon.png");
        protected JButton btnDelete = new JButton("<html><img src='" + deleteIconUrl + "' width='10' height='10'> Xóa</html>");
        public DeleteActionPanel() {
            setLayout(new GridBagLayout()); setOpaque(true);
            btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 11)); 
            btnDelete.setForeground(new Color(255, 59, 48));
            btnDelete.setBackground(Color.WHITE);
            btnDelete.setBorder(BorderFactory.createLineBorder(new Color(255, 59, 48), 1));
            btnDelete.setFocusPainted(false); btnDelete.setPreferredSize(new Dimension(58, 25)); 
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

    private QuantityActionListener cartQuantityListener;

    public void setCartQuantityListener(QuantityActionListener listener) {
        this.cartQuantityListener = listener;
    }

    public interface QuantityActionListener {
        void onIncrease(int row);
        void onDecrease(int row);
    }

    class QuantityPanel extends JPanel {
        protected JButton btnMinus = new JButton("-");
        protected JLabel lblQty = new JLabel("1", SwingConstants.CENTER);
        protected JButton btnPlus = new JButton("+");
        
        public QuantityPanel() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            
            styleBtn(btnMinus);
            styleBtn(btnPlus);
            
            lblQty.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblQty.setPreferredSize(new Dimension(20, 24));
            lblQty.setHorizontalAlignment(SwingConstants.CENTER);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.NONE;
            
            gbc.gridx = 0;
            gbc.insets = new Insets(0, 0, 0, 3);
            add(btnMinus, gbc);
            
            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 0, 3);
            add(lblQty, gbc);
            
            gbc.gridx = 2;
            gbc.insets = new Insets(0, 0, 0, 0);
            add(btnPlus, gbc);
        }
        
        private void styleBtn(JButton btn) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(24, 24));
            btn.setBackground(new Color(240, 240, 240));
            btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    class QuantityButtonRenderer implements TableCellRenderer {
        protected QuantityPanel panel;
        public QuantityButtonRenderer(QuantityPanel panel) {
            this.panel = panel;
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.lblQty.setText(value != null ? value.toString() : "1");
            return panel;
        }
    }

    class QuantityButtonEditor extends DefaultCellEditor {
        protected QuantityPanel panel;
        protected int currentRow;
        protected QuantityActionListener listener;
        
        public QuantityButtonEditor(QuantityActionListener listener, QuantityPanel panel) {
            super(new JCheckBox());
            this.listener = listener;
            this.panel = panel;
            
            this.panel.btnMinus.addActionListener(e -> {
                stopCellEditing();
                if (listener != null) {
                    listener.onDecrease(currentRow);
                }
            });
            
            this.panel.btnPlus.addActionListener(e -> {
                stopCellEditing();
                if (listener != null) {
                    listener.onIncrease(currentRow);
                }
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            panel.setBackground(table.getSelectionBackground());
            panel.lblQty.setText(value != null ? value.toString() : "1");
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return panel.lblQty.getText();
        }
    }
    
    // --- PHÂN QUYỀN ---
    private boolean hasEditPermission = true;
    private boolean hasDeletePermission = true;
    
    public void setActionPermissions(boolean canEdit, boolean canDelete) {
        this.hasEditPermission = canEdit;
        this.hasDeletePermission = canDelete;
        this.repaint();
    }
    
    public JButton getBtnClearCart() { return btnClearCart; }
    public JButton getBtnCreateOrder() { return btnCreateOrder; }
}