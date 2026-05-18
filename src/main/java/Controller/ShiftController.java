package Controller;

import Model.ShiftModel;
import Model.AccountModel;
import Service.AccountService;
import Service.ShiftService;
import Service.RoleService;
import Model.SessionManager;
import View.EmployeeSchedulePanel;
import View.MainFrame;
import javax.swing.JOptionPane;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftController {
    private MainFrame mainFrame;
    private EmployeeSchedulePanel view;
    private ShiftService shiftService;
    private AccountService accountService;
    private RoleService roleService;

    public ShiftController(MainFrame sharedMainFrame) {
        this.mainFrame = sharedMainFrame;
        this.view = mainFrame.getShiftPanel(); 
        this.shiftService = new ShiftService();
        this.accountService = new AccountService();
        this.roleService = new RoleService();
        
        try {
            hiddenButton();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (mainFrame != null) {
            mainFrame.registerPermissionReloader(() -> {
                try { hiddenButton(); } catch (Exception ex) { ex.printStackTrace(); }
            });
        }
        
        if (this.view != null) {
            initEvents();
            reloadShiftCards(); 
            view.loadEmployeesToSchedule(accountService.getAccountList());
            loadData();          
        }
    }

    private void reloadShiftCards() {
        try {
            List<ShiftModel> all = shiftService.getAllShifts();
            view.setAvailableShifts(all); 
            view.loadShiftCards(all); 
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadData() {
        try {
            List<AccountModel> accounts = view.getCurrentAccounts();
            if (accounts == null || accounts.isEmpty()) return;

            LocalDate start = view.getCurrentStartOfWeek();
            boolean isMonthView = view.isMonthViewActive();

            if (isMonthView) {
                // TẢI DỮ LIỆU CHO THÁNG
                LocalDate startOfMonth = start.withDayOfMonth(1);
                LocalDate endOfMonth = YearMonth.from(startOfMonth).atEndOfMonth();
                
                List<String[]> dbData = shiftService.getWeeklySchedules(startOfMonth, endOfMonth);
                
                // Gom nhóm dữ liệu theo Ngày để View dễ vẽ Lịch
                Map<LocalDate, List<String>> monthDataMap = new HashMap<>();
                for (String[] row : dbData) {
                    int maTk = Integer.parseInt(row[0]);
                    LocalDate date = LocalDate.parse(row[1]);
                    String tenCa = row[2];
                    
                    // Tìm tên nhân viên
                    String empName = "";
                    for(AccountModel acc : accounts) {
                        if (acc.getAccountID() == maTk) { empName = acc.getUsername(); break; }
                    }
                    
                    monthDataMap.computeIfAbsent(date, k -> new ArrayList<>())
                                .add("<b>" + empName + "</b>: " + tenCa);
                }
                view.renderMonthView(monthDataMap); 
                
            } else {
                // TẢI DỮ LIỆU CHO TUẦN (Mảng 2D)
                LocalDate end = start.plusDays(6);
                List<String[]> dbData = shiftService.getWeeklySchedules(start, end);
                
                String[][] grid = new String[accounts.size()][7];
                for(int i=0; i<accounts.size(); i++) for(int j=0; j<7; j++) grid[i][j] = "";

                for (String[] row : dbData) {
                    int maTk = Integer.parseInt(row[0]);
                    LocalDate date = LocalDate.parse(row[1]);
                    
                    int r = -1;
                    for(int i=0; i<accounts.size(); i++) if(accounts.get(i).getAccountID()==maTk) { r=i; break; }
                    
                    if (r != -1) {
                        int c = (int) java.time.temporal.ChronoUnit.DAYS.between(start, date);
                        if (c >= 0 && c < 7) {
                            grid[r][c] = grid[r][c].isEmpty() ? row[2] : grid[r][c] + "\n" + row[2];
                        }
                    }
                }
                view.applyScheduleData(grid);
            }
            
            view.markWeekLoadedFromDB(start);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void refreshEmployeeList() {
        if (view != null && accountService != null) {
            view.loadEmployeesToSchedule(accountService.getAccountList());
            loadData();
        }
    }

    private void initEvents() {
        view.addWeekNavigationListener(e -> loadData());

        view.addSaveShiftListener(e -> {
            try {
                int maCa = view.getMaCa();
                String ten = view.getTenCa().trim(), bd = view.getGioBatDau().trim(), kt = view.getGioKetThuc().trim();
                if (ten.isEmpty() || bd.isEmpty() || kt.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Vui lòng nhập đầy đủ thôngkiem (Tên ca, giờ)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (!bd.matches("^([01]\\d|2[0-3]):([0-5]\\d)$") || !kt.matches("^([01]\\d|2[0-3]):([0-5]\\d)$")) {
                    JOptionPane.showMessageDialog(null, "Định dạng giờ không hợp lệ!\nVui lòng nhập theo định dạng 24h: HH:mm (Ví dụ: 08:30, 15:00)", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (kt.compareTo(bd) <= 0) {
                    JOptionPane.showMessageDialog(null, "Giờ kết thúc phải lớn hơn Giờ bắt đầu", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return; 
                }

                if (maCa == 0) {
                    if (shiftService.isShiftNameExists(ten)) {
                        JOptionPane.showMessageDialog(null, "Tên ca này đang được sử dụng, vui lòng chọn tên khác", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        return; 
                    }
                    if (shiftService.addShift(new ShiftModel(0, ten, bd, kt, "Đang sử dụng"))) {
                        view.clearShiftForm(); reloadShiftCards(); 
                        view.forceReloadCurrentWeek(); loadData();
                        JOptionPane.showMessageDialog(null, "Đã lưu mẫu ca mới thành công!");
                    }
                } else {
                    if (shiftService.isShiftNameExistsExcludeCurrent(ten, maCa)) {
                        JOptionPane.showMessageDialog(null, "Tên ca này đang được sử dụng, vui lòng chọn tên khác", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        return; 
                    }
                    if (shiftService.checkUpdateConflict(maCa, bd, kt)) {
                        JOptionPane.showMessageDialog(null, "Không thể cập nhật giờ!\nThay đổi này gây trùng lặp thời gian với các ca khác mà nhân viên đã được phân công.", "Lỗi Xung Đột", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (shiftService.updateShift(new ShiftModel(maCa, ten, bd, kt, "Đang sử dụng"))) {
                        view.clearShiftForm(); reloadShiftCards(); 
                        view.forceReloadCurrentWeek(); loadData();
                        JOptionPane.showMessageDialog(null, "Đã cập nhật thay đổi thành công!");
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        view.addDeleteShiftListener(e -> {
            try {
                int id = view.getSelectedMaCa();
                if (id <= 0) return;
                int confirm = JOptionPane.showConfirmDialog(null, "Bạn có chắc chắn muốn ngưng sử dụng ca này không?\nCác lịch phân công ca này từ hôm nay trở đi sẽ bị ẩn lập tức.", "Cảnh báo", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION && shiftService.disableShift(id)) {
                    view.clearShiftForm(); reloadShiftCards();
                    view.forceReloadCurrentWeek(); loadData();
                    JOptionPane.showMessageDialog(null, "Đã ngưng sử dụng mẫu ca!");
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        view.setConfirmAssignListener(e -> {
            try {
                List<ShiftModel> sel = view.getSelectedShifts();
                int row = view.getEditingRow();
                int col = view.getEditingCol();
                
                for (int i = 0; i < sel.size(); i++) {
                    for (int j = i + 1; j < sel.size(); j++) {
                        if (view.isConflict(sel.get(i), sel.get(j))) {
                            JOptionPane.showMessageDialog(view.getDialog(), "Lỗi: Ca '" + sel.get(i).getTenCa() + "' và '" + sel.get(j).getTenCa() + "' bị trùng lặp thời gian với nhau!", "Xung đột ca", JOptionPane.ERROR_MESSAGE);
                            return; 
                        }
                    }
                }
                
                LocalDate date = view.getCurrentStartOfWeek().plusDays(col - 1);
                int maTk = view.getCurrentAccounts().get(row).getAccountID();
                
                shiftService.deleteSchedules(date, date, java.util.Collections.singletonList(maTk));
                for(ShiftModel s : sel) shiftService.insertSchedule(date, s.getMaCa(), maTk);
                view.setSchedule(row, col, sel);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        view.addSaveScheduleListener(e -> {
            try {
                if (!view.isRepeatChecked()) {
                    JOptionPane.showMessageDialog(null, "Bạn muốn 'Lặp lại lịch' cho các tuần tiếp theo ?", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                LocalDate startOfWeek = view.getCurrentStartOfWeek();
                int repeatWeeks = view.getRepeatWeeks();
                
                List<AccountModel> accounts = view.getCurrentAccounts();
                String[][] gridData = view.getCurrentScheduleData();
                List<ShiftModel> availableShifts = view.getAvailableShifts();
                
                if (accounts == null || accounts.isEmpty()) return;

                List<Integer> accountIds = new ArrayList<>();
                for (AccountModel acc : accounts) accountIds.add(acc.getAccountID()); 
                
                for (int w = 1; w <= repeatWeeks; w++) {
                    LocalDate targetStart = startOfWeek.plusWeeks(w);
                    LocalDate targetEnd = targetStart.plusDays(6);
                    
                    shiftService.deleteSchedules(targetStart, targetEnd, accountIds);
                    
                    for (int r = 0; r < accounts.size(); r++) {
                        int maTaiKhoan = accounts.get(r).getAccountID(); 
                        for (int c = 0; c < 7; c++) {
                            String cellText = gridData[r][c];
                            if (cellText == null || cellText.trim().isEmpty()) continue;
                            
                            LocalDate workDate = targetStart.plusDays(c);
                            String[] shiftNames = cellText.split("\n");
                            
                            for (String shiftName : shiftNames) {
                                shiftName = shiftName.trim();
                                if (shiftName.isEmpty()) continue;
                                
                                int maCa = -1;
                                for (ShiftModel sm : availableShifts) {
                                    if (sm.getTenCa().equalsIgnoreCase(shiftName)) { maCa = sm.getMaCa(); break; }
                                }
                                if (maCa != -1) shiftService.insertSchedule(workDate, maCa, maTaiKhoan);
                            }
                        }
                    }
                }
                view.cloneScheduleToNextWeeks(repeatWeeks);
                
                // THÔNG BÁO LẶP LỊCH THÀNH CÔNG RÕ RÀNG HƠN
                JOptionPane.showMessageDialog(null, "Đã sao chép và lưu lịch cho " + repeatWeeks + " tuần tiếp theo thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void hiddenButton() throws SQLException {
        int currentAccountId = SessionManager.getAccountId();
        int currentFunctionId = roleService.getFunctionIdByName("Quản lý ca làm việc");
        if (currentFunctionId == -1) currentFunctionId = 4; // Fallback
        
        boolean hasViewPermission = roleService.isPermissed("Xem", currentAccountId, currentFunctionId);
        boolean hasAddPermission = roleService.isPermissed("Them", currentAccountId, currentFunctionId);
        boolean hasEditPermission = roleService.isPermissed("Sua", currentAccountId, currentFunctionId);
        boolean hasDeletePermission = roleService.isPermissed("Xoa", currentAccountId, currentFunctionId);
        
        if (mainFrame != null) {
            mainFrame.setMenuVisible("Staff", hasViewPermission);
        }
        
        if (!hasViewPermission) {
            return;
        }
        
        if (view.getBtnSaveShift() != null) view.getBtnSaveShift().setVisible(hasAddPermission || hasEditPermission);
        if (view.getBtnDeleteShift() != null) view.getBtnDeleteShift().setVisible(hasDeletePermission);
        if (view.getBtnSaveSchedule() != null) view.getBtnSaveSchedule().setVisible(hasAddPermission || hasEditPermission);
        
        view.setActionPermissions(hasEditPermission, hasDeletePermission);
    }
}