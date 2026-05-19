package View;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class OrderPanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);
    private final Color BG_LIGHT = new Color(248, 249, 250);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color INFO_COLOR = new Color(41, 128, 185);
    private final Color WARNING_COLOR = new Color(243, 156, 18);
    private final Color PRINT_COLOR = new Color(142, 68, 173); // [MỚI] Màu tím cho nút In hóa đơn

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
    
    // Tách thành 2 Label trạng thái
    private JLabel lblPrepStatus;
    private JLabel lblPayStatus;
    
    private DefaultTableModel detailTableModel;
    private JTable detailTable;
    private JLabel lblTotalAmount;
    
    // [MỚI] Các Label hiển thị Điểm sử dụng và Số tiền giảm giá
    private JLabel lblPointsUsed;
    private JLabel lblDiscountAmount;

    // Nút cập nhật trạng thái
    private JButton btnAccept;   // Tiếp nhận
    private JButton btnComplete; // Hoàn thành
    private JButton btnPay;      // Đã thanh toán
    private JButton btnCancel;   // Hủy đơn
    private JButton btnPrintInvoice; // [MỚI] In / Xuất hóa đơn

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

        String[] statuses = {"Tất cả", "Chờ tiếp nhận", "Đang pha chế", "Đã hoàn thành", "Chưa thanh toán", "Đã thanh toán", "Đã hủy"};
        cbStatusFilter = new JComboBox<>(statuses);
        cbStatusFilter.setPreferredSize(new Dimension(150, 40));
        cbStatusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbStatusFilter.setBackground(Color.WHITE);

        btnRefresh = createModernButton("Làm mới", PRIMARY_COLOR, Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(120, 40));

        topBar.add(txtSearch);
        topBar.add(new JLabel("Lọc trạng thái:"));
        topBar.add(cbStatusFilter);
        topBar.add(btnRefresh);

        // 2. Bảng Danh Sách Đơn Hàng
        String[] cols = {"Mã Đơn", "Thời Gian", "Loại Đơn", "Pha Chế", "Thanh Toán", "Tổng Tiền"};
        orderTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        orderTable = new JTable(orderTableModel);
        styleTable(orderTable);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Chỉnh độ rộng cột
        TableColumnModel tcm = orderTable.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(70);
        tcm.getColumn(1).setPreferredWidth(130);
        tcm.getColumn(2).setPreferredWidth(100);
        tcm.getColumn(3).setPreferredWidth(120); 
        tcm.getColumn(4).setPreferredWidth(120); 
        tcm.getColumn(5).setPreferredWidth(110);

        // Gắn Custom Renderer để tô màu 2 cột trạng thái
        StatusCellRenderer statusRenderer = new StatusCellRenderer();
        tcm.getColumn(3).setCellRenderer(statusRenderer);
        tcm.getColumn(4).setCellRenderer(statusRenderer);

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

        lblPrepStatus = new JLabel("Pha chế: --");
        lblPrepStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        lblPayStatus = new JLabel("Thanh toán: --");
        lblPayStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // [MỚI] Khởi tạo các label điểm và giảm giá
        lblPointsUsed = new JLabel("Điểm đã dùng: --");
        lblPointsUsed.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPointsUsed.setForeground(TEXT_DARK);
        
        lblDiscountAmount = new JLabel("Giảm giá: --");
        lblDiscountAmount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDiscountAmount.setForeground(DANGER_COLOR);

        infoPanel.add(lblOrderId);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(lblOrderTime);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(lblOrderType);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(lblPrepStatus);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(lblPayStatus);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(lblPointsUsed);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(lblDiscountAmount);

        // 2. Bảng Chi Tiết Món
        String[] detailCols = {"Tên Món (Kèm Size/Topping)", "SL", "Thành Tiền"};
        detailTableModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        detailTable = new JTable(detailTableModel);
        styleTable(detailTable);
        detailTable.setRowHeight(65); 

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

        // Các nút hành động [ĐÃ CẬP NHẬT: Lưới 3 dòng, 2 cột để chứa 5 nút]
        JPanel actionPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        actionPanel.setOpaque(false);

        btnAccept = createModernButton("Tiếp nhận món", INFO_COLOR, Color.WHITE);
        btnComplete = createModernButton("Hoàn thành món", SUCCESS_COLOR, Color.WHITE);
        btnPay = createModernButton("Xác nhận đã thu tiền", PRIMARY_COLOR, Color.WHITE);
        btnCancel = createModernButton("Hủy đơn hàng", DANGER_COLOR, Color.WHITE);
        
        // [MỚI] Nút In / Xuất hóa đơn
        btnPrintInvoice = createModernButton("In Hóa Đơn", PRINT_COLOR, Color.WHITE);

        setActionsEnabled(false);

        actionPanel.add(btnAccept);
        actionPanel.add(btnComplete);
        actionPanel.add(btnPay);
        actionPanel.add(btnCancel);
        actionPanel.add(btnPrintInvoice); // Thêm nút In vào Lưới

        footerPanel.add(actionPanel, BorderLayout.CENTER);

        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(detailScroll, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================================
    // CUSTOM CELL RENDERER ĐỂ TÔ MÀU TRẠNG THÁI
    // ==========================================================
    class StatusCellRenderer extends DefaultTableCellRenderer {
        public StatusCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER); // [MỚI] Căn giữa văn bản cho cột trạng thái
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String status = value.toString();
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                
                switch (status) {
                    case "Chờ tiếp nhận": setForeground(WARNING_COLOR); break;
                    case "Đang pha chế": setForeground(INFO_COLOR); break;
                    case "Đã hoàn thành": 
                    case "Đã thanh toán": setForeground(SUCCESS_COLOR); break;
                    case "Chưa thanh toán": 
                    case "Đã hủy": 
                    case "Đã hoàn tiền": setForeground(DANGER_COLOR); break;
                    default: setForeground(TEXT_DARK);
                }
                
                if (isSelected) setForeground(Color.WHITE); 
            }
            return c;
        }
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
        table.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 180));
        
        // [MỚI] Căn giữa nội dung dữ liệu cho toàn bộ các cột trong bảng
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
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
                    g2.setColor(new Color(220, 220, 220)); 
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
    // [MỚI] Listener cho nút In hóa đơn
    public void addPrintInvoiceListener(ActionListener listener) { btnPrintInvoice.addActionListener(listener); }

    // Getters
    public String getSearchText() { return txtSearch.getText().trim(); }
    public String getSelectedFilter() { return (String) cbStatusFilter.getSelectedItem(); }
    public int getSelectedOrderRow() { return orderTable.getSelectedRow(); }
    
    public DefaultTableModel getOrderTableModel() { return orderTableModel; }
    public DefaultTableModel getDetailTableModel() { return detailTableModel; }

    public void setOrderInfo(String id, String time, String type, String prepStatus, String payStatus, String total) {
        setOrderInfo(id, time, type, prepStatus, payStatus, total, 0, 0);
    }

    // [MỚI] Nạp chồng phương thức setOrderInfo để cập nhật thêm Điểm đã dùng và Số tiền giảm giá
    public void setOrderInfo(String id, String time, String type, String prepStatus, String payStatus, String total, int pointsUsed, long discountAmount) {
        lblOrderId.setText("Mã đơn: #" + id);
        lblOrderTime.setText("Thời gian: " + time);
        lblOrderType.setText("Loại: " + type);
        
        lblPrepStatus.setText("Pha chế: " + prepStatus);
        lblPayStatus.setText("Thanh toán: " + payStatus);
        
        lblTotalAmount.setText("Tổng cộng: " + total);
        
        lblPointsUsed.setText("Điểm đã dùng: " + pointsUsed + " điểm");
        if (discountAmount > 0) {
            lblDiscountAmount.setText("Giảm giá: -" + String.format("%,d đ", discountAmount));
            lblDiscountAmount.setVisible(true);
        } else {
            lblDiscountAmount.setText("Giảm giá: 0 đ");
            lblDiscountAmount.setVisible(false);
        }
        
        // Cập nhật màu cho Label Pha Chế
        switch (prepStatus) {
            case "Chờ tiếp nhận": lblPrepStatus.setForeground(WARNING_COLOR); break;
            case "Đang pha chế": lblPrepStatus.setForeground(INFO_COLOR); break;
            case "Đã hoàn thành": lblPrepStatus.setForeground(SUCCESS_COLOR); break;
            case "Đã hủy": lblPrepStatus.setForeground(DANGER_COLOR); break;
            default: lblPrepStatus.setForeground(TEXT_DARK);
        }
        
        // Cập nhật màu cho Label Thanh Toán
        switch (payStatus) {
            case "Chưa thanh toán": 
            case "Đã hoàn tiền": lblPayStatus.setForeground(DANGER_COLOR); break;
            case "Đã thanh toán": lblPayStatus.setForeground(SUCCESS_COLOR); break;
            default: lblPayStatus.setForeground(TEXT_DARK);
        }
    }

    public void clearOrderInfo() {
        lblOrderId.setText("Chưa chọn đơn hàng");
        lblOrderTime.setText("Thời gian: --");
        lblOrderType.setText("Loại: --");
        
        lblPrepStatus.setText("Pha chế: --");
        lblPrepStatus.setForeground(TEXT_MUTED);
        
        lblPayStatus.setText("Thanh toán: --");
        lblPayStatus.setForeground(TEXT_MUTED);
        
        // [MỚI] Xóa thông tin điểm và giảm giá
        lblPointsUsed.setText("Điểm đã dùng: --");
        lblDiscountAmount.setText("Giảm giá: --");
        lblDiscountAmount.setVisible(true);
        
        lblTotalAmount.setText("Tổng cộng: 0 đ");
        detailTableModel.setRowCount(0);
        setActionsEnabled(false);
    }

    public void updateActionButtons(String prepStatus, String payStatus) {
        // Miễn là có chọn 1 đơn hàng và chưa bị hủy, nút in hóa đơn sẽ khả dụng
        btnPrintInvoice.setEnabled(!prepStatus.equals("Đã hủy"));

        // Nếu đơn đã hủy -> Đóng băng toàn bộ các nút xử lý trạng thái và in hóa đơn
        if (prepStatus.equals("Đã hủy")) {
            btnAccept.setEnabled(false);
            btnComplete.setEnabled(false);
            btnPay.setEnabled(false);
            btnCancel.setEnabled(false);
            btnPrintInvoice.setEnabled(false);
            return;
        }

        // Logic Bếp (Pha chế)
        btnAccept.setEnabled(prepStatus.equals("Chờ tiếp nhận"));
        btnComplete.setEnabled(prepStatus.equals("Đang pha chế"));
        
        // Logic Thu Ngân (Thanh toán)
        btnPay.setEnabled(payStatus.equals("Chưa thanh toán"));
        
        // Nút hủy chỉ khả dụng khi chưa giao xong cho khách
        btnCancel.setEnabled(!prepStatus.equals("Đã hoàn thành"));
    }

    private void setActionsEnabled(boolean enabled) {
        btnAccept.setEnabled(enabled);
        btnComplete.setEnabled(enabled);
        btnPay.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
        btnPrintInvoice.setEnabled(enabled); // [CẬP NHẬT] Đóng băng cả nút In khi chưa chọn đơn nào
    }
    
    // --- PHÂN QUYỀN ---
    private boolean hasEditPermission = true;
    private boolean hasDeletePermission = true;
    
    public void setActionPermissions(boolean canEdit, boolean canDelete, boolean canPrint) {
        this.hasEditPermission = canEdit;
        this.hasDeletePermission = canDelete;
        
        btnAccept.setVisible(canEdit);
        btnComplete.setVisible(canEdit);
        btnPay.setVisible(canEdit);
        btnCancel.setVisible(canDelete);
        btnPrintInvoice.setVisible(canPrint);
    }
    
    public JButton getBtnAccept() { return btnAccept; }
    public JButton getBtnComplete() { return btnComplete; }
    public JButton getBtnPay() { return btnPay; }
    public JButton getBtnCancel() { return btnCancel; }
}