package View;

import javax.swing.*;
import java.awt.*;

/**
 * Panel cơ sở cho các trang nội dung - The Bang Coffee.
 * Được tách ra file riêng để quản lý code sạch sẽ hơn.
 * 
 * @author Antigravity
 */
public class ContentBasePanel extends JPanel {
    
    public ContentBasePanel(String title, String desc) {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Sử dụng Named Inner Class "RoundedCard" thay vì Anonymous class để tránh tạo file $1.class
        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblTitle.setForeground(AppColor.PRIMARY);
        
        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblDesc.setForeground(AppColor.TEXT_MUTED);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0;
        card.add(lblTitle, gbc);
        gbc.gridy = 1;
        card.add(lblDesc, gbc);
        
        add(card, BorderLayout.CENTER);
    }

    /**
     * Panel con có hiệu ứng bo góc trắng.
     */
    private static class RoundedCard extends JPanel {
        public RoundedCard() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppColor.CARD_BG);
            // Vẽ hình chữ nhật bo góc
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
