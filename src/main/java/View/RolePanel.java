package View;

import Common.ComponentUI;
import Model.AccountModel;
import Model.FunctionModel;
import Model.RoleGroupModel;
import Model.RoleModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.List;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

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
    private DefaultTableModel tableModel;
    private JButton btnAddScope;
    private JButton btnAssignRole;
    
    private JComboBox<String> cbRoleGroup;
    private JComboBox<String> cbRole;
    private JComboBox<String> cbFunctions = new JComboBox<>();
    
    private DefaultTableModel assignedRolesTableModel;
    private JTable assignedRolesTable;
    private JLabel lblAssignedRolesTitle;
    
    // --- THÊM CÁC BIẾN NÀY CHO TAB 3 ---
    private JComboBox<String> cbAccount;
    private JComboBox<String> cbAccountRoleGroup;
    private JButton btnAssignRoleGroupToAccount;
    
    private DefaultTableModel assignedAccountRolesTableModel;
    private JTable assignedAccountRolesTable;
    private JLabel lblAssignedAccountRolesTitle;
    
    private JComboBox<String> cbAccountTab4;
    private JComboBox<String> cbRoleTab4;
    private JButton btnAssignScopeToAccount;
    
    private DefaultTableModel assignedAccountScopesTableModel;
    private JTable assignedAccountScopesTable;
    private JLabel lblAssignedAccountScopesTitle;
    
    private RoleActionListener roleTableListener;
    
    private DeleteActionListener tab2DeleteListener;
    private DeleteActionListener tab3DeleteListener;
    private DeleteActionListener tab4DeleteListener;
    
    // --- THÊM CÁC BIẾN NÀY CHO TAB 5 (QUẢN LÝ NHÓM QUYỀN) ---
    private DefaultTableModel roleGroupTableModel;
    private JTable roleGroupTable;
    private JButton btnAddRoleGroupTab5;
    private RoleActionListener roleGroupTableListener; // Tái sử dụng lại Interface của Tab 1
    // --------------------------------------------------------
   
    private boolean hasEditPermission = true;
    private boolean hasDeletePermission = true;

    // Hàm này để Controller gọi và truyền quyền vào
    public void setActionPermissions(boolean canEdit, boolean canDelete) {
        this.hasEditPermission = canEdit;
        this.hasDeletePermission = canDelete;
        this.repaint(); // Yêu cầu vẽ lại toàn bộ giao diện (bao gồm các bảng)
    }
    
    // ------------------------------------
    
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
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("Quản lý phạm vi quyền", createScopeTab());
        tabbedPane.addTab("Thiết lập nhóm quyền", createRoleAssignmentTab());
        tabbedPane.addTab("Cấu hình nhóm quyền - tài khoản", createAccountRoleTab());
        tabbedPane.addTab("Cấu hình tài Khoản - phạm vi quyền riêng", createAccountScopeTab());
        tabbedPane.addTab("Quản lý nhóm quyền", createRoleGroupManagerTab());
        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================================
    // TAB 1: TẠO PHẠM VI QUYỀN (1 Phạm vi giữ nhiều Chức năng)
    // ==========================================================
    private JPanel createScopeTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        
        // RIGHT: Các quyền trong nhiều chức năng
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);
        
        JLabel lblTarget = new JLabel("Các phạm vi quyền hiện tại");
        lblTarget.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTarget.setForeground(PRIMARY_COLOR);
        
        // 1. Thêm "Hành động" vào mảng cột
        String[] columns = {"Tên phạm vi", "Chức năng", "Thêm", "Sửa", "Xóa", "Xem", "Xuất file", "Hành động"};
        
        this.tableModel = new DefaultTableModel(null, columns) { 
            @Override 
            public Class<?> getColumnClass(int col) { 
                // Cột 0, 1 và 7 là kiểu Text/Object. Cột 2,3,4,5,6 là Checkbox (Boolean)
                return (col == 0 || col == 1 || col == 7) ? Object.class : Boolean.class; 
            }
            @Override 
            public boolean isCellEditable(int r, int c) { 
                // Chỉ cho phép click vào các checkbox (2-6) và nút Hành động (7)
                return c > 1; 
            }
        };
        
        JTable table = new JTable(this.tableModel);
        ComponentUI.styleTable(table, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);

        // Tắt tính năng tự động chia đều chiều rộng các cột (Auto Resize)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); 
        
        // Lấy mô hình cột của bảng
        javax.swing.table.TableColumnModel columnModel = table.getColumnModel();
        
        // Cột 0: Tên phạm vi (Mở rộng ra để không bị khuất chữ)
        columnModel.getColumn(0).setPreferredWidth(220); 
        
        // Cột 1: Chức năng (Thu hẹp lại vì thường chỉ hiện ID hoặc text ngắn)
        columnModel.getColumn(1).setPreferredWidth(100); 
        
        // Cột 2 đến 6: Các cột Checkbox (Thu hẹp vừa đủ chứa cái tick)
        columnModel.getColumn(2).setPreferredWidth(70); // Xem
        columnModel.getColumn(3).setPreferredWidth(70); // Thêm
        columnModel.getColumn(4).setPreferredWidth(70); // Sửa
        columnModel.getColumn(5).setPreferredWidth(70); // Xóa
        columnModel.getColumn(6).setPreferredWidth(80); // Xuất file
        
        // Cột 7: Cột Hành động chứa nút bấm (Bạn đã set trước đó là 140, ta gán lại cho đồng bộ)
        columnModel.getColumn(7).setPreferredWidth(140);
        
        // --- SỬA LẠI ĐOẠN CODE GẮN SỰ KIỆN CHO CỘT HÀNH ĐỘNG ---
        table.setRowHeight(45); 

        // TÌM TỰ ĐỘNG CỘT "Hành động" DỰA VÀO TÊN TIÊU ĐỀ
        int actionColumnIndex = -1;
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals("Hành động")) {
                actionColumnIndex = i;
                break;
            }
        }

        // Nếu tìm thấy cột "Hành động", tiến hành gắn nút bấm
        if (actionColumnIndex != -1) {
            TableColumn actionCol = table.getColumnModel().getColumn(actionColumnIndex);
            
            actionCol.setCellRenderer(new RoleActionButtonRenderer(new RoleActionPanel()));
            actionCol.setCellEditor(new RoleActionButtonEditor(new RoleActionListener() {
                @Override
                public void onEdit(int row) {
                    if (roleTableListener != null) {
                        roleTableListener.onEdit(row);
                    }
                }

                @Override
                public void onDelete(int row) {
                    if (roleTableListener != null) {
                        roleTableListener.onDelete(row);
                    }
                }
            }, new RoleActionPanel()));
            
            actionCol.setPreferredWidth(140);
        }
        
        rightPanel.add(lblTarget, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        this.btnAddScope = ComponentUI.createModernButton("Thêm phạm vi quyền", PRIMARY_COLOR, Color.WHITE);
        this.btnAddScope.setPreferredSize(new Dimension(200, 40));
        
        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightFooter.setOpaque(false);
        rightFooter.add(btnAddScope);
        rightPanel.add(rightFooter, BorderLayout.SOUTH);

        panel.add(rightPanel, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================================
    // TAB 2: GÁN PHẠM VI QUYỀN -> NHÓM QUYỀN (ĐÃ TỐI ƯU BỐ CỤC)
    // ==========================================================
    private JPanel createRoleAssignmentTab() {
        // Giảm padding tổng thể của panel
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40)); 

        // 1. TIÊU ĐỀ
        JLabel lblTitle = new JLabel("Thiết Lập Và Gán Phạm Vi Quyền Cho Nhóm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setForeground(PRIMARY_COLOR);
        panel.add(lblTitle, BorderLayout.NORTH);

        // 2. KHU VỰC TRUNG TÂM (CHỨA FORM TRÊN VÀ BẢNG DƯỚI)
        JPanel wrapper = new JPanel(new BorderLayout(0, 15)); 
        wrapper.setOpaque(false);
        
        // --- 2.1 BẢNG ĐIỀU KHIỂN (FORM) ĐƯỢC THU GỌN ---
        JPanel topControlsPanel = new JPanel(new GridBagLayout());
        topControlsPanel.setOpaque(false);
        // Thêm một khung viền nhẹ để phân tách khu vực form và bảng
        topControlsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Bảng điều khiển", 
                0, 0, 
                new Font("Segoe UI", Font.BOLD, 14), 
                TEXT_DARK));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15); // Ép khoảng cách giữa các hàng/cột nhỏ lại
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Dòng 1: Chọn Nhóm Quyền + Nút Thêm Mới
        JLabel lblRoleGroup = new JLabel("Chọn Nhóm Quyền:");
        lblRoleGroup.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        this.cbRoleGroup = new JComboBox<>();
        cbRoleGroup.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbRoleGroup.setPreferredSize(new Dimension(300, 35)); // Giảm chiều cao xuống 35

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; topControlsPanel.add(lblRoleGroup, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.8; topControlsPanel.add(cbRoleGroup, gbc);
        // Thay nút bấm bằng một khoảng trống để giao diện đồng bộ với các Tab khác
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; topControlsPanel.add(Box.createRigidArea(new Dimension(160, 35)), gbc);
        // Dòng 2: Gán Phạm Vi Quyền + Nút Gán
        JLabel lblScope = new JLabel("Gán Phạm Vi Quyền:");
        lblScope.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        this.cbRole = new JComboBox<>();
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbRole.setPreferredSize(new Dimension(300, 35)); // Giảm chiều cao xuống 35

        this.btnAssignRole = ComponentUI.createModernButton("Gán Quyền", PRIMARY_COLOR, Color.WHITE);
        this.btnAssignRole.setPreferredSize(new Dimension(160, 35));

        gbc.gridx = 0; gbc.gridy = 1; topControlsPanel.add(lblScope, gbc);
        gbc.gridx = 1; gbc.gridy = 1; topControlsPanel.add(cbRole, gbc);
        gbc.gridx = 2; gbc.gridy = 1; topControlsPanel.add(this.btnAssignRole, gbc);

        // Gom Form vào giữa theo chiều ngang
        JPanel formWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        formWrapper.setOpaque(false);
        formWrapper.add(topControlsPanel);
        
        wrapper.add(formWrapper, BorderLayout.NORTH); // Form đặt sát lên trên

        // --- 2.2 KHU VỰC BẢNG (CHIẾM TOÀN BỘ KHÔNG GIAN BÊN DƯỚI) ---
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);

        this.lblAssignedRolesTitle = new JLabel("Danh sách quyền hiện tại của nhóm: (Chưa chọn)");
        this.lblAssignedRolesTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        this.lblAssignedRolesTitle.setForeground(PRIMARY_COLOR);

        String[] cols = {"STT", "Tên phạm vi quyền đã gán", "Hành động"};
        this.assignedRolesTableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int column) { return column == 2; } 
        };
        this.assignedRolesTable = new JTable(this.assignedRolesTableModel);
        ComponentUI.styleTable(this.assignedRolesTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        
        this.assignedRolesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        this.assignedRolesTable.getColumnModel().getColumn(1).setPreferredWidth(600);
        this.assignedRolesTable.setRowHeight(35); // Thu nhỏ chiều cao từng dòng để hiển thị được nhiều hơn

        this.assignedRolesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        this.assignedRolesTable.getColumnModel().getColumn(1).setPreferredWidth(450);
        this.assignedRolesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        TableColumn actionCol2 = this.assignedRolesTable.getColumnModel().getColumn(2);
        actionCol2.setCellRenderer(new DeleteActionButtonRenderer(new DeleteActionPanel()));
        actionCol2.setCellEditor(new DeleteActionButtonEditor(row -> {
            if (tab2DeleteListener != null) tab2DeleteListener.onDelete(row);
        }, new DeleteActionPanel()));
        
        tablePanel.add(this.lblAssignedRolesTitle, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(this.assignedRolesTable), BorderLayout.CENTER);

        wrapper.add(tablePanel, BorderLayout.CENTER); // Bảng tự động giãn chiếm hết chỗ còn lại
        
        panel.add(wrapper, BorderLayout.CENTER);

        return panel;
    }
    
    // ==========================================================
    // TAB 3: TÀI KHOẢN -> NHÓM QUYỀN (ĐÃ TỐI ƯU BỐ CỤC NHƯ TAB 2)
    // ==========================================================
    private JPanel createAccountRoleTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40)); 

        // 1. TIÊU ĐỀ
        JLabel lblTitle = new JLabel("Thiết Lập Nhóm Quyền Cho Tài Khoản");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setForeground(PRIMARY_COLOR);
        panel.add(lblTitle, BorderLayout.NORTH);

        // 2. KHU VỰC TRUNG TÂM (CHỨA FORM TRÊN VÀ BẢNG DƯỚI)
        JPanel wrapper = new JPanel(new BorderLayout(0, 15)); 
        wrapper.setOpaque(false);
        
        // --- 2.1 BẢNG ĐIỀU KHIỂN (FORM) ---
        JPanel topControlsPanel = new JPanel(new GridBagLayout());
        topControlsPanel.setOpaque(false);
        topControlsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Bảng điều khiển", 
                0, 0, 
                new Font("Segoe UI", Font.BOLD, 14), 
                TEXT_DARK));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Dòng 1: Chọn Tài Khoản (Không có nút "Thêm mới" vì tài khoản lấy từ module Nhân sự/User)
        JLabel lblAcc = new JLabel("Chọn Tài Khoản:");
        lblAcc.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        this.cbAccount = new JComboBox<>(new String[]{
            "1 - Lê Quốc Kiệt", 
            "2 - Bành Xuân Phúc", 
            "3 - Lâm Nguyên Phát"
        });
        cbAccount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbAccount.setPreferredSize(new Dimension(300, 35)); 

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; topControlsPanel.add(lblAcc, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.8; topControlsPanel.add(cbAccount, gbc);
        // Cột 3 để trống cho cân đối với dòng dưới
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; topControlsPanel.add(Box.createRigidArea(new Dimension(160, 35)), gbc); 

        // Dòng 2: Chọn Nhóm Quyền Cần Gán + Nút Gán
        JLabel lblRole = new JLabel("Gán Nhóm Quyền:");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        this.cbAccountRoleGroup = new JComboBox<>(new String[]{
            "1 - Quản lý", 
            "2 - Nhân viên Bán hàng", 
            "3 - Quản lý kho"
        });
        cbAccountRoleGroup.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbAccountRoleGroup.setPreferredSize(new Dimension(300, 35)); 

        this.btnAssignRoleGroupToAccount = ComponentUI.createModernButton("Cập Nhật Vai Trò", PRIMARY_COLOR, Color.WHITE);
        this.btnAssignRoleGroupToAccount.setPreferredSize(new Dimension(160, 35));

        gbc.gridx = 0; gbc.gridy = 1; topControlsPanel.add(lblRole, gbc);
        gbc.gridx = 1; gbc.gridy = 1; topControlsPanel.add(cbAccountRoleGroup, gbc);
        gbc.gridx = 2; gbc.gridy = 1; topControlsPanel.add(this.btnAssignRoleGroupToAccount, gbc);

        // Gom Form vào giữa
        JPanel formWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        formWrapper.setOpaque(false);
        formWrapper.add(topControlsPanel);
        
        wrapper.add(formWrapper, BorderLayout.NORTH); 

        // --- 2.2 KHU VỰC BẢNG HIỂN THỊ VAI TRÒ HIỆN TẠI CỦA TÀI KHOẢN ---
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);

        this.lblAssignedAccountRolesTitle = new JLabel("Nhóm quyền hiện tại của tài khoản: (Chưa chọn)");
        this.lblAssignedAccountRolesTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        this.lblAssignedAccountRolesTitle.setForeground(PRIMARY_COLOR);

        String[] cols = {"STT", "Tên nhóm quyền đang giữ", "Hành động"};
        this.assignedAccountRolesTableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int column) { return column == 2; } 
        };
        this.assignedAccountRolesTable = new JTable(this.assignedAccountRolesTableModel);
        ComponentUI.styleTable(this.assignedAccountRolesTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        
        this.assignedAccountRolesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        this.assignedAccountRolesTable.getColumnModel().getColumn(1).setPreferredWidth(600);
        this.assignedAccountRolesTable.setRowHeight(35); 

        this.assignedAccountRolesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        this.assignedAccountRolesTable.getColumnModel().getColumn(1).setPreferredWidth(450);
        this.assignedAccountRolesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        TableColumn actionCol3 = this.assignedAccountRolesTable.getColumnModel().getColumn(2);
        actionCol3.setCellRenderer(new DeleteActionButtonRenderer(new DeleteActionPanel()));
        actionCol3.setCellEditor(new DeleteActionButtonEditor(row -> {
            if (tab3DeleteListener != null) tab3DeleteListener.onDelete(row);
        }, new DeleteActionPanel()));
        
        tablePanel.add(this.lblAssignedAccountRolesTitle, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(this.assignedAccountRolesTable), BorderLayout.CENTER);

        wrapper.add(tablePanel, BorderLayout.CENTER); 
        
        panel.add(wrapper, BorderLayout.CENTER);

        // Xử lý sự kiện tạm thời để test UI (Khi đổi tài khoản sẽ đổi title)
        cbAccount.addActionListener(e -> {
            if (cbAccount.getSelectedItem() != null) {
                String accName = cbAccount.getSelectedItem().toString().split(" - ")[1];
                lblAssignedAccountRolesTitle.setText("Nhóm quyền hiện tại của tài khoản: " + accName);
            }
        });
        cbAccount.setSelectedIndex(0); // Trigger chạy lần đầu

        return panel;
    }

    // ==========================================================
    // TAB 4: TÀI KHOẢN -> PHẠM VI QUYỀN (Quyền riêng đặc biệt)
    // ==========================================================
    private JPanel createAccountScopeTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40)); 

        // 1. TIÊU ĐỀ
        JLabel lblTitle = new JLabel("Cấp Thêm Phạm Vi Quyền Riêng Cho Tài Khoản");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setForeground(PRIMARY_COLOR);
        panel.add(lblTitle, BorderLayout.NORTH);

        // 2. KHU VỰC TRUNG TÂM (CHỨA FORM TRÊN VÀ BẢNG DƯỚI)
        JPanel wrapper = new JPanel(new BorderLayout(0, 15)); 
        wrapper.setOpaque(false);
        
        // --- 2.1 BẢNG ĐIỀU KHIỂN (FORM) ---
        JPanel topControlsPanel = new JPanel(new GridBagLayout());
        topControlsPanel.setOpaque(false);
        topControlsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Bảng điều khiển", 
                0, 0, 
                new Font("Segoe UI", Font.BOLD, 14), 
                TEXT_DARK));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Dòng 1: Chọn Tài Khoản
        JLabel lblAcc = new JLabel("Chọn Tài Khoản:");
        lblAcc.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        this.cbAccountTab4 = new JComboBox<>();
        cbAccountTab4.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbAccountTab4.setPreferredSize(new Dimension(300, 35)); 

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; topControlsPanel.add(lblAcc, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.8; topControlsPanel.add(cbAccountTab4, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; topControlsPanel.add(Box.createRigidArea(new Dimension(160, 35)), gbc); 

        // Dòng 2: Chọn Phạm Vi Quyền (Role)
        JLabel lblScope = new JLabel("Cấp Quyền Riêng:");
        lblScope.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        this.cbRoleTab4 = new JComboBox<>();
        cbRoleTab4.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbRoleTab4.setPreferredSize(new Dimension(300, 35)); 

        this.btnAssignScopeToAccount = ComponentUI.createModernButton("Cấp Quyền", PRIMARY_COLOR, Color.WHITE);
        this.btnAssignScopeToAccount.setPreferredSize(new Dimension(160, 35));

        gbc.gridx = 0; gbc.gridy = 1; topControlsPanel.add(lblScope, gbc);
        gbc.gridx = 1; gbc.gridy = 1; topControlsPanel.add(cbRoleTab4, gbc);
        gbc.gridx = 2; gbc.gridy = 1; topControlsPanel.add(this.btnAssignScopeToAccount, gbc);

        // Gom Form vào giữa
        JPanel formWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        formWrapper.setOpaque(false);
        formWrapper.add(topControlsPanel);
        
        wrapper.add(formWrapper, BorderLayout.NORTH); 

        // --- 2.2 KHU VỰC BẢNG HIỂN THỊ QUYỀN RIÊNG CỦA TÀI KHOẢN ---
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);


        this.lblAssignedAccountScopesTitle = new JLabel("Danh sách quyền riêng của tài khoản: (Chưa chọn)");
        this.lblAssignedAccountScopesTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        this.lblAssignedAccountScopesTitle.setForeground(PRIMARY_COLOR);

        JPanel titlePanel = new JPanel(new BorderLayout(0, 5));
        titlePanel.setOpaque(false);
        titlePanel.add(lblAssignedAccountScopesTitle, BorderLayout.SOUTH);

        String[] cols = {"STT", "Tên phạm vi quyền riêng đã cấp", "Hành động"};
        this.assignedAccountScopesTableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int column) { return column == 2; } 
        };
        this.assignedAccountScopesTable = new JTable(this.assignedAccountScopesTableModel);
        ComponentUI.styleTable(this.assignedAccountScopesTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        
        this.assignedAccountScopesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        this.assignedAccountScopesTable.getColumnModel().getColumn(1).setPreferredWidth(600);
        this.assignedAccountScopesTable.setRowHeight(35); 

        this.assignedAccountScopesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        this.assignedAccountScopesTable.getColumnModel().getColumn(1).setPreferredWidth(450);
        this.assignedAccountScopesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        TableColumn actionCol4 = this.assignedAccountScopesTable.getColumnModel().getColumn(2);
        actionCol4.setCellRenderer(new DeleteActionButtonRenderer(new DeleteActionPanel()));
        actionCol4.setCellEditor(new DeleteActionButtonEditor(row -> {
            if (tab4DeleteListener != null) tab4DeleteListener.onDelete(row);
        }, new DeleteActionPanel()));
        
        tablePanel.add(titlePanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(this.assignedAccountScopesTable), BorderLayout.CENTER);

        wrapper.add(tablePanel, BorderLayout.CENTER); 
        panel.add(wrapper, BorderLayout.CENTER);

        // Xử lý sự kiện tạm thời để test UI (Đổi title khi đổi account)
        cbAccountTab4.addActionListener(e -> {
            Object selected = cbAccountTab4.getSelectedItem();
            if (selected != null && selected.toString().contains(" - ")) {
                String accName = selected.toString().split(" - ")[1];
                lblAssignedAccountScopesTitle.setText("Danh sách quyền riêng của tài khoản: " + accName);
            }
        });

        return panel;
    }
    
    // ==========================================================
    // TAB 5: QUẢN LÝ NHÓM QUYỀN (THÊM / SỬA / XÓA)
    // ==========================================================
    private JPanel createRoleGroupManagerTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        JLabel lblTarget = new JLabel("Danh sách các Nhóm quyền hiện tại");
        lblTarget.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTarget.setForeground(PRIMARY_COLOR);

        // Bảng chỉ có Tên nhóm và Hành động
        String[] columns = {"STT", "Tên nhóm quyền", "Hành động"};
        this.roleGroupTableModel = new DefaultTableModel(null, columns) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2; // Chỉ cho phép click cột Hành động
            }
        };

        this.roleGroupTable = new JTable(this.roleGroupTableModel);
        ComponentUI.styleTable(this.roleGroupTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        this.roleGroupTable.setRowHeight(45);

        this.roleGroupTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        this.roleGroupTable.getColumnModel().getColumn(1).setPreferredWidth(500);
        this.roleGroupTable.getColumnModel().getColumn(2).setPreferredWidth(140);

        TableColumn actionCol = this.roleGroupTable.getColumnModel().getColumn(2);
        actionCol.setCellRenderer(new RoleActionButtonRenderer(new RoleActionPanel()));
        actionCol.setCellEditor(new RoleActionButtonEditor(new RoleActionListener() {
            @Override
            public void onEdit(int row) {
                if (roleGroupTableListener != null) roleGroupTableListener.onEdit(row);
            }
            @Override
            public void onDelete(int row) {
                if (roleGroupTableListener != null) roleGroupTableListener.onDelete(row);
            }
        }, new RoleActionPanel()));

        centerPanel.add(lblTarget, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(this.roleGroupTable), BorderLayout.CENTER);

        this.btnAddRoleGroupTab5 = ComponentUI.createModernButton("Thêm nhóm quyền", PRIMARY_COLOR, Color.WHITE);
        this.btnAddRoleGroupTab5.setPreferredSize(new Dimension(200, 40));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnAddRoleGroupTab5);
        centerPanel.add(footer, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);
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
    
    public void loadRolesToTab1Table(List<RoleModel> roles) {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ trên bảng
        if (roles != null) {
            for (RoleModel role : roles) { 
                boolean add = role.getAdd() > 0 ? true : false;
                boolean edit = role.getEdit() > 0 ? true : false;
                boolean delete = role.getDelete() > 0 ? true : false;
                boolean view = role.getView() > 0 ? true : false;
                boolean exportFile = role.getExportFile() > 0 ? true : false;
                tableModel.addRow(new Object[]{role.getRoleName(), role.getFunctionId(), add, edit, delete, view, exportFile, "Sửa / Xóa"});
            }
        }
    }
    
    // Hàm này giúp Controller đẩy dữ liệu Nhóm Quyền vào ComboBox
    public void loadRoleGroupsToTab2ComboBox(List<RoleGroupModel> roleGroups) {
        cbRoleGroup.removeAllItems(); // Xóa sạch dữ liệu cũ
        
        if (roleGroups != null) {
            for (RoleGroupModel group : roleGroups) {
                String displayItem = group.getRoleGroupId() + " - " + group.getRoleGroupName();
                cbRoleGroup.addItem(displayItem);
            }
        }
    }
    
    // Hàm này giúp Controller đẩy dữ liệu phạm vi quyền vào ComboBox (Tab 2)
    public void loadRolesToTab2ComboBox(List<RoleModel> roles) {
        cbRole.removeAllItems(); 
        
        if (roles != null && !roles.isEmpty()) {
            for (RoleModel role : roles) {
                String displayItem = role.getRoleId() + " - " + role.getRoleName();
                cbRole.addItem(displayItem);
            }
            if (btnAssignRole != null) {
                btnAssignRole.setEnabled(true);
            }
        } 
        else {
            cbRole.addItem("Tất cả các quyền đã được cấp cho nhóm này");
            if (btnAssignRole != null) {
                btnAssignRole.setEnabled(false);
            }
        }
    }
    
    public void loadFunctionsToComboBox(List<FunctionModel> functions) {
        cbFunctions.removeAllItems(); // Xóa sạch dữ liệu cũ
        
        if (functions != null) {
            for (FunctionModel function : functions) {
                String displayItem = function.getFunctionId() + " - " + function.getFunctionName();
                cbFunctions.addItem(displayItem);
            }
        }
    }
    
    public void loadConfiguredRolesToTab2Table(String groupName, List<RoleModel> assignedRoles) {
        lblAssignedRolesTitle.setText("Danh sách quyền hiện tại của nhóm: " + groupName);
        assignedRolesTableModel.setRowCount(0); // Xóa dữ liệu cũ
        
        if (assignedRoles != null) {
            int stt = 1;
            for (RoleModel role : assignedRoles) {
                assignedRolesTableModel.addRow(new Object[]{
                    stt++, 
                    role.getRoleName(),
                    ""
                });
            }
        }
    }
    
    // Hàm đẩy dữ liệu tài khoản vào ComboBox Tab 3
    // (Lưu ý: Bạn import class AccountModel vào nhé)
    public void loadAccountsToTab3ComboBox(List<AccountModel> accounts) {
        cbAccount.removeAllItems();
        if (accounts != null) {
            for (AccountModel acccount : accounts) {
                // Giả sử AccountModel có hàm getAccountId() và getUsername() / getFullName()
                cbAccount.addItem(acccount.getAccountID() + " - " + acccount.getUsername());
            }
        }
    }

    // Hàm đẩy dữ liệu Nhóm quyền vào ComboBox Tab 3
    // Hàm đẩy dữ liệu Nhóm quyền vào ComboBox Tab 3
    public void loadRoleGroupsToTab3ComboBox(List<RoleGroupModel> roleGroups) {
        cbAccountRoleGroup.removeAllItems();
        
        // Nếu danh sách khác null VÀ có chứa phần tử
        if (roleGroups != null && !roleGroups.isEmpty()) {
            for (RoleGroupModel group : roleGroups) {
                cbAccountRoleGroup.addItem(group.getRoleGroupId() + " - " + group.getRoleGroupName());
            }
            // Mở khóa nút Cập nhật
            if (btnAssignRoleGroupToAccount != null) {
                btnAssignRoleGroupToAccount.setEnabled(true);
            }
        } 
        // Nếu danh sách trống (tài khoản đã có đủ mọi nhóm quyền)
        else {
            cbAccountRoleGroup.addItem("Tài khoản này đã được cấp tất cả các nhóm quyền");
            // Khóa nút Cập nhật
            if (btnAssignRoleGroupToAccount != null) {
                btnAssignRoleGroupToAccount.setEnabled(false);
            }
        }
    }

    public void loadAssignedGroupsToTab3Table(String accountName, List<RoleGroupModel> assignedGroups) {
        lblAssignedAccountRolesTitle.setText("Nhóm quyền hiện tại của tài khoản: " + accountName);
        assignedAccountRolesTableModel.setRowCount(0); // Xóa dữ liệu cũ
        
        if (assignedGroups != null) {
            int stt = 1;
            for (RoleGroupModel group : assignedGroups) {
                assignedAccountRolesTableModel.addRow(new Object[]{
                    stt++, 
                    group.getRoleGroupName(),
                    ""
                });
            }
        }
    }
    
    // Load dữ liệu Account vào ComboBox của Tab 4
    public void loadAccountsToTab4ComboBox(List<AccountModel> accounts) {
        cbAccountTab4.removeAllItems();
        if (accounts != null) {
            for (AccountModel account : accounts) {
                cbAccountTab4.addItem(account.getAccountID() + " - " + account.getUsername());
            }
        }
    }

    // Load dữ liệu Phạm vi quyền vào ComboBox của Tab 4
    public void loadRolesToTab4ComboBox(List<RoleModel> roles) {
        cbRoleTab4.removeAllItems();
        
        // Nếu danh sách khác null VÀ có chứa phần tử
        if (roles != null && !roles.isEmpty()) {
            for (RoleModel role : roles) {
                cbRoleTab4.addItem(role.getRoleId() + " - " + role.getRoleName());
            }
            // Mở khóa nút Cấp quyền
            if (btnAssignScopeToAccount != null) {
                btnAssignScopeToAccount.setEnabled(true);
            }
        } 
        // Nếu danh sách trống (tài khoản đã có đủ mọi quyền riêng)
        else {
            cbRoleTab4.addItem("Tài khoản này đã được cấp tất cả các quyền");
            // Khóa nút Cấp quyền
            if (btnAssignScopeToAccount != null) {
                btnAssignScopeToAccount.setEnabled(false);
            }
        }
    }

    // Đẩy dữ liệu quyền riêng lên Bảng của Tab 4
    public void loadAssignedRolesToTab4Table(String accountName, List<RoleModel> assignedScopes) {
        lblAssignedAccountScopesTitle.setText("Danh sách quyền riêng của tài khoản: " + accountName);
        assignedAccountScopesTableModel.setRowCount(0); // Xóa dữ liệu cũ
        
        if (assignedScopes != null) {
            int stt = 1;
            for (RoleModel role : assignedScopes) {
                assignedAccountScopesTableModel.addRow(new Object[]{
                    stt++, 
                    role.getRoleName(),
                    ""
                });
            }
        }
    }
    
    public void loadRoleGroupsToTab5Table(List<RoleGroupModel> groups) {
        roleGroupTableModel.setRowCount(0);
        if (groups != null) {
            int stt = 1;
            for (RoleGroupModel group : groups) {
                roleGroupTableModel.addRow(new Object[]{
                    stt++, 
                    group.getRoleGroupName(), 
                    "Sửa / Xóa"
                });
            }
        }
    }
    
    public void addCreateRoleListener(ActionListener listener) {
        btnAddScope.addActionListener(listener);
    }
    
    public void addRoleGroupAssignRoleListener(ActionListener listener) {
        btnAssignRole.addActionListener(listener);
    }
    
    public void addRoleGroupSelectionListener(java.awt.event.ItemListener listener) {
        // Xóa các listener cũ để tránh bị lặp sự kiện nếu gọi hàm này nhiều lần
        for (java.awt.event.ItemListener l : cbRoleGroup.getItemListeners()) {
            cbRoleGroup.removeItemListener(l);
        }
        cbRoleGroup.addItemListener(listener);
    }
    
    public void addAccountSelectionListener(java.awt.event.ItemListener listener) {
        for (java.awt.event.ItemListener l : cbAccount.getItemListeners()) {
            cbAccount.removeItemListener(l);
        }
        cbAccount.addItemListener(listener);
    }

    public void addAssignRoleGroupToAccountListener(ActionListener listener) {
        btnAssignRoleGroupToAccount.addActionListener(listener);
    }
    
    public void addAccountTab4SelectionListener(java.awt.event.ItemListener listener) {
        for (java.awt.event.ItemListener l : cbAccountTab4.getItemListeners()) {
            cbAccountTab4.removeItemListener(l);
        }
        cbAccountTab4.addItemListener(listener);
    }

    public void addAssignScopeToAccountListener(ActionListener listener) {
        btnAssignScopeToAccount.addActionListener(listener);
    }
    
    public void addCreateRoleGroupTab5Listener(ActionListener listener) {
        btnAddRoleGroupTab5.addActionListener(listener);
    }
    
    
    
    public JComboBox<String> getRoleGroupComboBox(){
        return cbRoleGroup;
    }
    
    public JComboBox<String> getRoleComboBox(){
        return cbRole;
    }
    
    public JComboBox<String> getAccountComboBox() {
        return cbAccount;
    }

    public JComboBox<String> getAccountRoleGroupComboBox() {
        return cbAccountRoleGroup;
    }
    
    public JComboBox<String> getAccountTab4ComboBox() {
        return cbAccountTab4;
    }

    public JComboBox<String> getRoleTab4ComboBox() {
        return cbRoleTab4;
    }
    
    // =========================================
    // GETTERS CHO CÁC NÚT BẤM (DÙNG ĐỂ CHECK QUYỀN)
    // =========================================
    public JButton getAddScopeButton() {
        return btnAddScope; // Nút thêm phạm vi quyền ở Tab 1
    }
    
    public JButton getAssignRoleButton() {
        return btnAssignRole; // Nút Gán quyền ở Tab 2
    }
    
    public JButton getAssignRoleGroupToAccountButton() {
        return btnAssignRoleGroupToAccount; // Nút Cập nhật vai trò ở Tab 3
    }
    
    public JButton getAssignScopeToAccountButton() {
        return btnAssignScopeToAccount; // Nút Cấp quyền riêng ở Tab 4
    }
    
    public JButton getAddRoleGroupTab5Button() {
        return btnAddRoleGroupTab5; // Nút thêm nhóm quyền ở Tab 5
    }
    
    public void setRoleTableListener(RoleActionListener listener) {
        this.roleTableListener = listener;
    }
    
    public void setTab2DeleteListener(DeleteActionListener listener) { this.tab2DeleteListener = listener; }
    public void setTab3DeleteListener(DeleteActionListener listener) { this.tab3DeleteListener = listener; }
    public void setTab4DeleteListener(DeleteActionListener listener) { this.tab4DeleteListener = listener; }
    public void setRoleGroupTableListener(RoleActionListener listener) {
        this.roleGroupTableListener = listener;
    }
    
    // ==========================================================
    // DIALOG: THÊM PHẠM VI QUYỀN MỚI
    // ==========================================================
    // Đổi kiểu trả về từ void thành RoleModel (hoặc tạo một class DTO riêng)
    public RoleModel showAddScopeDialog() {
        // 1. Tạo Panel chứa các thành phần của Form (giữ nguyên như cũ)
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 15));
        dialogPanel.setPreferredSize(new Dimension(450, 150));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JLabel lblName = new JLabel("Tên phạm vi quyền:");
        JTextField txtScopeName = new JTextField();

        JLabel lblFunction = new JLabel("Chọn chức năng:");

        inputPanel.add(lblName);
        inputPanel.add(txtScopeName);
        inputPanel.add(lblFunction);
        inputPanel.add(this.cbFunctions);

        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        JCheckBox chkView = new JCheckBox("Xem");
        JCheckBox chkAdd = new JCheckBox("Thêm");
        JCheckBox chkEdit = new JCheckBox("Sửa");
        JCheckBox chkDelete = new JCheckBox("Xóa");
        JCheckBox chkExport = new JCheckBox("Xuất file");

        checkboxPanel.add(chkView); checkboxPanel.add(chkAdd);
        checkboxPanel.add(chkEdit); checkboxPanel.add(chkDelete);
        checkboxPanel.add(chkExport);

        dialogPanel.add(inputPanel, BorderLayout.NORTH);
        dialogPanel.add(checkboxPanel, BorderLayout.CENTER);

        // 2. Hiển thị JOptionPane
        int result = JOptionPane.showConfirmDialog(
                this, 
                dialogPanel, 
                "Thêm Phạm Vi Quyền Mới", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
        );

        // 3. Xử lý khi bấm OK -> Gói dữ liệu vào RoleModel và trả về
        if (result == JOptionPane.OK_OPTION) {
            String scopeName = txtScopeName.getText().trim();
            if (scopeName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên phạm vi không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return null; // Trả về null nếu validate trượt
            }

            String funcId = cbFunctions.getSelectedItem().toString().split(" - ")[0];

            RoleModel newRole = new RoleModel();
            newRole.setRoleName(scopeName);
            newRole.setFunctionId(Integer.parseInt(funcId)); // Giả sử model nhận string, nếu int thì Integer.parseInt(funcId)
            
            // Ép kiểu boolean sang int (1 hoặc 0) tùy theo cách bạn định nghĩa trong DB
            newRole.setView(chkView.isSelected() ? 1 : 0);
            newRole.setAdd(chkAdd.isSelected() ? 1 : 0);
            newRole.setEdit(chkEdit.isSelected() ? 1 : 0);
            newRole.setDelete(chkDelete.isSelected() ? 1 : 0);
            newRole.setExportFile(chkExport.isSelected() ? 1 : 0);

            // KHÔNG xử lý bảng ở đây nữa. Chỉ return data.
            return newRole; 
        }

        // Bấm Cancel hoặc tắt hộp thoại
        return null;
    }
    
    public RoleGroupModel showAddRoleGroupDialog() {
        // 1. Tạo Panel chứa các thành phần của Form (giữ nguyên như cũ)
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 15));
        dialogPanel.setPreferredSize(new Dimension(350, 100));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JLabel lblName = new JLabel("Tên nhóm quyền:");
        JTextField txtRoleGroupName = new JTextField();

        inputPanel.add(lblName);
        inputPanel.add(txtRoleGroupName);

        dialogPanel.add(inputPanel, BorderLayout.CENTER);

        // 2. Hiển thị JOptionPane
        int result = JOptionPane.showConfirmDialog(
                this, 
                dialogPanel, 
                "Thêm Nhóm Quyền Mới", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
        );

        // 3. Xử lý khi bấm OK -> Gói dữ liệu vào RoleGroupModel và trả về
        if (result == JOptionPane.OK_OPTION) {
            String roleGroupName = txtRoleGroupName.getText().trim();
            if (roleGroupName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên nhóm quyền không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return null; // Trả về null nếu validate trượt
            }

            RoleGroupModel newRoleGroup = new RoleGroupModel();
            newRoleGroup.setRoleGroupName(roleGroupName);
            
            // KHÔNG xử lý bảng ở đây nữa. Chỉ return data.
            return newRoleGroup; 
        }

        // Bấm Cancel hoặc tắt hộp thoại
        return null;
    }
    
    // ==========================================================
    // DIALOG: CHỈNH SỬA PHẠM VI QUYỀN
    // ==========================================================
    public RoleModel showEditScopeDialog(RoleModel currentRole) {
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 15));
        dialogPanel.setPreferredSize(new Dimension(450, 150));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JLabel lblName = new JLabel("Tên phạm vi quyền:");
        // Đổ dữ liệu cũ vào TextField
        JTextField txtScopeName = new JTextField(currentRole.getRoleName()); 

        JLabel lblFunction = new JLabel("Chọn chức năng:");
        // (Combobox this.cbFunctions đã được Controller nạp data trước khi gọi hàm này)

        // Tìm và chọn đúng chức năng mà Role này đang giữ
        for (int i = 0; i < cbFunctions.getItemCount(); i++) {
            if (cbFunctions.getItemAt(i).startsWith(currentRole.getFunctionId() + " - ")) {
                cbFunctions.setSelectedIndex(i);
                break;
            }
        }

        inputPanel.add(lblName);
        inputPanel.add(txtScopeName);
        inputPanel.add(lblFunction);
        inputPanel.add(this.cbFunctions);

        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        
        // Đổ trạng thái quyền cũ vào Checkbox
        JCheckBox chkView = new JCheckBox("Xem", currentRole.getView() > 0);
        JCheckBox chkAdd = new JCheckBox("Thêm", currentRole.getAdd() > 0);
        JCheckBox chkEdit = new JCheckBox("Sửa", currentRole.getEdit() > 0);
        JCheckBox chkDelete = new JCheckBox("Xóa", currentRole.getDelete() > 0);
        JCheckBox chkExport = new JCheckBox("Xuất file", currentRole.getExportFile() > 0);

        checkboxPanel.add(chkView); checkboxPanel.add(chkAdd);
        checkboxPanel.add(chkEdit); checkboxPanel.add(chkDelete);
        checkboxPanel.add(chkExport);

        dialogPanel.add(inputPanel, BorderLayout.NORTH);
        dialogPanel.add(checkboxPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this, 
                dialogPanel, 
                "Chỉnh Sửa Phạm Vi Quyền", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String scopeName = txtScopeName.getText().trim();
            if (scopeName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên phạm vi không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            String funcIdStr = cbFunctions.getSelectedItem().toString().split(" - ")[0].trim();

            // Tạo Object mới chứa dữ liệu cập nhật
            RoleModel updatedRole = new RoleModel();
            
            // CỰC KỲ QUAN TRỌNG: Giữ lại ID cũ để DB biết đang sửa dòng nào
            updatedRole.setRoleId(currentRole.getRoleId()); 
            
            updatedRole.setRoleName(scopeName);
            updatedRole.setFunctionId(Integer.parseInt(funcIdStr)); 
            updatedRole.setView(chkView.isSelected() ? 1 : 0);
            updatedRole.setAdd(chkAdd.isSelected() ? 1 : 0);
            updatedRole.setEdit(chkEdit.isSelected() ? 1 : 0);
            updatedRole.setDelete(chkDelete.isSelected() ? 1 : 0);
            updatedRole.setExportFile(chkExport.isSelected() ? 1 : 0);

            return updatedRole; 
        }
        return null;
    }
    
    public RoleGroupModel showEditRoleGroupDialog(RoleGroupModel currentGroup) {
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 15));
        dialogPanel.setPreferredSize(new Dimension(350, 80));

        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JLabel lblName = new JLabel("Tên nhóm quyền:");
        JTextField txtRoleGroupName = new JTextField(currentGroup.getRoleGroupName());

        inputPanel.add(lblName);
        inputPanel.add(txtRoleGroupName);
        dialogPanel.add(inputPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this, dialogPanel, "Chỉnh Sửa Nhóm Quyền", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String newName = txtRoleGroupName.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên nhóm quyền không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            RoleGroupModel updatedGroup = new RoleGroupModel();
            updatedGroup.setRoleGroupId(currentGroup.getRoleGroupId());
            updatedGroup.setRoleGroupName(newName);
            return updatedGroup;
        }
        return null;
    }
    
    // ==========================================================
    // CLASS HỖ TRỢ TẠO NÚT HÀNH ĐỘNG CHO BẢNG PHÂN QUYỀN
    // ==========================================================
    public interface RoleActionListener {
        void onEdit(int row);
        void onDelete(int row);
    }

    class RoleActionPanel extends JPanel {
        protected JButton btnEdit = new JButton("Sửa");
        protected JButton btnDelete = new JButton("Xóa");

        public RoleActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 8));
            setOpaque(true);
            styleButton(btnEdit, new Color(0, 122, 255), 60, 30);
            styleButton(btnDelete, new Color(255, 59, 48), 60, 30);
            add(btnEdit);
            add(btnDelete);
        }

        protected void styleButton(JButton btn, Color color, int width, int height) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(color);
            btn.setBackground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(color, 1));
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(width, height));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    class RoleActionButtonRenderer implements TableCellRenderer {
        protected RoleActionPanel panel; // Đổi JPanel thành RoleActionPanel

        public RoleActionButtonRenderer(RoleActionPanel panel) {
            this.panel = panel;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            // CHECK QUYỀN VÀ ẨN HIỆN NÚT
            panel.btnEdit.setVisible(hasEditPermission);
            panel.btnDelete.setVisible(hasDeletePermission);
            return panel;
        }
    }

    class RoleActionButtonEditor extends DefaultCellEditor {
        protected RoleActionPanel panel;
        protected RoleActionListener listener;
        protected int currentRow;

        public RoleActionButtonEditor(RoleActionListener listener, RoleActionPanel panel) {
            super(new JCheckBox());
            this.listener = listener;
            this.panel = panel;

            this.panel.btnEdit.addActionListener(e -> { stopCellEditing(); listener.onEdit(currentRow); });
            this.panel.btnDelete.addActionListener(e -> { stopCellEditing(); listener.onDelete(currentRow); });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            panel.setBackground(table.getSelectionBackground());
            // CHECK QUYỀN VÀ ẨN HIỆN NÚT
            panel.btnEdit.setVisible(hasEditPermission);
            panel.btnDelete.setVisible(hasDeletePermission);
            return panel;
        }

        @Override public Object getCellEditorValue() { return ""; }
    }

    // ==========================================================
    // CLASS HỖ TRỢ TẠO NÚT "XÓA / THU HỒI" CHO TAB 2, 3, 4
    // ==========================================================
    public interface DeleteActionListener {
        void onDelete(int row);
    }

    class DeleteActionPanel extends JPanel {
        protected JButton btnDelete = new JButton("Thu hồi");

        public DeleteActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 4));
            setOpaque(true);
            
            btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnDelete.setForeground(new Color(255, 59, 48));
            btnDelete.setBackground(Color.WHITE);
            btnDelete.setBorder(BorderFactory.createLineBorder(new Color(255, 59, 48), 1));
            btnDelete.setFocusPainted(false);
            btnDelete.setPreferredSize(new Dimension(80, 26));
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            add(btnDelete);
        }
    }

    class DeleteActionButtonRenderer implements TableCellRenderer {
        protected DeleteActionPanel panel; // Đổi JPanel thành DeleteActionPanel
        public DeleteActionButtonRenderer(DeleteActionPanel panel) { this.panel = panel; }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            // CHECK QUYỀN XÓA (Thu hồi)
            panel.btnDelete.setVisible(hasDeletePermission);
            return panel;
        }
    }

    class DeleteActionButtonEditor extends DefaultCellEditor {
        protected DeleteActionPanel panel;
        protected DeleteActionListener listener;
        protected int currentRow;

        public DeleteActionButtonEditor(DeleteActionListener listener, DeleteActionPanel panel) {
            super(new JCheckBox());
            this.listener = listener;
            this.panel = panel;
            this.panel.btnDelete.addActionListener(e -> { stopCellEditing(); listener.onDelete(currentRow); });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            panel.setBackground(table.getSelectionBackground());
            // CHECK QUYỀN XÓA (Thu hồi)
            panel.btnDelete.setVisible(hasDeletePermission);
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
}


