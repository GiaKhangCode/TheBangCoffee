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
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int btnWidth = (int) (screenWidth * 0.16); // Khoảng 250px
        int btnHeight = (int) (screenHeight * 0.052); // Khoảng 45px
        int fontSize = Math.max(14, (int) (screenWidth * 0.010)); // Khoảng 15px
        int paddingLeft = (int) (screenWidth * 0.016); // Khoảng 25px
        
        setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
        setForeground(AppColor.TEXT_MUTED);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(SwingConstants.LEFT);
        setPreferredSize(new Dimension(btnWidth, btnHeight));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, btnHeight));
        setBorder(new EmptyBorder(0, paddingLeft, 0, 0));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public void setActive(boolean active) {
        this.isActive = active;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int fontSize = Math.max(14, (int) (screenSize.width * 0.010));
        setForeground(active ? AppColor.PRIMARY : AppColor.TEXT_MUTED);
        setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, fontSize));
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
