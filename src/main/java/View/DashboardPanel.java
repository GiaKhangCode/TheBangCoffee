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
import org.jfree.chart.renderer.category.LineAndShapeRenderer; // Bổ sung cho Biểu đồ đường
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import com.toedter.calendar.JDateChooser;

/**
 * Dashboard Panel - Giao diện Báo cáo & Thống kê (The Bang Coffee).
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
    private JButton btnRefresh;
    private JButton btnExportExcel; // Thêm nút Xuất Excel
    
    // Khai báo Báo Cáo Doanh Thu
    private StatCard cardTotalRevenue;
    private StatCard cardTotalOrders;
    private StatCard cardAvgOrder;
    private DefaultTableModel topSellingTableModel;
    private DefaultCategoryDataset revenueDataset;
    private DefaultPieDataset categoryDataset;
    private JComboBox<String> cbFilterRevenue;
    private JButton btnFilterRevenue;
    private JFreeChart revenueBarChart;
    
    // Khai báo Báo Cáo Kho
    private StatCard cardTotalInventory;
    private StatCard cardLowStockCount;
    private StatCard cardImportMonth;
    private DefaultTableModel expiringTableModel; 
    private DefaultTableModel mostUsedTableModel; 
    

    private StatCard cardTotalNewCustomers;
    private StatCard cardRetentionRate;
    private StatCard cardARPU;
    private StatCard cardTotalPoints;
    private JComboBox<String> cbFilterCustomer;
    private JButton btnFilterCustomer;
    private DefaultCategoryDataset customerGrowthDataset;
    private JFreeChart customerGrowthLineChart;
    

    public DashboardPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Tạo thanh tiêu đề trên cùng
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Báo cáo & Thống kê");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, titleFont));
        lblTitle.setForeground(TEXT_DARK);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        btnRefresh = ComponentUI.createModernButton(" Làm mới", PRIMARY_COLOR, Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, normalFont));
        btnRefresh.setPreferredSize(new Dimension(140, 35));
        
        btnExportExcel = ComponentUI.createModernButton(" Xuất Excel", PRIMARY_COLOR, Color.WHITE); // Màu xanh lá Excel
        btnExportExcel.setFont(new Font("Segoe UI", Font.BOLD, normalFont));
        btnExportExcel.setPreferredSize(new Dimension(140, 35));

        JPanel rightActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActionsPanel.setOpaque(false);
        rightActionsPanel.add(btnExportExcel);
        rightActionsPanel.add(btnRefresh);

        headerPanel.add(rightActionsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, normalFont));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(TEXT_DARK);
        
        tabbedPane.addTab("Báo cáo Doanh thu", createRevenueTab());
        tabbedPane.addTab("Báo cáo Bán hàng", createSalesTab());

        tabbedPane.addTab("Báo cáo Kho", createInventoryTab());
        tabbedPane.addTab("Báo cáo Khách hàng", createCustomerTab()); // Gọi hàm tạo Tab mới

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================================
    // TAB 1: BÁO CÁO DOANH THU 
    // ==========================================================
    private JPanel createRevenueTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        cardTotalRevenue = new StatCard("Tổng Doanh Thu Ngày", "0 đ", "Cập nhật...");
        cardTotalOrders = new StatCard("Tổng Số Đơn", "0", "");
        cardAvgOrder = new StatCard("Doanh Thu Trung Bình/Đơn", "0 đ", "");
        
        statsPanel.add(cardTotalRevenue);
        statsPanel.add(cardTotalOrders);
        statsPanel.add(cardAvgOrder);
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);
        cbFilterRevenue = new JComboBox<>(new String[]{"Hôm nay", "Tuần này", "Tháng này", "Tùy chỉnh"});
        btnFilterRevenue = ComponentUI.createModernButton("Lọc dữ liệu", PRIMARY_COLOR, Color.WHITE);
        
        filterPanel.add(cbFilterRevenue);
        filterPanel.add(btnFilterRevenue);

        revenueDataset = new DefaultCategoryDataset();
        
        revenueBarChart = ChartFactory.createBarChart(
                "Doanh Thu 7 Ngày Gần Nhất", 
                "Thời gian",                     
                "Doanh thu (VNĐ)",            
                revenueDataset,              
                PlotOrientation.VERTICAL,
                false, true, false
        );

        CategoryPlot plot = revenueBarChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE); 
        plot.setRangeGridlinePaint(new Color(220, 220, 220)); 
        plot.setOutlineVisible(false); 

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, PRIMARY_COLOR); 
        renderer.setBarPainter(new StandardBarPainter()); 
        renderer.setMaximumBarWidth(0.1); 
        
        revenueBarChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

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
    // TAB 2: BÁO CÁO BÁN HÀNG
    // ==========================================================
    private JPanel createSalesTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTarget = new JLabel("Thống Kê Sản Phẩm Bán Chạy");
        lblTarget.setFont(new Font("Segoe UI", Font.BOLD, labelFont));
        lblTarget.setForeground(PRIMARY_COLOR);

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

        categoryDataset = new DefaultPieDataset();
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Tỉ Lệ Bán Theo Danh Mục", 
                categoryDataset,           
                true,                      
                true, false
        );

        pieChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        pieChart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        PiePlot piePlot = (PiePlot) pieChart.getPlot();
        piePlot.setBackgroundPaint(Color.WHITE); 
        piePlot.setOutlineVisible(false); 
        piePlot.setShadowPaint(null); 
        
        piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}")); 
        piePlot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        piePlot.setLabelBackgroundPaint(Color.WHITE);

        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setPreferredSize(new Dimension((int)(screenW * 0.3), 0));
        pieChartPanel.setBackground(Color.WHITE);
        pieChartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(tableWrapper, BorderLayout.CENTER);
        panel.add(pieChartPanel, BorderLayout.EAST);

        return panel;
    }



    // ==========================================================
    // TAB 4: BÁO CÁO KHO 
    // ==========================================================
    private JPanel createInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        cardTotalInventory = new StatCard("Tổng Vốn Tồn Kho", "0 đ", "");
        cardLowStockCount = new StatCard("Nguyên Liệu Sắp Hết", "0", "Cần nhập thêm gấp");
        cardImportMonth = new StatCard("Tiền Đã Nhập (Tháng)", "0 đ", "");
        
        statsPanel.add(cardTotalInventory);
        statsPanel.add(cardLowStockCount);
        statsPanel.add(cardImportMonth);
        
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0)); 
        centerPanel.setOpaque(false);

        JPanel expiringPanel = new JPanel(new BorderLayout(0, 10));
        expiringPanel.setOpaque(false);
        JLabel lblExpiring = new JLabel("Nguyên liệu sắp hết hạn (≤ 7 ngày)");
        lblExpiring.setFont(new Font("Segoe UI", Font.BOLD, labelFont));
        lblExpiring.setForeground(new Color(220, 53, 69)); 

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

        centerPanel.add(expiringPanel);
        centerPanel.add(mostUsedPanel);

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================================
    // TAB 5: BÁO CÁO KHÁCH HÀNG [ĐÃ ĐƯỢC VIẾT LẠI HOÀN TOÀN]
    // ==========================================================
    private JPanel createCustomerTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Stats Layer (4 thẻ thống kê nằm ngang)
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        
        cardTotalNewCustomers = new StatCard("Khách Hàng Mới", "0", "Người");
        cardRetentionRate = new StatCard("Tỷ Lệ Quay Lại", "0%", "Mua ≥ 2 lần");
        cardARPU = new StatCard("ARPU (Doanh thu/Khách)", "0 đ", "Trung bình");
        cardTotalPoints = new StatCard("Điểm Tích Lũy", "0", "Đã phát hành");

        statsPanel.add(cardTotalNewCustomers);
        statsPanel.add(cardRetentionRate);
        statsPanel.add(cardARPU);
        statsPanel.add(cardTotalPoints);

        // 2. Center Layer (Bộ lọc + Biểu đồ Line Chart)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        // Filter Bar (Lọc theo: Tuần, Tháng, Tùy chỉnh)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);
        cbFilterCustomer = new JComboBox<>(new String[]{"Tuần này", "Tháng này", "Tùy chỉnh"});
        btnFilterCustomer = ComponentUI.createModernButton("Lọc dữ liệu", PRIMARY_COLOR, Color.WHITE);

        JLabel lblFilter = new JLabel("Lọc theo:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        filterPanel.add(lblFilter);
        filterPanel.add(cbFilterCustomer);
        filterPanel.add(btnFilterCustomer);

        // Khởi tạo Biểu đồ đường (Line Chart) cho Tăng trưởng
        customerGrowthDataset = new DefaultCategoryDataset();
        customerGrowthLineChart = ChartFactory.createLineChart(
                "Tốc Độ Tăng Trưởng Khách Hàng Mới", // Tiêu đề biểu đồ
                "Thời gian",                        // Trục X
                "Số lượng khách (Người)",           // Trục Y
                customerGrowthDataset,              // Dữ liệu
                PlotOrientation.VERTICAL,
                false, true, false
        );

        // Customize Biểu đồ đường
        CategoryPlot plot = customerGrowthLineChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE); 
        plot.setRangeGridlinePaint(new Color(220, 220, 220)); 
        plot.setOutlineVisible(false); 

        // Style cho đường nét (dày dặn, có nốt chấm)
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, PRIMARY_COLOR); 
        renderer.setSeriesStroke(0, new BasicStroke(3.0f)); // Chỉnh độ dày của đường
        renderer.setSeriesShapesVisible(0, true); // Hiện các chấm tròn tại điểm giao

        // Căn chỉnh Font
        customerGrowthLineChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

        ChartPanel chartPanel = new ChartPanel(customerGrowthLineChart);
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
    // API CUNG CẤP CHO BÁO CÁO DOANH THU & KHO
    // ==========================================================
    public String getSelectedRevenueFilter() { return cbFilterRevenue.getSelectedItem().toString(); }
    public void addRevenueFilterListener(ActionListener listener) { btnFilterRevenue.addActionListener(listener); }
    
    public void updateRevenueCards(String totalRev, String totalOrders, String avgOrder) {
        cardTotalRevenue.setValue(totalRev); 
        cardTotalOrders.setValue(totalOrders);
        cardAvgOrder.setValue(avgOrder);
    }

    public void updateTopSellingTable(java.util.List<Object[]> dataList) {
        topSellingTableModel.setRowCount(0); 
        int stt = 1;
        for (Object[] row : dataList) {
            topSellingTableModel.addRow(new Object[]{
                stt++, 
                row[0], // Tên món
                row[1], // Danh mục
                row[2], // SL Bán
                String.format("%,d đ", (Long) row[3]) // Doanh thu
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
            expiringTableModel.addRow(new Object[]{ stt++, row[0], row[1], row[2], row[3] });
        }
    }

    public void updateMostUsedTable(java.util.List<Object[]> dataList) {
        mostUsedTableModel.setRowCount(0); 
        int stt = 1;
        for (Object[] row : dataList) {
            mostUsedTableModel.addRow(new Object[]{ stt++, row[0], row[1] });
        }
    }
    
    // ==========================================================
    // API MỚI CHO BÁO CÁO KHÁCH HÀNG
    // ==========================================================
    public String getSelectedCustomerFilter() {
        return cbFilterCustomer.getSelectedItem().toString();
    }

    public void addCustomerFilterListener(ActionListener listener) {
        btnFilterCustomer.addActionListener(listener);
    }

    public void updateCustomerCards(String newCustomers, String retentionRate, String arpu, String totalPoints) {
        cardTotalNewCustomers.setValue(newCustomers);
        cardRetentionRate.setValue(retentionRate);
        cardARPU.setValue(arpu);
        cardTotalPoints.setValue(totalPoints);
    }

    public void updateCustomerGrowthChart(java.util.List<Object[]> dataList, String chartTitle) {
        customerGrowthLineChart.setTitle(new org.jfree.chart.title.TextTitle(chartTitle, new Font("Segoe UI", Font.BOLD, 18)));
        customerGrowthDataset.clear();
        for (Object[] row : dataList) {
            String dateStr = (String) row[0];
            Integer count = (Integer) row[1]; // Số lượng khách hàng mới
            customerGrowthDataset.addValue(count, "Khách Mới", dateStr);
        }
    }

    public void addRefreshListener(ActionListener listener) {
        btnRefresh.addActionListener(listener);
    }
    
    public void addExportExcelListener(ActionListener listener) {
        btnExportExcel.addActionListener(listener);
    }

    public void setSelectedRevenueFilter(String filter) { cbFilterRevenue.setSelectedItem(filter); }

    public void setSelectedCustomerFilter(String filter) { cbFilterCustomer.setSelectedItem(filter); }

    // ==========================================================
    // POPUP TÙY CHỈNH THỜI GIAN
    // ==========================================================
    public java.util.Date[] showCustomDateDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn khoảng thời gian", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this); 
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
            result[0] = dcStart.getDate();
            result[1] = dcEnd.getDate();
            dialog.dispose();
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnOK);
        btnPanel.add(btnCancel);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true); 

        return result[0] != null ? result : null;
    }

    // ==========================================================
    // CLASS HỖ TRỢ VẼ (Giữ nguyên)
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
            g2.setColor(new Color(240, 240, 240)); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15); 
            g2.dispose();
            super.paintComponent(g);
        }
    }
}