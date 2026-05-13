package View;

import Model.CategoryModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class CategoryManagementPanel extends JPanel {
    private final Color PRIMARY_COLOR = new Color(67, 142, 104); 
    private final Color EDIT_COLOR = new Color(41, 128, 185);    
    private final Color HIDE_COLOR = new Color(231, 76, 60);     
    private final Color DISABLED_BG = new Color(245, 245, 245);  

    private JButton btnAddCategory;
    private JTable table;
    private DefaultTableModel tableModel;

    public interface AddCategoryCallback { void onAdd(String name, double vat); }
    public interface EditCategoryCallback { void onEdit(int id, String newName, double newVat); }
    public interface ToggleStatusCallback { void onToggle(int id, String currentStatus); }

    private AddCategoryCallback addCallback;
    private EditCategoryCallback editCallback;
    private ToggleStatusCallback toggleCallback;

    public CategoryManagementPanel() {
        setLayout(new BorderLayout(0, 10)); 
        setBackground(Color.WHITE);         
        setOpaque(true);
        setBorder(new EmptyBorder(10, 10, 10, 10)); 

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    // ==========================================
    // 1. TẠO THANH CÔNG CỤ (CHỈ CÓ NÚT THÊM MỚI)
    // ==========================================
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10)); // Canh phải cho đẹp
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230))); 

        btnAddCategory = new JButton("+ Thêm Danh Mục");
        btnAddCategory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddCategory.setBackground(PRIMARY_COLOR);
        btnAddCategory.setForeground(Color.WHITE);
        btnAddCategory.setFocusPainted(false);
        btnAddCategory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddCategory.setPreferredSize(new Dimension(180, 40));

        btnAddCategory.addActionListener(e -> showAddDialog());

        panel.add(btnAddCategory);

        return panel;
    }

    // ==========================================
    // 2. TẠO BẢNG DANH MỤC CÓ NÚT BẤM
    // ==========================================
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] cols = {"Mã", "Tên Danh Mục", "Thuế VAT (%)", "Trạng Thái", "Hành Động"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; 
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 245, 245));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(180); 

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String dbStatus = (String) table.getModel().getValueAt(row, 3);
                
                if (column == 3 && "Đã ẩn".equals(value)) {
                    value = "Tạm ngừng sử dụng";
                }

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if ("Đã ẩn".equals(dbStatus)) {
                    c.setBackground(DISABLED_BG);
                    c.setForeground(Color.GRAY);
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                    c.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
                }
                return c;
            }
        });

        table.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonsRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionButtonsEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // 3. DIALOG VÀ CALLBACKS
    // ==========================================
    public void setAddAction(AddCategoryCallback callback) { this.addCallback = callback; }
    public void setEditAction(EditCategoryCallback callback) { this.editCallback = callback; }
    public void setToggleStatusAction(ToggleStatusCallback callback) { this.toggleCallback = callback; }

    public void loadDataToTable(List<CategoryModel> list) {
        tableModel.setRowCount(0);
        for (CategoryModel c : list) {
            tableModel.addRow(new Object[]{
                    c.getCategoryID(),
                    c.getCategoryName(),
                    c.getDefaultVat(),
                    c.getCategoryStatus(),
                    "" 
            });
        }
    }

    private void showAddDialog() {
        JTextField txtAddName = new JTextField();
        JTextField txtAddVat = new JTextField("8"); // Mặc định 8%

        Object[] message = { "Tên danh mục:", txtAddName, "Thuế VAT (%):", txtAddVat };
        int option = JOptionPane.showConfirmDialog(this, message, "Thêm Danh Mục Mới", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION && addCallback != null) {
            try {
                String newName = txtAddName.getText().trim();
                double newVat = Double.parseDouble(txtAddVat.getText().trim());
                addCallback.onAdd(newName, newVat); 
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Thuế VAT phải là số hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void showEditDialog(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        String oldName = (String) tableModel.getValueAt(row, 1);
        double oldVat = (double) tableModel.getValueAt(row, 2);

        JTextField txtEditName = new JTextField(oldName);
        JTextField txtEditVat = new JTextField(String.valueOf(oldVat));

        Object[] message = { "Tên danh mục:", txtEditName, "Thuế VAT (%):", txtEditVat };
        int option = JOptionPane.showConfirmDialog(this, message, "Cập Nhật Danh Mục #" + id, JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION && editCallback != null) {
            try {
                String newName = txtEditName.getText().trim();
                double newVat = Double.parseDouble(txtEditVat.getText().trim());
                editCallback.onEdit(id, newName, newVat); 
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Thuế VAT phải là số hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // ==========================================
    // 4. LỚP PANEL GỘP 2 NÚT (Sửa & Ẩn)
    // ==========================================
    class ActionButtonsPanel extends JPanel {
        JButton btnEdit;
        JButton btnToggle;

        public ActionButtonsPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
            setOpaque(true);

            btnEdit = new JButton("Sửa");
            btnEdit.setBackground(EDIT_COLOR);
            btnEdit.setForeground(Color.WHITE);
            btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnEdit.setFocusPainted(false);
            btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnToggle = new JButton("Ẩn");
            btnToggle.setBackground(HIDE_COLOR);
            btnToggle.setForeground(Color.WHITE);
            btnToggle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnToggle.setFocusPainted(false);
            btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));

            add(btnEdit);
            add(btnToggle);
        }

        public void updateData(String status, boolean isSelected, JTable table) {
            if ("Đã ẩn".equals(status)) {
                btnToggle.setText("Sử dụng lại");
                btnToggle.setBackground(PRIMARY_COLOR); // Màu xanh lá
                setBackground(DISABLED_BG); 
            } else {
                btnToggle.setText("Ẩn");
                btnToggle.setBackground(HIDE_COLOR);    // Màu đỏ
                setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            }
        }
    }

    class ActionButtonsRenderer implements javax.swing.table.TableCellRenderer {
        private ActionButtonsPanel panel = new ActionButtonsPanel();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (String) table.getModel().getValueAt(row, 3);
            panel.updateData(status, isSelected, table);
            return panel;
        }
    }

    class ActionButtonsEditor extends DefaultCellEditor {
        private ActionButtonsPanel panel;
        private int currentRow;

        public ActionButtonsEditor() {
            super(new JCheckBox());
            panel = new ActionButtonsPanel();

            panel.btnEdit.addActionListener(e -> {
                fireEditingStopped();
                showEditDialog(currentRow);
            });

            panel.btnToggle.addActionListener(e -> {
                fireEditingStopped();
                if (toggleCallback != null) {
                    int id = (int) tableModel.getValueAt(currentRow, 0);
                    String currentStatus = (String) tableModel.getValueAt(currentRow, 3);
                    toggleCallback.onToggle(id, currentStatus);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            String status = (String) table.getModel().getValueAt(row, 3);
            panel.updateData(status, isSelected, table);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}