package Common;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author SONY
 */
public class ComponentUI {
    
    public static JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color baseBg = getBackground();
                // Thiết lập màu vẽ dựa trên trạng thái nút (Nhấn / Di chuột / Bình thường)
                if (getModel().isPressed()) {
                    g2.setColor(getPressedColor(baseBg));
                    g2.translate(0, 1); // Dịch chuyển nhẹ 1px xuống dưới để tạo hiệu ứng 3D lún nút
                } else if (getModel().isRollover()) {
                    g2.setColor(getHoverColor(baseBg));
                } else {
                    g2.setColor(baseBg);
                }
                
                // Đảm bảo vẽ nền không bị lòi ngoài viền khi dịch xuống 1px
                g2.fillRoundRect(0, 0, getWidth(), getModel().isPressed() ? getHeight() - 1 : getHeight(), 10, 10);
                g2.dispose();
                
                // Dịch chuyển graphics chính để vẽ chữ/icon cũng thụt xuống 1px tương ứng
                if (getModel().isPressed()) {
                    g.translate(0, 1);
                }
                super.paintComponent(g);
                if (getModel().isPressed()) {
                    g.translate(0, -1); // Trả graphics chính về trạng thái ban đầu để tránh ảnh hưởng lần vẽ sau
                }
            }
        };
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int btnFontSize = Math.max(12, (int) (screenSize.width * 0.009));
        int paddingTopBottom = Math.max(5, (int) (screenSize.height * 0.012));
        int paddingLeftRight = Math.max(10, (int) (screenSize.width * 0.013));
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, btnFontSize));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(paddingTopBottom, paddingLeftRight, paddingTopBottom, paddingLeftRight));
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    // Hàm hỗ trợ giảm độ sáng của màu khi nhấn nút
    private static Color getPressedColor(Color bg) {
        int r = bg.getRed();
        int g = bg.getGreen();
        int b = bg.getBlue();
        int a = bg.getAlpha();
        
        // Giảm độ sáng khoảng 30 đơn vị để tạo hiệu ứng nhấn mạnh rõ rệt
        r = Math.max(0, r - 30);
        g = Math.max(0, g - 30);
        b = Math.max(0, b - 30);
        
        return new Color(r, g, b, a);
    }
    
    // Hàm hỗ trợ điều chỉnh màu khi di chuột qua nút (Hover)
    private static Color getHoverColor(Color bg) {
        int r = bg.getRed();
        int g = bg.getGreen();
        int b = bg.getBlue();
        int a = bg.getAlpha();
        
        // Xác định độ sáng tổng thể để điều chỉnh màu Hover phù hợp
        int brightness = (r * 299 + g * 587 + b * 114) / 1000;
        if (brightness > 200) {
            // Nút màu sáng (Trắng/Xám nhạt): di chuột vào sẽ hơi sẫm màu đi 12 đơn vị
            r = Math.max(0, r - 12);
            g = Math.max(0, g - 12);
            b = Math.max(0, b - 12);
        } else {
            // Nút màu tối (Xanh/Đỏ/Đậm): di chuột vào sẽ sáng lên 20 đơn vị
            r = Math.min(255, r + 20);
            g = Math.min(255, g + 20);
            b = Math.min(255, b + 20);
        }
        return new Color(r, g, b, a);
    }
    
    public static void styleTable(JTable table, Color foreground, Color selectionForeground, Color selectionBackground) {
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int rowHeight = Math.max(30, (int) (screenSize.height * 0.057));
        int tableFontSize = Math.max(12, (int) (screenSize.width * 0.009));

        table.setRowHeight(rowHeight);
        table.setFont(new Font("Segoe UI", Font.PLAIN, tableFontSize));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, tableFontSize));
        table.getTableHeader().setBackground(new Color(242, 242, 242));
        table.getTableHeader().setForeground(foreground);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        // Pha màu đục (opaque) bằng cách mix 15% màu được truyền vào với 85% màu trắng
        // Để tránh lỗi khác màu giữa các loại cell renderer (Boolean vs String vs JPanel) khi dùng Alpha
        int r = (int) (selectionBackground.getRed() * 0.15 + 255 * 0.85);
        int g = (int) (selectionBackground.getGreen() * 0.15 + 255 * 0.85);
        int b = (int) (selectionBackground.getBlue() * 0.15 + 255 * 0.85);
        table.setSelectionBackground(new Color(r, g, b));
        
        table.setSelectionForeground(selectionForeground);
    }
}
