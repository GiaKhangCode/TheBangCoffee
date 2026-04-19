package View;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard Panel - Giao diện tổng quan.
 */
public class DashboardPanel extends JPanel {
    public DashboardPanel() {
        setLayout(new BorderLayout(0, 25));
        setOpaque(false);

        // 1. Stats Layer (3 thẻ thống kê)
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(new StatCard("Đơn hàng mới", "0", ""));
        statsPanel.add(new StatCard("Doanh thu", "0 VND", ""));
        statsPanel.add(new StatCard("Bán chạy nhất", "---", ""));
        
        // 2. Center Layer (Biểu đồ và Bảng)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 25));
        centerPanel.setOpaque(false);

        // Khung biểu đồ Placeholder
        JPanel chartPlaceholder = new RoundedChartPlaceholder();
        chartPlaceholder.setPreferredSize(new Dimension(800, 350));
        
        JLabel chartMsg = new JLabel("Khu vực hiển thị Biểu đồ (Dùng JFreeChart để tích hợp)");
        chartMsg.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        chartMsg.setHorizontalAlignment(SwingConstants.CENTER);
        chartPlaceholder.setLayout(new BorderLayout());
        chartPlaceholder.add(chartMsg, BorderLayout.CENTER);

        // Bảng Đơn hàng gần đây
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        
        String[] columns = {"ID", "Khách hàng", "Thời gian", "Trạng thái", "Tổng tiền"};
        Object[][] data = {}; // Trống dữ liệu để load sau từ DB

        JTable table = new JTable(data, columns);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(chartPlaceholder, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(statsPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Inner class có tên để vẽ placeholder biểu đồ.
     */
    private static class RoundedChartPlaceholder extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppColor.CHART_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.dispose();
        }
    }
}
