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
        setPreferredSize(new Dimension(800, 80)); // Chiều rộng mặc định sẽ được BorderLayout.NORTH của container xử lý
        setBorder(new EmptyBorder(10, 0, 20, 0));

        titleLabel = new JLabel(defaultTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(AppColor.TEXT_DARK);

        // Nút "Tạo đơn hàng mới" ở góc phải
        JButton actionBtn = new JButton("Tạo đơn hàng mới +");
        actionBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        actionBtn.setBackground(AppColor.PRIMARY);
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setFocusPainted(false);
        actionBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
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
