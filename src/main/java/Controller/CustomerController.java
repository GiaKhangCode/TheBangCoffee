package Controller;

import Service.CustomerService;
import View.CustomerManagementPanel;
import View.LoyaltyManagementPanel;
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
    
    private static CustomerController instance;

    public CustomerController(CustomerManagementPanel customerView, LoyaltyManagementPanel loyaltyView) {
        this.customerView = customerView;
        this.loyaltyView = loyaltyView;
        this.customerService = new CustomerService();
        
        instance = this; 
        
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
                    rs.getInt("MaHang"), tenHang, rs.getInt("DiemYeuCau"), isDefault, ""
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

    private void initCustomerListeners() {
        customerView.getBtnSearch().addActionListener(e -> {
            String searchText = customerView.getTxtSearchCustomer().getText().trim();
            if (searchText.equals("Nhập SĐT hoặc Tên...")) {
                searchText = "";
            }
            String selectedTier = customerView.getCbMembershipTier().getSelectedItem().toString();

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
    }

    private void initLoyaltyListeners() {
        loyaltyView.getBtnAddTier().addActionListener(e -> {
            showTierDialog(0, "", 0); 
        });

        loyaltyView.setActionListener(new LoyaltyManagementPanel.ActionButtonListener() {
            @Override
            public void onEdit(int row) {
                int id = Integer.parseInt(loyaltyView.getTierTable().getValueAt(row, 0).toString());
                String name = loyaltyView.getTierTable().getValueAt(row, 1).toString();
                int points = Integer.parseInt(loyaltyView.getTierTable().getValueAt(row, 2).toString());
                showTierDialog(id, name, points); 
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
                        JOptionPane.showMessageDialog(loyaltyView, result);
                        loadTiers();
                        loadCustomers(); 
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(loyaltyView, "Lỗi: " + ex.getMessage());
                    }
                }
            }
        });

        loyaltyView.getBtnSyncTiers().addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(loyaltyView, 
                "Hệ thống sẽ dựa vào luật thăng hạng mới nhất để quét và cập nhật lại Hạng cho TOÀN BỘ khách hàng.\nBạn có muốn tiếp tục?", 
                "Xác nhận Đồng bộ", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String result = customerService.syncTiers();
                    JOptionPane.showMessageDialog(loyaltyView, result);
                    loadCustomers(); 
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(loyaltyView, "Sự cố: " + ex.getMessage(), "Lỗi Đồng Bộ", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loyaltyView.getBtnSaveRule().addActionListener(e -> {
            try {
                int tien = Integer.parseInt(loyaltyView.getTxtTienTichMotDiem().getText());
                int giaTri = Integer.parseInt(loyaltyView.getTxtGiaTriMotDiem().getText());
                int diemDoi = Integer.parseInt(loyaltyView.getTxtDiemDoiMotLy().getText()); // [CẬP NHẬT] Lấy N
                
                if(tien <= 0 || giaTri <= 0 || diemDoi <= 0) throw new NumberFormatException();
                
                customerService.updatePointRule(tien, giaTri, diemDoi);
                JOptionPane.showMessageDialog(loyaltyView, "Lưu Tỷ lệ quy đổi thành công!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(loyaltyView, "Tỷ lệ quy đổi phải là số nguyên dương lớn hơn 0!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(loyaltyView, "Lỗi hệ thống: " + ex.getMessage());
            }
        });
    }

    private void showTierDialog(int id, String currentName, int currentPoints) {
        JTextField txtName = new JTextField(currentName);
        JTextField txtPoints = new JTextField(id == 0 ? "" : String.valueOf(currentPoints));
        
        Object[] message = {
            "Tên hạng thẻ:", txtName,
            "Số điểm yêu cầu để lên hạng:", txtPoints
        };
        
        int option = JOptionPane.showConfirmDialog(loyaltyView, message, 
                id == 0 ? "Thêm Hạng Mới" : "Sửa Thông Tin Hạng", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = txtName.getText().trim();
                int points = Integer.parseInt(txtPoints.getText().trim());
                
                if (name.isEmpty() || points < 0) {
                    throw new IllegalArgumentException("Dữ liệu không hợp lệ!");
                }
                
                customerService.saveTier(id, name, points);
                JOptionPane.showMessageDialog(loyaltyView, id == 0 ? "Thêm hạng thẻ thành công!" : "Cập nhật thành công!");
                loadTiers(); 
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(loyaltyView, "Vui lòng nhập Điểm là một số hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(loyaltyView, "Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}