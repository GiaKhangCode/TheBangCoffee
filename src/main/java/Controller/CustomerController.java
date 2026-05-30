package Controller;

import Service.CustomerService;
import Service.RoleService;
import Model.SessionManager;
import View.CustomerManagementPanel;
import View.LoyaltyManagementPanel;
import View.MainFrame;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CustomerController {
    
    private CustomerManagementPanel customerView;
    private LoyaltyManagementPanel loyaltyView;
    private CustomerService customerService;
    private MainFrame mainFrame;
    private RoleService roleService;
    
    private static CustomerController instance;

    public CustomerController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.customerView = mainFrame.getCustomerPanel();
        this.loyaltyView = mainFrame.getLoyaltyPanel();
        this.customerService = new CustomerService();
        this.roleService = new RoleService();
        
        instance = this; 
        
        try {
            hiddenButton();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (mainFrame != null) {
            mainFrame.registerPermissionReloader(() -> {
                try { hiddenButton(); } catch (Exception ex) { ex.printStackTrace(); }
            });
        }
        
        initViews();
        initLoyaltyListeners();
        initCustomerListeners();
    }

    public static CustomerController getInstance() {
        return instance;
    }

    private void initViews() {
        loadTiers();     
        loadCustomers(); 
        loadRules();     
    }

    public void loadCustomers() {
        try {
            DefaultTableModel model = customerView.getTableModel();
            model.setRowCount(0);
            ResultSet rs = customerService.getAllCustomers();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            while (rs.next()) {
                String ngayDK = rs.getDate("NgayDangKy") != null ? sdf.format(rs.getDate("NgayDangKy")) : "";
                String tenHang = rs.getString("HangThanhVien");
                if (tenHang == null) tenHang = "Chưa có"; 
                
                model.addRow(new Object[]{
                    "KH" + rs.getInt("MaKhachHang"), 
                    rs.getString("SoDienThoai"), 
                    rs.getString("HoTen"), 
                    ngayDK, 
                    rs.getInt("DiemHienTai"), 
                    rs.getInt("DiemTichLuy"), 
                    tenHang
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTiers() {
        try {
            DefaultTableModel model = loyaltyView.getTierTableModel();
            model.setRowCount(0);
            
            JComboBox<String> cbFilter = customerView.getCbMembershipTier();
            cbFilter.removeAllItems();
            cbFilter.addItem("Tất cả hạng");

            ResultSet rs = customerService.getAllTiers();
            while (rs.next()) {
                String tenHang = rs.getString("TenHang");
                String isDefault = rs.getInt("LaMacDinh") == 1 ? "Có" : "Không";
                
                model.addRow(new Object[]{
                    rs.getInt("MaHang"), tenHang, rs.getInt("DiemYeuCau"), rs.getDouble("PhanTramChietKhau"), isDefault, ""
                });
                
                cbFilter.addItem(tenHang); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRules() {
        try {
            int[] rules = customerService.getPointRule();
            loyaltyView.getTxtTienTichMotDiem().setText(String.valueOf(rules[0]));
            loyaltyView.getTxtGiaTriMotDiem().setText(String.valueOf(rules[1]));
            loyaltyView.getTxtDiemDoiMotLy().setText(String.valueOf(rules[2])); // [CẬP NHẬT] Hiện dữ liệu N
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyCustomerFilter() {
        String searchText = customerView.getTxtSearchCustomer().getText().trim();
        if (searchText.equals("Nhập SĐT hoặc Tên...")) {
            searchText = "";
        }
        Object selectedItem = customerView.getCbMembershipTier().getSelectedItem();
        String selectedTier = selectedItem != null ? selectedItem.toString() : "Tất cả hạng";

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(customerView.getTableModel());
        customerView.getCustomerTable().setRowSorter(sorter);

        List<RowFilter<Object,Object>> filters = new ArrayList<>();
        
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText, 1, 2));
        }
        
        if (!selectedTier.equals("Tất cả hạng")) {
            filters.add(RowFilter.regexFilter("(?i)^" + selectedTier + "$", 6)); 
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void initCustomerListeners() {
        // Lọc tự động khi thay đổi giá trị Combobox hạng thẻ
        customerView.getCbMembershipTier().addActionListener(e -> applyCustomerFilter());

        // Lọc tự động khi gõ phím tìm kiếm
        customerView.getTxtSearchCustomer().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void triggerFilter() {
                SwingUtilities.invokeLater(() -> applyCustomerFilter());
            }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { triggerFilter(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { triggerFilter(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { triggerFilter(); }
        });

        customerView.getBtnAddCustomer().addActionListener(e -> {
            JTextField txtPhone = new JTextField();
            JTextField txtName = new JTextField();
            
            Object[] message = {
                "Số điện thoại (*):", txtPhone,
                "Họ và Tên:", txtName
            };
            
            int option = JOptionPane.showConfirmDialog(customerView, message, 
                    "Thêm Khách Hàng Mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    
            if (option == JOptionPane.OK_OPTION) {
                try {
                    String phone = txtPhone.getText().trim();
                    String name = txtName.getText().trim();
                    
                    if (phone.isEmpty()) {
                        JOptionPane.showMessageDialog(customerView, "Số điện thoại không được để trống!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    Model.CustomerModel newCus = customerService.registerNewCustomer(phone, name);
                    if (newCus != null) {
                        JOptionPane.showMessageDialog(customerView, "Đăng ký khách hàng thành công!");
                        loadCustomers(); 
                    } else {
                        JOptionPane.showMessageDialog(customerView, "Đăng ký thất bại. Số điện thoại có thể đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(customerView, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        customerView.setActionListener(new View.CustomerManagementPanel.ActionButtonListener() {
            @Override
public void onEdit(int row) {

    // Lấy mã KH dạng KH21
    String maKH = customerView.getCustomerTable()
            .getValueAt(row, 0)
            .toString();

    // Chuyển KH21 -> 21
    if (maKH.startsWith("KH")) {
        maKH = maKH.substring(2);
    }

    int id = Integer.parseInt(maKH);

    String oldPhone = customerView.getCustomerTable()
            .getValueAt(row, 1)
            .toString();

    String oldName = customerView.getCustomerTable()
            .getValueAt(row, 2)
            .toString();

    JTextField txtPhone = new JTextField(oldPhone);
    JTextField txtName = new JTextField(oldName);

    Object[] message = {
        "Số điện thoại (*):", txtPhone,
        "Họ và Tên:", txtName
    };

    int option = JOptionPane.showConfirmDialog(
            customerView,
            message,
            "Sửa Khách Hàng",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (option == JOptionPane.OK_OPTION) {
        try {

            String phone = txtPhone.getText().trim();
            String name = txtName.getText().trim();

            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(
                        customerView,
                        "Số điện thoại không được để trống!",
                        "Lỗi",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            boolean success = customerService.updateCustomer(id, phone, name);

            if (success) {
                JOptionPane.showMessageDialog(
                        customerView,
                        "Cập nhật thành công!"
                );

                loadCustomers();

            } else {
                JOptionPane.showMessageDialog(
                        customerView,
                        "Cập nhật thất bại. Số điện thoại có thể đã tồn tại hoặc không hợp lệ!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    customerView,
                    "Lỗi hệ thống: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
        });
    }

    private void initLoyaltyListeners() {
        loyaltyView.getBtnAddTier().addActionListener(e -> {
            showTierDialog(0, "", 0, 0.0); 
        });

        loyaltyView.setActionListener(new LoyaltyManagementPanel.ActionButtonListener() {
            @Override
            public void onEdit(int row) {
                int id = Integer.parseInt(loyaltyView.getTierTable().getValueAt(row, 0).toString());
                String name = loyaltyView.getTierTable().getValueAt(row, 1).toString();
                int points = Integer.parseInt(loyaltyView.getTierTable().getValueAt(row, 2).toString());
                double discount = Double.parseDouble(loyaltyView.getTierTable().getValueAt(row, 3).toString());
                showTierDialog(id, name, points, discount); 
            }

            @Override
            public void onDelete(int row) {
                int id = Integer.parseInt(loyaltyView.getTierTable().getValueAt(row, 0).toString());
                int confirm = JOptionPane.showConfirmDialog(loyaltyView, 
                    "Bạn có chắc chắn muốn xóa hạng thẻ này?\nToàn bộ khách hàng đang ở hạng này sẽ tạm thời được chuyển về hạng Mặc định.", 
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        String result = customerService.deleteTier(id);
                        customerService.syncTiers(); // Đồng bộ lại toàn bộ khách hàng sau khi xóa hạng
                        JOptionPane.showMessageDialog(loyaltyView, result);
                        loadTiers();
                        loadCustomers(); 
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(loyaltyView, "Lỗi: " + ex.getMessage());
                    }
                }
            }
        });



        loyaltyView.getBtnSaveRule().addActionListener(e -> {
            try {
                int tien = Integer.parseInt(loyaltyView.getTxtTienTichMotDiem().getText().trim());
                int giaTri = Integer.parseInt(loyaltyView.getTxtGiaTriMotDiem().getText().trim());
                int diemDoi = Integer.parseInt(loyaltyView.getTxtDiemDoiMotLy().getText().trim());
                
                if (tien <= 0 || giaTri <= 0 || diemDoi <= 0) throw new NumberFormatException();
                
                customerService.updatePointRule(tien, giaTri, diemDoi);
                
                // [SỬA] Reload lại từ DB để xác nhận dữ liệu đã được lưu đúng
                loadRules();
                
                if (mainFrame != null && mainFrame.getPosController() != null) {
                    mainFrame.getPosController().reloadPosData();
                }
                
                JOptionPane.showMessageDialog(loyaltyView, "Lưu Tỷ lệ quy đổi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(loyaltyView, "Tỷ lệ quy đổi phải là số nguyên dương lớn hơn 0!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                // [SỬA] Tránh hiện "null" nếu exception không có message
                String errMsg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                JOptionPane.showMessageDialog(loyaltyView, "Lỗi hệ thống: " + errMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showTierDialog(int id, String currentName, int currentPoints, double currentDiscount) {
        JTextField txtName = new JTextField(currentName);
        JTextField txtPoints = new JTextField(id == 0 ? "" : String.valueOf(currentPoints));
        JTextField txtDiscount = new JTextField(id == 0 ? "0.0" : String.valueOf(currentDiscount));
        
        Object[] message = {
            "Tên hạng thẻ:", txtName,
            "Số điểm yêu cầu để lên hạng:", txtPoints,
            "Chiết khấu (0 - 100%):", txtDiscount
        };
        
        int option = JOptionPane.showConfirmDialog(loyaltyView, message, 
                id == 0 ? "Thêm Hạng Mới" : "Sửa Thông Tin Hạng", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = txtName.getText().trim();
                int points = Integer.parseInt(txtPoints.getText().trim());
                double discount = Double.parseDouble(txtDiscount.getText().trim());
                
                if (name.isEmpty() || points < 0 || discount < 0 || discount > 100) {
                    throw new IllegalArgumentException("Dữ liệu không hợp lệ!");
                }
                
                customerService.saveTier(id, name, points, discount);
                customerService.syncTiers(); // Tự động quét đồng bộ lại hạng cho toàn bộ khách hàng khi thêm/sửa hạng
                JOptionPane.showMessageDialog(loyaltyView, id == 0 ? "Thêm hạng thẻ thành công!" : "Cập nhật thành công!");
                loadTiers(); 
                loadCustomers();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(loyaltyView, "Vui lòng nhập Điểm là một số hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(loyaltyView, "Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void hiddenButton() throws Exception {
        int currentAccountId = SessionManager.getAccountId();
        
        // Khách hàng
        int functionIdCus = roleService.getFunctionIdByName("Khách hàng");
        if(functionIdCus == -1) functionIdCus = 1;
        boolean hasViewCus = roleService.isPermissed("Xem", currentAccountId, functionIdCus);
        boolean hasAddCus = roleService.isPermissed("Them", currentAccountId, functionIdCus);
        
        if (mainFrame != null) mainFrame.setMenuVisible("Customer", hasViewCus);
        if (customerView.getBtnAddCustomer() != null) customerView.getBtnAddCustomer().setVisible(hasAddCus);

        // Cấu hình tích điểm
        int functionIdLoy = roleService.getFunctionIdByName("Cấu hình tích điểm");
        if(functionIdLoy == -1) functionIdLoy = 1;
        boolean hasViewLoy = roleService.isPermissed("Xem", currentAccountId, functionIdLoy);
        boolean hasAddLoy = roleService.isPermissed("Them", currentAccountId, functionIdLoy);
        boolean hasEditLoy = roleService.isPermissed("Sua", currentAccountId, functionIdLoy);
        boolean hasDeleteLoy = roleService.isPermissed("Xoa", currentAccountId, functionIdLoy);
        
        if (mainFrame != null) mainFrame.setMenuVisible("Loyalty", hasViewLoy);
        
        if (loyaltyView.getBtnAddTier() != null) loyaltyView.getBtnAddTier().setVisible(hasAddLoy);
        if (loyaltyView.getBtnSaveRule() != null) loyaltyView.getBtnSaveRule().setVisible(hasEditLoy);
        
        loyaltyView.setActionPermissions(hasEditLoy, hasDeleteLoy);
    }
}