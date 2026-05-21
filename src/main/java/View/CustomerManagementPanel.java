package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.net.URL;

public class CustomerManagementPanel extends JPanel {
    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private JTextField txtSearchCustomer;
    private JComboBox<String> cbMembershipTier;
    private JButton btnAddCustomer;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private ActionButtonListener actionListener; // [MỚI]

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
        String[] columnNames = {"Mã KH", "Số Điện Thoại", "Tên Khách Hàng", "Ngày Đăng Ký", "Điểm Hiện Tại", "Điểm Tích Lũy", "Hạng Thành Viên", "Hành động"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 7; } // [MỚI] Cột hành động có thể "sửa" để bấm nút
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
        
        // Cài đặt Renderer và Editor cho cột "Hành động"
        TableColumn actionCol = customerTable.getColumnModel().getColumn(7);
        actionCol.setCellRenderer(new ActionButtonRenderer());
        actionCol.setCellEditor(new ActionButtonEditor(new JCheckBox()));

        add(scrollPane, BorderLayout.CENTER);
    }

    public JTextField getTxtSearchCustomer() { return txtSearchCustomer; }
    public JComboBox<String> getCbMembershipTier() { return cbMembershipTier; }
    public JButton getBtnAddCustomer() { return btnAddCustomer; }
    public JTable getCustomerTable() { return customerTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    
    // --- Các thành phần cho Cột Hành Động ---
    public interface ActionButtonListener { 
        void onEdit(int row); 
    }

    public void setActionListener(ActionButtonListener listener) {
        this.actionListener = listener;
    }

    class ActionPanel extends JPanel {
        URL editIconUrl = getClass().getResource("/images/edit-247.png");
        JButton btnEdit = new JButton("<html><img src='" + editIconUrl + "' width='12' height='12'> Sửa</html>");
        
        public ActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0)); 
            setOpaque(true); setBackground(Color.WHITE);
            btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12)); 
            btnEdit.setForeground(new Color(0, 122, 255));
            btnEdit.setBackground(Color.WHITE); 
            btnEdit.setBorder(BorderFactory.createLineBorder(new Color(0, 122, 255), 1));
            btnEdit.setPreferredSize(new Dimension(65, 25)); 
            btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
            add(btnEdit);
        }
    }

    class ActionButtonRenderer implements TableCellRenderer {
        ActionPanel panel = new ActionPanel();
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return panel;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        ActionPanel panel = new ActionPanel(); 
        int currentRow;
        public ActionButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel.btnEdit.addActionListener(e -> { 
                fireEditingStopped(); 
                if(actionListener != null) actionListener.onEdit(currentRow); 
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row; 
            panel.setBackground(table.getSelectionBackground()); 
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
}