package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PaymentMethodDialog extends JDialog {
    
    private int selectedOption = -1; // -1: Hủy, 0: Tiền mặt, 1: Chuyển khoản
    
    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color INFO_COLOR = new Color(41, 128, 185);
    private final Color DANGER_COLOR = new Color(231, 76, 60);

    public PaymentMethodDialog(Frame parent, int orderId) {
        super(parent, "Xác nhận thanh toán", true);
        setUndecorated(true); // Tắt thanh tiêu đề mặc định của Windows
        setSize(480, 220);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        // Viền xanh lá bọc ngoài
        mainPanel.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));

        // 1. Header Tiêu đề
        JLabel lblTitle = new JLabel("THANH TOÁN ĐƠN HÀNG #" + orderId, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBorder(new EmptyBorder(25, 10, 10, 10));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // 2. Nội dung hướng dẫn
        JLabel lblMsg = new JLabel("Vui lòng chọn hình thức thanh toán của khách hàng:", SwingConstants.CENTER);
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblMsg.setForeground(new Color(33, 37, 41));
        mainPanel.add(lblMsg, BorderLayout.CENTER);

        // 3. Khu vực chứa 3 nút bấm
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 25));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCash = createButton("Tiền mặt", PRIMARY_COLOR, Color.WHITE);
        JButton btnTransfer = createButton("Chuyển khoản", INFO_COLOR, Color.WHITE);
        JButton btnCancel = createButton("Hủy bỏ", DANGER_COLOR, Color.WHITE);

        btnCash.addActionListener(e -> { selectedOption = 0; dispose(); });
        btnTransfer.addActionListener(e -> { selectedOption = 1; dispose(); });
        btnCancel.addActionListener(e -> { selectedOption = -1; dispose(); });

        btnPanel.add(btnCash);
        btnPanel.add(btnTransfer);
        btnPanel.add(btnCancel);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    // Hàm tiện ích tạo nút bấm bo tròn hiện đại
    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(135, 45));
        if (text.contains("Chuyển khoản")) {
            btn.setPreferredSize(new Dimension(170, 45)); // Nút chuyển khoản dài hơn một chút
        }
        return btn;
    }

    public int getSelectedOption() {
        return selectedOption;
    }
}