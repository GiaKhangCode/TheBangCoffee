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
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class ReportPanel extends JPanel {

    // Bảng màu chuẩn của ứng dụng
    private final Color PRIMARY_GREEN = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(50, 50, 50);
    private final Color TEXT_GRAY = new Color(120, 120, 120);

    public ReportPanel() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false); // Bắt buộc để không bị lỗi bóng ma với kính mờ
        setBorder(new EmptyBorder(10, 20, 20, 20));

        // 1. HEADER CỦA BÁO CÁO
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("BÁO CÁO & THỐNG KÊ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_DARK);
        
        // Nút lọc thời gian (Giả lập)
        JPanel filterPanel = createRoundedPanel(Color.WHITE, 20);
        filterPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        JLabel filterLabel = new JLabel("Tháng này: 01/03/2026 - 31/03/2026 📅");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filterPanel.add(filterLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);

        // 2. KHU VỰC CHỨA CÁC THẺ KPI
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.add(createKPICard("TỔNG DOANH THU", "245,678,000 VND", "▲ +12.5% so với tháng trước", true));
        kpiPanel.add(createKPICard("TỔNG ĐƠN HÀNG", "1,890 ĐƠN", "▲ +5%", true));
        kpiPanel.add(createKPICard("MẶT HÀNG BÁN CHẠY NHẤT", "Cà phê cốt dừa", "310 cốc", false));

        // 3. KHU VỰC CHỨA CÁC BIỂU ĐỒ
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setOpaque(false);
        
        // 3.1 Biểu đồ đường (Line Chart)
        JPanel lineChartContainer = createRoundedPanel(Color.WHITE, 20);
        lineChartContainer.setLayout(new BorderLayout());
        lineChartContainer.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel lineChartTitle = new JLabel("Biểu đồ xu hướng doanh thu hàng ngày");
        lineChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lineChartContainer.add(lineChartTitle, BorderLayout.NORTH);
        lineChartContainer.add(new CustomLineChart(), BorderLayout.CENTER); // Thêm biểu đồ tự vẽ

        // 3.2 Biểu đồ cột ngang (Horizontal Bar Chart)
        JPanel barChartContainer = createRoundedPanel(Color.WHITE, 20);
        barChartContainer.setLayout(new BorderLayout());
        barChartContainer.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel barChartTitle = new JLabel("Top 5 mặt hàng bán chạy nhất");
        barChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        barChartContainer.add(barChartTitle, BorderLayout.NORTH);
        barChartContainer.add(new CustomBarChart(), BorderLayout.CENTER); // Thêm biểu đồ tự vẽ

        chartsPanel.add(lineChartContainer);
        chartsPanel.add(barChartContainer);

        // 4. LẮP RÁP CÁC THÀNH PHẦN VÀO PANEL CHÍNH
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 20));
        centerWrapper.setOpaque(false);
        centerWrapper.add(kpiPanel, BorderLayout.NORTH);
        centerWrapper.add(chartsPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
    }

    // --- HÀM HỖ TRỢ TẠO THẺ TÓM TẮT (KPI CARD) ---
    private JPanel createKPICard(String title, String value, String subText, boolean isPositiveTrend) {
        JPanel card = createRoundedPanel(Color.WHITE, 20);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel(subText);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (isPositiveTrend) {
            lblSub.setForeground(PRIMARY_GREEN); // Màu xanh lá cho tăng trưởng
        } else {
            lblSub.setForeground(TEXT_GRAY); // Màu xám cho thông tin thường
        }

        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(lblValue);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(lblSub);

        return card;
    }

    // --- HÀM HỖ TRỢ TẠO PANEL BO GÓC KHÔNG BỊ BUG ---
    private JPanel createRoundedPanel(Color bgColor, int radius) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
                g2.dispose();
                super.paintComponent(g); // Gọi super SAU KHI vẽ nền để các Component con hiện lên
            }
        };
        panel.setOpaque(false); // Bắt buộc false
        return panel;
    }

    // =========================================================
    // CLASS VẼ BIỂU ĐỒ ĐƯỜNG (LINE CHART) BẰNG CODE THUẦN
    // =========================================================
    class CustomLineChart extends JPanel {
        private int[] dataPoints = {2, 5, 4, 6, 5, 8, 14, 6, 5, 11, 7, 10, 5}; // Mock data

        public CustomLineChart() {
            setOpaque(false);
            setBorder(new EmptyBorder(20, 0, 10, 0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int paddingX = 30;
            int paddingY = 20;

            // Vẽ các đường lưới ngang (Grid lines)
            g2.setColor(new Color(230, 230, 230));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f}, 0f)); // Nét đứt
            for (int i = 0; i < 4; i++) {
                int y = paddingY + i * ((height - 2 * paddingY) / 3);
                g2.drawLine(paddingX, y, width - paddingX, y);
            }

            // Vẽ đường Line Chart
            g2.setColor(PRIMARY_GREEN);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int pointCount = dataPoints.length;
            int xStep = (width - 2 * paddingX) / (pointCount - 1);
            int maxY = 15; // Giả sử đỉnh biểu đồ là 15M

            Path2D path = new Path2D.Double();
            int[] xCoords = new int[pointCount];
            int[] yCoords = new int[pointCount];

            for (int i = 0; i < pointCount; i++) {
                xCoords[i] = paddingX + i * xStep;
                yCoords[i] = height - paddingY - (dataPoints[i] * (height - 2 * paddingY) / maxY);
                
                if (i == 0) path.moveTo(xCoords[i], yCoords[i]);
                else path.lineTo(xCoords[i], yCoords[i]);
            }
            g2.draw(path);

            // Vẽ các điểm chấm tròn trên đường
            for (int i = 0; i < pointCount; i++) {
                g2.setColor(Color.WHITE);
                g2.fillOval(xCoords[i] - 5, yCoords[i] - 5, 10, 10);
                g2.setColor(PRIMARY_GREEN);
                g2.drawOval(xCoords[i] - 5, yCoords[i] - 5, 10, 10);
            }
        }
    }

    // =========================================================
    // CLASS VẼ BIỂU ĐỒ CỘT NGANG (BAR CHART) BẰNG CODE THUẦN
    // =========================================================
    class CustomBarChart extends JPanel {
        private String[] labels = {"1. Cà phê cốt dừa", "2. Trà sữa trân châu", "3. Sinh tố Xoài", "4. Cà phê nâu đá", "5. Trà chanh dây"};
        private int[] values = {310, 280, 210, 195, 150};
        private int maxVal = 350;

        public CustomBarChart() {
            setOpaque(false);
            setBorder(new EmptyBorder(20, 0, 10, 0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int barHeight = 25;
            int gap = (height - (labels.length * barHeight)) / (labels.length + 1);

            for (int i = 0; i < labels.length; i++) {
                int y = gap + i * (barHeight + gap);

                // Vẽ Text tên món
                g2.setColor(TEXT_DARK);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.drawString(labels[i], 10, y + 17);

                // Vẽ Cột nền (Xám nhạt)
                int barX = 140; // Điểm bắt đầu vẽ thanh ngang
                int maxBarWidth = width - barX - 10;
                g2.setColor(new Color(240, 240, 240));
                g2.fillRoundRect(barX, y, maxBarWidth, barHeight, 10, 10);

                // Vẽ Cột giá trị (Xanh lá)
                int valWidth = (int) ((double) values[i] / maxVal * maxBarWidth);
                g2.setColor(PRIMARY_GREEN);
                g2.fillRoundRect(barX, y, valWidth, barHeight, 10, 10);

                // Vẽ số lượng lên trên cột
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String valText = String.valueOf(values[i]);
                int textWidth = g2.getFontMetrics().stringWidth(valText);
                g2.drawString(valText, barX + valWidth - textWidth - 10, y + 17);
            }
        }
    }
}
