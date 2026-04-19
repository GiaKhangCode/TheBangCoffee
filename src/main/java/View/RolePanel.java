package View;

import Common.ComponentUI;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Vector;

/**
 * Giao diện Phân Quyền Access Control theo quy trình 4 bước:
 * 1. Tạo Phạm vi quyền (Policies)
 * 2. Gán Phạm vi quyền cho Nhóm (Policies -> Role)
 * 3. Gán Nhóm cho Tài khoản (Role -> User)
 * 4. Gán Phạm vi quyền cho Tài khoản (Policies -> User)
 */
public class RolePanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(33, 37, 41);

    private JTabbedPane tabbedPane;

    public RolePanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("PHÂN QUYỀN TRUY CẬP");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setBorder(new EmptyBorder(10, 15, 20, 10));
        add(titleLabel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("Quản lý phạm vi quyền", createScopeTab());
        tabbedPane.addTab("Thiết lập nhóm quyền", createRoleAssignmentTab());
        tabbedPane.addTab("Cấu hình nhóm quyền - tài khoản", createAccountRoleTab());
        tabbedPane.addTab("Cấu hình tài Khoản - phạm vi quyền riêng", createAccountScopeTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================================
    // TAB 1: TẠO PHẠM VI QUYỀN (1 Phạm vi giữ nhiều Chức năng)
    // ==========================================================
    private JPanel createScopeTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT: Danh sách các Phạm vi quyền đã tạo
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Các Phạm Vi Quyền", 0, 0, new Font("Segoe UI", Font.BOLD, 14)));

        DefaultListModel<String> scopeListModel = new DefaultListModel<>();
        scopeListModel.addElement("Toàn quyền hệ thống");
        scopeListModel.addElement("Quản trị bán hàng vip pro");
        scopeListModel.addElement("Quản trị bán hàng cơ bản");
        scopeListModel.addElement("Quản trị kho");

        JList<String> listScopes = new JList<>(scopeListModel);
        listScopes.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        listScopes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listScopes.setSelectedIndex(1);
        leftPanel.add(new JScrollPane(listScopes), BorderLayout.CENTER);

        JButton btnAddScope = Common.ComponentUI.createModernButton("+ Tạo Phạm vi quyền mới", PRIMARY_COLOR, Color.WHITE);
        leftPanel.add(btnAddScope, BorderLayout.SOUTH);

        // RIGHT: Các quyền trong nhiều chức năng
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);
        
        JLabel lblTarget = new JLabel("Cấu hình chức năng cho: Quản trị mua bán vip pro");
        lblTarget.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTarget.setForeground(PRIMARY_COLOR);
        
        String[] columns = {"Chức năng", "Xem", "Thêm", "Sửa", "Xóa", "Xuất file"};
        DefaultTableModel tableModel = new DefaultTableModel(null, columns) {
            @Override public Class<?> getColumnClass(int col) { return col == 0 ? String.class : Boolean.class; }
            @Override public boolean isCellEditable(int r, int c) { return c > 0; }
        };
        JTable table = new JTable(tableModel);
        Common.ComponentUI.styleTable(table, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);

        String[] features = {"Sản phẩm", "Đơn hàng", "Khách hàng", "Quản lý kho", "Nhân sự", "Thống kê"};
        for (String f : features) {
            // Mock selected permissions for "Quản trị Mua Bán"
            boolean v = f.equals("Sản phẩm") || f.equals("Đơn hàng") || f.equals("Khách hàng");
            boolean write = f.equals("Đơn hàng");
            tableModel.addRow(new Object[]{f, v, write, write, false, false});
        }

        rightPanel.add(lblTarget, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnSaveScope = createSaveButton("Lưu Phạm Vi Quyền");
        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightFooter.setOpaque(false);
        rightFooter.add(btnSaveScope);
        rightPanel.add(rightFooter, BorderLayout.SOUTH);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================================
    // TAB 2: GÁN PHẠM VI QUYỀN -> NHÓM QUYỀN
    // ==========================================================
    private JPanel createRoleAssignmentTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT: Danh sách Nhóm Quyền
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Các Nhóm Quyền (Roles)", 0, 0, new Font("Segoe UI", Font.BOLD, 14)));

        DefaultListModel<String> roleListModel = new DefaultListModel<>();
        roleListModel.addElement("Quản lý");
        roleListModel.addElement("Nhân viên Bán hàng");
        roleListModel.addElement("Quản lý kho");
        
        JList<String> listRoles = new JList<>(roleListModel);
        listRoles.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        listRoles.setSelectedIndex(1);
        leftPanel.add(new JScrollPane(listRoles), BorderLayout.CENTER);

        JButton btnAddRole = Common.ComponentUI.createModernButton("+ Thêm Nhóm quyền", PRIMARY_COLOR, Color.WHITE);
        leftPanel.add(btnAddRole, BorderLayout.SOUTH);

        // RIGHT: Danh sách Phạm Vi Quyền để Gán (Checkboxes)
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);

        JLabel lblAssign = new JLabel("Gán Phạm Vi Quyền cho: Nhân viên Bán hàng");
        lblAssign.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAssign.setForeground(PRIMARY_COLOR);
        
        JPanel checkboxesPanel = new JPanel();
        checkboxesPanel.setLayout(new BoxLayout(checkboxesPanel, BoxLayout.Y_AXIS));
        checkboxesPanel.setOpaque(false);
        checkboxesPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JCheckBox cb1 = new JCheckBox("Toàn quyền hệ thống");
        JCheckBox cb2 = new JCheckBox("Quản trị bán hàng vip pro", true);
        JCheckBox cb3 = new JCheckBox("Quản trị bán hàng cơ Bản");
        JCheckBox cb4 = new JCheckBox("Quản trị kho");

        Font fontBox = new Font("Segoe UI", Font.PLAIN, 16);
        cb1.setFont(fontBox); cb2.setFont(fontBox); cb3.setFont(fontBox); cb4.setFont(fontBox);

        checkboxesPanel.add(cb1); checkboxesPanel.add(Box.createVerticalStrut(15));
        checkboxesPanel.add(cb2); checkboxesPanel.add(Box.createVerticalStrut(15));
        checkboxesPanel.add(cb3); checkboxesPanel.add(Box.createVerticalStrut(15));
        checkboxesPanel.add(cb4);

        rightPanel.add(lblAssign, BorderLayout.NORTH);
        rightPanel.add(checkboxesPanel, BorderLayout.CENTER);
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(createSaveButton("Gán Quyền"));
        rightPanel.add(footer, BorderLayout.SOUTH);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================================
    // TAB 3: TÀI KHOẢN -> NHÓM QUYỀN
    // ==========================================================
    private JPanel createAccountRoleTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel lblTitle = new JLabel("Thiết lập Nhóm Quyền (Vai trò làm việc chính) cho Tài khoản");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel centerForm = new JPanel(new GridBagLayout());
        centerForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblAcc = new JLabel("Chọn Tài Khoản:");
        lblAcc.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JComboBox<String> cbTaiKhoan = new JComboBox<>(new String[]{
            "Lê Quốc Kiệt", 
            "Bành Xuân Phúc", 
            "Lâm Nguyên Phát"
        });
        cbTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbTaiKhoan.setPreferredSize(new Dimension(300, 40));
        
        JLabel lblRole = new JLabel("Nhóm Quyền Cần Gán:");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JComboBox<String> cbNhomQuyen = new JComboBox<>(new String[]{
            "(Chưa phân nhóm)", 
            "Quản lý", 
            "Nhân viên Bán hàng", 
            "Quản lý kho"
        });
        cbNhomQuyen.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbNhomQuyen.setPreferredSize(new Dimension(300, 40));
        
        // Cập nhật Nhóm hiện tại khi đổi người (để làm mẫu)
        cbTaiKhoan.addActionListener(e -> {
            int idx = cbTaiKhoan.getSelectedIndex();
            if (idx == 0) cbNhomQuyen.setSelectedIndex(1);
            else if (idx == 1) cbNhomQuyen.setSelectedIndex(2);
            else cbNhomQuyen.setSelectedIndex(0);
        });
        cbTaiKhoan.setSelectedIndex(0);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3; centerForm.add(lblAcc, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7; centerForm.add(cbTaiKhoan, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3; centerForm.add(lblRole, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7; centerForm.add(cbNhomQuyen, gbc);

        // Gom vào giữa
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(centerForm);
        
        panel.add(wrapper, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBorder(new EmptyBorder(20, 0, 0, 0));
        footer.setOpaque(false);
        JButton btnSave = ComponentUI.createModernButton("Cập Nhật Vai Trò", PRIMARY_COLOR, Color.WHITE);
        btnSave.setPreferredSize(new Dimension(250, 45));
        footer.add(btnSave);

        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================================
    // TAB 4: TÀI KHOẢN -> PHẠM VI QUYỀN (Quyền riêng đặc biệt)
    // ==========================================================
    private JPanel createAccountScopeTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT: Danh sách Tài khoản
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Danh sách Tài khoản", 0, 0, new Font("Segoe UI", Font.BOLD, 14)));

        DefaultListModel<String> accountListModel = new DefaultListModel<>();
        accountListModel.addElement("Lê Quốc Kiệt");
        accountListModel.addElement("Bành Xuân Phúc");
        accountListModel.addElement("Lâm Nguyên Phát");

        JList<String> listAccounts = new JList<>(accountListModel);
        listAccounts.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        listAccounts.setSelectedIndex(1);
        leftPanel.add(new JScrollPane(listAccounts), BorderLayout.CENTER);

        // RIGHT: Gán Phạm vi quyền
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);

        JPanel headerRight = new JPanel();
        headerRight.setLayout(new BoxLayout(headerRight, BoxLayout.Y_AXIS));
        headerRight.setOpaque(false);
        
        JLabel lblHeader = new JLabel("Cấp thêm PHẠM VI QUYỀN cho riêng Tài Khoản:");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeader.setForeground(PRIMARY_COLOR);
        
        JLabel lblDesc = new JLabel("<html><i>Ghi chú: Các Phạm vi quyền bị mờ đi là họ đã có do được kế thừa từ Nhóm Quyền chính.</i></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(Color.GRAY);
        
        headerRight.add(lblHeader);
        headerRight.add(Box.createVerticalStrut(5));
        headerRight.add(lblDesc);

        JPanel checkboxesPanel = new JPanel();
        checkboxesPanel.setLayout(new BoxLayout(checkboxesPanel, BoxLayout.Y_AXIS));
        checkboxesPanel.setOpaque(false);
        checkboxesPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JCheckBox cb1 = new JCheckBox("Toàn quyền hệ thống");
        JCheckBox cb2 = new JCheckBox("Quản trị bán hàng vip pro  [Kế thừa từ Nhóm NV Bán Hàng]", true);
        cb2.setEnabled(false); // Kế thừa từ Role rồi nên mờ đi
        JCheckBox cb3 = new JCheckBox("Quản trị bán hàng cơ bản", true); // Cấp thêm quyền quản lý kho cho nhân viên bán hàng này!
        JCheckBox cb4 = new JCheckBox("Quản trị nhập kho vip pro");

        Font fontBox = new Font("Segoe UI", Font.PLAIN, 16);
        cb1.setFont(fontBox); cb2.setFont(fontBox); cb3.setFont(fontBox); cb4.setFont(fontBox);

        checkboxesPanel.add(cb1); checkboxesPanel.add(Box.createVerticalStrut(15));
        checkboxesPanel.add(cb2); checkboxesPanel.add(Box.createVerticalStrut(15));
        checkboxesPanel.add(cb3); checkboxesPanel.add(Box.createVerticalStrut(15));
        checkboxesPanel.add(cb4);

        rightPanel.add(headerRight, BorderLayout.NORTH);
        rightPanel.add(checkboxesPanel, BorderLayout.CENTER);
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(createSaveButton("Lưu quyền"));
        rightPanel.add(footer, BorderLayout.SOUTH);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================================
    // UTILS 
    // ==========================================================
    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(240, 240, 240));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(100, 45));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    private JButton createSaveButton(String text) {
        JButton btnSave = Common.ComponentUI.createModernButton(text, PRIMARY_COLOR, Color.WHITE);
        btnSave.setPreferredSize(new Dimension(200, 40));
        btnSave.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã lưu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));
        return btnSave;
    }
}
