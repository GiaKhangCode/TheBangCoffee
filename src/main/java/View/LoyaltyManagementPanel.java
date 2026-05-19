package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

public class LoyaltyManagementPanel extends JPanel {
    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color WARNING_COLOR = new Color(243, 156, 18);

    private JTable tierTable;
    private DefaultTableModel tierTableModel;
    private JButton btnAddTier;
    private ActionButtonListener actionListener;
    private boolean hasEditPermission = true;
    private boolean hasDeletePermission = true;

    private JTextField txtTienTichMotDiem;
    private JTextField txtGiaTriMotDiem;
    private JTextField txtDiemDoiMotLy; // [MỚI]
    private JButton btnSaveRule;

    public LoyaltyManagementPanel() {
        setLayout(new BorderLayout(15, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createTierManagementPanel(), BorderLayout.CENTER);
        add(createPointRulePanel(), BorderLayout.SOUTH);
    }

    private JPanel createTierManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        TitledBorder title = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(172, 197, 219), 1), 
            "1. Danh sách Hạng thẻ"
        );
        title.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setTitleColor(new Color(0, 0, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(title, new EmptyBorder(10, 10, 10, 10)));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);

        btnAddTier = createButton("+ Thêm Hạng", PRIMARY_COLOR); 

        actionPanel.add(btnAddTier);

        String[] columns = {"Mã Hạng", "Tên Hạng", "Điểm Yêu Cầu", "Mặc Định", "Hành động"};
        tierTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; 
            }
        };

        tierTable = new JTable(tierTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String isDefault = (String) getModel().getValueAt(convertRowIndexToModel(row), 3);
                    if ("Có".equals(isDefault)) {
                        c.setBackground(new Color(240, 240, 240)); 
                        c.setForeground(Color.GRAY);
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        };
        
        tierTable.setRowHeight(40);
        tierTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tierTable.setSelectionBackground(new Color(220, 240, 230));

        JTableHeader header = tierTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(245, 245, 245));

        tierTable.getColumnModel().getColumn(3).setMinWidth(0);
        tierTable.getColumnModel().getColumn(3).setMaxWidth(0);
        tierTable.getColumnModel().getColumn(3).setWidth(0);

        TableColumn actionCol = tierTable.getColumnModel().getColumn(4);
        actionCol.setCellRenderer(new ActionButtonRenderer());
        actionCol.setCellEditor(new ActionButtonEditor(new JCheckBox()));

        panel.add(actionPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tierTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPointRulePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panel.setBackground(Color.WHITE);

        TitledBorder title = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(172, 197, 219), 1), 
            "2. Tỷ lệ Quy đổi Điểm"
        );
        title.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setTitleColor(new Color(0, 0, 0));
        panel.setBorder(title);

        txtTienTichMotDiem = new JTextField("10000", 8);
        txtGiaTriMotDiem = new JTextField("100", 8);
        txtDiemDoiMotLy = new JTextField("50", 5); // [MỚI]
        
        styleTextField(txtTienTichMotDiem);
        styleTextField(txtGiaTriMotDiem);
        styleTextField(txtDiemDoiMotLy);

        btnSaveRule = createButton("Lưu Tỷ Lệ", PRIMARY_COLOR);

        panel.add(new JLabel("Chi tiêu:"));
        panel.add(txtTienTichMotDiem);
        panel.add(new JLabel("VNĐ = 1 Điểm  |  1 Điểm ="));
        panel.add(txtGiaTriMotDiem);
        panel.add(new JLabel("VNĐ  |  Đổi 1 ly cần:"));
        panel.add(txtDiemDoiMotLy);
        panel.add(new JLabel("Điểm"));
        panel.add(new JLabel("   ")); 
        panel.add(btnSaveRule);

        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = Common.ComponentUI.createModernButton(text, color, Color.WHITE);
        btn.setPreferredSize(new Dimension(160, 35));
        return btn;
    }

    private void styleTextField(JTextField txt) {
        txt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txt.setHorizontalAlignment(JTextField.RIGHT);
    }

    public interface ActionButtonListener { 
        void onEdit(int row); 
        void onDelete(int row); 
    }

    public void setActionListener(ActionButtonListener listener) {
        this.actionListener = listener;
    }

    public void setActionPermissions(boolean canEdit, boolean canDelete) {
        this.hasEditPermission = canEdit;
        this.hasDeletePermission = canDelete;
        
        // Khóa các ô nhập tỷ lệ quy đổi nếu không có quyền sửa
        txtTienTichMotDiem.setEditable(canEdit);
        txtGiaTriMotDiem.setEditable(canEdit);
        txtDiemDoiMotLy.setEditable(canEdit);
        
        // Vẽ lại bảng để cập nhật trạng thái các nút hành động (Sửa/Xóa)
        if (tierTable != null) {
            tierTable.repaint();
        }
    }

    class ActionPanel extends JPanel {
        URL editIconUrl = getClass().getResource("/images/edit-247.png");
        URL deleteIconUrl = getClass().getResource("/images/delete-icon.png");
        JButton btnEdit = new JButton("<html><img src='" + editIconUrl + "' width='12' height='12'> Sửa</html>");
        JButton btnDelete = new JButton("<html><img src='" + deleteIconUrl + "' width='12' height='12'> Xóa</html>");
        public ActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5)); 
            setOpaque(true); setBackground(Color.WHITE);
            styleBtn(btnEdit, new Color(0, 122, 255)); 
            styleBtn(btnDelete, new Color(255, 59, 48));
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
            String isDefault = (String) table.getModel().getValueAt(table.convertRowIndexToModel(row), 3);
            if ("Có".equals(isDefault)) {
                panel.setBackground(new Color(240, 240, 240));
                panel.btnEdit.setEnabled(false);
                panel.btnDelete.setEnabled(false);
            } else {
                panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                panel.btnEdit.setEnabled(hasEditPermission);
                panel.btnDelete.setEnabled(hasDeletePermission);
            }
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
            currentRow = row; 
            String isDefault = (String) table.getModel().getValueAt(table.convertRowIndexToModel(row), 3);
            if ("Có".equals(isDefault)) {
                panel.setBackground(new Color(240, 240, 240));
                panel.btnEdit.setEnabled(false);
                panel.btnDelete.setEnabled(false);
            } else {
                panel.setBackground(table.getSelectionBackground()); 
                panel.btnEdit.setEnabled(hasEditPermission);
                panel.btnDelete.setEnabled(hasDeletePermission);
            }
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }

    public DefaultTableModel getTierTableModel() { return tierTableModel; }
    public JTable getTierTable() { return tierTable; }
    public JButton getBtnAddTier() { return btnAddTier; }
    public JButton getBtnSaveRule() { return btnSaveRule; }
    public JTextField getTxtTienTichMotDiem() { return txtTienTichMotDiem; }
    public JTextField getTxtGiaTriMotDiem() { return txtGiaTriMotDiem; }
    public JTextField getTxtDiemDoiMotLy() { return txtDiemDoiMotLy; } // [MỚI]
}