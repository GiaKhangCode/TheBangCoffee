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
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
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
