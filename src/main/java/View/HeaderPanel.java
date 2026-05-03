package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Header Bar với tiêu đề và nút hành động.
 */
public class HeaderPanel extends JPanel {
    private JLabel titleLabel;

    public HeaderPanel(String defaultTitle) {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        int headerHeight = (int) (screenHeight * 0.06); // Khoảng 80px
        int titleFontSize = Math.max(15, (int) (screenWidth * 0.012)); // Khoảng 28px
        int paddingTop = (int) (screenHeight * 0.012); // Khoảng 10px
        int paddingBottom = (int) (screenHeight * 0.023); // Khoảng 20px
        
        setPreferredSize(new Dimension(screenWidth, headerHeight)); 
        setBorder(new EmptyBorder(paddingTop, 0, paddingBottom, 0));

        titleLabel = new JLabel(defaultTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, titleFontSize));
        titleLabel.setForeground(AppColor.TEXT_DARK);
        add(titleLabel, BorderLayout.WEST);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
