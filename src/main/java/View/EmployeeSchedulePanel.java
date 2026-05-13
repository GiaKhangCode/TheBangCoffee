package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import Model.ShiftModel;
import Model.AccountModel;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import javax.swing.border.LineBorder;
import javax.swing.event.EventListenerList;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

public class EmployeeSchedulePanel extends JPanel {
    private JTabbedPane tabbedPane;
    
    // Components Tab 1
    private JPanel cardsContainer;
    private JTextField txtTenCa;
    // [CẬP NHẬT] Đổi từ JTextField sang JSpinner để chọn thời gian
    private JSpinner spnGioBatDau, spnGioKetThuc; 
    private JButton btnSaveShift, btnDeleteShift; 
    private IOSSwitch tglShowActiveOnly;

    // Components Tab 2 (Xếp lịch)
    private JPanel scheduleCardPanel; 
    private CardLayout scheduleCardLayout;
    private boolean isMonthView = false;
    
    // Tuần View
    private JTable scheduleTable;
    private DefaultTableModel scheduleModel;
    private JLabel lblCurrentRange;
    private LocalDate currentPointer; 
    
    // Tháng View
    private JPanel monthCalendarPanel;

    private JPanel centerTool; 
    private JButton btnSaveSchedule, btnToggleView; 
    private JCheckBox chkRepeat;
    private JSpinner spnRepeatWeeks;
    
    private int editingRow = -1, editingCol = -1;
    private ActionListener confirmAssignListener;
    private List<ActionListener> navListeners = new ArrayList<>();
    
    private Map<LocalDate, DefaultTableModel> weeklyModels = new HashMap<>();
    private List<String> currentEmployees = new ArrayList<>();
    private List<AccountModel> currentAccountsList = new ArrayList<>(); 
    private Set<LocalDate> loadedWeeksFromDB = new HashSet<>();
    private List<ShiftModel> availableShifts; 
    
    // Biến cho các popup Dialog
    private JButton btnConfirm;
    private List<JCheckBox> checkBoxes;
    private List<ShiftModel> activeShifts, selectedShifts;
    private JDialog dialog;
    private int currentMaCa = 0; 
    private ShiftCard selectedCard = null;

    public EmployeeSchedulePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        currentPointer = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE); 
        
        tabbedPane.addTab("Thiết lập ca làm việc", createShiftSetupPanel());
        tabbedPane.addTab("Xếp lịch làm việc", createScheduleWrapperPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        updateDisplayRange();
    }

    // =========================================================================
    // CÁC HÀM GETTER/SETTER VÀ LOGIC ĐIỀU KHIỂN CHUNG
    // =========================================================================
    public boolean isMonthViewActive() { return isMonthView; }
    public void setAvailableShifts(List<ShiftModel> shifts) { this.availableShifts = shifts; }
    public void addWeekNavigationListener(ActionListener l) { navListeners.add(l); }
    private void fireNavEvent() {
        ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, isMonthView ? "MONTH_CHANGED" : "WEEK_CHANGED");
        for (ActionListener l : navListeners) l.actionPerformed(e);
    }
    public void setConfirmAssignListener(ActionListener l) { this.confirmAssignListener = l; }
    public boolean isWeekLoadedFromDB(LocalDate week) { return loadedWeeksFromDB.contains(week); }
    public void markWeekLoadedFromDB(LocalDate week) { loadedWeeksFromDB.add(week); }
    public void forceReloadCurrentWeek() { loadedWeeksFromDB.remove(currentPointer); }
    public LocalDate getCurrentStartOfWeek() { return currentPointer; }

    public void cloneScheduleToNextWeeks(int additionalWeeks) {
        String[][] currentData = getCurrentScheduleData();
        for (int w = 1; w <= additionalWeeks; w++) {
            LocalDate targetWeek = currentPointer.plusWeeks(w);
            DefaultTableModel targetModel = weeklyModels.get(targetWeek);
            
            if (targetModel == null) {
                targetModel = createNewScheduleModel();
                weeklyModels.put(targetWeek, targetModel);
            }
            
            if (targetModel.getRowCount() != currentAccountsList.size()) {
                targetModel.setRowCount(0); 
                for (AccountModel a : currentAccountsList) {
                    targetModel.addRow(new Object[]{a.getUsername(), "", "", "", "", "", "", ""});
                }
            }
            
            for (int r = 0; r < currentData.length; r++) {
                for (int c = 0; c < 7; c++) {
                    targetModel.setValueAt(currentData[r][c], r, c + 1);
                }
            }
            loadedWeeksFromDB.add(targetWeek);
        }
    }

    public boolean isConflict(ShiftModel s1, ShiftModel s2) { 
        return s1.getGioBatDau().compareTo(s2.getGioKetThuc()) < 0 && s2.getGioBatDau().compareTo(s1.getGioKetThuc()) < 0; 
    }

    public void loadShiftCards(List<ShiftModel> shifts) {
        cardsContainer.removeAll(); selectedCard = null; clearShiftForm(); 
        boolean showActiveOnly = tglShowActiveOnly.isSelected();
        if (shifts != null) {
            for (ShiftModel s : shifts) {
                boolean isActive = "Đang sử dụng".equals(s.getTrangThai());
                if (showActiveOnly && !isActive) continue; 
                cardsContainer.add(new ShiftCard(s));
            }
        }
        cardsContainer.revalidate(); cardsContainer.repaint();
    }

    public void clearShiftForm() {
        currentMaCa = 0;
        txtTenCa.setText(""); 
        txtTenCa.setEditable(true); 
        
        // [CẬP NHẬT] Đưa JSpinner về mốc 00:00 mặc định
        try {
            Date defaultTime = new SimpleDateFormat("HH:mm").parse("00:00");
            spnGioBatDau.setValue(defaultTime);
            spnGioKetThuc.setValue(defaultTime);
        } catch (Exception e) {}
        
        spnGioBatDau.setEnabled(true); 
        spnGioKetThuc.setEnabled(true);
        btnSaveShift.setVisible(true); 
        btnDeleteShift.setVisible(false); 
        
        if (selectedCard != null) { selectedCard.setHighlight(false); selectedCard = null; }
    }

    // =========================================================================
    // TAB 1: THIẾT LẬP CA
    // =========================================================================
    private JPanel createShiftSetupPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE); 
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topContainer = new JPanel(new BorderLayout(5, 5));
        topContainer.setBackground(Color.WHITE); 
        
        JLabel lblTitle = new JLabel("Thiết Lập Mẫu Ca Làm Việc", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(46, 139, 87));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        topContainer.add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE); 
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), "Bảng điều khiển", 
                0, 0, new Font("Segoe UI", Font.BOLD, 14)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Tên ca:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; txtTenCa = new JTextField(15); formPanel.add(txtTenCa, gbc);
        
        // [CẬP NHẬT] Khởi tạo JSpinner cho Giờ Bắt Đầu
        gbc.gridx = 2; gbc.gridy = 0; formPanel.add(new JLabel("Giờ bắt đầu (HH:mm):"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; 
        SpinnerDateModel modelBD = new SpinnerDateModel();
        modelBD.setCalendarField(Calendar.MINUTE);
        spnGioBatDau = new JSpinner(modelBD);
        spnGioBatDau.setEditor(new JSpinner.DateEditor(spnGioBatDau, "HH:mm"));
        formPanel.add(spnGioBatDau, gbc);
        
        // [CẬP NHẬT] Khởi tạo JSpinner cho Giờ Kết Thúc
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Giờ kết thúc (HH:mm):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; 
        SpinnerDateModel modelKT = new SpinnerDateModel();
        modelKT.setCalendarField(Calendar.MINUTE);
        spnGioKetThuc = new JSpinner(modelKT);
        spnGioKetThuc.setEditor(new JSpinner.DateEditor(spnGioKetThuc, "HH:mm"));
        formPanel.add(spnGioKetThuc, gbc);

        // Thiết lập giờ mặc định 00:00
        try {
            Date defaultTime = new SimpleDateFormat("HH:mm").parse("00:00");
            spnGioBatDau.setValue(defaultTime);
            spnGioKetThuc.setValue(defaultTime);
        } catch (Exception e) {}

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE); 
        
        btnSaveShift = new JButton("Lưu mẫu ca");
        btnSaveShift.setBackground(new Color(46, 139, 87)); btnSaveShift.setForeground(Color.WHITE);
        btnSaveShift.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        btnDeleteShift = new JButton("Ngưng sử dụng"); 
        btnDeleteShift.setBackground(new Color(231, 76, 60)); btnDeleteShift.setForeground(Color.WHITE);
        btnDeleteShift.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnDeleteShift.setVisible(false);
        
        buttonPanel.add(btnSaveShift); buttonPanel.add(btnDeleteShift);
        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 2; formPanel.add(buttonPanel, gbc);
        
        JPanel centerFormWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerFormWrapper.setBackground(Color.WHITE); 
        centerFormWrapper.add(formPanel);
        topContainer.add(centerFormWrapper, BorderLayout.CENTER);

        JPanel listHeaderPanel = new JPanel(new BorderLayout());
        listHeaderPanel.setBackground(Color.WHITE); 
        listHeaderPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 5, 0));
        
        JLabel lblListTitle = new JLabel(" Danh sách mẫu ca làm việc hiện tại");
        lblListTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblListTitle.setForeground(new Color(46, 139, 87));
        
        tglShowActiveOnly = new IOSSwitch();
        tglShowActiveOnly.setSelected(true); 
        JLabel lblToggleDesc = new JLabel("Chỉ hiện ca Đang sử dụng ");
        lblToggleDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JPanel toggleContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        toggleContainer.setBackground(Color.WHITE);
        toggleContainer.add(lblToggleDesc); toggleContainer.add(tglShowActiveOnly);
        
        tglShowActiveOnly.addActionListener(e -> {
            if(availableShifts != null) loadShiftCards(availableShifts); 
        });
        
        listHeaderPanel.add(lblListTitle, BorderLayout.WEST);
        listHeaderPanel.add(toggleContainer, BorderLayout.EAST);

        cardsContainer = new JPanel(new GridLayout(0, 4, 15, 15)); 
        cardsContainer.setBackground(new Color(250, 250, 250)); 
        cardsContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel cardsWrapper = new JPanel(new BorderLayout());
        cardsWrapper.setBackground(new Color(250, 250, 250));
        cardsWrapper.add(cardsContainer, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(cardsWrapper);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel listMainPanel = new JPanel(new BorderLayout());
        listMainPanel.setBackground(Color.WHITE);
        listMainPanel.add(listHeaderPanel, BorderLayout.NORTH);
        listMainPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(topContainer, BorderLayout.NORTH);
        panel.add(listMainPanel, BorderLayout.CENTER);

        MouseAdapter clearFormAdapter = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { clearShiftForm(); }
        };
        scrollPane.getViewport().addMouseListener(clearFormAdapter);
        cardsWrapper.addMouseListener(clearFormAdapter);

        return panel;
    }

    private class ShiftCard extends JPanel {
        private ShiftModel shift;
        public ShiftCard(ShiftModel shift) {
            this.shift = shift; setLayout(new BorderLayout());
            setPreferredSize(new Dimension(240, 80)); setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
                BorderFactory.createEmptyBorder(0, 0, 5, 0)
            ));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            boolean isActive = "Đang sử dụng".equals(shift.getTrangThai());
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(isActive ? new Color(46, 139, 87) : new Color(169, 169, 169)); 
            topPanel.setPreferredSize(new Dimension(240, 50)); 
            
            JLabel lblName = new JLabel(shift.getTenCa() + (isActive ? "" : " (Ngưng SD)"), SwingConstants.CENTER);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblName.setForeground(Color.WHITE);
            topPanel.add(lblName, BorderLayout.CENTER);

            JLabel lblTime = new JLabel(shift.getGioBatDau() + " - " + shift.getGioKetThuc(), SwingConstants.CENTER);
            lblTime.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblTime.setForeground(new Color(80, 80, 80));
            lblTime.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5)); 

            add(topPanel, BorderLayout.NORTH); add(lblTime, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (selectedCard != null) selectedCard.setHighlight(false);
                    selectedCard = ShiftCard.this; setHighlight(true);
                    currentMaCa = shift.getMaCa();
                    txtTenCa.setText(shift.getTenCa()); 
                    
                    // [CẬP NHẬT] Gán giờ từ chuỗi vào JSpinner
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        spnGioBatDau.setValue(sdf.parse(shift.getGioBatDau()));
                        spnGioKetThuc.setValue(sdf.parse(shift.getGioKetThuc()));
                    } catch (Exception ex) {}

                    txtTenCa.setEditable(isActive); 
                    spnGioBatDau.setEnabled(isActive); 
                    spnGioKetThuc.setEnabled(isActive);
                    
                    btnSaveShift.setVisible(isActive); btnDeleteShift.setVisible(isActive);
                    if(!isActive) JOptionPane.showMessageDialog(EmployeeSchedulePanel.this, "Ca làm việc này đã ngưng sử dụng nên không thể chỉnh sửa.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
                public void mouseEntered(MouseEvent e) {
                    if (selectedCard != ShiftCard.this) setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(isActive ? new Color(46, 139, 87) : Color.GRAY, 2, true), BorderFactory.createEmptyBorder(0, 0, 4, 0)));
                }
                public void mouseExited(MouseEvent e) {
                    if (selectedCard != ShiftCard.this) setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true), BorderFactory.createEmptyBorder(0, 0, 5, 0)));
                }
            });
        }
        public void setHighlight(boolean isHighlighted) {
            if (isHighlighted) setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 2, true), BorderFactory.createEmptyBorder(0, 0, 4, 0)));
            else setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true), BorderFactory.createEmptyBorder(0, 0, 5, 0)));
        }
    }

    // =========================================================================
    // TAB 2: XẾP LỊCH (GỒM TUẦN VÀ THÁNG)
    // =========================================================================
    private JPanel createScheduleWrapperPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        
        JPanel commonToolbar = new JPanel(new GridBagLayout());
        commonToolbar.setBackground(Color.WHITE);
        commonToolbar.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL; gbc.weighty = 1.0;
        
        // GÓC TRÁI
        JPanel leftTool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); leftTool.setBackground(Color.WHITE);
        JButton btnPrev = new JButton("< Trước"); btnPrev.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCurrentRange = new JLabel(); lblCurrentRange.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblCurrentRange.setForeground(new Color(46, 139, 87));
        JButton btnNext = new JButton("Sau >"); btnNext.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        btnPrev.addActionListener(e -> navigate(-1)); btnNext.addActionListener(e -> navigate(1));
        leftTool.add(btnPrev); leftTool.add(lblCurrentRange); leftTool.add(btnNext);
        gbc.gridx = 0; gbc.weightx = 0.3; gbc.anchor = GridBagConstraints.WEST; commonToolbar.add(leftTool, gbc);
        
        // GIỮA
        centerTool = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0)); centerTool.setBackground(Color.WHITE);
        chkRepeat = new JCheckBox("Lặp lại trong"); chkRepeat.setFont(new Font("Segoe UI", Font.PLAIN, 14)); chkRepeat.setBackground(Color.WHITE);
        spnRepeatWeeks = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1)); spnRepeatWeeks.setFont(new Font("Segoe UI", Font.PLAIN, 14)); spnRepeatWeeks.setEnabled(false); 
        chkRepeat.addActionListener(e -> spnRepeatWeeks.setEnabled(chkRepeat.isSelected()));
        btnSaveSchedule = new JButton("Lặp lịch tuần"); btnSaveSchedule.setBackground(new Color(46, 139, 87)); btnSaveSchedule.setForeground(Color.WHITE); btnSaveSchedule.setFont(new Font("Segoe UI", Font.BOLD, 13));
        centerTool.add(chkRepeat); centerTool.add(spnRepeatWeeks); centerTool.add(new JLabel("tuần")); centerTool.add(Box.createHorizontalStrut(10)); centerTool.add(btnSaveSchedule);
        gbc.gridx = 1; gbc.weightx = 0.4; gbc.anchor = GridBagConstraints.CENTER; commonToolbar.add(centerTool, gbc);
        
        // GÓC PHẢI
        JPanel rightTool = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); rightTool.setBackground(Color.WHITE);
        btnToggleView = new JButton("Góc nhìn: Tháng"); btnToggleView.setBackground(new Color(70, 130, 180)); btnToggleView.setForeground(Color.WHITE); btnToggleView.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggleView.addActionListener(e -> toggleViewMode());
        rightTool.add(btnToggleView);
        gbc.gridx = 2; gbc.weightx = 0.3; gbc.anchor = GridBagConstraints.EAST; commonToolbar.add(rightTool, gbc);
        
        scheduleCardLayout = new CardLayout();
        scheduleCardPanel = new JPanel(scheduleCardLayout);
        scheduleCardPanel.add(createWeeklySchedulePanel(), "WEEK_VIEW");
        scheduleCardPanel.add(createMonthlySchedulePanel(), "MONTH_VIEW");
        
        wrapper.add(commonToolbar, BorderLayout.NORTH);
        wrapper.add(scheduleCardPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createWeeklySchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(Color.WHITE); panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        scheduleModel = createNewScheduleModel();
        scheduleTable = new JTable(scheduleModel);
        scheduleTable.setRowHeight(80); scheduleTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13)); scheduleTable.getTableHeader().setBackground(new Color(240, 240, 240));
        scheduleTable.setDefaultRenderer(Object.class, new MultiLineCellRenderer());
        
        scheduleTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { 
                    int r = scheduleTable.rowAtPoint(e.getPoint()), c = scheduleTable.columnAtPoint(e.getPoint());
                    if (c > 0) { 
                        if (currentPointer.plusDays(c - 1).isBefore(LocalDate.now())) {
                            JOptionPane.showMessageDialog(EmployeeSchedulePanel.this, "Không thể thay đổi lịch làm việc của ngày trong quá khứ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return; 
                        }
                        editingRow = r; editingCol = c;
                        showAssignShiftDialog(scheduleTable.getValueAt(r, 0).toString(), scheduleTable.getColumnName(c), r, c);
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(scheduleTable); scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200,200,200))); scrollPane.getViewport().setBackground(Color.WHITE); 
        panel.add(scrollPane, BorderLayout.CENTER); return panel;
    }

    private JPanel createMonthlySchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(Color.WHITE); panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        
        JPanel headerPnl = new JPanel(new GridLayout(1, 7)); headerPnl.setBackground(new Color(240,240,240));
        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        for(String d : days) {
            JLabel l = new JLabel(d, SwingConstants.CENTER); l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setBorder(BorderFactory.createEmptyBorder(10,0,10,0)); headerPnl.add(l);
        }
        
        monthCalendarPanel = new JPanel(new GridLayout(0, 7, 0, 0)); monthCalendarPanel.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(monthCalendarPanel); scroll.setBorder(new LineBorder(new Color(200,200,200))); scroll.getViewport().setBackground(Color.WHITE);
        
        panel.add(headerPnl, BorderLayout.NORTH); panel.add(scroll, BorderLayout.CENTER); return panel;
    }

    public void renderMonthView(Map<LocalDate, List<String>> scheduleByDate) {
        monthCalendarPanel.removeAll();
        YearMonth ym = YearMonth.from(currentPointer);
        LocalDate firstOfMonth = ym.atDay(1);
        int dayOfWeekVal = firstOfMonth.getDayOfWeek().getValue(); 
        int offset = dayOfWeekVal - 1; 

        LocalDate startDateOfGrid = firstOfMonth.minusDays(offset);

        for (int i = 0; i < 42; i++) {
            LocalDate cellDate = startDateOfGrid.plusDays(i);
            boolean isCurrentMonth = cellDate.getMonth().equals(ym.getMonth());
            List<String> info = isCurrentMonth ? scheduleByDate.get(cellDate) : null;
            monthCalendarPanel.add(new DayPanel(cellDate, info, !isCurrentMonth));
        }
        monthCalendarPanel.revalidate(); monthCalendarPanel.repaint();
    }

    private class DayPanel extends JPanel {
        private LocalDate date;
        private Color baseColor = Color.WHITE;

        public DayPanel(LocalDate date, List<String> info, boolean isOtherMonth) {
            this.date = date; setLayout(new BorderLayout());
            setBorder(new LineBorder(new Color(230,230,230), 1));
            
            if (isOtherMonth) {
                baseColor = new Color(248, 248, 248);
                setBackground(baseColor);
                JLabel lblNum = new JLabel(" " + date.getDayOfMonth());
                lblNum.setFont(new Font("Segoe UI", Font.PLAIN, 13)); lblNum.setForeground(new Color(180,180,180));
                add(lblNum, BorderLayout.NORTH);
            } else {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                if (date.isBefore(LocalDate.now())) {
                    baseColor = new Color(235, 235, 235); 
                } else if (date.isEqual(LocalDate.now())) {
                    baseColor = new Color(255, 235, 205); 
                } else {
                    baseColor = (info != null && !info.isEmpty()) ? new Color(220, 240, 255) : Color.WHITE;
                }
                setBackground(baseColor);

                JLabel lblNum = new JLabel(" " + date.getDayOfMonth());
                lblNum.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblNum.setForeground(Color.BLACK); 
                add(lblNum, BorderLayout.NORTH);

                if (info != null && !info.isEmpty()) {
                    JPanel infoPnl = new JPanel(); infoPnl.setLayout(new BoxLayout(infoPnl, BoxLayout.Y_AXIS)); infoPnl.setOpaque(false);
                    infoPnl.setBorder(BorderFactory.createEmptyBorder(2,5,0,0));
                    int count = 0;
                    for(String s : info) {
                        if(count >= 3) {
                            JLabel l = new JLabel("+" + (info.size() - 3) + " ca khác..."); l.setFont(new Font("Segoe UI", Font.ITALIC, 11)); l.setForeground(Color.BLACK);
                            infoPnl.add(l); break;
                        }
                        String plainText = s.replaceAll("<[^>]*>", "");
                        JLabel l = new JLabel("• " + plainText); 
                        l.setFont(new Font("Segoe UI", Font.BOLD, 11)); l.setForeground(Color.BLACK); 
                        infoPnl.add(l); count++;
                    }
                    add(infoPnl, BorderLayout.CENTER);
                    
                    StringBuilder sb = new StringBuilder("<html><body style='padding:5px; width:200px;'><b>Lịch ngày " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</b><br>");
                    for(String s : info) sb.append("• ").append(s).append("<br>");
                    setToolTipText(sb.toString());
                }

                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { 
                        setBackground(baseColor.darker());
                        setBorder(new LineBorder(new Color(46, 139, 87), 2));
                    }
                    public void mouseExited(MouseEvent e) { 
                        setBackground(baseColor);
                        setBorder(new LineBorder(new Color(230,230,230), 1));
                    }
                });
            }
        }
    }

    private void navigate(int val) {
        if (isMonthView) {
            currentPointer = val > 0 ? currentPointer.plusMonths(1) : currentPointer.minusMonths(1);
            currentPointer = currentPointer.withDayOfMonth(1);
        } else {
            currentPointer = val > 0 ? currentPointer.plusWeeks(1) : currentPointer.minusWeeks(1);
        }
        updateDisplayRange();
        fireNavEvent();
    }

    private void toggleViewMode() {
        isMonthView = !isMonthView;
        if (isMonthView) {
            currentPointer = currentPointer.withDayOfMonth(1); 
            btnToggleView.setText("Góc nhìn: Tuần");
            scheduleCardLayout.show(scheduleCardPanel, "MONTH_VIEW");
            centerTool.setVisible(false);
        } else {
            currentPointer = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)); 
            btnToggleView.setText("Góc nhìn: Tháng");
            scheduleCardLayout.show(scheduleCardPanel, "WEEK_VIEW");
            centerTool.setVisible(true);
        }
        updateDisplayRange();
        fireNavEvent();
    }

    private void updateDisplayRange() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (isMonthView) {
            lblCurrentRange.setText("Tháng " + currentPointer.getMonthValue() + " / " + currentPointer.getYear());
        } else {
            LocalDate end = currentPointer.plusDays(6);
            lblCurrentRange.setText(" " + currentPointer.format(dtf) + " - " + end.format(dtf) + " ");
            
            String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
            DateTimeFormatter colFormatter = DateTimeFormatter.ofPattern("dd/MM");
            if (scheduleTable != null && scheduleTable.getColumnModel().getColumnCount() > 0) {
                for (int i = 0; i < 7; i++) scheduleTable.getColumnModel().getColumn(i+1).setHeaderValue(days[i] + " (" + currentPointer.plusDays(i).format(colFormatter) + ")");
                scheduleTable.getTableHeader().repaint();
            }
        }
    }

    class MultiLineCellRenderer extends JTextArea implements javax.swing.table.TableCellRenderer {
        public MultiLineCellRenderer() { setLineWrap(true); setWrapStyleWord(true); setOpaque(true); setBorder(BorderFactory.createEmptyBorder(5,5,5,5)); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(v != null ? v.toString() : "");
            
            setFont(new Font("Segoe UI", Font.BOLD, 13)); 
            setForeground(Color.BLACK);

            if (c == 0) { 
                setBackground(new Color(250,250,250)); 
            } else {
                LocalDate d = currentPointer.plusDays(c-1);
                boolean hasData = (v != null && !v.toString().trim().isEmpty());
                
                if (s) {
                    setBackground(t.getSelectionBackground());
                } else if (d.isBefore(LocalDate.now())) {
                    setBackground(new Color(235, 235, 235)); 
                } else if (d.isEqual(LocalDate.now())) {
                    setBackground(new Color(255, 235, 205));
                } else {
                    if (hasData) {
                        setBackground(new Color(220, 240, 255)); 
                    } else {
                        setBackground(Color.WHITE);
                    }
                }
            }
            return this;
        }
    }

    private DefaultTableModel createNewScheduleModel() {
        String[] cols = {"Nhân Sự", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        return new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    }

    public void loadEmployeesToSchedule(List<AccountModel> list) {
        this.currentAccountsList = list; scheduleModel.setRowCount(0);
        for(AccountModel a : list) scheduleModel.addRow(new Object[]{a.getUsername(), "", "", "", "", "", "", ""});
        fireNavEvent();
    }

    public void applyScheduleData(String[][] data) {
        for(int r=0; r<data.length; r++)
            for(int c=0; c<7; c++) scheduleModel.setValueAt(data[r][c], r, c+1);
    }
    
    public void clearScheduleGrid() {
        for(int r=0; r<scheduleModel.getRowCount(); r++)
            for(int c=1; c<=7; c++) scheduleModel.setValueAt("", r, c);
    }

    private void showAssignShiftDialog(String emp, String day, int r, int c) {
        dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gán ca làm việc", true);
        dialog.setLayout(new BorderLayout(15, 15)); dialog.setSize(380, 300); dialog.setLocationRelativeTo(this);

        JPanel pnlHeader = new JPanel(); pnlHeader.add(new JLabel("<html><center><b>Nhân sự:</b> " + emp + "<br><b>Ngày:</b> " + day + "</center></html>"));
        JPanel pnlChecks = new JPanel(); pnlChecks.setLayout(new BoxLayout(pnlChecks, BoxLayout.Y_AXIS));
        
        checkBoxes = new ArrayList<>(); activeShifts = new ArrayList<>();
        String curVal = scheduleTable.getValueAt(r, c) != null ? scheduleTable.getValueAt(r, c).toString() : "";

        if (availableShifts != null) {
            for (ShiftModel s : availableShifts) {
                if ("Đang sử dụng".equals(s.getTrangThai())) {
                    activeShifts.add(s);
                    JCheckBox chk = new JCheckBox(s.getTenCa() + " (" + s.getGioBatDau() + " - " + s.getGioKetThuc() + ")");
                    if (curVal.contains(s.getTenCa())) chk.setSelected(true);
                    checkBoxes.add(chk); pnlChecks.add(chk);
                }
            }
        }
        
        btnConfirm = new JButton("Xác nhận & Lưu"); btnConfirm.setBackground(new Color(46, 139, 87)); btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> {
            selectedShifts = new ArrayList<>();
            for (int i=0; i<checkBoxes.size(); i++) if(checkBoxes.get(i).isSelected()) selectedShifts.add(activeShifts.get(i));
            if (confirmAssignListener != null) confirmAssignListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CONFIRM"));
        });
        dialog.add(pnlHeader, BorderLayout.NORTH); dialog.add(new JScrollPane(pnlChecks), BorderLayout.CENTER);
        JPanel pnlF = new JPanel(new FlowLayout(FlowLayout.CENTER)); pnlF.add(btnConfirm); dialog.add(pnlF, BorderLayout.SOUTH); dialog.setVisible(true); 
    }
    
    public void setSchedule(int r, int c, List<ShiftModel> list) {
        StringBuilder sb = new StringBuilder();
        for (ShiftModel s : list) sb.append(s.getTenCa()).append("\n");
        scheduleTable.setValueAt(sb.toString().trim(), r, c);
        if (dialog != null) dialog.dispose(); 
    }

    public int getMaCa() { return currentMaCa; }
    public String getTenCa() { return txtTenCa.getText(); }
    
    // [CẬP NHẬT] Đọc giá trị từ JSpinner và format ra String "HH:mm" để tương thích ngược với luồng Data cũ
    public String getGioBatDau() { return new SimpleDateFormat("HH:mm").format(spnGioBatDau.getValue()); }
    public String getGioKetThuc() { return new SimpleDateFormat("HH:mm").format(spnGioKetThuc.getValue()); }
    
    public int getSelectedMaCa() { return currentMaCa; } 
    public boolean isRepeatChecked() { return chkRepeat.isSelected(); }
    public int getRepeatWeeks() { return (Integer) spnRepeatWeeks.getValue(); }
    public List<ShiftModel> getAvailableShifts() { return this.availableShifts; }
    public List<AccountModel> getCurrentAccounts() { return currentAccountsList; }
    public String[][] getCurrentScheduleData() {
        String[][] d = new String[scheduleModel.getRowCount()][7]; 
        for(int i=0; i<d.length; i++) for(int j=0; j<7; j++) d[i][j] = scheduleModel.getValueAt(i, j+1).toString();
        return d;
    }
    public int getEditingRow() { return editingRow; }
    public int getEditingCol() { return editingCol; }
    public List<ShiftModel> getSelectedShifts() { return selectedShifts; }
    public JDialog getDialog() { return dialog; }
    public void addSaveScheduleListener(ActionListener l) { btnSaveSchedule.addActionListener(l); }
    public void addSaveShiftListener(ActionListener l) { btnSaveShift.addActionListener(l); }
    public void addDeleteShiftListener(ActionListener l) { btnDeleteShift.addActionListener(l); }

    public class IOSSwitch extends JComponent {
        private boolean selected = false; private float animation = 0f; private Timer timer;
        private final Color onColor = new Color(52, 199, 89), offColor = new Color(225, 225, 230), thumbColor = Color.WHITE;
        private final EventListenerList listenerList = new EventListenerList();
        public IOSSwitch() {
            setPreferredSize(new Dimension(50, 26)); setMinimumSize(new Dimension(50, 26)); setCursor(new Cursor(Cursor.HAND_CURSOR)); setOpaque(false);
            addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) { setSelected(!selected); } });
            timer = new Timer(5, e -> {
                float t = selected ? 1f : 0f;
                if (animation < t) { animation += 0.1f; if (animation > t) animation = t; }
                else if (animation > t) { animation -= 0.1f; if (animation < t) animation = t; }
                repaint(); if (animation == t) timer.stop();
            });
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int r = (int) (offColor.getRed() + (onColor.getRed() - offColor.getRed()) * animation);
            int gr = (int) (offColor.getGreen() + (onColor.getGreen() - offColor.getGreen()) * animation);
            int b = (int) (offColor.getBlue() + (onColor.getBlue() - offColor.getBlue()) * animation);
            g2.setColor(new Color(r, gr, b)); g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            int m = 2, ts = getHeight() - m * 2; float x = m + (getWidth() - ts - m * 2) * animation;
            g2.setColor(thumbColor); g2.fillOval((int) x, m, ts, ts); g2.dispose();
        }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean s) { if (selected != s) { selected = s; fireActionPerformed(); if (!timer.isRunning()) timer.start(); } }
        public void addActionListener(ActionListener l) { listenerList.add(ActionListener.class, l); }
        private void fireActionPerformed() { ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "t"); for (ActionListener l : listenerList.getListeners(ActionListener.class)) l.actionPerformed(e); }
    }
}