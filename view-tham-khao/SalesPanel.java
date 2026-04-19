/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author SONY
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class SalesPanel extends JPanel {
    private JTable orderTable;
    private OrderTableModel orderTableModel;
    private JLabel lblSubtotal;
    private JLabel lblTax;
    private JLabel lblTotal;
    private JPanel menuGridPanel;

    private List<Product> productList;
    private List<OrderLine> currentOrder;

    public SalesPanel() {
        setLayout(new BorderLayout(20, 0)); // Horizontal gap between areas
        setOpaque(false);
        setBorder(new EmptyBorder(10, 20, 10, 10));

        this.currentOrder = new ArrayList<>();
        this.productList = loadProducts(); // Load mock data

        // 1. Menu Area (Left/Center)
        JPanel menuArea = new JPanel(new BorderLayout());
        menuArea.setOpaque(false);

        // Header for menu area
        JPanel menuHeader = new JPanel(new BorderLayout());
        menuHeader.setOpaque(false);
        JLabel titleLabel = new JLabel("MENU BÁN HÀNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JTextField searchField = new JTextField("Thanh liên"); // Mock search
        searchField.setPreferredSize(new Dimension(200, 30));
        
        menuHeader.add(titleLabel, BorderLayout.WEST);
        menuHeader.add(searchField, BorderLayout.EAST);
        menuHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Filters Panel
        JPanel menuFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        menuFilters.setOpaque(false);
        menuFilters.setBorder(new EmptyBorder(0, 0, 20, 0));
        String[] filters = {"Tất cả", "Cà phê", "Trà sữa", "Sinh tố", "Đồ ăn vặt"};
        for (String filter : filters) {
            JButton btnFilter = new JButton(filter);
            btnFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btnFilter.setFocusPainted(false);
            if (filter.equals("Tất cả")) {
                btnFilter.setBackground(new Color(67, 142, 104));
                btnFilter.setForeground(Color.WHITE);
            } else {
                btnFilter.setBackground(new Color(245, 245, 245));
                btnFilter.setForeground(new Color(80, 80, 80));
            }
            menuFilters.add(btnFilter);
        }

        // Product Grid Panel (Scrollable)
        menuGridPanel = new JPanel(new GridLayout(0, 4, 15, 15)); // 4 columns
        menuGridPanel.setOpaque(false);
        for (Product product : productList) {
            ProductCardPanel card = new ProductCardPanel(product, this);
            menuGridPanel.add(card);
        }
        JScrollPane menuScrollPane = new JScrollPane(menuGridPanel);
        menuScrollPane.setBorder(null);
        menuScrollPane.setOpaque(false);
        menuScrollPane.getViewport().setOpaque(false);

        menuArea.add(menuHeader, BorderLayout.NORTH);
        menuArea.add(menuFilters, BorderLayout.CENTER);
        JPanel gridWrapper = new JPanel(new BorderLayout()); // Ensure grid takes all space
        gridWrapper.setOpaque(false);
        gridWrapper.add(menuScrollPane, BorderLayout.CENTER);
        menuArea.add(gridWrapper, BorderLayout.SOUTH);

        add(menuArea, BorderLayout.CENTER);

        // 2. Order Area (Right)
        JPanel orderArea = new JPanel(new BorderLayout());
        orderArea.setOpaque(false);
        orderArea.setPreferredSize(new Dimension(350, 0)); // Fixed width
        orderArea.setBorder(new EmptyBorder(0, 20, 0, 10));

        // Order Header
        JPanel orderHeader = new JPanel(new BorderLayout());
        orderHeader.setOpaque(false);
        JLabel orderTitle = new JLabel("ĐƠN HÀNG #486");
        orderTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        orderHeader.add(orderTitle, BorderLayout.WEST);
        // (Add settings icon here)
        orderHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Order Table (Scrollable)
        orderTableModel = new OrderTableModel();
        orderTable = new JTable(orderTableModel);
        orderTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orderTable.setRowHeight(35);
        // Custom table styling can be added here
        JScrollPane orderTableScrollPane = new JScrollPane(orderTable);
        orderTableScrollPane.setPreferredSize(new Dimension(0, 250));

        // Order Summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        lblSubtotal = createSummaryRow(summaryPanel, "Tạm tính", "0 VND");
        lblTax = createSummaryRow(summaryPanel, "Thuế (10%)", "0 VND");
        lblTotal = createSummaryRow(summaryPanel, "Tổng cộng", "0 VND");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(new Color(67, 142, 104));

        // Payment Buttons
        JPanel paymentPanel = new JPanel();
        paymentPanel.setLayout(new BoxLayout(paymentPanel, BoxLayout.Y_AXIS));
        paymentPanel.setOpaque(false);

        JPanel payMethods = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        payMethods.setOpaque(false);
        payMethods.add(new JButton("Tiền mặt"));
        payMethods.add(new JButton("Chuyển khoản"));
        payMethods.add(new JButton("Thẻ"));
        
        JButton btnPayAll = new JButton("Thanh toán & In hóa đơn");
        btnPayAll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btnPayAll.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnPayAll.setBackground(new Color(67, 142, 104));
        btnPayAll.setForeground(Color.WHITE);
        btnPayAll.setFocusPainted(false);
        btnPayAll.setAlignmentX(Component.CENTER_ALIGNMENT);

        paymentPanel.add(payMethods);
        paymentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        paymentPanel.add(btnPayAll);

        orderArea.add(orderHeader, BorderLayout.NORTH);
        orderArea.add(orderTableScrollPane, BorderLayout.CENTER);
        
        JPanel orderBottom = new JPanel(new BorderLayout());
        orderBottom.setOpaque(false);
        orderBottom.add(summaryPanel, BorderLayout.NORTH);
        orderBottom.add(paymentPanel, BorderLayout.CENTER);
        orderArea.add(orderBottom, BorderLayout.SOUTH);

        add(orderArea, BorderLayout.EAST);
    }

    private JLabel createSummaryRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblLabel.setForeground(Color.GRAY);
        JLabel lblValue = new JLabel(value, SwingConstants.RIGHT);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.CENTER);
        panel.add(row);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        return lblValue;
    }

    public void addProductToOrder(Product product) {
        OrderLine existingLine = null;
        for (OrderLine line : currentOrder) {
            if (line.getProduct().getId().equals(product.getId())) {
                existingLine = line;
                break;
            }
        }
        if (existingLine != null) {
            existingLine.incrementQuantity();
        } else {
            currentOrder.add(new OrderLine(product, 1));
        }
        updateOrderUI();
    }

    private void updateOrderUI() {
        orderTableModel.updateData(currentOrder);
        // Calculate totals (simple simulation)
        long subtotal = 0;
        for (OrderLine line : currentOrder) {
            subtotal += line.getProduct().getPrice() * line.getQuantity();
        }
        long tax = (long) (subtotal * 0.1);
        long total = subtotal + tax;

        lblSubtotal.setText(formatCurrency(subtotal));
        lblTax.setText(formatCurrency(tax));
        lblTotal.setText(formatCurrency(total));
    }

    private String formatCurrency(long amount) {
        // Basic formatting, could be improved with Locale
        return String.format("%,d VND", amount);
    }

    // Mock Product Data
    private List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        // Make sure these image files exist in the 'images' folder
        products.add(new Product("01", "Cà phê cốt dừa", 35000, "images/coffee.png"));
        products.add(new Product("02", "Trà sữa trân châu", 30000, "images/bubble_tea.png"));
        products.add(new Product("03", "Sinh tố Xoài", 32000, "images/mango_smoothie.png"));
        products.add(new Product("04", "Cà phê nâu đá", 28000, "images/coffee.png"));
        // Add more products...
        return products;
    }
}
