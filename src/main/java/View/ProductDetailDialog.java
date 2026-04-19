package View;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Giao diện Tạo món mới và Thiết lập công thức - The Bang Coffee.
 * Thiết kế hiện đại, đồng bộ với phong cách hệ thống.
 */
public class ProductDetailDialog extends JDialog {

    private JTabbedPane tabbedPane;
    private JPanel tabInfo, tabRecipe;
    private Color PRIMARY_COLOR = AppColor.PRIMARY;
    private Color TEXT_DARK = AppColor.TEXT_DARK;
    private Color TEXT_MUTED = AppColor.TEXT_MUTED;

    // Components Tab 1
    private JTextField txtMaMon, txtTenMon, txtGiaBan;
    private JComboBox<String> cbDanhMuc;
    private JTextArea txtMoTa;
    private JLabel lblImagePlaceholder;
    private JCheckBox chkSizeM, chkSizeL, chk100Da, chk50Da, chk0Da, chk100Duong, chk50Duong, chk0Duong;
    private JRadioButton rbDangBan, rbTamHet, rbNgungBan;

    // Components Tab 2
    private JComboBox<String> cbNguyenLieu;
    private JTextField txtDonVi, txtDinhLuong;
    private JTable recipeTable;
    private DefaultTableModel recipeModel;
    private JLabel lblTotalCost;

    public ProductDetailDialog(Frame parent) {
        super(parent, "TẠO MÓN NƯỚC MỚI", true);
        initComponents();
    }

    private void initComponents() {
        setSize(1050, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(getWidth(), 50));
        JLabel title = new JLabel("  TẠO MÓN NƯỚC MỚI");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);
        
        initTabInfo();
        initTabRecipe();

        tabbedPane.addTab("Thông tin chung", tabInfo);
        tabbedPane.addTab("Công thức (Định lượng)", tabRecipe);
        
        add(tabbedPane, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.setBackground(new Color(245, 245, 245));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnSave = createModernButton("Lưu món nước", PRIMARY_COLOR, Color.WHITE);
        JButton btnCancel = createModernButton("Hủy bỏ", new Color(220, 220, 220), TEXT_DARK);
        
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnSave);
        footer.add(btnCancel);
        add(footer, BorderLayout.SOUTH);
    }

    private void initTabInfo() {
        tabInfo = new JPanel(new GridBagLayout());
        tabInfo.setBackground(Color.WHITE);
        tabInfo.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.fill = GridBagConstraints.BOTH;
        mainGbc.weighty = 1.0;

        // LEFT: Basic Info
        JPanel leftPanel = createSectionPanel("Thông tin cơ bản");
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        txtTenMon = createStyledTextField("");
        cbDanhMuc = new JComboBox<>();
        cbDanhMuc.setPreferredSize(new Dimension(200, 35));
        txtGiaBan = createStyledTextField("");
        txtMoTa = new JTextArea(3, 20);
        txtMoTa.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        txtMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        
        gbc.weightx = 0.3; addComponent(leftPanel, new JLabel("Tên món: *"), 0, 1, gbc);
        gbc.weightx = 0.7; addComponent(leftPanel, txtTenMon, 1, 1, gbc);
        
        gbc.weightx = 0.3; addComponent(leftPanel, new JLabel("Danh mục món: *"), 0, 2, gbc);
        gbc.weightx = 0.7; addComponent(leftPanel, cbDanhMuc, 1, 2, gbc);
        
        gbc.weightx = 0.3; addComponent(leftPanel, new JLabel("Giá bán tiêu chuẩn: *"), 0, 3, gbc);
        gbc.weightx = 0.7; addComponent(leftPanel, txtGiaBan, 1, 3, gbc);
        
        // Image Section
        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        imgPanel.setOpaque(false);
        lblImagePlaceholder = new JLabel("Hình ảnh mẫu", SwingConstants.CENTER);
        lblImagePlaceholder.setPreferredSize(new Dimension(100, 100));
        lblImagePlaceholder.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        JButton btnUpload = new JButton("Tải ảnh lên");
        btnUpload.setFont(new Font("Segoe UI", Font.BOLD, 12));
        imgPanel.add(lblImagePlaceholder);
        imgPanel.add(btnUpload);
        
        gbc.gridwidth = 2; gbc.weightx = 1.0;
        addComponent(leftPanel, new JLabel("Hình ảnh:"), 0, 4, gbc);
        addComponent(leftPanel, imgPanel, 0, 5, gbc);
        addComponent(leftPanel, new JLabel("Mô tả món:"), 0, 6, gbc);
        gbc.weighty = 0.1; addComponent(leftPanel, new JScrollPane(txtMoTa), 0, 7, gbc);
        gbc.weighty = 0;

        // RIGHT: Options & Status
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setOpaque(false);
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.fill = GridBagConstraints.BOTH;
        rightGbc.weightx = 1.0;
        rightGbc.insets = new Insets(0, 10, 0, 0);

        JPanel optionsPanel = createSectionPanel("Tùy chọn & Biến thể");
        optionsPanel.setLayout(new GridBagLayout());
        GridBagConstraints optGbc = new GridBagConstraints();
        optGbc.fill = GridBagConstraints.HORIZONTAL;
        optGbc.insets = new Insets(5, 5, 5, 5);
        optGbc.anchor = GridBagConstraints.NORTHWEST;

        // Size Options
        optGbc.gridx = 0; optGbc.gridy = 0; optGbc.weightx = 0.3;
        optionsPanel.add(new JLabel("Tùy chọn Size:"), optGbc);

        JPanel sizeItems = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sizeItems.setOpaque(false);
        chkSizeM = new JCheckBox("Size M (+0đ)", true);
        chkSizeL = new JCheckBox("Size L (+10.000đ)", true);
        sizeItems.add(chkSizeM); sizeItems.add(chkSizeL);

        optGbc.gridx = 1; optGbc.weightx = 0.7;
        optionsPanel.add(sizeItems, optGbc);

        // Ice/Sugar Options
        optGbc.gridx = 0; optGbc.gridy = 1; optGbc.weightx = 0.3;
        optionsPanel.add(new JLabel("Tùy chọn Đá / Đường:"), optGbc);

        JPanel iceSugarPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        iceSugarPanel.setOpaque(false);
        
        JPanel iceSub = new JPanel(); iceSub.setLayout(new BoxLayout(iceSub, BoxLayout.Y_AXIS)); iceSub.setOpaque(false);
        JLabel lblIce = new JLabel("Đá:"); lblIce.setFont(new Font("Segoe UI", Font.BOLD, 12));
        iceSub.add(lblIce);
        chk100Da = new JCheckBox("100% đá", true); chk50Da = new JCheckBox("50% đá"); chk0Da = new JCheckBox("Không đá");
        iceSub.add(chk100Da); iceSub.add(chk50Da); iceSub.add(chk0Da);

        JPanel sugarSub = new JPanel(); sugarSub.setLayout(new BoxLayout(sugarSub, BoxLayout.Y_AXIS)); sugarSub.setOpaque(false);
        JLabel lblSugar = new JLabel("Đường:"); lblSugar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sugarSub.add(lblSugar);
        chk100Duong = new JCheckBox("100% đường", true); chk50Duong = new JCheckBox("50% đường"); chk0Duong = new JCheckBox("Không đường");
        sugarSub.add(chk100Duong); sugarSub.add(chk50Duong); sugarSub.add(chk0Duong);

        iceSugarPanel.add(iceSub);
        iceSugarPanel.add(sugarSub);

        optGbc.gridx = 1; optGbc.weightx = 0.7;
        optionsPanel.add(iceSugarPanel, optGbc);

        // Topping Group
        optGbc.gridx = 0; optGbc.gridy = 2; optGbc.weightx = 0.3;
        optionsPanel.add(new JLabel("Nhóm Topping:"), optGbc);

        JPanel toppingItems = new JPanel();
        toppingItems.setLayout(new BoxLayout(toppingItems, BoxLayout.Y_AXIS));
        toppingItems.setOpaque(false);
        toppingItems.add(new JCheckBox("Trân châu đen", true));
        toppingItems.add(new JCheckBox("Thạch nha đam"));
        toppingItems.add(new JCheckBox("Kem cheese"));

        optGbc.gridx = 1; optGbc.weightx = 0.7;
        optionsPanel.add(toppingItems, optGbc);

        // Thêm khoảng trống để đẩy lên trên
        optGbc.gridx = 0; optGbc.gridy = 3; optGbc.gridwidth = 2; optGbc.weighty = 1.0;
        optionsPanel.add(Box.createVerticalGlue(), optGbc);

        // Status Panel
        JPanel statusPanel = createSectionPanel("Trạng thái");
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 8));
        rbDangBan = new JRadioButton("Đang bán", true);
        rbTamHet = new JRadioButton("Tạm hết");
        rbNgungBan = new JRadioButton("Ngừng bán");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbDangBan); bg.add(rbTamHet); bg.add(rbNgungBan);
        statusPanel.add(rbDangBan); statusPanel.add(rbTamHet); statusPanel.add(rbNgungBan);

        rightGbc.gridy = 0; rightGbc.weighty = 0.8;
        rightContainer.add(optionsPanel, rightGbc);
        rightGbc.gridy = 1; rightGbc.weighty = 0.2;
        rightContainer.add(statusPanel, rightGbc);

        mainGbc.gridx = 0; mainGbc.weightx = 0.6;
        tabInfo.add(leftPanel, mainGbc);
        mainGbc.gridx = 1; mainGbc.weightx = 0.4;
        tabInfo.add(rightContainer, mainGbc);
    }

    private void initTabRecipe() {
        tabRecipe = new JPanel(new BorderLayout(0, 20));
        tabRecipe.setBackground(Color.WHITE);
        tabRecipe.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Info
        JPanel recipeHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recipeHeader.setOpaque(false);
        JLabel lblTarget = new JLabel("Đang tạo món: ---");
        lblTarget.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recipeHeader.add(lblTarget);

        // Input Area
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(248, 249, 250));
        inputPanel.setBorder(new CompoundBorder(new LineBorder(new Color(230, 230, 230)), new EmptyBorder(15, 15, 15, 15)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        cbNguyenLieu = new JComboBox<>();
        txtDonVi = createStyledTextField("");
        txtDonVi.setEditable(false);
        txtDinhLuong = createStyledTextField("");
        JButton btnAdd = createModernButton(" ➕ Thêm vào công thức", PRIMARY_COLOR, Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("Nguyên liệu:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(cbNguyenLieu, gbc);
        gbc.gridx = 1; gbc.gridy = 0; inputPanel.add(new JLabel("Đơn vị:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(txtDonVi, gbc);
        gbc.gridx = 2; gbc.gridy = 0; inputPanel.add(new JLabel("Định lượng (Lượng):"), gbc);
        gbc.gridx = 2; gbc.gridy = 1; inputPanel.add(txtDinhLuong, gbc);
        gbc.gridx = 3; gbc.gridy = 1; inputPanel.add(btnAdd, gbc);

        // Table
        String[] cols = {"STT", "Mã NL", "Tên Nguyên Liệu", "Đơn vị", "Định lượng", "Giá vốn ước tính", "Thành tiền", "Thao tác"};
        recipeModel = new DefaultTableModel(cols, 0);
        recipeTable = new JTable(recipeModel);
        styleTable(recipeTable);
        
        JScrollPane scroll = new JScrollPane(recipeTable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));

        // Summary
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.setOpaque(false);
        lblTotalCost = new JLabel("Tổng giá vốn ước tính: 0 VND");
        lblTotalCost.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalCost.setForeground(PRIMARY_COLOR);
        summaryPanel.add(lblTotalCost);

        tabRecipe.add(recipeHeader, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);
        centerPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        tabRecipe.add(centerPanel, BorderLayout.CENTER);
    }

    // --- Helper Methods (Consistent with StockPanel) ---

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new TitledBorder(new LineBorder(new Color(230, 230, 230)), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));
        return p;
    }

    private JPanel createSubSection(String label) {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.setOpaque(false);
        p.add(new JLabel(label));
        return p;
    }

    private JTextField createStyledTextField(String text) {
        JTextField tf = new JTextField(text, 15);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200)), new EmptyBorder(5, 10, 5, 10)));
        return tf;
    }

    private void addComponent(JPanel p, Component c, int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x; gbc.gridy = y;
        p.add(c, gbc);
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

    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(242, 242, 242));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30));
        table.setSelectionForeground(TEXT_DARK);
    }

    // Public setters for data injection (to be used by Controller)
    public void setCategoryList(String[] categories) {
        cbDanhMuc.setModel(new DefaultComboBoxModel<>(categories));
    }

    public void setIngredientList(String[] ingredients) {
        cbNguyenLieu.setModel(new DefaultComboBoxModel<>(ingredients));
    }

    /**
     * Hàm main hỗ trợ chạy thử giao diện độc lập.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ProductDetailDialog dialog = new ProductDetailDialog(frame);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }
}
