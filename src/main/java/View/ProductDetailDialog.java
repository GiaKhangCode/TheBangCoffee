package View;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Giao diện Chi tiết / Tạo Sản Phẩm - The Bang Coffee.
 * Thiết kế hiện đại, đồng bộ database SAN_PHAM + LOAI_SAN_PHAM.
 */
public class ProductDetailDialog extends JDialog {

    // ── Palette ────────────────────────────────────────────────────────────
    private static final Color PRIMARY      = new Color(44, 62, 80);   // deep navy
    private static final Color ACCENT       = new Color(52, 152, 219); // blue
    private static final Color ACCENT_LIGHT = new Color(235, 245, 255);
    private static final Color SUCCESS      = new Color(39, 174, 96);
    private static final Color SURFACE      = Color.WHITE;
    private static final Color BG           = new Color(245, 247, 250);
    private static final Color BORDER_CLR   = new Color(218, 224, 232);
    private static final Color TEXT_PRIMARY = new Color(30, 39, 46);
    private static final Color TEXT_MUTED   = new Color(127, 140, 141);
    private static final Color HEADER_BG    = new Color(52, 73, 94);

    // ── Tab panels ─────────────────────────────────────────────────────────
    private JTabbedPane tabbedPane;
    private JPanel tabInfo, tabRecipe;

    // ── Tab 1 – Thông tin sản phẩm ─────────────────────────────────────────
    private JTextField txtTenMon;
    private JTextField txtGiaBan;
    private JComboBox<String> cbDanhMuc;
    private JLabel    lblImagePreview;
    private JRadioButton rbDangSuDung, rbChuaSuDung;

    // ── Tab 2 – Công thức / Định lượng ─────────────────────────────────────
    private JComboBox<String> cbNguyenLieu;
    private JTextField txtDonVi, txtDinhLuong;
    private JTable recipeTable;
    private DefaultTableModel recipeModel;
    private JLabel lblTotalCost;
    
    private JButton btnSave;
    private JButton btnUpload;

    // ── Constructor ────────────────────────────────────────────────────────
    public ProductDetailDialog(Frame parent) {
        super(parent, "THÊM SẢN PHẨM MỚI", true);
        initComponents();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════════════════
    private void initComponents() {
        setSize(1000, 680);
        setLocationRelativeTo(null);
        setUndecorated(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ─────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, HEADER_BG, getWidth(), 0, new Color(41, 128, 185));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(getWidth(), 58));
        header.setBorder(new EmptyBorder(0, 24, 0, 16));

        // Icon + Title
        JLabel icon = new JLabel("☕");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        icon.setForeground(Color.WHITE);

        JLabel title = new JLabel("  THÊM SẢN PHẨM MỚI");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 14));
        left.setOpaque(false);
        left.add(icon);
        left.add(title);

        // Close button
        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setForeground(new Color(255,255,255,180));
        btnClose.setBackground(new Color(0,0,0,0));
        btnClose.setBorder(new EmptyBorder(8, 12, 8, 8));
        btnClose.setFocusPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnClose.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e)  { btnClose.setForeground(new Color(255,255,255,180)); }
        });
        btnClose.addActionListener(e -> dispose());

        header.add(left,     BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);
        return header;
    }

    // ── Body (TabbedPane) ──────────────────────────────────────────────────
    private JComponent buildBody() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP) {
            @Override public void updateUI() {
                super.updateUI();
                setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
                    @Override protected void installDefaults() {
                        super.installDefaults();
                        highlight       = BG;
                        lightHighlight  = BG;
                        shadow          = BORDER_CLR;
                        darkShadow      = BORDER_CLR;
                        focus           = ACCENT;
                    }
                });
            }
        };
        tabbedPane.setBackground(BG);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setBorder(new EmptyBorder(8, 8, 0, 8));

        initTabInfo();
        initTabRecipe();

        tabbedPane.addTab("Thông tin sản phẩm  ", tabInfo);
        tabbedPane.addTab("Công thức & Định lượng  ", tabRecipe);

        return tabbedPane;
    }

    // ── Footer ─────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_CLR));

        JButton btnCancel = buildButton("Hủy bỏ",        new Color(236,240,241), TEXT_PRIMARY, false);
        btnSave = buildButton("Lưu sản phẩm", ACCENT, Color.WHITE, true);

        btnCancel.addActionListener(e -> dispose());
        // btnSave.addActionListener(e -> controller.save());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB 1 – THÔNG TIN SẢN PHẨM
    // ══════════════════════════════════════════════════════════════════════
    private void initTabInfo() {
        tabInfo = new JPanel(new BorderLayout());
        tabInfo.setBackground(BG);
        tabInfo.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.add(buildInfoLeftCard());
        row.add(buildInfoRightCard());

        tabInfo.add(row, BorderLayout.CENTER);
    }

    // LEFT card: Thông tin cơ bản + Hình ảnh + Mô tả
    private JPanel buildInfoLeftCard() {
        JPanel card = createCard("Thông tin cơ bản");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Tên sản phẩm
        txtTenMon = createTextField("");
        card.add(buildFieldRow("Tên sản phẩm *", txtTenMon, false));
        card.add(vgap(6));

        // Loại sản phẩm (LOAI_SAN_PHAM)
        cbDanhMuc = createComboBox();
        card.add(buildFieldRow("Loại sản phẩm *", cbDanhMuc, false));
        card.add(vgap(6));

        // Giá cơ bản (GiaCoBan)
        txtGiaBan = createTextField("0");
        card.add(buildFieldRow("Giá cơ bản (VND) *", txtGiaBan, false));
        card.add(vgap(12));

        // Hình ảnh
        card.add(buildImageSection());
        card.add(vgap(12));


        // glue
        card.add(Box.createVerticalGlue());
        return card;
    }

    // RIGHT card: Trạng thái
    private JPanel buildInfoRightCard() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel statusCard = createCard("Trạng thái sản phẩm");
        statusCard.setLayout(new BoxLayout(statusCard, BoxLayout.Y_AXIS));

        ButtonGroup bg = new ButtonGroup();
        rbDangSuDung = buildRadioButton("Đang sử dụng", SUCCESS, true);
        rbChuaSuDung = buildRadioButton("Chưa sử dụng", TEXT_MUTED, false);
        bg.add(rbDangSuDung);
        bg.add(rbChuaSuDung);

        statusCard.add(vgap(8));
        statusCard.add(buildRadioRow(rbDangSuDung, "Sản phẩm đang được kinh doanh"));
        statusCard.add(vgap(8));
        statusCard.add(buildRadioRow(rbChuaSuDung, "Sản phẩm tạm ngừng / chưa dùng"));
        statusCard.add(vgap(8));
        statusCard.add(Box.createVerticalGlue());

        // Tip box
        JPanel tip = new JPanel(new BorderLayout());
        tip.setBackground(ACCENT_LIGHT);
        tip.setBorder(new CompoundBorder(
                new LineBorder(new Color(174, 214, 241), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        tip.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel tipText = new JLabel("<html><b>💡 Lưu ý:</b> Tùy chọn đá, đường, size<br>được cấu hình riêng ở phần <b>Tùy chọn thêm</b><br>khi tạo/sửa đơn hàng.</html>");
        tipText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tipText.setForeground(new Color(41, 128, 185));
        tip.add(tipText);
        tip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        statusCard.add(tip);
        statusCard.add(vgap(12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        wrapper.add(statusCard, gbc);
        return wrapper;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB 2 – CÔNG THỨC
    // ══════════════════════════════════════════════════════════════════════
    private void initTabRecipe() {
        tabRecipe = new JPanel(new BorderLayout(0, 12));
        tabRecipe.setBackground(BG);
        tabRecipe.setBorder(new EmptyBorder(16, 16, 16, 16));

        // ── Tiêu đề ───────────────────────────────────────────────────────
        JPanel recipeHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        recipeHeader.setOpaque(false);
        JLabel lblTarget = new JLabel("Đang tạo công thức cho: ");
        lblTarget.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTarget.setForeground(TEXT_MUTED);
        JLabel lblProductName = new JLabel("— chưa đặt tên —");
        lblProductName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblProductName.setForeground(ACCENT);
        recipeHeader.add(lblTarget);
        recipeHeader.add(lblProductName);

        // ── Input card ────────────────────────────────────────────────────
        JPanel inputCard = createCard(null);
        inputCard.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.anchor = GridBagConstraints.WEST;
        g.fill   = GridBagConstraints.HORIZONTAL;

        cbNguyenLieu = createComboBox();
        cbNguyenLieu.setPreferredSize(new Dimension(220, 36));
        txtDonVi = createTextField("");
        txtDonVi.setEditable(false);
        txtDonVi.setPreferredSize(new Dimension(100, 36));
        txtDonVi.setBackground(new Color(248, 249, 250));
        txtDinhLuong = createTextField("0");
        txtDinhLuong.setPreferredSize(new Dimension(110, 36));

        JButton btnAdd = buildButton("➕  Thêm vào công thức", ACCENT, Color.WHITE, true);

        // Labels row
        g.gridy = 0;
        g.gridx = 0; inputCard.add(makeLabel("Nguyên liệu"), g);
        g.gridx = 1; inputCard.add(makeLabel("Đơn vị"), g);
        g.gridx = 2; inputCard.add(makeLabel("Định lượng"), g);
        g.gridx = 3; inputCard.add(new JLabel(""), g);

        // Inputs row
        g.gridy = 1;
        g.gridx = 0; inputCard.add(cbNguyenLieu, g);
        g.gridx = 1; inputCard.add(txtDonVi, g);
        g.gridx = 2; inputCard.add(txtDinhLuong, g);
        g.gridx = 3; inputCard.add(btnAdd, g);

        // ── Table ─────────────────────────────────────────────────────────
        String[] cols = {"STT", "Tên nguyên liệu", "Đơn vị", "Định lượng", "Thao tác"};
        recipeModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recipeTable = new JTable(recipeModel);
        styleTable(recipeTable);
        JScrollPane scroll = new JScrollPane(recipeTable);
        scroll.getViewport().setBackground(SURFACE);
        scroll.setBorder(new LineBorder(BORDER_CLR));

        // ── Summary ───────────────────────────────────────────────────────
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        summaryPanel.setOpaque(false);
        lblTotalCost = new JLabel("Tổng nguyên liệu: 0 dòng");
        lblTotalCost.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalCost.setForeground(ACCENT);
        summaryPanel.add(lblTotalCost);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(inputCard, BorderLayout.NORTH);
        center.add(scroll,    BorderLayout.CENTER);
        center.add(summaryPanel, BorderLayout.SOUTH);

        tabRecipe.add(recipeHeader, BorderLayout.NORTH);
        tabRecipe.add(center,       BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPER – UI Builders
    // ══════════════════════════════════════════════════════════════════════

    /** Card với title (optional) và drop-shadow giả bằng border */
    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(SURFACE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        Border shadow = new EmptyBorder(1, 1, 3, 3);
        Border line   = new LineBorder(BORDER_CLR, 1, true);
        Border pad    = new EmptyBorder(16, 18, 16, 18);

        if (title != null && !title.isEmpty()) {
            TitledBorder titled = new TitledBorder(
                    new CompoundBorder(shadow, line),
                    title,
                    TitledBorder.LEFT, TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 13),
                    TEXT_PRIMARY);
            card.setBorder(new CompoundBorder(titled, pad));
        } else {
            card.setBorder(new CompoundBorder(new CompoundBorder(shadow, line), pad));
        }
        return card;
    }

    /** Field label + component row */
    private JPanel buildFieldRow(String labelText, Component field, boolean stretch) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(stretch
                ? new Dimension(Integer.MAX_VALUE, 120)
                : new Dimension(Integer.MAX_VALUE, 64));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);

        row.add(lbl,   BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    /** Image upload section */
    private JPanel buildImageSection() {
        JPanel section = new JPanel(new BorderLayout(12, 0));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Preview box
        lblImagePreview = new JLabel("📷", SwingConstants.CENTER);
        lblImagePreview.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        lblImagePreview.setPreferredSize(new Dimension(90, 90));
        lblImagePreview.setMinimumSize(new Dimension(90, 90));
        lblImagePreview.setBackground(new Color(248, 249, 250));
        lblImagePreview.setOpaque(true);
        lblImagePreview.setBorder(new DashBorder(BORDER_CLR, 1, 6));

        // Buttons
        JPanel btnCol = new JPanel(new GridLayout(3, 1, 0, 6));
        btnCol.setOpaque(false);
        btnUpload = buildButton("Chọn ảnh", ACCENT, Color.WHITE, true);
        JButton btnRemove = buildButton("Xóa ảnh", new Color(231,76,60), Color.WHITE, false);
        JLabel  lblHint   = new JLabel("JPG / PNG, tối đa 2MB");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(TEXT_MUTED);
        btnCol.add(btnUpload);
        btnCol.add(btnRemove);
        btnCol.add(lblHint);

        JLabel lbl = new JLabel("Hình ảnh sản phẩm");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);

        JPanel inner = new JPanel(new BorderLayout(0, 4));
        inner.setOpaque(false);
        inner.add(lbl,    BorderLayout.NORTH);
        inner.add(btnCol, BorderLayout.CENTER);

        section.add(lblImagePreview, BorderLayout.WEST);
        section.add(inner,           BorderLayout.CENTER);
        return section;
    }

    /** Radio button with color indicator */
    private JRadioButton buildRadioButton(String text, Color color, boolean selected) {
        JRadioButton rb = new JRadioButton(text, selected);
        rb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rb.setForeground(color);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return rb;
    }

    /** Radio row with description */
    private JPanel buildRadioRow(JRadioButton rb, String desc) {
        JPanel row = new JPanel(new BorderLayout(0, 2));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel d = new JLabel("   " + desc);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        d.setForeground(TEXT_MUTED);
        row.add(rb, BorderLayout.NORTH);
        row.add(d,  BorderLayout.CENTER);
        return row;
    }

    private JButton buildButton(String text, Color bg, Color fg, boolean primary) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? bg.darker()
                        : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", primary ? Font.BOLD : Font.PLAIN, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder, 16);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_PRIMARY);
        tf.setBackground(SURFACE);
        tf.setPreferredSize(new Dimension(200, 36));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    private JComboBox<String> createComboBox() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setPreferredSize(new Dimension(200, 36));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cb.setBackground(SURFACE);
        return cb;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private Component vgap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(SURFACE);
        table.setSelectionBackground(ACCENT_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 244, 248));
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_CLR));
        header.setReorderingAllowed(false);

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? SURFACE : new Color(248, 250, 253));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PUBLIC API (Controller injection)
    // ══════════════════════════════════════════════════════════════════════

    /** Inject danh sách loại sản phẩm (LOAI_SAN_PHAM.TenLoaiSanPham) */
    public void setCategoryList(java.util.List<String> categories) {
        cbDanhMuc.removeAllItems();
        for (String c : categories) {
            cbDanhMuc.addItem(c);
        }
    }

    /** Inject danh sách nguyên liệu (NGUYEN_LIEU.TenNguyenLieu) */
    public void setIngredientList(String[] ingredients) {
        cbNguyenLieu.setModel(new DefaultComboBoxModel<>(ingredients));
    }

    /** Đổi tiêu đề dialog (edit vs create) */
    public void setDialogMode(boolean isEdit) {
        setTitle(isEdit ? "CHỈNH SỬA SẢN PHẨM" : "THÊM SẢN PHẨM MỚI");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Inner: Dashed border cho image preview
    // ══════════════════════════════════════════════════════════════════════
    private static class DashBorder extends AbstractBorder {
        private final Color color; private final int thick, arc;
        DashBorder(Color c, int t, int a) { color = c; thick = t; arc = a; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thick, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                    0, new float[]{6, 4}, 0));
            g2.drawRoundRect(x, y, w-1, h-1, arc, arc);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(thick, thick, thick, thick); }
    }
    
    public void setImage(ImageIcon icon) {
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            lblImagePreview.setIcon(new ImageIcon(img));
            lblImagePreview.setText("");
        } else {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("📷");
        }
    }
    
    public String getTenSanPham() {
        return txtTenMon.getText().trim();
    }

    public double getGiaBan() {
        try {
            return Double.parseDouble(txtGiaBan.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public String getLoaiSanPham() {
        return (String) cbDanhMuc.getSelectedItem();
    }

    public String getTrangThai() {
        return rbDangSuDung.isSelected() ? "Đang sử dụng" : "Chưa sử dụng";
    }
    
    public void addSaveListener(java.awt.event.ActionListener listener) {
        btnSave.addActionListener(listener);
    }
    
    public void addChooseImageListener(java.awt.event.ActionListener listener) {
        btnUpload.addActionListener(listener);
    }
    // ══════════════════════════════════════════════════════════════════════
    //  Main – standalone preview
    // ══════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
//        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
//        catch (Exception ignored) {}
//
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame();
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            ProductDetailDialog dialog = new ProductDetailDialog(frame);
//
//            // Demo data
//            dialog.setCategoryList(new String[]{"Cà phê", "Trà sữa", "Nước ép", "Sinh tố"});
//            dialog.setIngredientList(new String[]{"Cà phê Arabica", "Sữa tươi", "Đường", "Trà xanh"});
//
//            dialog.addWindowListener(new WindowAdapter() {
//                @Override public void windowClosing(WindowEvent e) { System.exit(0); }
//            });
//            dialog.setVisible(true);
//        });
    }
}