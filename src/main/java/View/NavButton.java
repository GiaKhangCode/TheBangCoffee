package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Nút điều hướng phong cách Modern Minimalist.
 */
public class NavButton extends JButton {
    private String cardName;
    private boolean isActive = false;

    public NavButton(String text, String icon, String cardName) {
        this.cardName = cardName;
        setText(text);
        setFont(new Font("Segoe UI", Font.PLAIN, 15));
        setForeground(AppColor.TEXT_MUTED);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(SwingConstants.LEFT);
        setPreferredSize(new Dimension(250, 45));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        setBorder(new EmptyBorder(0, 25, 0, 0));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public void setActive(boolean active) {
        this.isActive = active;
        setForeground(active ? AppColor.PRIMARY : AppColor.TEXT_MUTED);
        setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 15));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isActive) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppColor.PRIMARY_LIGHT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
