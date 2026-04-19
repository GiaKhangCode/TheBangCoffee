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
import java.awt.*;
import java.io.File;

class ProductCardPanel extends JPanel {
    private Product product;
    private SalesPanel salesPanel;

    public ProductCardPanel(Product product, SalesPanel panel) {
        this.product = product;
        this.salesPanel = panel;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        setAlignmentX(Component.CENTER_ALIGNMENT);

        // Product Image
        ImageIcon icon = new ImageIcon(product.getImageUrl());
        // Scale image to a reasonable size
        Image scaledImage = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        JLabel lblImage = new JLabel(new ImageIcon(scaledImage));
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Product Name
        JLabel lblName = new JLabel("<html><center>" + product.getName() + "</center></html>");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblName.setBorder(new EmptyBorder(10, 0, 5, 0));

        // Price and Add Button in a panel
        JPanel priceAddPanel = new JPanel(new BorderLayout());
        priceAddPanel.setOpaque(false);
        JLabel lblPrice = new JLabel(formatCurrency(product.getPrice()));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPrice.setForeground(new Color(67, 142, 104));
        
        JButton btnAdd = new JButton("+");
        btnAdd.setFont(new Font("Arial", Font.BOLD, 18));
        btnAdd.setForeground(new Color(67, 142, 104));
        btnAdd.setBorderPainted(false);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> salesPanel.addProductToOrder(product));

        priceAddPanel.add(lblPrice, BorderLayout.WEST);
        priceAddPanel.add(btnAdd, BorderLayout.EAST);

        add(lblImage);
        add(lblName);
        add(Box.createVerticalGlue()); // Push price to bottom
        add(priceAddPanel);
    }

    private String formatCurrency(long amount) {
        return String.format("%,d", amount);
    }
}
