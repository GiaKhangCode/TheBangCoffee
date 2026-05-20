package View;

import Model.CustomerModel;
import Service.CustomerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class CheckoutDialog extends JDialog {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    
    private CustomerService customerService;
    private CustomerModel currentCustomer = null;

    private long subTotal;
    private double totalVat;
    private long totalBill;
    private long nonRewardTotal;
    private int giaTriMotDiem;
    private int tienTichMotDiem;
    private int diemDoiMotLy;
    private java.util.List<Model.CartItemModel> currentCart;

    private boolean isConfirmed = false;
    private int pointsUsed = 0;
    private long discountAmount = 0;

    // UI Components
    private JTextField txtPhone;
    private JButton btnCheckPhone;
    
    private JPanel registerPanel;
    private JTextField txtNewName;
    private JButton btnRegister;

    private JPanel pointsPanel;
    private JLabel lblCustomerName;
    private JLabel lblCurrentPoints;
    private JCheckBox chkUsePoints;
    private JTextField txtPointsToUse;
    private JLabel lblPointsError;

    private JLabel lblSubtotal;
    private JLabel lblVat;
    private JLabel lblTotalBill;
    private JLabel lblDiscount;
    private JLabel lblFinalTotal;
    private JLabel lblEarnedPoints;
    
    private JRadioButton rbGuest;
    private JRadioButton rbMember;
    private JPanel searchPanel;

    private JButton btnConfirm;

    public CheckoutDialog(Frame owner, long subTotal, double totalVat, long totalBill, long nonRewardTotal, int giaTriMotDiem, int tienTichMotDiem, int diemDoiMotLy, java.util.List<Model.CartItemModel> currentCart) {
        super(owner, "Thanh Toán & Tạo Đơn", true);
        this.customerService = new CustomerService();
        this.subTotal = subTotal;
        this.totalVat = totalVat;
        this.totalBill = totalBill;
        this.nonRewardTotal = nonRewardTotal;
        this.giaTriMotDiem = giaTriMotDiem;
        this.tienTichMotDiem = tienTichMotDiem;
        this.diemDoiMotLy = diemDoiMotLy;
        this.currentCart = currentCart;

        initComponents();
        updateSummary();
    }

    private void initComponents() {
        setSize(450, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 0. Customer Type Panel
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        typePanel.setBackground(Color.WHITE);
        
        rbGuest = new JRadioButton("Khách vãng lai");
        rbMember = new JRadioButton("Khách thành viên");
        rbGuest.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rbMember.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rbGuest.setBackground(Color.WHITE);
        rbMember.setBackground(Color.WHITE);
        
        ButtonGroup bgType = new ButtonGroup();
        bgType.add(rbGuest);
        bgType.add(rbMember);
        rbGuest.setSelected(true); // Mặc định khách vãng lai
        
        typePanel.add(rbGuest);
        typePanel.add(rbMember);

        // 1. Phone Search Panel
        searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(230, 230, 230)), "Tìm khách hàng",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), PRIMARY_COLOR
        ));

        txtPhone = new JTextField();
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPhone.setPreferredSize(new Dimension(0, 35));
        
        btnCheckPhone = new JButton("Kiểm tra");
        btnCheckPhone.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCheckPhone.setBackground(PRIMARY_COLOR);
        btnCheckPhone.setForeground(Color.WHITE);
        btnCheckPhone.setFocusPainted(false);
        
        btnCheckPhone.addActionListener(e -> handleCheckCustomer());
        txtPhone.addActionListener(e -> handleCheckCustomer()); // Enter in text field

        searchPanel.add(txtPhone, BorderLayout.CENTER);
        searchPanel.add(btnCheckPhone, BorderLayout.EAST);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        searchPanel.setVisible(false); // Mặc định ẩn vì chọn Khách vãng lai

        // Listener for Radio Buttons
        rbGuest.addActionListener(e -> {
            searchPanel.setVisible(false);
            registerPanel.setVisible(false);
            pointsPanel.setVisible(false);
            
            currentCustomer = null;
            pointsUsed = 0;
            discountAmount = 0;
            updateSummary();
            
            revalidate();
            repaint();
        });
        
        rbMember.addActionListener(e -> {
            searchPanel.setVisible(true);
            
            if (currentCustomer != null) {
                pointsPanel.setVisible(true);
            } else if (txtPhone.getText().trim().length() > 0) {
                // Đã nhập số nhưng chưa tìm thấy
                handleCheckCustomer();
            }
            
            revalidate();
            repaint();
        });

        // 2. Register Panel (Hidden by default)
        registerPanel = new JPanel(new BorderLayout(10, 0));
        registerPanel.setBackground(new Color(248, 249, 250));
        registerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        registerPanel.setVisible(false);

        JLabel lblReg = new JLabel("Khách mới! Nhập tên để đăng ký:");
        lblReg.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblReg.setForeground(new Color(231, 76, 60));
        
        txtNewName = new JTextField();
        txtNewName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        btnRegister = new JButton("Đăng ký");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegister.setBackground(new Color(41, 128, 185));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.addActionListener(e -> handleRegister());

        JPanel regInput = new JPanel(new BorderLayout(5, 0));
        regInput.setOpaque(false);
        regInput.add(txtNewName, BorderLayout.CENTER);
        regInput.add(btnRegister, BorderLayout.EAST);

        registerPanel.add(lblReg, BorderLayout.NORTH);
        registerPanel.add(regInput, BorderLayout.CENTER);
        registerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        // 3. Points Panel (Hidden by default)
        pointsPanel = new JPanel();
        pointsPanel.setLayout(new BoxLayout(pointsPanel, BoxLayout.Y_AXIS));
        pointsPanel.setBackground(Color.WHITE);
        pointsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pointsPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(230, 230, 230)), "Thông tin & Điểm",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), PRIMARY_COLOR
        ));
        pointsPanel.setVisible(false);

        lblCustomerName = new JLabel("Tên: -");
        lblCustomerName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCurrentPoints = new JLabel("Điểm hiện tại: 0");
        lblCurrentPoints.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCurrentPoints.setForeground(PRIMARY_COLOR);

        JPanel infoRow = new JPanel(new GridLayout(1, 2));
        infoRow.setOpaque(false);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoRow.add(lblCustomerName);
        infoRow.add(lblCurrentPoints);
        
        JLabel lblRule = new JLabel(String.format("Quy tắc: %d điểm = 1 ly miễn phí, 1 điểm dư = %d đ", diemDoiMotLy, giaTriMotDiem));
        lblRule.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblRule.setForeground(Color.GRAY);
        lblRule.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        chkUsePoints = new JCheckBox("Dùng điểm tích lũy");
        chkUsePoints.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkUsePoints.setBackground(Color.WHITE);
        chkUsePoints.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkUsePoints.addActionListener(e -> togglePointsInput());

        JPanel pointsInputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pointsInputRow.setOpaque(false);
        pointsInputRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblWantUse = new JLabel("Số điểm muốn dùng: ");
        lblWantUse.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pointsInputRow.add(lblWantUse);
        txtPointsToUse = new JTextField(8);
        txtPointsToUse.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtPointsToUse.setEnabled(false);
        pointsInputRow.add(txtPointsToUse);

        lblPointsError = new JLabel(" ");
        lblPointsError.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblPointsError.setForeground(Color.RED);
        lblPointsError.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPointsToUse.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calculateDiscount(); }
            public void removeUpdate(DocumentEvent e) { calculateDiscount(); }
            public void changedUpdate(DocumentEvent e) { calculateDiscount(); }
        });

        pointsPanel.add(infoRow);
        pointsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        pointsPanel.add(lblRule);
        pointsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        pointsPanel.add(chkUsePoints);
        pointsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        pointsPanel.add(pointsInputRow);
        pointsPanel.add(lblPointsError);
        pointsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // 4. Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(6, 2, 0, 10));
        summaryPanel.setBackground(new Color(248, 249, 250));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)), 
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel l0_1 = new JLabel("Tạm tính:"); l0_1.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSubtotal = new JLabel("0 đ", SwingConstants.RIGHT); lblSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel l0_2 = new JLabel("VAT:"); l0_2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblVat = new JLabel("0 đ", SwingConstants.RIGHT); lblVat.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel l1 = new JLabel("Tổng tiền món:"); l1.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblTotalBill = new JLabel("0 đ", SwingConstants.RIGHT); lblTotalBill.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        JLabel l2 = new JLabel("Giảm giá (từ điểm):"); l2.setFont(new Font("Segoe UI", Font.PLAIN, 15)); l2.setForeground(new Color(39, 174, 96));
        lblDiscount = new JLabel("-0 đ", SwingConstants.RIGHT); lblDiscount.setFont(new Font("Segoe UI", Font.BOLD, 15)); lblDiscount.setForeground(new Color(39, 174, 96));

        JLabel l3 = new JLabel("Khách phải trả:"); l3.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblFinalTotal = new JLabel("0 đ", SwingConstants.RIGHT); lblFinalTotal.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblFinalTotal.setForeground(new Color(231, 76, 60));

        JLabel l4 = new JLabel("Điểm cộng thêm:"); l4.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblEarnedPoints = new JLabel("+0 điểm", SwingConstants.RIGHT); lblEarnedPoints.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblEarnedPoints.setForeground(PRIMARY_COLOR);

        summaryPanel.add(l0_1); summaryPanel.add(lblSubtotal);
        summaryPanel.add(l0_2); summaryPanel.add(lblVat);
        summaryPanel.add(l1); summaryPanel.add(lblTotalBill);
        summaryPanel.add(l2); summaryPanel.add(lblDiscount);
        summaryPanel.add(l3); summaryPanel.add(lblFinalTotal);
        summaryPanel.add(l4); summaryPanel.add(lblEarnedPoints);
        summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        // Add to body
        body.add(typePanel);
        body.add(Box.createRigidArea(new Dimension(0, 15)));
        body.add(searchPanel);
        body.add(Box.createRigidArea(new Dimension(0, 5)));
        body.add(registerPanel);
        body.add(pointsPanel);
        body.add(Box.createVerticalGlue());
        body.add(summaryPanel);

        add(body, BorderLayout.CENTER);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnCancel.addActionListener(e -> dispose());

        btnConfirm = new JButton("XÁC NHẬN TẠO ĐƠN");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setBackground(PRIMARY_COLOR);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> {
            if (lblPointsError.getText().trim().length() > 0 && lblPointsError.getForeground().equals(Color.RED)) {
                JOptionPane.showMessageDialog(this, "Số điểm sử dụng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            isConfirmed = true;
            dispose();
        });

        footer.add(btnCancel);
        footer.add(btnConfirm);

        add(footer, BorderLayout.SOUTH);
    }

    private void handleCheckCustomer() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            return;
        }

        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CustomerModel customer = customerService.findCustomerByPhone(phone);
        
        if (customer != null) {
            currentCustomer = customer;
            registerPanel.setVisible(false);
            pointsPanel.setVisible(true);
            
            lblCustomerName.setText("Tên: " + customer.getTenKH());
            lblCurrentPoints.setText("Điểm hiện tại: " + customer.getDiemHienTai());
            
            chkUsePoints.setSelected(false);
            txtPointsToUse.setText("");
            txtPointsToUse.setEnabled(false);
            
            
        } else {
            currentCustomer = null;
            registerPanel.setVisible(true);
            pointsPanel.setVisible(false);
            txtNewName.requestFocus();
        }
        
        pointsUsed = 0;
        discountAmount = 0;
        updateSummary();
        revalidate();
        repaint();
    }

    private void handleRegister() {
        String phone = txtPhone.getText().trim();
        String name = txtNewName.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            CustomerModel newCustomer = customerService.registerNewCustomer(phone, name);
            if (newCustomer != null) {
                JOptionPane.showMessageDialog(this, "Đăng ký thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                handleCheckCustomer(); // Reload customer info
            } else {
                JOptionPane.showMessageDialog(this, "Đăng ký thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void togglePointsInput() {
        if (chkUsePoints.isSelected()) {
            txtPointsToUse.setEnabled(true);
            txtPointsToUse.requestFocus();
        } else {
            txtPointsToUse.setEnabled(false);
            txtPointsToUse.setText("");
            pointsUsed = 0;
            discountAmount = 0;
            lblPointsError.setText(" ");
            updateSummary();
        }
    }

    private java.util.List<Long> getEligibleItemPrices() {
        java.util.List<Long> prices = new java.util.ArrayList<>();
        if (currentCart != null) {
            for (Model.CartItemModel item : currentCart) {
                if (!item.isReward() && item.getTotalPrice() > 0) {
                    long unitPrice = item.getUnitPrice();
                    for (int i = 0; i < item.getQuantity(); i++) {
                        prices.add(unitPrice);
                    }
                }
            }
        }
        java.util.Collections.sort(prices); 
        return prices;
    }

    private void calculateDiscount() {
        if (!chkUsePoints.isSelected() || currentCustomer == null) return;
        
        String text = txtPointsToUse.getText().trim();
        if (text.isEmpty()) {
            pointsUsed = 0;
            discountAmount = 0;
            lblPointsError.setText(" ");
            btnConfirm.setEnabled(true);
            updateSummary();
            return;
        }

        try {
            int p = Integer.parseInt(text);
            if (p <= 0) {
                lblPointsError.setForeground(Color.RED);
                lblPointsError.setText("Vui lòng nhập số > 0");
                btnConfirm.setEnabled(false);
                return;
            }
            if (p > currentCustomer.getDiemHienTai()) {
                lblPointsError.setForeground(Color.RED);
                lblPointsError.setText("Vượt quá số điểm hiện tại!");
                btnConfirm.setEnabled(false);
                return;
            }

            java.util.List<Long> eligiblePrices = getEligibleItemPrices();
            int maxDrinksInCart = eligiblePrices.size();

            int potentialFreeDrinks = p / diemDoiMotLy;
            int actualFreeDrinks = Math.min(potentialFreeDrinks, maxDrinksInCart);

            int pointsUsedForDrinks = actualFreeDrinks * diemDoiMotLy;
            int leftoverPoints = p - pointsUsedForDrinks;

            long drinksDiscount = 0;
            for (int i = 0; i < actualFreeDrinks; i++) {
                drinksDiscount += eligiblePrices.get(i);
            }

            long cashDiscount = (long) leftoverPoints * giaTriMotDiem;
            long discount = drinksDiscount + cashDiscount;

            if (discount > totalBill) {
                discount = totalBill;
            }

            if (actualFreeDrinks > 0 || cashDiscount > 0) {
                lblPointsError.setForeground(new Color(39, 174, 96)); // Màu xanh lá
                String msg = "";
                if (actualFreeDrinks > 0) msg += "Quy đổi: " + actualFreeDrinks + " ly miễn phí";
                if (cashDiscount > 0) msg += (msg.isEmpty() ? "Giảm: " : " và giảm thêm ") + String.format("%,d đ", cashDiscount);
                lblPointsError.setText(msg);
            } else {
                lblPointsError.setText(" ");
            }

            btnConfirm.setEnabled(true);
            pointsUsed = p;
            discountAmount = discount;
            updateSummary();

        } catch (NumberFormatException ex) {
            lblPointsError.setForeground(Color.RED);
            lblPointsError.setText("Dữ liệu không hợp lệ!");
            btnConfirm.setEnabled(false);
        }
    }

    private void updateSummary() {
        lblSubtotal.setText(String.format("%,d đ", subTotal));
        lblVat.setText(String.format("%,.0f đ", totalVat));
        lblTotalBill.setText(String.format("%,d đ", totalBill));
        lblDiscount.setText(String.format("-%,d đ", discountAmount));
        
        long finalAmt = totalBill - discountAmount;
        if (finalAmt < 0) finalAmt = 0;
        lblFinalTotal.setText(String.format("%,d đ", finalAmt));
        
        int earned = 0;
        if (currentCustomer != null && tienTichMotDiem > 0) {
            long paid = nonRewardTotal - discountAmount;
            if (paid > 0) {
                earned = (int) (paid / tienTichMotDiem);
            }
        }
        lblEarnedPoints.setText(String.format("+%d điểm", earned));
    }

    public boolean isConfirmed() { return isConfirmed; }
    public int getCustomerId() { return currentCustomer != null ? currentCustomer.getMaKH() : -1; }
    public int getPointsUsed() { return pointsUsed; }
    public long getDiscountAmount() { return discountAmount; }
}
