package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Thẻ thống kê nhỏ gọn với hiệu ứng bo góc.
 */
public class StatCard extends JPanel {
    
    // [MỚI] Đưa các Label ra làm biến toàn cục để Setter có thể truy cập
    private JLabel lblTitle;
    private JLabel lblValue;
    private JLabel lblSub;

    public StatCard(String title, String value, String sub) {
        setLayout(new BorderLayout(0, 5)); // Thêm gap 5px giữa các dòng cho thoáng
        setBackground(AppColor.CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(235, 235, 235), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(AppColor.TEXT_MUTED);

        lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(AppColor.TEXT_DARK);

        // [MỚI] Thêm Label cho phần sub (Chú thích phụ)
        lblSub = new JLabel(sub);
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSub.setForeground(new Color(150, 150, 150)); // Màu xám nhạt

        add(lblTitle, BorderLayout.NORTH);
        add(lblValue, BorderLayout.CENTER);
        
        // Chỉ thêm Sub vào giao diện nếu có truyền text
        if (sub != null && !sub.trim().isEmpty()) {
            add(lblSub, BorderLayout.SOUTH);
        }
        
        setOpaque(false); // Để paintComponent hiển thị bo góc
    }

    // ==========================================================
    // CÁC HÀM SETTER ĐỂ CONTROLLER CẬP NHẬT DỮ LIỆU
    // ==========================================================

    public void setValue(String value) {
        this.lblValue.setText(value);
    }

    public void setTitle(String title) {
        this.lblTitle.setText(title);
    }

    public void setSub(String sub) {
        this.lblSub.setText(sub);
        // Nếu trước đó chưa có sub mà giờ lại có, ta add thêm vào và vẽ lại UI
        if (sub != null && !sub.trim().isEmpty() && this.lblSub.getParent() == null) {
            add(this.lblSub, BorderLayout.SOUTH);
            revalidate();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g2.dispose();
        super.paintComponent(g);
    }
}