package Common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BadgeRenderer extends DefaultTableCellRenderer {
    private Map<String, Color[]> statusColors = new HashMap<>();

    public BadgeRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Thêm màu nền và màu chữ cho một trạng thái.
     * @param status Tên trạng thái (VD: "Đang hoạt động")
     * @param bgColor Màu nền của badge (VD: Xanh dương nhạt)
     * @param fgColor Màu chữ của badge (VD: Xanh dương đậm)
     */
    public void addBadgeStyle(String status, Color bgColor, Color fgColor) {
        statusColors.put(status, new Color[]{bgColor, fgColor});
    }

    /**
     * Thêm màu cho trạng thái tự động tính toán nền nhạt.
     * @param status Tên trạng thái
     * @param mainColor Màu chữ (sẽ tự pha 15% opacity để làm nền)
     */
    public void addBadgeStyle(String status, Color mainColor) {
        int r = (int) (mainColor.getRed() * 0.15 + 255 * 0.85);
        int g = (int) (mainColor.getGreen() * 0.15 + 255 * 0.85);
        int b = (int) (mainColor.getBlue() * 0.15 + 255 * 0.85);
        Color bgColor = new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b));
        statusColors.put(status, new Color[]{bgColor, mainColor});
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Đảm bảo luôn được lấp đầy nền mặc định của JTable (nếu đang được chọn)
        setOpaque(true);
        setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
        setBorder(new EmptyBorder(0, 0, 0, 0)); // Bỏ border

        if (value != null) {
            String status = value.toString();
            Color[] colors = statusColors.get(status);
            if (colors != null) {
                // Đặt màu chữ là fgColor
                setForeground(colors[1]);
                setFont(new Font("Segoe UI", Font.BOLD, 12)); // Font cho Badge
            } else {
                setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            }
        }
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 1. Vẽ nền (trắng hoặc xanh nhạt khi chọn)
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        // 2. Nếu có badge style, vẽ badge
        Color[] colors = statusColors.get(getText());
        if (colors != null && getText() != null && !getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Lấy kích thước Text
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textWidth = fm.stringWidth(getText());
            int textHeight = fm.getHeight();

            // Tính toán kích thước khối Badge
            int paddingX = 16;
            int paddingY = 6;
            int badgeWidth = textWidth + paddingX * 2;
            int badgeHeight = textHeight + paddingY * 2;

            // Tính vị trí vẽ nằm chính giữa ô
            int x = (getWidth() - badgeWidth) / 2;
            int y = (getHeight() - badgeHeight) / 2;

            // Vẽ nền Badge (hình viên thuốc / pill shape)
            g2.setColor(colors[0]);
            g2.fillRoundRect(x, y, badgeWidth, badgeHeight, badgeHeight, badgeHeight); // Bo tròn hoàn toàn 2 đầu
            g2.dispose();
        }

        // 3. Vẽ chữ (JLabel mặc định)
        // Cần tắt Opaque tạm thời để JLabel không vẽ lại Background che mất Badge
        setOpaque(false);
        super.paintComponent(g);
        setOpaque(true);
    }
}
