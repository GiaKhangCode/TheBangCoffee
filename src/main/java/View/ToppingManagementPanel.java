package View;

import Model.ToppingModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.net.URL;
import java.util.List;

public class ToppingManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd;
    private JTextField txtSearch;
    private ActionButtonListener actionListener;

    public ToppingManagementPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Top Panel (Search & Add Button) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setOpaque(false);
        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.setText("Tìm kiếm topping...");
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Tìm kiếm topping...")) {
                    txtSearch.setText(""); txtSearch.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm kiếm topping..."); txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        searchPanel.add(txtSearch);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnAdd = Common.ComponentUI.createModernButton("+ Thêm Topping", new Color(67, 142, 104), Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(150, 40));
        btnPanel.add(btnAdd);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);

        // --- Bảng Topping ---
        String[] cols = {"Mã Topping", "Tên Topping", "Giá Bán", "Mã NL Trừ", "Hao Hụt", "Thuế VAT (%)", "Hành động"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        table = new JTable(tableModel);
        Common.ComponentUI.applyTableAlignment(table);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(242, 242, 242));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Cột Hành động (Sửa / Xóa)
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Mã Topping
        table.getColumnModel().getColumn(1).setPreferredWidth(180); // Tên Topping
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Giá Bán
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Mã NL Trừ
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Hao Hụt
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Thuế VAT (%)

        TableColumn actionCol = table.getColumnModel().getColumn(6);
        actionCol.setCellRenderer(new ActionButtonRenderer());
        actionCol.setCellEditor(new ActionButtonEditor(new JCheckBox()));
        actionCol.setPreferredWidth(160); // Đảm bảo đủ chiều rộng để hiển thị 2 nút trên cùng 1 dòng

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void loadDataToTable(List<ToppingModel> list) {
        tableModel.setRowCount(0);
        if (list != null) {
            for (ToppingModel t : list) {
                tableModel.addRow(new Object[]{
                    t.getToppingID(), t.getToppingName(), String.format("%,d", t.getPrice()), 
                    t.getIngredientID(), t.getLossAmount(), t.getVat(), "Sửa / Xóa"
                });
            }
        }
    }

    public void addAddButtonListener(ActionListener listener) { btnAdd.addActionListener(listener); }
    public void addSearchListener(KeyAdapter adapter) { txtSearch.addKeyListener(adapter); }
    public String getSearchText() { return txtSearch.getText().trim(); }
    public void setActionListener(ActionButtonListener listener) { this.actionListener = listener; }

    // Getters dữ liệu dòng
    public int getToppingIdAt(int row) { return (int) tableModel.getValueAt(row, 0); }
    public String getToppingNameAt(int row) { return (String) tableModel.getValueAt(row, 1); }
    public long getToppingPriceAt(int row) { 
        try { return Long.parseLong(tableModel.getValueAt(row, 2).toString().replace(",", "").trim()); } 
        catch(Exception e) { return 0; }
    }
    public int getIngredientIdAt(int row) { return (int) tableModel.getValueAt(row, 3); }
    public double getLossAmountAt(int row) { return (double) tableModel.getValueAt(row, 4); }
    public double getVatAt(int row) { return (double) tableModel.getValueAt(row, 5); }

    // Interface và Class phục vụ cột Sửa / Xóa
    public interface ActionButtonListener { void onEdit(int row); void onDelete(int row); }

    class ActionPanel extends JPanel {
        URL editIconUrl = getClass().getResource("/images/edit-247.png");
        URL deleteIconUrl = getClass().getResource("/images/delete-icon.png");
        JButton btnEdit = new JButton("<html><img src='" + editIconUrl + "' width='12' height='12'> Sửa</html>");
        JButton btnDelete = new JButton("<html><img src='" + deleteIconUrl + "' width='12' height='12'> Xóa</html>");
        public ActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5)); setOpaque(true); setBackground(Color.WHITE);
            styleBtn(btnEdit, new Color(0, 122, 255)); styleBtn(btnDelete, new Color(255, 59, 48));
            add(btnEdit); add(btnDelete);
        }
        void styleBtn(JButton b, Color c) {
            b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setForeground(c);
            b.setBackground(Color.WHITE); b.setBorder(new LineBorder(c, 1));
            b.setPreferredSize(new Dimension(65, 25)); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    class ActionButtonRenderer implements TableCellRenderer {
        ActionPanel panel = new ActionPanel();
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE); 
            panel.btnEdit.setVisible(hasEditPermission);
            panel.btnDelete.setVisible(hasDeletePermission);
            return panel;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        ActionPanel panel = new ActionPanel(); int currentRow;
        public ActionButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel.btnEdit.addActionListener(e -> { fireEditingStopped(); if(actionListener!=null) actionListener.onEdit(currentRow); });
            panel.btnDelete.addActionListener(e -> { fireEditingStopped(); if(actionListener!=null) actionListener.onDelete(currentRow); });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row; panel.setBackground(table.getSelectionBackground()); 
            panel.btnEdit.setVisible(hasEditPermission);
            panel.btnDelete.setVisible(hasDeletePermission);
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
    
    // --- PHÂN QUYỀN ---
    private boolean hasEditPermission = true;
    private boolean hasDeletePermission = true;
    
    public void setActionPermissions(boolean canEdit, boolean canDelete) {
        this.hasEditPermission = canEdit;
        this.hasDeletePermission = canDelete;
        this.repaint();
    }
    
    public JButton getBtnAdd() { return btnAdd; }
}