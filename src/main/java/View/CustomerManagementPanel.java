package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class CustomerManagementPanel extends JPanel {
    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private JTextField txtSearchCustomer;
    private JComboBox<String> cbMembershipTier;
    private JButton btnAddCustomer;
    private JTable customerTable;
    private DefaultTableModel tableModel;

    public CustomerManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initHeaderPanel();
        initTablePanel();
    }

    private void initHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Color.WHITE);

        txtSearchCustomer = new JTextField(20);
        txtSearchCustomer.setPreferredSize(new Dimension(250, 35));
        txtSearchCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearchCustomer.setText("Nhập SĐT hoặc Tên...");
        txtSearchCustomer.setForeground(Color.GRAY);
        
        txtSearchCustomer.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearchCustomer.getText().equals("Nhập SĐT hoặc Tên...")) {
                    txtSearchCustomer.setText("");
                    txtSearchCustomer.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearchCustomer.getText().trim().isEmpty()) {
                    txtSearchCustomer.setForeground(Color.GRAY);
                    txtSearchCustomer.setText("Nhập SĐT hoặc Tên...");
                }
            }
        });

        cbMembershipTier = new JComboBox<>();
        cbMembershipTier.setPreferredSize(new Dimension(150, 35));
        cbMembershipTier.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbMembershipTier.setBackground(Color.WHITE);

        filterPanel.add(new JLabel("Tìm kiếm:"));
        filterPanel.add(txtSearchCustomer);
        filterPanel.add(new JLabel("Hạng:"));
        filterPanel.add(cbMembershipTier);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.setBackground(Color.WHITE);

        btnAddCustomer = Common.ComponentUI.createModernButton("+ Thêm Khách Hàng", PRIMARY_COLOR, Color.WHITE);
        btnAddCustomer.setPreferredSize(new Dimension(180, 35));

        actionPanel.add(btnAddCustomer);

        headerPanel.add(filterPanel, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void initTablePanel() {
        // [CẬP NHẬT] Tách rõ 2 cột điểm
        String[] columnNames = {"Mã KH", "Số Điện Thoại", "Tên Khách Hàng", "Ngày Đăng Ký", "Điểm Hiện Tại", "Điểm Tích Lũy", "Hạng Thành Viên"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(35);
        customerTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        customerTable.setSelectionBackground(new Color(220, 240, 230));
        customerTable.setGridColor(new Color(230, 230, 230));

        JTableHeader header = customerTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(245, 245, 245));
        header.setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        add(scrollPane, BorderLayout.CENTER);
    }

    public JTextField getTxtSearchCustomer() { return txtSearchCustomer; }
    public JComboBox<String> getCbMembershipTier() { return cbMembershipTier; }
    public JButton getBtnAddCustomer() { return btnAddCustomer; }
    public JTable getCustomerTable() { return customerTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
}