/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import Model.AccountModel;
import java.util.List;
import Model.FunctionModel;
import Model.RoleGroupModel;
import Model.RoleModel;
import Model.SessionManager;
import Service.AccountService;
import Service.RoleService;
import View.MainFrame;
import View.RolePanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/**
 *
 * @author SONY
 */
public class RoleController {
    private MainFrame mainFrame;
    private RolePanel rolePanelView;
    private RoleService roleService;
    private AccountService accountService;
    private List<AccountModel> currentAccountList;
    private List<RoleModel> currentRolesList;
    private List<RoleGroupModel> currentRoleGroupsList;
    // Danh sách tài khoản cho Tab 6 Quản lý tài khoản
    private List<AccountModel> currentAccountManagementList;
    
    public RoleController(MainFrame sharedMainFrame) throws SQLException {
        this.mainFrame = sharedMainFrame;
        this.roleService = new RoleService();
        this.rolePanelView = mainFrame.getRolePanel();
        this.accountService = new AccountService();

        initRoleListeners();
        if (mainFrame != null) {
            mainFrame.registerPermissionReloader(() -> {
                try { hiddenButton(); } catch (Exception ex) { ex.printStackTrace(); }
            });
        }
    }
    
    private void initRoleListeners() throws SQLException {
        if (rolePanelView != null) {
            // -- Tab 1 & Tab 5 --
            reloadRolesTableTab1();
            reloadRoleGroupsTableTab5();
            
            // -- Tab 2 --
            reloadRoleGroupsComboBoxTab2(); 
            reloadRolesComboBoxTab2();
            reloadRolesTableTab2();
            
            // -- Tab 3 --
            reloadAccountComboBoxTab3();
            reloadRoleGroupsComboBoxTab3();
            reloadRoleGroupsTableTab3();
            
            // -- Tab 4 --
            reloadAccountComboBoxTab4();
            reloadRolesComboBoxTab4();
            reloadRolesTableTab4();
            
            // -- Tab 6 Quản lý tài khoản --
            reloadAccountManagementTable();
            
            initEvents();
        }
    }
    
    private void initEvents() {
        this.rolePanelView.addCreateRoleListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                try {
                    // Kêu View hiển thị dialog và hứng cục Data trả về
                    rolePanelView.loadFunctionsToComboBox(roleService.getFunctionList());
                } catch (SQLException ex) {
                    System.getLogger(RoleController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                RoleModel inputData = rolePanelView.showAddScopeDialog();
                
                if (inputData != null) {
                    try {
                        boolean isSuccess = roleService.createRole(inputData);
                        if (isSuccess){
                            reloadRolesTableTab1();
                            reloadRolesComboBoxTab2();
                            reloadRolesComboBoxTab4();
                            
                            mainFrame.reloadPermissions();
                            JOptionPane.showMessageDialog(rolePanelView, "Đã thêm phạm vi quyền thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else {
                            JOptionPane.showMessageDialog(
                                rolePanelView, 
                                "Đã xảy ra lỗi khi lưu phạm vi quyền",
                                "Lỗi hệ thống",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }    
                    } catch (SQLException ex) {
                        System.getLogger(RoleController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
            }
        });   
        
        this.rolePanelView.addRoleGroupAssignRoleListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                JComboBox<String> cbRole = rolePanelView.getRoleComboBox();
                JComboBox<String> cbRoleGroup = rolePanelView.getRoleGroupComboBox();
                
                if (cbRole.getSelectedItem() == null || cbRoleGroup.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(null, "Vui lòng chọn đầy đủ Nhóm Quyền và Phạm Vi Quyền!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int roleId = Integer.parseInt(cbRole.getSelectedItem().toString().split(" - ")[0]); 
                int roleGroupId = Integer.parseInt(cbRoleGroup.getSelectedItem().toString().split(" - ")[0]);
                
                boolean isSuccess = false;
                
                try {
                    isSuccess = roleService.assignRoleToRoleGroup(roleGroupId, roleId);
                } catch (SQLException ex) {
                    System.getLogger(RoleController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                
                if (isSuccess) {
                    try {
                        reloadRolesTableTab2();
                        reloadRolesComboBoxTab2();
                        reloadRolesTableTab4();
                        reloadRolesComboBoxTab4();
                        
                        mainFrame.reloadPermissions();
                    } catch (SQLException ex) {
                        System.getLogger(RoleController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
                
            }
        });
        
        this.rolePanelView.addRoleGroupSelectionListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent e) {
                if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {

                    // Lấy item vừa được chọn từ biến event (Rất an toàn)
                    Object selectedItem = e.getItem();

                    // Chốt chặn cuối cùng
                    if (selectedItem == null || !selectedItem.toString().contains(" - ")) {
                        return;
                    }

                    try {
                        reloadRolesTableTab2();
                        reloadRolesComboBoxTab2();
                        
                        mainFrame.reloadPermissions();
                    } catch (SQLException ex) {
                        System.getLogger(RoleController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
            }
        });
        
        // ==========================================================
        // CÁC SỰ KIỆN DÀNH CHO TAB 3 (TÀI KHOẢN - NHÓM QUYỀN)
        // ==========================================================
        
        // 1. Sự kiện khi chọn một Tài Khoản khác trong ComboBox
        this.rolePanelView.addAccountSelectionListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent e) {
                // CHỈ chạy khi có một item MỚI được chọn (Bỏ qua sự kiện Deselected)
                if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                    
                    Object selectedItem = e.getItem();
                    
                    // Chốt chặn an toàn khi removeAllItems chạy
                    if (selectedItem == null || !selectedItem.toString().contains(" - ")) {
                        return; 
                    }
                    
                    try {
                        // Lấy danh sách nhóm quyền của tài khoản này từ DB
                        reloadRoleGroupsTableTab3();
                        reloadRoleGroupsComboBoxTab3();
                        
                        mainFrame.reloadPermissions();
                        
                    } catch (SQLException ex) {
                        System.getLogger(RoleController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
            }
        });
        

        // 2. Sự kiện khi bấm nút "Cập Nhật Vai Trò" (Gán Nhóm Quyền cho Tài Khoản)
        this.rolePanelView.addAssignRoleGroupToAccountListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> cbAccount = rolePanelView.getAccountComboBox();
                JComboBox<String> cbAccountRoleGroup = rolePanelView.getAccountRoleGroupComboBox();
                
                Object selectedAcc = cbAccount.getSelectedItem();
                Object selectedGroup = cbAccountRoleGroup.getSelectedItem();

                // Chốt chặn an toàn
                if (selectedAcc == null || selectedGroup == null || 
                    !selectedAcc.toString().contains(" - ") || !selectedGroup.toString().contains(" - ")) {
                    JOptionPane.showMessageDialog(null, "Vui lòng chọn đầy đủ Tài khoản và Nhóm quyền!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int accountId = Integer.parseInt(selectedAcc.toString().split(" - ")[0].trim());
                int roleGroupId = Integer.parseInt(selectedGroup.toString().split(" - ")[0].trim());
                
                try {
                    // Gọi Service thực hiện insert xuống DB
                    boolean isSuccess = accountService.assignRoleGroupToAccount(accountId, roleGroupId);
                    
                    if (isSuccess) {
                        // Load lại bảng danh sách nhóm quyền của tài khoản đó
                        reloadRoleGroupsTableTab3();
                        reloadRoleGroupsComboBoxTab3();
                        reloadRolesTableTab4();
                        reloadRolesComboBoxTab4();
                        
                        mainFrame.reloadPermissions();
                        JOptionPane.showMessageDialog(null, "Cấp vai trò cho tài khoản thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Cấp vai trò thất bại! Có thể tài khoản này đã giữ vai trò này rồi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    System.getLogger(RoleController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        });
        
        // ==========================================================
        // CÁC SỰ KIỆN DÀNH CHO TAB 4 (CẤP QUYỀN RIÊNG TÀI KHOẢN)
        // ==========================================================
        
        // 1. Sự kiện khi chọn một Tài Khoản khác trong ComboBox ở Tab 4
        this.rolePanelView.addAccountTab4SelectionListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent e) {
                // CHỈ chạy khi có một item MỚI được chọn
                if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                    
                    Object selectedItem = e.getItem();
                    
                    // Chốt chặn an toàn
                    if (selectedItem == null || !selectedItem.toString().contains(" - ")) {
                        return; 
                    }
                    
                    int accountId = Integer.parseInt(selectedItem.toString().split(" - ")[0].trim());
                    String accountName = selectedItem.toString().split(" - ")[1].trim();
                    
                    try {
                        // Lấy danh sách quyền riêng của tài khoản này từ DB
                        List<RoleModel> assignedRoles = accountService.getAssignedRolesByAccountId(accountId);
                        rolePanelView.loadAssignedRolesToTab4Table(accountName, assignedRoles);
                        
                        reloadRolesComboBoxTab4();
                        
                        mainFrame.reloadPermissions();
                        
                    } catch (SQLException ex) {
                        System.getLogger(RoleController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
            }
        });
        
        // 2. Sự kiện khi bấm nút "Cấp Quyền" (Gán Phạm vi quyền cho Tài Khoản)
        this.rolePanelView.addAssignScopeToAccountListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                JComboBox<String> cbAccountTab4 = rolePanelView.getAccountTab4ComboBox();
                JComboBox<String> cbRoleTab4 = rolePanelView.getRoleTab4ComboBox();
                
                Object selectedAcc = cbAccountTab4.getSelectedItem();
                Object selectedScope = cbRoleTab4.getSelectedItem();

                // Chốt chặn an toàn
                if (selectedAcc == null || selectedScope == null || 
                    !selectedAcc.toString().contains(" - ") || !selectedScope.toString().contains(" - ")) {
                    JOptionPane.showMessageDialog(null, "Vui lòng chọn đầy đủ Tài khoản và Phạm vi quyền!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                try {
                    int accountId = Integer.parseInt(selectedAcc.toString().split(" - ")[0].trim());
                    String accountName = selectedAcc.toString().split(" - ")[1].trim();
                    int roleId = Integer.parseInt(selectedScope.toString().split(" - ")[0].trim());
                    
                    // Gọi Service thực hiện insert xuống DB bảng Account_Role (quyền riêng)
                    boolean isSuccess = accountService.assignRoleToAccount(accountId, roleId);
                    
                    if (isSuccess) {
                        // Load lại bảng danh sách quyền riêng của tài khoản đó
                        List<RoleModel> assignedRoles = accountService.getAssignedRolesByAccountId(accountId);
                        rolePanelView.loadAssignedRolesToTab4Table(accountName, assignedRoles);
                        
                        reloadRolesComboBoxTab4();
                        mainFrame.reloadPermissions();
                        
                        JOptionPane.showMessageDialog(null, "Cấp quyền riêng cho tài khoản thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Cấp quyền thất bại! Có thể tài khoản này đã được cấp quyền này từ trước.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Lỗi Database: \n" + ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Lỗi Hệ Thống: \n" + ex.getMessage(), "Lỗi Không Xác Định", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ==========================================================
        // SỰ KIỆN TRÊN BẢNG (SỬA / XÓA DÒNG TAB 1)
        // ==========================================================
        this.rolePanelView.setRoleTableListener(new RolePanel.RoleActionListener() {
            @Override
            public void onEdit(int row) {
                // Lấy RoleModel ở dòng hiện tại dựa vào list lưu ở Controller
                RoleModel targetRole = currentRolesList.get(row);
                
                try {
                    // Nạp danh sách chức năng vào Combobox trước khi mở Dialog
                    rolePanelView.loadFunctionsToComboBox(roleService.getFunctionList());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                // Gọi Dialog sửa và hứng dữ liệu trả về
                RoleModel updatedData = rolePanelView.showEditScopeDialog(targetRole);

                if (updatedData != null) {
                    try {
                        boolean isSuccess = roleService.updateRole(updatedData); 
                        
                        if (isSuccess) {
                            reloadRolesTableTab1();
                            reloadRolesComboBoxTab2();
                            reloadRolesComboBoxTab4();
                            mainFrame.reloadPermissions();

                            JOptionPane.showMessageDialog(null, "Cập nhật phạm vi quyền thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void onDelete(int row) {
                RoleModel targetRole = currentRolesList.get(row);
                int confirm = JOptionPane.showConfirmDialog(null, 
                        "Bạn có chắc muốn xóa phạm vi quyền: " + targetRole.getRoleName() + "?", 
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // Gọi Service delete
                        boolean isSuccess = roleService.deleteRole(targetRole.getRoleId());
                        
                        if (isSuccess) {
                            // 1. Load lại list cho bảng Tab 1
                            reloadRolesTableTab1();
                            reloadRolesComboBoxTab2();
                            reloadRolesComboBoxTab4();
                            
                            mainFrame.reloadPermissions();
                            JOptionPane.showMessageDialog(null, "Đã xóa thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, 
                                "Không thể xóa!\nPhạm vi quyền này đang được gán cho một Nhóm quyền hoặc Tài khoản nào đó.", 
                                "Lỗi ràng buộc dữ liệu", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        System.getLogger(RoleController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        JOptionPane.showMessageDialog(null, 
                                "Không thể xóa!\nPhạm vi quyền [" + targetRole.getRoleName() + "] đang được sử dụng bởi một Nhóm quyền hoặc Tài khoản.\n\nVui lòng thu hồi quyền này trước khi xóa.", 
                                "Từ chối xóa", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Lỗi không xác định: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        // ==========================================================
        // SỰ KIỆN NÚT "THU HỒI" TRÊN BẢNG TAB 2, 3, 4
        // ==========================================================
        
        // ----------------------------------------------------------
        // TAB 2: THU HỒI PHẠM VI QUYỀN KHỎI NHÓM QUYỀN
        // ----------------------------------------------------------
        this.rolePanelView.setTab2DeleteListener(row -> {
            JComboBox<String> cbRoleGroup = rolePanelView.getRoleGroupComboBox();
            if (cbRoleGroup.getSelectedItem() == null) return;
            
            int roleGroupId = Integer.parseInt(cbRoleGroup.getSelectedItem().toString().split(" - ")[0].trim());
            String roleGroupName = cbRoleGroup.getSelectedItem().toString().split(" - ")[1].trim();
            
            try {
                // Lấy lại list hiện tại từ DB để biết dòng user đang bấm là Role nào
                List<RoleModel> currentAssignedRoles = roleService.getConfiguredRolesByGroupId(roleGroupId);
                RoleModel targetRole = currentAssignedRoles.get(row);
                
                int confirm = JOptionPane.showConfirmDialog(null, 
                        "Bạn có chắc muốn thu hồi quyền [" + targetRole.getRoleName() + "] khỏi nhóm [" + roleGroupName + "]?", 
                        "Xác nhận thu hồi", JOptionPane.YES_NO_OPTION);
                        
                if (confirm == JOptionPane.YES_OPTION) {
                    // TODO: Bạn cần viết hàm removeRoleFromRoleGroup trong RoleService
                    boolean isSuccess = roleService.deleteRoleFromRoleGroup(roleGroupId, targetRole.getRoleId());
                    
                    if (isSuccess) {
                        // Reload lại dữ liệu Tab 2
                        reloadRolesComboBoxTab2();
                        reloadRolesTableTab2();
                        
                        mainFrame.reloadPermissions();
                        JOptionPane.showMessageDialog(null, "Thu hồi quyền thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Thu hồi thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ----------------------------------------------------------
        // TAB 3: THU HỒI NHÓM QUYỀN KHỎI TÀI KHOẢN
        // ----------------------------------------------------------
        this.rolePanelView.setTab3DeleteListener(row -> {
            JComboBox<String> cbAccount = rolePanelView.getAccountComboBox();
            if (cbAccount.getSelectedItem() == null) return;
            
            int accountId = Integer.parseInt(cbAccount.getSelectedItem().toString().split(" - ")[0].trim());
            String accountName = cbAccount.getSelectedItem().toString().split(" - ")[1].trim();
            
            try {
                List<RoleGroupModel> currentAssignedGroups = accountService.getAssignedRoleGroupsByAccountId(accountId);
                RoleGroupModel targetGroup = currentAssignedGroups.get(row);
                
                int confirm = JOptionPane.showConfirmDialog(null, 
                        "Bạn có chắc muốn thu hồi nhóm quyền [" + targetGroup.getRoleGroupName() + "] khỏi tài khoản [" + accountName + "]?", 
                        "Xác nhận thu hồi", JOptionPane.YES_NO_OPTION);
                        
                if (confirm == JOptionPane.YES_OPTION) {
                    // TODO: Bạn cần viết hàm removeRoleGroupFromAccount trong AccountService
                    boolean success = accountService.deleteRoleGroupFromAccount(accountId, targetGroup.getRoleGroupId());
                    
                    if (success) {
                        List<RoleGroupModel> newAssigned = accountService.getAssignedRoleGroupsByAccountId(accountId);
                        rolePanelView.loadAssignedGroupsToTab3Table(accountName, newAssigned);
                        
                        reloadRoleGroupsComboBoxTab3();
                        reloadRolesComboBoxTab4();
                        reloadRolesTableTab4();
                        
                        mainFrame.reloadPermissions();
                        JOptionPane.showMessageDialog(null, "Thu hồi phạm vi quyền thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Thu hồi phạm vi quyền thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ----------------------------------------------------------
        // TAB 4: THU HỒI QUYỀN RIÊNG KHỎI TÀI KHOẢN
        // ----------------------------------------------------------
        this.rolePanelView.setTab4DeleteListener(row -> {
            JComboBox<String> cbAccountTab4 = rolePanelView.getAccountTab4ComboBox();
            if (cbAccountTab4.getSelectedItem() == null) return;
            
            int accountId = Integer.parseInt(cbAccountTab4.getSelectedItem().toString().split(" - ")[0].trim());
            String accountName = cbAccountTab4.getSelectedItem().toString().split(" - ")[1].trim();
            
            try {
                List<RoleModel> currentAssignedScopes = accountService.getAssignedRolesByAccountId(accountId);
                RoleModel targetScope = currentAssignedScopes.get(row);
                
                int confirm = JOptionPane.showConfirmDialog(null, 
                        "Bạn có chắc muốn thu hồi quyền riêng [" + targetScope.getRoleName() + "] khỏi tài khoản [" + accountName + "]?", 
                        "Xác nhận thu hồi", JOptionPane.YES_NO_OPTION);
                        
                if (confirm == JOptionPane.YES_OPTION) {
                    // TODO: Bạn cần viết hàm removeRoleFromAccount trong AccountService
                    boolean isSuccess = accountService.deleteRoleFromAccount(accountId, targetScope.getRoleId());
                    
                    if (isSuccess) {
                        List<RoleModel> newAssigned = accountService.getAssignedRolesByAccountId(accountId);
                        rolePanelView.loadAssignedRolesToTab4Table(accountName, newAssigned);
                        
                        reloadRolesComboBoxTab4();
                        
                        mainFrame.reloadPermissions();
                        JOptionPane.showMessageDialog(null, "Thu hồi quyền riêng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Vai trò này có được do phân quyền nhóm, không thể xoá riêng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // ==========================================================
        // SỰ KIỆN DÀNH CHO TAB 5 (QUẢN LÝ NHÓM QUYỀN)
        // ==========================================================
        
        // 1. Sự kiện nút "Thêm nhóm quyền" ở Tab 5 (Tái sử dụng logic thêm nhóm)
        this.rolePanelView.addCreateRoleGroupTab5Listener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RoleGroupModel inputData = rolePanelView.showAddRoleGroupDialog();
                if (inputData != null) {
                    try {
                        boolean isSuccess = roleService.createRoleGroup(inputData);
                        if (isSuccess){
                            // Cập nhật lại toàn bộ Bảng và ComboBox liên quan
                            reloadRoleGroupsTableTab5();
                            reloadRoleGroupsComboBoxTab2();
                            reloadRoleGroupsComboBoxTab3();
                            
                            JOptionPane.showMessageDialog(rolePanelView, "Đã thêm nhóm quyền thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(rolePanelView, "Đã xảy ra lỗi khi lưu nhóm quyền", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }    
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // 2. Sự kiện trên Bảng Tab 5 (Sửa / Xóa dòng)
        this.rolePanelView.setRoleGroupTableListener(new RolePanel.RoleActionListener() {
            @Override
            public void onEdit(int row) {
                RoleGroupModel targetGroup = currentRoleGroupsList.get(row);
                RoleGroupModel updatedData = rolePanelView.showEditRoleGroupDialog(targetGroup);

                if (updatedData != null) {
                    try {
                        // TODO: Bạn cần viết hàm updateRoleGroup trong RoleService
                        boolean isSuccess = roleService.updateRoleGroup(updatedData); 
                        
                        if (isSuccess) {
                            reloadRoleGroupsTableTab5();
                            reloadRoleGroupsComboBoxTab2();
                            reloadRoleGroupsComboBoxTab3();
                            JOptionPane.showMessageDialog(null, "Cập nhật tên nhóm quyền thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void onDelete(int row) {
                RoleGroupModel targetGroup = currentRoleGroupsList.get(row);
                int confirm = JOptionPane.showConfirmDialog(null, 
                        "Bạn có chắc muốn xóa Nhóm quyền: [" + targetGroup.getRoleGroupName() + "]?\nTất cả tài khoản thuộc nhóm này sẽ bị ảnh hưởng.", 
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // TODO: Bạn cần viết hàm deleteRoleGroup trong RoleService
                        boolean isSuccess = roleService.deleteRoleGroup(targetGroup.getRoleGroupId());
                        
                        if (isSuccess) {
                            reloadRoleGroupsTableTab5();
                            reloadRoleGroupsComboBoxTab2();
                            reloadRoleGroupsComboBoxTab3();
                            JOptionPane.showMessageDialog(null, "Đã xóa nhóm quyền thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, 
                                "Không thể xóa!\nNhóm quyền này đang chứa tài khoản hoặc đã được gán các phạm vi quyền.", 
                                "Lỗi ràng buộc dữ liệu", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, 
                                "Không thể xóa Nhóm quyền [" + targetGroup.getRoleGroupName() + "].\nVui lòng gỡ tất cả tài khoản và phạm vi quyền khỏi nhóm này trước.", 
                                "Từ chối xóa", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        
        // ==========================================================
        // SỰ KIỆN DÀNH CHO TAB 6 (QUẢN LÝ TÀI KHOẢN)
        // ==========================================================
        
        // 1. Nút "Thêm tài khoản nhân viên"
        this.rolePanelView.addCreateEmployeeAccountListener(e -> {
            String[] inputData = rolePanelView.showAddEmployeeAccountDialog();
            if (inputData != null) {
                String fullName = inputData[0];
                String email    = inputData[1];
                String phone    = inputData[2];
                String username = inputData[3];
                
                String result = accountService.createAccountByManager(fullName, email, phone, username);
                
                if ("Thành công".equals(result)) {
                    reloadAccountManagementTable();
                    // Cập nhật các ComboBox tài khoản ở Tab 3 và Tab 4
                    refreshAccountList();
                    JOptionPane.showMessageDialog(rolePanelView,
                        "Tạo tài khoản nhân viên thành công!\n" +
                        "Tên đăng nhập: " + username + "\n" +
                        "Mật khẩu mặc định: 123456",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(rolePanelView,
                        "Tạo tài khoản thất bại!\n" + result,
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // 2. Nút "Vô hiệu hoá / Kích hoạt lại" trong bảng Tab 6
        this.rolePanelView.setAccountManagementActionListener(new RolePanel.AccountManagementActionListener() {
            @Override
            public void onToggleStatus(int row) {
                if (currentAccountManagementList == null || row >= currentAccountManagementList.size()) return;
                
                AccountModel target = currentAccountManagementList.get(row);
                String currentStatus = target.getStatus();
                String newStatus = "Đang hoạt động".equals(currentStatus) ? "Bị khóa" : "Đang hoạt động";
                String action = "Đang hoạt động".equals(currentStatus) ? "vô hiệu hoá" : "kích hoạt lại";
                
                int confirm = JOptionPane.showConfirmDialog(null,
                    "Bạn có chắc muốn " + action + " tài khoản [" + target.getUsername() + "]?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = accountService.updateAccountStatus(target.getAccountID(), newStatus);
                    if (success) {
                        if ("Bị khóa".equals(newStatus)) {
                            accountService.revokeAllTokens(target.getEmail());
                        }
                        reloadAccountManagementTable();
                        JOptionPane.showMessageDialog(null, 
                            "" + ("vô hiệu hoá".equals(action) ? "Vô hiệu hoá" : "Kích hoạt lại") + " tài khoản thành công!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Thao tác thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            @Override
            public void onEdit(int row) {
                if (currentAccountManagementList == null || row >= currentAccountManagementList.size()) return;
                AccountModel target = currentAccountManagementList.get(row);
                
                String[] inputData = rolePanelView.showEditEmployeeAccountDialog(target);
                if (inputData != null) {
                    String hoTen = inputData[0];
                    String email = inputData[1];
                    String phone = inputData[2];
                    
                    String result = accountService.updateEmployeeAccount(target.getAccountID(), hoTen, email, phone);
                    if ("Thành công".equals(result)) {
                        reloadAccountManagementTable();
                        refreshAccountList();
                        JOptionPane.showMessageDialog(rolePanelView, "Cập nhật thông tin thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(rolePanelView, "Cập nhật thất bại!\n" + result, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }
    
    // =========================================================
    // CÁC HÀM RELOAD DỮ LIỆU (ĐÃ UPDATE TÊN HÀM)
    // =========================================================
    private void reloadRolesTableTab1() throws SQLException{
        this.currentRolesList = roleService.getRoleList(); 
        rolePanelView.loadRolesToTab1Table(currentRolesList);
    }
    
    private void reloadRoleGroupsTableTab5() {
        this.currentRoleGroupsList = roleService.getRoleGroupList();
        rolePanelView.loadRoleGroupsToTab5Table(currentRoleGroupsList);
    }
    
    // --- TAB 2 ---
    private void reloadRoleGroupsComboBoxTab2() {
        this.currentRoleGroupsList = roleService.getRoleGroupList();
        rolePanelView.loadRoleGroupsToTab2ComboBox(currentRoleGroupsList);
    }
    
    private void reloadRolesComboBoxTab2() throws SQLException{
        JComboBox<String> cbRoleGroup = rolePanelView.getRoleGroupComboBox();
        Object selected = cbRoleGroup.getSelectedItem();
        if (selected == null || !selected.toString().contains(" - ")) return; 

        int roleGroupId = Integer.parseInt(selected.toString().split(" - ")[0].trim());
        List<RoleModel> unconfiguredRoles = roleService.getUnconfiguredRolesByGroupId(roleGroupId);
        rolePanelView.loadRolesToTab2ComboBox(unconfiguredRoles);
    }
    
    private void reloadRolesTableTab2() throws SQLException{
        JComboBox<String> cbRoleGroup = rolePanelView.getRoleGroupComboBox();
        Object selected = cbRoleGroup.getSelectedItem();
        if (selected == null || !selected.toString().contains(" - ")) return; 

        int roleGroupId = Integer.parseInt(selected.toString().split(" - ")[0].trim());
        String roleGroupName = selected.toString().split(" - ")[1].trim();
        List<RoleModel> configuredRoles = roleService.getConfiguredRolesByGroupId(roleGroupId);
        
        rolePanelView.loadConfiguredRolesToTab2Table(roleGroupName, configuredRoles);
    }

    // --- TAB 3 ---
    private void reloadAccountComboBoxTab3(){
        this.currentAccountList = accountService.getAccountList();
        rolePanelView.loadAccountsToTab3ComboBox(currentAccountList);
    }
    
    private void reloadRoleGroupsComboBoxTab3() throws SQLException{
        JComboBox<String> cbAccount = rolePanelView.getAccountComboBox();
        Object selected = cbAccount.getSelectedItem();
        if (selected == null || !selected.toString().contains(" - ")) return; 

        int accountId = Integer.parseInt(selected.toString().split(" - ")[0].trim());
        List<RoleGroupModel> unassignedRoleGroups = accountService.getUnassignedRoleGroupsByAccountId(accountId);
        rolePanelView.loadRoleGroupsToTab3ComboBox(unassignedRoleGroups);
    }
    
    private void reloadRoleGroupsTableTab3() throws SQLException{
        JComboBox<String> cbAccount = rolePanelView.getAccountComboBox();
        Object selected = cbAccount.getSelectedItem();
        if (selected == null || !selected.toString().contains(" - ")) return; 

        int accountId = Integer.parseInt(selected.toString().split(" - ")[0].trim());
        String accountName = selected.toString().split(" - ")[1].trim();   
        
        List<RoleGroupModel> assignedRoleGroups = accountService.getAssignedRoleGroupsByAccountId(accountId);
        rolePanelView.loadAssignedGroupsToTab3Table(accountName, assignedRoleGroups);
    }
    
    // --- TAB 4 ---
    public void reloadAccountComboBoxTab4(){
        this.currentAccountList = accountService.getAccountList();
        rolePanelView.loadAccountsToTab4ComboBox(currentAccountList);
    }
    
    private void reloadRolesComboBoxTab4() throws SQLException{
        JComboBox<String> cbAccountTab4 = rolePanelView.getAccountTab4ComboBox();
        Object selected = cbAccountTab4.getSelectedItem();
        if (selected == null || !selected.toString().contains(" - ")) return; 

        int accountId = Integer.parseInt(selected.toString().split(" - ")[0].trim());
        List<RoleModel> unassignedRoles = accountService.getUnassignedRolesByAccountId(accountId);
        rolePanelView.loadRolesToTab4ComboBox(unassignedRoles);
    }
    
    private void reloadRolesTableTab4() throws SQLException{
        JComboBox<String> cbAccountTab4 = rolePanelView.getAccountTab4ComboBox();
        Object selected = cbAccountTab4.getSelectedItem();
        if (selected == null || !selected.toString().contains(" - ")) return; 
        
        int accountId = Integer.parseInt(selected.toString().split(" - ")[0].trim());
        String accountName = selected.toString().split(" - ")[1].trim();
        
        List<RoleModel> newAssigned = accountService.getAssignedRolesByAccountId(accountId);
        rolePanelView.loadAssignedRolesToTab4Table(accountName, newAssigned);
    }
    
    // =========================================================
    // HÀM PUBLIC ĐỂ CÁC CONTROLLER KHÁC GỌI KHI CÓ SỰ THAY ĐỔI DỮ LIỆU
    // =========================================================
    public void refreshAccountList() {
        reloadAccountComboBoxTab3();
        reloadAccountComboBoxTab4();
        reloadAccountManagementTable();
    }
    
    /** Reload bảng quản lý tài khoản ở Tab 6 */
    private void reloadAccountManagementTable() {
        this.currentAccountManagementList = accountService.getAllAccountsForManagement();
        rolePanelView.loadAccountsToManagementTable(currentAccountManagementList);
    }
    
    public void hiddenButton() throws SQLException {
        int currentAccountId = SessionManager.getAccountId();
        int currentFunctionId = 5; // ID của chức năng Phân quyền
        
        // 1. Kiểm tra CẢ 4 QUYỀN (Thêm, Sửa, Xóa, XEM)
        boolean hasViewPermission = roleService.isPermissed("Xem", currentAccountId, currentFunctionId);
        boolean hasAddPermission = roleService.isPermissed("Them", currentAccountId, currentFunctionId);
        boolean hasEditPermission = roleService.isPermissed("Sua", currentAccountId, currentFunctionId);
        boolean hasDeletePermission = roleService.isPermissed("Xoa", currentAccountId, currentFunctionId);
        
        // --- XỬ LÝ QUYỀN XEM (ẨN/HIỆN ENTIRE MENU) ---
        // Gọi hàm bên MainFrame để ẩn/hiện cục menu Phân Quyền trên Sidebar
        if (mainFrame != null) {
            mainFrame.setMenuVisible("Role", hasViewPermission);
        }
        
        // Nếu không có quyền xem thì không cần tốn thời gian chạy mấy dòng dưới nữa, return luôn!
        if (!hasViewPermission) {
            return;
        }
        // ----------------------------------------------
        
        // 2. Lấy tất cả các nút Thêm/Gán độc lập từ View
        JButton btnAddScope = rolePanelView.getAddScopeButton();
        JButton btnAssignRole = rolePanelView.getAssignRoleButton();
        JButton btnAssignRoleGroupToAccount = rolePanelView.getAssignRoleGroupToAccountButton();
        JButton btnAssignScopeToAccount = rolePanelView.getAssignScopeToAccountButton();
        JButton btnAddRoleGroupTab5 = rolePanelView.getAddRoleGroupTab5Button();
        
        // 3. Bật/Tắt các nút Thêm độc lập
        if (btnAddScope != null) btnAddScope.setVisible(hasAddPermission);
        if (btnAssignRole != null) btnAssignRole.setVisible(hasAddPermission);
        if (btnAssignRoleGroupToAccount != null) btnAssignRoleGroupToAccount.setVisible(hasAddPermission);
        if (btnAssignScopeToAccount != null) btnAssignScopeToAccount.setVisible(hasAddPermission);
        if (btnAddRoleGroupTab5 != null) btnAddRoleGroupTab5.setVisible(hasAddPermission);
        
        // 4. Truyền lệnh Bật/Tắt các nút Sửa, Xóa bên trong các Bảng
        rolePanelView.setActionPermissions(hasEditPermission, hasDeletePermission);
    }
    
    
}
