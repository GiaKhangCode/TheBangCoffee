package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Sidebar với thiết kế Flat Light hiện đại.
 */
public class SidebarPanel extends JPanel {
    public SidebarPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(AppColor.SIDEBAR_BG);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int paddingSide = (int) (screenSize.width * 0.010); // Khoảng 15px trên màn 1536
        setBorder(new EmptyBorder(0, paddingSide, 0, paddingSide));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        // Đường kẻ ngăn cách sidebar và nội dung
        g2.setColor(AppColor.LINE_LIGHT);
        g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
        g2.dispose();
    }
}
