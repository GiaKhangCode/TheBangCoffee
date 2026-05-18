package View;

import Model.CategoryModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.util.List;

public class CategoryManagementPanel extends JPanel {
    private final Color PRIMARY_COLOR = new Color(67, 142, 104); 
    private final Color HIDE_COLOR = new Color(231, 76, 60);     
    private final Color DISABLED_BG = new Color(245, 245, 245);  

    private JTextField txtSearch;
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
        setLayout(new BorderLayout(15, 15)); 
        setBackground(Color.WHITE);         
        setOpaque(true);
        setBorder(new EmptyBorder(20, 20, 20, 20)); 

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    // ==========================================
    // 1. TẠO THANH CÔNG CỤ (TÌM KIẾM VÀ THÊM MỚI)
    // ==========================================
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // --- Tìm Kiếm (Bên trái) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setOpaque(false);
        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.setText("Tìm kiếm danh mục...");
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Tìm kiếm danh mục...")) {
                    txtSearch.setText(""); txtSearch.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm kiếm danh mục..."); txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        searchPanel.add(txtSearch);

        // --- Thêm Mới (Bên phải) ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnAddCategory = new JButton("+ Thêm Danh Mục");
        btnAddCategory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddCategory.setBackground(PRIMARY_COLOR);
        btnAddCategory.setForeground(Color.WHITE);
        btnAddCategory.setFocusPainted(false);
        btnAddCategory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddCategory.setPreferredSize(new Dimension(180, 40));

        btnAddCategory.addActionListener(e -> showAddDialog());
        btnPanel.add(btnAddCategory);

        panel.add(searchPanel, BorderLayout.WEST);
        panel.add(btnPanel, BorderLayout.EAST);

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
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

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
    // 3. DIALOG VÀ CALLBACKS & API
    // ==========================================
    public void addSearchListener(KeyAdapter adapter) { txtSearch.addKeyListener(adapter); }
    public String getSearchText() { return txtSearch.getText().trim(); }

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
        JTextField txtAddVat = new JTextField("8");

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
    // 4. LỚP PANEL GỘP 2 NÚT ĐỒNG BỘ UI
    // ==========================================
    class ActionButtonsPanel extends JPanel {
        JButton btnEdit;
        JButton btnToggle;

        public ActionButtonsPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);
            setBackground(Color.WHITE);

            btnEdit = new JButton("Sửa");
            btnToggle = new JButton("Ẩn");

            add(btnEdit);
            add(btnToggle);
        }

        private void styleBtn(JButton b, Color c) {
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setForeground(c);
            b.setBackground(Color.WHITE);
            b.setBorder(new LineBorder(c, 1));
            b.setPreferredSize(new Dimension(80, 25)); // Kích thước cố định để cân xứng cho cả chữ Hiện / Ẩn
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public void updateData(String status, boolean isSelected, JTable table) {
            styleBtn(btnEdit, new Color(0, 122, 255)); // Sửa luôn là màu xanh viền

            if ("Đã ẩn".equals(status) || "Tạm ngừng sử dụng".equals(status)) {
                btnToggle.setText("Hiện lại");
                styleBtn(btnToggle, PRIMARY_COLOR); 
                setBackground(DISABLED_BG); 
            } else {
                btnToggle.setText("Ẩn");
                styleBtn(btnToggle, HIDE_COLOR);
                setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            }
            
            btnEdit.setVisible(hasEditPermission);
            btnToggle.setVisible(hasDeletePermission);
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
    
    // --- PHÂN QUYỀN ---
    private boolean hasEditPermission = true;
    private boolean hasDeletePermission = true;
    
    public void setActionPermissions(boolean canEdit, boolean canDelete) {
        this.hasEditPermission = canEdit;
        this.hasDeletePermission = canDelete;
        this.repaint();
    }
    
    public JButton getBtnAddCategory() { return btnAddCategory; }
}