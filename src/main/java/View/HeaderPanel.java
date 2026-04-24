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

        int headerHeight = (int) (screenHeight * 0.09); // Khoảng 80px
        int titleFontSize = Math.max(20, (int) (screenWidth * 0.018)); // Khoảng 28px
        int btnFontSize = Math.max(12, (int) (screenWidth * 0.009)); // Khoảng 14px
        int paddingTop = (int) (screenHeight * 0.012); // Khoảng 10px
        int paddingBottom = (int) (screenHeight * 0.023); // Khoảng 20px
        int btnPaddingSide = (int) (screenWidth * 0.013); // Khoảng 20px
        
        setPreferredSize(new Dimension(screenWidth, headerHeight)); 
        setBorder(new EmptyBorder(paddingTop, 0, paddingBottom, 0));

        titleLabel = new JLabel(defaultTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, titleFontSize));
        titleLabel.setForeground(AppColor.TEXT_DARK);

        // Nút "Tạo đơn hàng mới" ở góc phải
        JButton actionBtn = new JButton("Tạo đơn hàng mới +");
        actionBtn.setFont(new Font("Segoe UI", Font.BOLD, btnFontSize));
        actionBtn.setBackground(AppColor.PRIMARY);
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setFocusPainted(false);
        actionBtn.setBorder(new EmptyBorder(paddingTop, btnPaddingSide, paddingTop, btnPaddingSide));
        actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        actionBtn.setContentAreaFilled(false);
        actionBtn.setOpaque(false);
        
        // Sử dụng Named Inner Class RoundedButtonWrapper để tránh file $1.class
        RoundedButtonWrapper btnWrapper = new RoundedButtonWrapper();
        btnWrapper.add(actionBtn);

        add(titleLabel, BorderLayout.WEST);
        add(btnWrapper, BorderLayout.EAST);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * Lớp bao quanh nút bấm với hiệu ứng bo góc xanh.
     */
    private static class RoundedButtonWrapper extends JPanel {
        public RoundedButtonWrapper() {
            setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 5));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppColor.PRIMARY);
            g2.fillRoundRect(0, 5, getWidth(), getHeight() - 10, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
