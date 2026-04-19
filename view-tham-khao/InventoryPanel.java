/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author SONY
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;

public class InventoryPanel extends JPanel {

    private final Color PRIMARY_GREEN = new Color(67, 142, 104);
    private final Color DANGER_RED = new Color(220, 80, 80);
    private final Color TEXT_DARK = new Color(50, 50, 50);
    private final Color BG_CARD = new Color(255, 255, 255, 230); // Trắng mờ an toàn cho JTable

    public InventoryPanel() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false); // Bắt buộc để fix lỗi bóng ma
        setBorder(new EmptyBorder(10, 20, 20, 20));

        // 1. HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("QUẢN LÝ KHO & ĐỊNH LƯỢNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. MAIN CONTENT (Chia làm 2 phần: Kho và Công thức)
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        // 2.1 Khu vực HÀNG TỒN KHO (Bên trái)
        JPanel inventorySection = buildInventorySection();
        contentPanel.add(inventorySection, BorderLayout.CENTER);

        // 2.2 Khu vực CÔNG THỨC & ĐỊNH LƯỢNG (Bên phải)
        JPanel recipeSection = buildRecipeSection();
        contentPanel.add(recipeSection, BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
    }

    // =========================================================
    // XÂY DỰNG KHU VỰC KHO (TRÁI)
    // =========================================================
    private JPanel buildInventorySection() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // --- Top: Tiêu đề & Nút thao tác ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        
        JLabel lblTitle = new JLabel("HÀNG TỒN KHO & CẢNH BÁO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topBar.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);
        actionBtns.add(createFlatButton("Nhập kho mới", new Color(240, 240, 240), TEXT_DARK));
        actionBtns.add(createFlatButton("Xuất kho", new Color(240, 240, 240), TEXT_DARK));
        topBar.add(actionBtns, BorderLayout.EAST);

        // --- Filter Bar: Tìm kiếm & Lọc ---
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterBar.setOpaque(false);
        
        JTextField searchField = new JTextField("🔍 Tìm nguyên liệu...");
        searchField.setPreferredSize(new Dimension(200, 35));
        
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Tất cả loại hình", "Nguyên liệu", "Chế phẩm"});
        typeCombo.setPreferredSize(new Dimension(150, 35));
        typeCombo.setBackground(Color.WHITE);

        JButton btnSetThreshold = createFlatButton("Thiết lập ngưỡng", new Color(240, 240, 240), TEXT_DARK);
        
        filterBar.add(searchField);
        filterBar.add(typeCombo);
        filterBar.add(btnSetThreshold);

        JPanel headerContainer = new JPanel(new BorderLayout(0, 15));
        headerContainer.setOpaque(false);
        headerContainer.add(topBar, BorderLayout.NORTH);
        headerContainer.add(filterBar, BorderLayout.CENTER);

        // --- Table: Danh sách nguyên liệu ---
        JPanel tableContainer = createRoundedPanel(BG_CARD, 15);
        tableContainer.setLayout(new BorderLayout());
        
        String[] columns = {"ID", "Tên nguyên liệu", "Loại hình", "Tồn kho", "Đơn vị", "Ngưỡng", "Trạng thái"};
        Object[][] data = {
            {"NL001", "Sữa tươi", "Nguyên liệu", "45.5", "Lít", "5.0", "Bình thường"},
            {"NL002", "Hạt trân châu", "Nguyên liệu", "2.1", "Kg", "3.0", "CẢNH BÁO THẤP"},
            {"NL003", "Sữa đặc", "Nguyên liệu", "7.8", "Kg", "3.0", "Bình thường"},
            {"NL004", "Trà đen", "Nguyên liệu", "1.5", "Kg", "2.0", "CẢNH BÁO THẤP"},
            {"NL005", "Syrup Đào", "Nguyên liệu", "12.0", "Chai", "5.0", "Bình thường"}
        };
        
        DefaultTableModel model = new DefaultTableModel(data, columns);
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));

        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID (Hẹp)
        table.getColumnModel().getColumn(1).setPreferredWidth(180); // Tên nguyên liệu (Rộng nhất)
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Loại hình
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Tồn kho
        table.getColumnModel().getColumn(4).setPreferredWidth(70);  // Đơn vị
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // Ngưỡng
        table.getColumnModel().getColumn(6).setPreferredWidth(140); // Trạng thái (Rộng để chứa chữ CẢNH BÁO)
        
        // Custom Header
        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 14));
        th.setBackground(new Color(245, 245, 245));
        th.setPreferredSize(new Dimension(0, 40));

        // Custom Cột Trạng Thái (Tô màu Đỏ/Xanh)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) value;
                if (status.equals("CẢNH BÁO THẤP")) {
                    c.setForeground(DANGER_RED);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(PRIMARY_GREEN);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE); // Tránh lỗi render text khi cuộn
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // --- Bottom: Các thẻ tóm tắt ---
        JPanel bottomSummary = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomSummary.setOpaque(false);
        bottomSummary.setPreferredSize(new Dimension(0, 160));

        // Thẻ Nhập kho gần đây
        JPanel recentImportPanel = createRoundedPanel(BG_CARD, 15);
        recentImportPanel.setLayout(new BoxLayout(recentImportPanel, BoxLayout.Y_AXIS));
        recentImportPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel lblImportTitle = new JLabel("TÓM TẮT NHẬP KHO GẦN ĐÂY");
        lblImportTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        recentImportPanel.add(lblImportTitle);
        recentImportPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        recentImportPanel.add(createSummaryRow("Ngày:", "15/03/2026"));
        recentImportPanel.add(createSummaryRow("Mã Phiếu:", "PN_092"));
        recentImportPanel.add(createSummaryRow("Tổng tiền:", "18,500,000 VND"));

        // Thẻ COGS (Giá vốn)
        JPanel cogsPanel = createRoundedPanel(BG_CARD, 15);
        cogsPanel.setLayout(new BorderLayout());
        cogsPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel lblCogsTitle = new JLabel("TÓM TẮT GIÁ VỐN (COGS)");
        lblCogsTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cogsPanel.add(lblCogsTitle, BorderLayout.NORTH);
        
        // Thêm biểu đồ Pie Chart tự vẽ
        JPanel chartWrapper = new JPanel(new BorderLayout());
        chartWrapper.setOpaque(false);
        chartWrapper.add(new CustomPieChart(), BorderLayout.WEST);
        
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(new EmptyBorder(15, 10, 0, 0));
        legendPanel.add(new JLabel("● Sữa & Kem"));
        legendPanel.add(new JLabel("● Trà & Cà phê"));
        legendPanel.add(new JLabel("● Topping"));
        chartWrapper.add(legendPanel, BorderLayout.CENTER);

        cogsPanel.add(chartWrapper, BorderLayout.CENTER);
        
        JLabel lblCogsAvg = new JLabel("COGS Trung bình: 32%");
        lblCogsAvg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cogsPanel.add(lblCogsAvg, BorderLayout.SOUTH);

        bottomSummary.add(recentImportPanel);
        bottomSummary.add(cogsPanel);

        // Ráp nối cột trái
        panel.add(headerContainer, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);
        panel.add(bottomSummary, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================
    // XÂY DỰNG KHU VỰC CÔNG THỨC (PHẢI)
    // =========================================================
    private JPanel buildRecipeSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(280, 0)); // Cố định chiều rộng

        // --- Top: Tiêu đề & Tìm kiếm ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel lblTitle = new JLabel("CÔNG THỨC & ĐỊNH LƯỢNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topBar.add(lblTitle, BorderLayout.NORTH);
        
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(15, 0, 0, 0));
        JTextField searchField = new JTextField("🔍 Tìm đồ uống...");
        searchField.setPreferredSize(new Dimension(0, 35));
        searchBar.add(searchField, BorderLayout.CENTER);
        topBar.add(searchBar, BorderLayout.SOUTH);

        // --- Bảng Công thức ---
        JPanel recipeContainer = createRoundedPanel(BG_CARD, 15);
        recipeContainer.setLayout(new BorderLayout());
        recipeContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel drinkName = new JLabel("☕ Cà phê cốt dừa");
        drinkName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        drinkName.setForeground(PRIMARY_GREEN);
        drinkName.setBorder(new EmptyBorder(5, 5, 15, 5));
        recipeContainer.add(drinkName, BorderLayout.NORTH);

        String[] cols = {"Thành phần", "Định lượng", "Đơn vị"};
        Object[][] data = {
            {"Cà phê Espresso", "60", "ml"},
            {"Cốt dừa", "150", "ml"},
            {"Sữa đặc", "30", "gr"},
            {"Đá viên", "200", "gr"}
        };
        JTable table = new JTable(new DefaultTableModel(data, cols));
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        recipeContainer.add(scrollPane, BorderLayout.CENTER);

        // --- Bottom: Nút tác vụ ---
        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);
        actionBtns.add(createFlatButton("Sửa công thức", new Color(230, 245, 235), PRIMARY_GREEN));
        actionBtns.add(createFlatButton("Xóa", new Color(255, 235, 235), DANGER_RED));
        recipeContainer.add(actionBtns, BorderLayout.SOUTH);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(recipeContainer, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // CÁC HÀM TIỆN ÍCH (UI HELPERS)
    // =========================================================

    private JPanel createSummaryRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_DARK);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JButton createFlatButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(bgColor.darker());
                else g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // Bo góc 10px
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fgColor);
        btn.setPreferredSize(new Dimension(130, 35));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false); // Bắt buộc false
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createRoundedPanel(Color bgColor, int radius) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false); // Bắt buộc false
        return panel;
    }

    // =========================================================
    // CLASS VẼ BIỂU ĐỒ TRÒN (PIE CHART) BẰNG CODE THUẦN
    // =========================================================
    class CustomPieChart extends JPanel {
        private final double[] values = {45, 30, 25}; // Phần trăm
        private final Color[] colors = {
            new Color(67, 142, 104), // Xanh lá
            new Color(220, 150, 80), // Cam
            new Color(150, 180, 100) // Xanh nhạt
        };

        public CustomPieChart() {
            setOpaque(false);
            setPreferredSize(new Dimension(90, 90));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 10;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            double total = 100;
            double startAngle = 90; // Bắt đầu từ đỉnh

            for (int i = 0; i < values.length; i++) {
                double extent = (values[i] / total) * 360;
                g2.setColor(colors[i]);
                g2.fill(new Arc2D.Double(x, y, size, size, startAngle, -extent, Arc2D.PIE));
                startAngle -= extent;
            }

            // Khoét lỗ ở giữa để thành Donut Chart (Trông xịn hơn)
            g2.setColor(Color.WHITE);
            int innerSize = size / 2;
            g2.fillOval(x + size/4, y + size/4, innerSize, innerSize);

            g2.dispose();
        }
    }
}