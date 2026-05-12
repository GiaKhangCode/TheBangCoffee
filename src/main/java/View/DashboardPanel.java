package View;

import Common.ComponentUI;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import com.toedter.calendar.JDateChooser;

/**
 * Dashboard Panel - Giao diện Báo cáo & Thống kê (The Bang Coffee).
 * Thiết kế đồng bộ với RolePanel (Có 5 Tab chức năng).
 */
public class DashboardPanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);
    private final Color BG_LIGHT = new Color(248, 249, 250);

    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int screenW = screenSize.width;
    private int screenH = screenSize.height;

    private int titleFont = Math.max(16, (int)(screenW * 0.012));
    private int normalFont = Math.max(14, (int)(screenW * 0.010));
    private int labelFont = Math.max(14, (int)(screenW * 0.011));

    private JTabbedPane tabbedPane;
    
    private StatCard cardTotalRevenue;
    private StatCard cardTotalOrders;
    private StatCard cardAvgOrder;
    private DefaultTableModel topSellingTableModel;
    
    private DefaultCategoryDataset revenueDataset;
    private DefaultPieDataset categoryDataset;
    
    private StatCard cardTotalInventory;
    private StatCard cardLowStockCount;
    private StatCard cardImportMonth;
    private DefaultTableModel expiringTableModel; 
    private DefaultTableModel mostUsedTableModel; 
    
    private JComboBox<String> cbFilterRevenue;
    private JButton btnFilterRevenue;
    
    private JFreeChart revenueBarChart;

    public DashboardPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Khởi tạo TabbedPane mang phong cách Modern
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, normalFont));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(TEXT_DARK);
        
        // Thêm 5 tab Báo cáo
        tabbedPane.addTab("Báo cáo Doanh thu", createRevenueTab());
        tabbedPane.addTab("Báo cáo Bán hàng", createSalesTab());
        tabbedPane.addTab("Báo cáo Ca làm việc", createShiftTab());
        tabbedPane.addTab("Báo cáo Kho", createInventoryTab());
        tabbedPane.addTab("Báo cáo Khách hàng", createCustomerTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================================
    // TAB 1: BÁO CÁO DOANH THU (Doanh thu tổng quan, Biểu đồ)
    // ==========================================================
    private JPanel createRevenueTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Stats Layer (3 thẻ thống kê trên cùng)
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        cardTotalRevenue = new StatCard("Tổng Doanh Thu Ngày", "0 đ", "Cập nhật...");
        cardTotalOrders = new StatCard("Tổng Số Đơn", "0", "");
        cardAvgOrder = new StatCard("Doanh Thu Trung Bình/Đơn", "0 đ", "");
        
        statsPanel.add(cardTotalRevenue);
        statsPanel.add(cardTotalOrders);
        statsPanel.add(cardAvgOrder);
        
        // 2. Center Layer (Biểu đồ và Bộ lọc)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        // Filter Bar (Bộ lọc thời gian)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);
        cbFilterRevenue = new JComboBox<>(new String[]{"Hôm nay", "Tuần này", "Tháng này", "Tùy chỉnh"});
        btnFilterRevenue = ComponentUI.createModernButton("Lọc dữ liệu", PRIMARY_COLOR, Color.WHITE);
        
        filterPanel.add(cbFilterRevenue);
        filterPanel.add(btnFilterRevenue);

        revenueDataset = new DefaultCategoryDataset();
        
        revenueBarChart = ChartFactory.createBarChart(
                "Doanh Thu 7 Ngày Gần Nhất", // Tiêu đề biểu đồ
                "Ngày",                      // Trục X
                "Doanh thu (VNĐ)",           // Trục Y
                revenueDataset,              // Dữ liệu
                PlotOrientation.VERTICAL,
                false, true, false
        );

        // Customize giao diện biểu đồ cho hiện đại (Material Design)
        CategoryPlot plot = revenueBarChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE); // Nền trắng
        plot.setRangeGridlinePaint(new Color(220, 220, 220)); // Đường kẻ ngang màu xám nhạt
        plot.setOutlineVisible(false); // Bỏ viền đen ngoài cùng

        // Đổi màu cột thành màu xanh chủ đạo của The Bang Coffee
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, PRIMARY_COLOR); 
        renderer.setBarPainter(new StandardBarPainter()); // Bỏ hiệu ứng gradient bóng bẩy cũ kĩ của Java
        renderer.setMaximumBarWidth(0.1); // Chỉnh độ rộng của cột cho thanh mảnh
        
        // Căn chỉnh Font chữ
        revenueBarChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Nhúng biểu đồ vào Panel
        ChartPanel chartPanel = new ChartPanel(revenueBarChart);
        chartPanel.setPreferredSize(new Dimension(800, 350));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(chartPanel, BorderLayout.CENTER);
        
        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================================
    // TAB 2: BÁO CÁO BÁN HÀNG (Món bán chạy, Tỉ lệ hủy đơn)
    // ==========================================================
    private JPanel createSalesTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTarget = new JLabel("Thống Kê Sản Phẩm Bán Chạy");
        lblTarget.setFont(new Font("Segoe UI", Font.BOLD, labelFont));
        lblTarget.setForeground(PRIMARY_COLOR);

        // Bảng dữ liệu món bán chạy
        String[] columns = {"STT", "Tên Món Nước", "Danh Mục", "Số Lượng Bán", "Doanh Thu Mang Lại"};
        topSellingTableModel = new DefaultTableModel(null, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(topSellingTableModel);
        ComponentUI.styleTable(table, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        table.setRowHeight((int)(screenH * 0.052));

        JPanel tableWrapper = new JPanel(new BorderLayout(0, 10));
        tableWrapper.setOpaque(false);
        tableWrapper.add(lblTarget, BorderLayout.NORTH);
        tableWrapper.add(new JScrollPane(table), BorderLayout.CENTER);

        // Khung biểu đồ tròn Placeholder (Tỉ lệ danh mục)
        categoryDataset = new DefaultPieDataset();
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Tỉ Lệ Bán Theo Danh Mục", // Tiêu đề
                categoryDataset,           // Dữ liệu
                true,                      // Có hiện chú thích (Legend)
                true, false
        );

        // Customize cho biểu đồ tròn đẹp hơn
        pieChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        pieChart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        PiePlot piePlot = (PiePlot) pieChart.getPlot();
        piePlot.setBackgroundPaint(Color.WHITE); // Nền trắng
        piePlot.setOutlineVisible(false); // Bỏ viền
        piePlot.setShadowPaint(null); // Bỏ đổ bóng cho phẳng (Flat design)
        
        // Cài đặt hiển thị Label (Ví dụ: "Trà sữa: 60%")
        piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}")); // {0} là Tên, {2} là %
        piePlot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        piePlot.setLabelBackgroundPaint(Color.WHITE);

        // Nhúng biểu đồ vào Panel
        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setPreferredSize(new Dimension((int)(screenW * 0.3), 0));
        pieChartPanel.setBackground(Color.WHITE);
        pieChartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(tableWrapper, BorderLayout.CENTER);
        panel.add(pieChartPanel, BorderLayout.EAST);

        return panel;
    }

    // ==========================================================
    // TAB 3: BÁO CÁO CA LÀM VIỆC (Hiệu suất nhân viên)
    // ==========================================================
    private JPanel createShiftTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20)); 

        JLabel lblTitle = new JLabel("Hiệu Suất Theo Ca Làm Việc & Nhân Viên");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, titleFont));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setForeground(PRIMARY_COLOR);
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);

        String[] cols = {"Mã NV", "Tên Nhân Viên", "Số Ca Làm", "Tổng Số Đơn Đã Tạo", "Doanh Thu Mang Về"};
        DefaultTableModel model = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int column) { return false; } 
        };
        JTable table = new JTable(model);
        ComponentUI.styleTable(table, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        table.setRowHeight((int)(screenH * 0.040)); 
        
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnExport = ComponentUI.createModernButton("Xuất báo cáo Excel", PRIMARY_COLOR, Color.WHITE);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnExport);

        panel.add(tablePanel, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================================
    // TAB 4: BÁO CÁO KHO (Xuất/Nhập/Tồn)
    // ==========================================================
    private JPanel createInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Stats Layer 
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        cardTotalInventory = new StatCard("Tổng Vốn Tồn Kho", "0 đ", "");
        cardLowStockCount = new StatCard("Nguyên Liệu Sắp Hết", "0", "Cần nhập thêm gấp");
        cardImportMonth = new StatCard("Tiền Đã Nhập (Tháng)", "0 đ", "");
        
        statsPanel.add(cardTotalInventory);
        statsPanel.add(cardLowStockCount);
        statsPanel.add(cardImportMonth);
        
        // 2. Center Layer - Chia làm 2 bảng
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // Chia cột 1:2 khoảng cách 20px
        centerPanel.setOpaque(false);

        // --- BẢNG 1: NGUYÊN LIỆU GẦN HẾT HẠN ---
        JPanel expiringPanel = new JPanel(new BorderLayout(0, 10));
        expiringPanel.setOpaque(false);
        JLabel lblExpiring = new JLabel("Nguyên liệu sắp hết hạn (≤ 7 ngày)");
        lblExpiring.setFont(new Font("Segoe UI", Font.BOLD, labelFont));
        lblExpiring.setForeground(new Color(220, 53, 69)); // Màu đỏ cảnh báo

        String[] colsExpiring = {"STT", "Lô", "Tên Nguyên Liệu", "Còn Lại", "Hạn Sử Dụng"};
        expiringTableModel = new DefaultTableModel(null, colsExpiring) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tableExpiring = new JTable(expiringTableModel);
        ComponentUI.styleTable(tableExpiring, TEXT_DARK, TEXT_DARK, new Color(220, 53, 69));
        tableExpiring.setRowHeight((int)(screenH * 0.045));
        tableExpiring.getColumnModel().getColumn(0).setPreferredWidth(40);
        
        expiringPanel.add(lblExpiring, BorderLayout.NORTH);
        expiringPanel.add(new JScrollPane(tableExpiring), BorderLayout.CENTER);

        // --- BẢNG 2: TOP TIÊU HAO NHIỀU NHẤT ---
        JPanel mostUsedPanel = new JPanel(new BorderLayout(0, 10));
        mostUsedPanel.setOpaque(false);
        JLabel lblMostUsed = new JLabel("Top tiêu hao nhiều nhất (Tháng này)");
        lblMostUsed.setFont(new Font("Segoe UI", Font.BOLD, labelFont));
        lblMostUsed.setForeground(PRIMARY_COLOR);

        String[] colsMostUsed = {"STT", "Tên Nguyên Liệu", "Tổng Tiêu Hao"};
        mostUsedTableModel = new DefaultTableModel(null, colsMostUsed) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tableMostUsed = new JTable(mostUsedTableModel);
        ComponentUI.styleTable(tableMostUsed, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        tableMostUsed.setRowHeight((int)(screenH * 0.045));
        tableMostUsed.getColumnModel().getColumn(0).setPreferredWidth(40);

        mostUsedPanel.add(lblMostUsed, BorderLayout.NORTH);
        mostUsedPanel.add(new JScrollPane(tableMostUsed), BorderLayout.CENTER);

        // Add 2 bảng vào layout
        centerPanel.add(expiringPanel);
        centerPanel.add(mostUsedPanel);

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================================
    // TAB 5: BÁO CÁO KHÁCH HÀNG (Lịch sử, Hạng thẻ)
    // ==========================================================
    private JPanel createCustomerTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topControlsPanel.setOpaque(false);
        topControlsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Tra cứu Khách Hàng", 0, 0, 
                new Font("Segoe UI", Font.BOLD, normalFont), TEXT_DARK));

        JTextField txtPhone = new JTextField();
        txtPhone.setPreferredSize(new Dimension(250, 35));
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, normalFont));
        
        JButton btnSearch = ComponentUI.createModernButton("Tìm Kiếm", PRIMARY_COLOR, Color.WHITE);

        topControlsPanel.add(new JLabel("Số điện thoại:"));
        topControlsPanel.add(txtPhone);
        topControlsPanel.add(btnSearch);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Lịch sử mua hàng & Tích điểm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, labelFont));
        lblTitle.setForeground(PRIMARY_COLOR);

        String[] cols = {"Mã Đơn", "Ngày Mua", "Sản Phẩm Đã Mua", "Thành Tiền", "Điểm Tích Lũy"};
        DefaultTableModel model = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int column) { return false; } 
        };
        JTable table = new JTable(model);
        ComponentUI.styleTable(table, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        table.setRowHeight((int)(screenH * 0.040)); 
        
        tablePanel.add(lblTitle, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(topControlsPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }
    
    // Lấy giá trị đang chọn trong ComboBox
    public String getSelectedRevenueFilter() {
        return cbFilterRevenue.getSelectedItem().toString();
    }

    // Gắn tai nghe sự kiện cho nút "Lọc dữ liệu"
    public void addRevenueFilterListener(ActionListener listener) {
        btnFilterRevenue.addActionListener(listener);
    }
    
    public void updateRevenueCards(String totalRev, String totalOrders, String avgOrder) {
        // Class StatCard của bạn có hàm setValue() chứ? Nếu không, bạn cần sửa lớp StatCard để nó có hàm update giá trị.
        cardTotalRevenue.setValue(totalRev); 
        cardTotalOrders.setValue(totalOrders);
        cardAvgOrder.setValue(avgOrder);
    }

    public void updateTopSellingTable(java.util.List<Object[]> dataList) {
        topSellingTableModel.setRowCount(0); // Xóa dữ liệu cũ
        int stt = 1;
        for (Object[] row : dataList) {
            topSellingTableModel.addRow(new Object[]{
                stt++, 
                row[0], // Tên món
                row[1], // Danh mục
                row[2], // SL Bán
                String.format("%,d đ", (Long) row[3]) // Doanh thu mang lại
            });
        }
    }
    
    public void updateRevenueChart(java.util.List<Object[]> dataList, String chartTitle) {
        revenueBarChart.setTitle(new org.jfree.chart.title.TextTitle(chartTitle, new Font("Segoe UI", Font.BOLD, 18)));
        
        revenueDataset.clear(); 
        for (Object[] row : dataList) {
            String dateStr = (String) row[0];
            Long revenue = (Long) row[1];
            revenueDataset.addValue(revenue, "Doanh Thu", dateStr); 
        }
    }
    
    public void updateCategoryPieChart(java.util.List<Object[]> dataList) {
        categoryDataset.clear();
        for (Object[] row : dataList) {
            String category = (String) row[0];
            Integer quantity = (Integer) row[1];
            categoryDataset.setValue(category, quantity);
        }
    }
    
    public void updateInventoryCards(String totalInventory, String lowStock, String importMonth) {
        cardTotalInventory.setValue(totalInventory);
        cardLowStockCount.setValue(lowStock);
        if (!lowStock.equals("0")) cardLowStockCount.setForeground(Color.RED); 
        cardImportMonth.setValue(importMonth);
    }

    public void updateExpiringTable(java.util.List<Object[]> dataList) {
        expiringTableModel.setRowCount(0); 
        int stt = 1;
        for (Object[] row : dataList) {
            // STT, Mã Lô, Tên, Số lượng còn, HSD
            expiringTableModel.addRow(new Object[]{ stt++, row[0], row[1], row[2], row[3] });
        }
    }

    public void updateMostUsedTable(java.util.List<Object[]> dataList) {
        mostUsedTableModel.setRowCount(0); 
        int stt = 1;
        for (Object[] row : dataList) {
            // STT, Tên NL, Tổng tiêu hao
            mostUsedTableModel.addRow(new Object[]{ stt++, row[0], row[1] });
        }
    }
    
    // Hiển thị Popup chọn ngày (Dùng thư viện jcalendar)
    public java.util.Date[] showCustomDateDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn khoảng thời gian", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this); // Hiển thị ra giữa màn hình
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel lblStart = new JLabel("Từ ngày:");
        lblStart.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JDateChooser dcStart = new JDateChooser();
        dcStart.setDateFormatString("dd/MM/yyyy");
        dcStart.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel lblEnd = new JLabel("Đến ngày:");
        lblEnd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JDateChooser dcEnd = new JDateChooser();
        dcEnd.setDateFormatString("dd/MM/yyyy");
        dcEnd.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(lblStart);
        panel.add(dcStart);
        panel.add(lblEnd);
        panel.add(dcEnd);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);
        JButton btnOK = ComponentUI.createModernButton("Xác nhận", PRIMARY_COLOR, Color.WHITE);
        JButton btnCancel = ComponentUI.createModernButton("Hủy", Color.GRAY, Color.WHITE);

        // Mảng để lưu trữ ngày kết quả trả về
        java.util.Date[] result = new java.util.Date[2];

        btnOK.addActionListener(e -> {
            if (dcStart.getDate() == null || dcEnd.getDate() == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn đầy đủ Từ ngày và Đến ngày!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dcStart.getDate().after(dcEnd.getDate())) {
                JOptionPane.showMessageDialog(dialog, "Từ ngày không được lớn hơn Đến ngày!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Lưu kết quả và đóng dialog
            result[0] = dcStart.getDate();
            result[1] = dcEnd.getDate();
            dialog.dispose();
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnOK);
        btnPanel.add(btnCancel);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true); // Mở cửa sổ lên (nó sẽ chặn màn hình lại cho đến khi tắt)

        return result[0] != null ? result : null;
    }

    // ==========================================================
    // CÁC CLASS HỖ TRỢ VẼ GIAO DIỆN (Giữ nguyên từ bản cũ)
    // ==========================================================
    private static class RoundedChartPlaceholder extends JPanel {
        private String message;

        public RoundedChartPlaceholder(String message) {
            this.message = message;
            setLayout(new BorderLayout());
            setOpaque(false);
            
            JLabel lblMsg = new JLabel(message);
            lblMsg.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
            lblMsg.setForeground(Color.GRAY);
            add(lblMsg, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(240, 240, 240)); // Màu xám nhạt làm nền chờ
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15); // Viền
            g2.dispose();
            super.paintComponent(g);
        }
    }
}