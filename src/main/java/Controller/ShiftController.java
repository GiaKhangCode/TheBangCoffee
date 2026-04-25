package Controller;

import View.EmployeeSchedulePanel;
import DatabaseAccessObject.AccountDAO;
import Service.ShiftService; // ĐỔI SANG SỬ DỤNG SERVICE
import Model.AccountModel;
import Model.ShiftModel;
import View.MainFrame;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import java.awt.*;
import javax.swing.*;

public class ShiftController {

    private EmployeeSchedulePanel shiftView;
    private AccountDAO accountDAO;
    private ShiftService shiftService; // DÙNG SERVICE THAY VÌ DAO
    private MainFrame mainframe;

    private int currentYear;
    private int currentMonth;

    private Map<LocalDate, EmployeeSchedulePanel.DaySchedule> currentScheduleMap;
    
    public ShiftController(MainFrame mainframe) {
        this.mainframe = mainframe;
        this.shiftView = mainframe.getShiftPanel();
        this.accountDAO = new AccountDAO();
        this.shiftService = new ShiftService(); 
        initController();
    }

    private void initController() {
        this.shiftView.setAddShiftListener((LocalDate date, String shiftType) -> {
            handleAddShift(date, shiftType);
        });

        this.shiftView.setFilterChangeListener((int year, int month) -> {
            loadScheduleData(year, month);
        });

        // --- LẮNG NGHE SỰ KIỆN SỬA/XÓA TỪ VIEW ---
        this.shiftView.setShiftActionUpdateListener(new EmployeeSchedulePanel.ShiftActionUpdateListener() {
            @Override
            public void onEditShift(int maCa, int currentMaTaiKhoan, LocalDate date, String shiftType) {
                handleEditShift(maCa, currentMaTaiKhoan, date, shiftType);
            }

            @Override
            public void onDeleteShift(int maCa) {
                handleDeleteShift(maCa);
            }

            // --- BẮT LỆNH THÊM NV TỪ CỬA SỔ CHI TIẾT ---
            @Override
            public void onAddMoreEmployee(LocalDate date, String shiftType) {
                handleAddShift(date, shiftType); // Gọi lại form thêm như bình thường
            }
        });

        LocalDate today = LocalDate.now();
        loadScheduleData(today.getYear(), today.getMonthValue());
    }

    private void loadScheduleData(int year, int month) {
        this.currentYear = year;
        this.currentMonth = month;

        List<ShiftModel> dbShifts = shiftService.getShiftsByMonthYear(month, year);
        
        // --- SỬA DÒNG NÀY ---
        this.currentScheduleMap = new HashMap<>();

        for (ShiftModel shift : dbShifts) {
            LocalDate date = shift.getNgayLam();
            
            // --- SỬA currentScheduleMap Ở ĐÂY ---
            currentScheduleMap.putIfAbsent(date, new EmployeeSchedulePanel.DaySchedule());
            EmployeeSchedulePanel.DaySchedule daySchedule = currentScheduleMap.get(date);

            EmployeeSchedulePanel.ShiftDetail detail = new EmployeeSchedulePanel.ShiftDetail(
                    shift.getMaCa(), 
                    shift.getMaTaiKhoan(), 
                    shift.getTenTaiKhoan(), 
                    shift.getSoGioLamViec()
            );

            if ("Sáng".equalsIgnoreCase(shift.getBuoiLamViec())) {
                daySchedule.morningShifts.add(detail);
            } else if ("Chiều".equalsIgnoreCase(shift.getBuoiLamViec())) {
                daySchedule.afternoonShifts.add(detail);
            }
        }
        
        // --- ĐẨY currentScheduleMap XUỐNG VIEW ---
        shiftView.renderCalendar(year, month, currentScheduleMap);
    }
    
    // --- HÀM THÊM CA (GIỮ NGUYÊN NHƯ CŨ, CHỈ ĐỔI GỌI SERVICE) ---
    private void handleAddShift(LocalDate date, String shiftType) {
        showShiftDialog(0, "Thêm Ca Làm Việc", date, shiftType, -1);
    }

    // --- HÀM SỬA CA MỚI ---
    private void handleEditShift(int maCa, int currentMaTaiKhoan, LocalDate date, String shiftType) {
        showShiftDialog(maCa, "Sửa Ca Làm Việc", date, shiftType, currentMaTaiKhoan);
    }

    // --- HÀM XÓA CA MỚI ---
    private void handleDeleteShift(int maCa) {
        int confirm = JOptionPane.showConfirmDialog(shiftView, "Bạn có chắc chắn muốn xóa ca làm việc này không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean isSuccess = shiftService.deleteShift(maCa);
            if (isSuccess) {
                JOptionPane.showMessageDialog(shiftView, "Đã xóa thành công!");
                loadScheduleData(currentYear, currentMonth); // Tự động Refresh
            } else {
                JOptionPane.showMessageDialog(shiftView, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showShiftDialog(int maCa, String title, LocalDate date, String shiftType, int selectedMaTaiKhoan) {
        
        // 1. TÌM XEM CA NÀY ĐÃ CÓ NHỮNG AI LÀM RỒI
        java.util.Set<Integer> assignedAccountIds = new java.util.HashSet<>();
        if (currentScheduleMap != null && currentScheduleMap.containsKey(date)) {
            EmployeeSchedulePanel.DaySchedule ds = currentScheduleMap.get(date);
            List<EmployeeSchedulePanel.ShiftDetail> shiftsForType = 
                    "Sáng".equalsIgnoreCase(shiftType) ? ds.morningShifts : ds.afternoonShifts;
            
            for (EmployeeSchedulePanel.ShiftDetail detail : shiftsForType) {
                assignedAccountIds.add(detail.maTaiKhoan);
            }
        }

        // 2. LẤY TẤT CẢ TÀI KHOẢN VÀ LỌC RA NHỮNG NGƯỜI CHƯA CÓ LỊCH
        List<AccountModel> allAccounts = accountDAO.getAccountList();
        List<AccountModel> availableAccounts = new java.util.ArrayList<>();
        int preSelectedIndex = -1;
        
        for (AccountModel acc : allAccounts) {
            int accId = acc.getAccountID();
            // Nếu người này CHƯA có lịch HOẶC chính là người đang được chọn để "Sửa", thì mới cho vào list
            if (!assignedAccountIds.contains(accId) || accId == selectedMaTaiKhoan) {
                availableAccounts.add(acc);
                if (accId == selectedMaTaiKhoan) {
                    preSelectedIndex = availableAccounts.size() - 1; // Nhớ vị trí để bôi xanh sẵn khi Sửa
                }
            }
        }

        // 3. NẾU TẤT CẢ NHÂN VIÊN ĐỀU ĐÃ ĐƯỢC XẾP VÀO CA NÀY -> CHẶN KHÔNG CHO MỞ FORM
        if (availableAccounts.isEmpty()) {
            JOptionPane.showMessageDialog(shiftView, "Tất cả nhân viên đã được xếp lịch vào ca " + shiftType + " ngày " + date + "!\nKhông còn nhân viên trống.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return; 
        }

        // --- BẮT ĐẦU VẼ GIAO DIỆN FORM VỚI DANH SÁCH ĐÃ LỌC ---
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(shiftView), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(shiftView);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblHeader = new JLabel(title + " " + shiftType + " - Ngày " + date.toString());
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setOpaque(false);
        JLabel lblEmp = new JLabel(maCa == 0 ? "Chọn nhân viên (Giữ phím Ctrl để chọn nhiều):" : "Chọn nhân viên để đổi:");
        lblEmp.setFont(new Font("SansSerif", Font.ITALIC, 13));
        contentPanel.add(lblEmp, BorderLayout.NORTH);

        // Chuyển mảng danh sách từ object sang mảng String để hiển thị
        String[] employeeNames = new String[availableAccounts.size()];
        for (int i = 0; i < availableAccounts.size(); i++) {
            employeeNames[i] = availableAccounts.get(i).getUsername(); 
        }

        JList<String> listEmployee = new JList<>(employeeNames);
        listEmployee.setFont(new Font("SansSerif", Font.PLAIN, 15));
        
        if (maCa == 0) {
            listEmployee.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            listEmployee.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            if (preSelectedIndex != -1) listEmployee.setSelectedIndex(preSelectedIndex);
        }
        
        JScrollPane scrollList = new JScrollPane(listEmployee);
        contentPanel.add(scrollList, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = new JButton("Lưu Danh Sách");
        btnSave.setBackground(new Color(67, 142, 104));
        btnSave.setForeground(Color.WHITE);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnSave.addActionListener(e -> {
            int[] selectedIndices = listEmployee.getSelectedIndices();
            if (selectedIndices.length == 0) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn ít nhất 1 nhân viên!");
                return;
            }
            
            boolean allSuccess = true;
            
            for (int idx : selectedIndices) {
                // Lấy Account từ mảng đã lọc (availableAccounts) thay vì allAccounts cũ
                AccountModel acc = availableAccounts.get(idx);
                ShiftModel newShift = new ShiftModel(maCa, acc.getAccountID(), acc.getUsername(), shiftType, date, 4.0);
                
                if (maCa == 0) { 
                    if (!shiftService.insertShift(newShift)) allSuccess = false;
                } else { 
                    if (!shiftService.updateShift(newShift)) allSuccess = false;
                }
            }
            
            if (allSuccess) {
                JOptionPane.showMessageDialog(dialog, "Thao tác thành công!");
                dialog.dispose();
                loadScheduleData(currentYear, currentMonth); 
            } else {
                JOptionPane.showMessageDialog(dialog, "Có lỗi xảy ra trong quá trình lưu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionPanel.add(btnCancel);
        actionPanel.add(btnSave);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}