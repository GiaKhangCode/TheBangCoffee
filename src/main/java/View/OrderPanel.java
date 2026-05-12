package View;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class OrderPanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);
    private final Color BG_LIGHT = new Color(248, 249, 250);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color INFO_COLOR = new Color(41, 128, 185);

    // --- LEFT PANEL (DANH SÁCH ĐƠN HÀNG) ---
    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;
    private JButton btnRefresh;
    private DefaultTableModel orderTableModel;
    private JTable orderTable;

    // --- RIGHT PANEL (CHI TIẾT & HÀNH ĐỘNG) ---
    private JLabel lblOrderId;
    private JLabel lblOrderTime;
    private JLabel lblOrderType;
    private JLabel lblOrderStatus;
    private DefaultTableModel detailTableModel;
    private JTable detailTable;
    private JLabel lblTotalAmount;

    // Nút cập nhật trạng thái
    private JButton btnAccept;   // Tiếp nhận
    private JButton btnComplete; // Hoàn thành (Sẽ kích hoạt trừ kho ở Controller)
    private JButton btnPay;      // Đã thanh toán
    private JButton btnCancel;   // Hủy đơn

    public OrderPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 0));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int rightPanelWidth = (int) (screenSize.width * 0.35); // Bên phải chiếm 35% màn hình

        add(createLeftPanel(), BorderLayout.CENTER);
        add(createRightPanel(rightPanelWidth), BorderLayout.EAST);
    }

    // ==========================================================
    // KHU VỰC BÊN TRÁI: DANH SÁCH ĐƠN HÀNG
    // ==========================================================
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // 1. Top Bar (Tìm kiếm & Lọc)
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topBar.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(5, 15, 5, 15)));
        txtSearch.setText("Tìm theo mã đơn...");
        txtSearch.setForeground(Color.GRAY);

        // Placeholder effect
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Tìm theo mã đơn...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(TEXT_DARK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setText("Tìm theo mã đơn...");
                }
            }
        });

        String[] statuses = {"Tất cả", "Chờ tiếp nhận", "Đang pha chế", "Hoàn thành", "Đã thanh toán", "Đã hủy"};
        cbStatusFilter = new JComboBox<>(statuses);
        cbStatusFilter.setPreferredSize(new Dimension(150, 40));
        cbStatusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbStatusFilter.setBackground(Color.WHITE);

        btnRefresh = createModernButton("Làm mới", PRIMARY_COLOR, Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(120, 40));

        topBar.add(txtSearch);
        topBar.add(new JLabel("Trạng thái:"));
        topBar.add(cbStatusFilter);
        topBar.add(btnRefresh);

        // 2. Bảng Danh Sách Đơn Hàng
        String[] cols = {"Mã Đơn", "Thời Gian", "Loại Đơn", "Trạng Thái", "Tổng Tiền"};
        orderTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        orderTable = new JTable(orderTableModel);
        styleTable(orderTable);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Chỉnh độ rộng cột
        TableColumnModel tcm = orderTable.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(80);
        tcm.getColumn(1).setPreferredWidth(150);
        tcm.getColumn(2).setPreferredWidth(150);
        tcm.getColumn(3).setPreferredWidth(150);
        tcm.getColumn(4).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================================
    // KHU VỰC BÊN PHẢI: CHI TIẾT & CẬP NHẬT TRẠNG THÁI
    // ==========================================================
    private JPanel createRightPanel(int width) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setPreferredSize(new Dimension(width, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        // 1. Tiêu đề & Thông tin đơn
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        lblOrderId = new JLabel("Chưa chọn đơn hàng");
        lblOrderId.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblOrderId.setForeground(PRIMARY_COLOR);

        lblOrderTime = new JLabel("Thời gian: --");
        lblOrderTime.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblOrderTime.setForeground(TEXT_MUTED);

        lblOrderType = new JLabel("Loại: --");
        lblOrderType.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOrderType.setForeground(TEXT_DARK);

        lblOrderStatus = new JLabel("Trạng thái: --");
        lblOrderStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOrderStatus.setForeground(DANGER_COLOR);

        infoPanel.add(lblOrderId);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(lblOrderTime);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(lblOrderType);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(lblOrderStatus);

        // 2. Bảng Chi Tiết Món
        String[] detailCols = {"Tên Món (Kèm Size/Topping)", "SL", "Thành Tiền"};
        detailTableModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        detailTable = new JTable(detailTableModel);
        styleTable(detailTable);
        detailTable.setRowHeight(65); // Cao hơn chút để chứa text dài

        TableColumnModel dtcm = detailTable.getColumnModel();
        dtcm.getColumn(0).setPreferredWidth(250);
        dtcm.getColumn(1).setPreferredWidth(40);
        dtcm.getColumn(2).setPreferredWidth(100);

        JScrollPane detailScroll = new JScrollPane(detailTable);
        detailScroll.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(new Color(230, 230, 230)), "Chi Tiết Sản Phẩm",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), PRIMARY_COLOR
        ));
        detailScroll.getViewport().setBackground(Color.WHITE);

        // 3. Footer (Tổng tiền & Các nút hành động)
        JPanel footerPanel = new JPanel(new BorderLayout(0, 15));
        footerPanel.setOpaque(false);

        lblTotalAmount = new JLabel("Tổng cộng: 0 đ", SwingConstants.RIGHT);
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalAmount.setForeground(DANGER_COLOR);
        footerPanel.add(lblTotalAmount, BorderLayout.NORTH);

        // Các nút hành động (Grid 2 dòng 2 cột)
        JPanel actionPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        actionPanel.setOpaque(false);

        btnAccept = createModernButton("Tiếp nhận", INFO_COLOR, Color.WHITE);
        btnComplete = createModernButton("Hoàn thành (Trừ kho)", PRIMARY_COLOR, Color.WHITE);
        btnPay = createModernButton("Đã thanh toán", PRIMARY_COLOR, Color.WHITE);
        btnCancel = createModernButton("Hủy đơn", DANGER_COLOR, Color.WHITE);

        // Mặc định vô hiệu hóa khi chưa chọn đơn
        setActionsEnabled(false);

        actionPanel.add(btnAccept);
        actionPanel.add(btnComplete);
        actionPanel.add(btnPay);
        actionPanel.add(btnCancel);

        footerPanel.add(actionPanel, BorderLayout.CENTER);

        // Gắn vào Right Panel
        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(detailScroll, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================================
    // UTILS & STYLE
    // ==========================================================
    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 245, 245));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30));
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (!isEnabled()) {
                    g2.setColor(new Color(220, 220, 220)); // Màu xám khi disabled
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ==========================================================
    // API CUNG CẤP CHO CONTROLLER
    // ==========================================================
    
    // Listeners
    public void addSearchListener(java.awt.event.KeyListener listener) { txtSearch.addKeyListener(listener); }
    public void addFilterListener(ActionListener listener) { cbStatusFilter.addActionListener(listener); }
    public void addRefreshListener(ActionListener listener) { btnRefresh.addActionListener(listener); }
    public void addTableSelectionListener(javax.swing.event.ListSelectionListener listener) { orderTable.getSelectionModel().addListSelectionListener(listener); }
    
    public void addAcceptListener(ActionListener listener) { btnAccept.addActionListener(listener); }
    public void addCompleteListener(ActionListener listener) { btnComplete.addActionListener(listener); }
    public void addPayListener(ActionListener listener) { btnPay.addActionListener(listener); }
    public void addCancelListener(ActionListener listener) { btnCancel.addActionListener(listener); }

    // Getters
    public String getSearchText() { return txtSearch.getText().trim(); }
    public String getSelectedFilter() { return (String) cbStatusFilter.getSelectedItem(); }
    public int getSelectedOrderRow() { return orderTable.getSelectedRow(); }
    
    // Setters để hiển thị dữ liệu
    public DefaultTableModel getOrderTableModel() { return orderTableModel; }
    public DefaultTableModel getDetailTableModel() { return detailTableModel; }

    public void setOrderInfo(String id, String time, String type, String status, String total) {
        lblOrderId.setText("Mã đơn: #" + id);
        lblOrderTime.setText("Thời gian: " + time);
        lblOrderType.setText("Loại: " + type);
        lblOrderStatus.setText("Trạng thái: " + status);
        lblTotalAmount.setText("Tổng cộng: " + total);
        
        // Đổi màu Label trạng thái cho sinh động
        switch (status) {
            case "Chờ tiếp nhận": lblOrderStatus.setForeground(Color.ORANGE); break;
            case "Đang pha chế": lblOrderStatus.setForeground(INFO_COLOR); break;
            case "Hoàn thành": 
            case "Đã thanh toán": lblOrderStatus.setForeground(PRIMARY_COLOR); break;
            case "Đã hủy": lblOrderStatus.setForeground(DANGER_COLOR); break;
            default: lblOrderStatus.setForeground(TEXT_DARK);
        }
    }

    public void clearOrderInfo() {
        lblOrderId.setText("Chưa chọn đơn hàng");
        lblOrderTime.setText("Thời gian: --");
        lblOrderType.setText("Loại: --");
        lblOrderStatus.setText("Trạng thái: --");
        lblOrderStatus.setForeground(TEXT_MUTED);
        lblTotalAmount.setText("Tổng cộng: 0 đ");
        detailTableModel.setRowCount(0);
        setActionsEnabled(false);
    }

    /**
     * Tự động bật/tắt các nút thao tác dựa trên Trạng thái hiện tại của đơn hàng.
     * Logic luồng: Chờ tiếp nhận -> Đang pha chế (Tiếp nhận) -> Hoàn thành (Trừ kho) -> Đã thanh toán.
     */
    public void updateActionButtons(String status) {
        setActionsEnabled(true); // Bật hết lên trước
        
        if (status.equals("Chờ tiếp nhận")) {
            btnComplete.setEnabled(false);
            btnPay.setEnabled(false);
        } 
        else if (status.equals("Đang pha chế")) {
            btnAccept.setEnabled(false);
            btnPay.setEnabled(false);
        } 
        else if (status.equals("Hoàn thành")) {
            btnAccept.setEnabled(false);
            btnComplete.setEnabled(false);
            // Vẫn cho phép Hủy hoặc Thanh toán
        } 
        else if (status.equals("Đã thanh toán") || status.equals("Đã hủy")) {
            // Đơn đã kết thúc vòng đời -> Khóa tất cả các nút
            setActionsEnabled(false);
        }
    }

    private void setActionsEnabled(boolean enabled) {
        btnAccept.setEnabled(enabled);
        btnComplete.setEnabled(enabled);
        btnPay.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
    }
}