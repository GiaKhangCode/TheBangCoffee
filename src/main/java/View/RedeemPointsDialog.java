package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class RedeemPointsDialog extends JDialog {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private int availablePoints;
    private int pointValue;
    private long currentTotalBill;
    
    private int appliedPoints = 0;
    private long discountAmount = 0;
    private boolean isConfirmed = false;

    private JTextField txtPointsToUse;
    private JLabel lblDiscountPreview;
    private JButton btnConfirm;

    public RedeemPointsDialog(Frame owner, int availablePoints, int pointValue, long currentTotalBill) {
        super(owner, "Sử dụng điểm tích lũy", true);
        // Ngăn chặn trường hợp Database bị sửa bậy bạ làm điểm âm
        this.availablePoints = Math.max(0, availablePoints); 
        this.pointValue = pointValue;
        this.currentTotalBill = currentTotalBill;

        initComponents();
    }

    private void initComponents() {
        setSize(400, 320);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Thông tin điểm hiện tại 
        JLabel lblInfo = new JLabel(String.format("Điểm hiện tại có thể dùng: %d điểm", availablePoints));
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblRule = new JLabel(String.format("(1 Điểm = %,d đ)", pointValue));
        lblRule.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblRule.setForeground(Color.GRAY);
        lblRule.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Ô nhập số điểm muốn dùng
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.add(new JLabel("Nhập số điểm muốn dùng: "));
        
        txtPointsToUse = new JTextField(10);
        txtPointsToUse.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtPointsToUse.setHorizontalAlignment(JTextField.CENTER);
        inputPanel.add(txtPointsToUse);

        // 3. Label hiển thị số tiền được giảm
        lblDiscountPreview = new JLabel("Số tiền được giảm: 0 đ");
        lblDiscountPreview.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDiscountPreview.setForeground(new Color(231, 76, 60)); // Màu đỏ
        lblDiscountPreview.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Lắng nghe sự kiện gõ phím để tính tiền giảm ngay lập tức
        txtPointsToUse.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calculateDiscount(); }
            public void removeUpdate(DocumentEvent e) { calculateDiscount(); }
            public void changedUpdate(DocumentEvent e) { calculateDiscount(); }
        });

        body.add(lblInfo);
        body.add(Box.createRigidArea(new Dimension(0, 5)));
        body.add(lblRule);
        body.add(Box.createRigidArea(new Dimension(0, 20)));
        body.add(inputPanel);
        body.add(Box.createRigidArea(new Dimension(0, 15)));
        body.add(lblDiscountPreview);

        add(body, BorderLayout.CENTER);

        // --- FOOTER BUTTONS ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        footer.setBackground(new Color(248, 249, 250));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setPreferredSize(new Dimension(120, 35));
        btnCancel.addActionListener(e -> dispose());

        btnConfirm = new JButton("Áp dụng");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setBackground(PRIMARY_COLOR);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setPreferredSize(new Dimension(120, 35));
        btnConfirm.setEnabled(false); // Khóa nút lúc đầu
        btnConfirm.addActionListener(e -> {
            isConfirmed = true;
            dispose();
        });

        footer.add(btnCancel);
        footer.add(btnConfirm);
        add(footer, BorderLayout.SOUTH);
    }

    private void calculateDiscount() {
        try {
            String text = txtPointsToUse.getText().trim();
            if (text.isEmpty()) {
                resetPreview();
                return;
            }

            int points = Integer.parseInt(text);
            
            // Validate logic
            if (points <= 0) {
                lblDiscountPreview.setText("Vui lòng nhập số > 0");
                btnConfirm.setEnabled(false);
                return;
            }
            if (points > availablePoints) {
                lblDiscountPreview.setText("Vượt quá số điểm hiện tại!");
                btnConfirm.setEnabled(false);
                return;
            }

            long discount = (long) points * pointValue;
            if (discount > currentTotalBill) {
                // Không cho trừ âm tiền bill 
                lblDiscountPreview.setText("Tiền giảm vượt quá Tổng Bill!");
                btnConfirm.setEnabled(false);
                return;
            }

            // Hợp lệ
            appliedPoints = points;
            discountAmount = discount;
            lblDiscountPreview.setText(String.format("Số tiền được giảm: -%,d đ", discountAmount));
            btnConfirm.setEnabled(true);

        } catch (NumberFormatException ex) {
            lblDiscountPreview.setText("Dữ liệu nhập không hợp lệ!");
            btnConfirm.setEnabled(false);
        }
    }

    private void resetPreview() {
        appliedPoints = 0;
        discountAmount = 0;
        lblDiscountPreview.setText("Số tiền được giảm: 0 đ");
        btnConfirm.setEnabled(false);
    }

    public boolean isConfirmed() { return isConfirmed; }
    public int getAppliedPoints() { return appliedPoints; }
    public long getDiscountAmount() { return discountAmount; }
}