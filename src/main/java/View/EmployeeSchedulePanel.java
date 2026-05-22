package View;

import Controller.EmployeeScheduleController;
import Model.EmployeeScheduleModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeSchedulePanel extends JPanel {
    private EmployeeScheduleController controller;
    private JComboBox<Integer> cboMonth;
    private JComboBox<Integer> cboYear;
    private JPanel calendarPanel;
    private int currentMonth;
    private int currentYear;

    public EmployeeSchedulePanel() {
        controller = new EmployeeScheduleController();
        LocalDate now = LocalDate.now();
        currentMonth = now.getMonthValue();
        currentYear = now.getYear();

        initComponents();
        loadCalendar();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        setBackground(new Color(248, 250, 252)); // Slightly off-white modern background

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("Lịch Làm Việc Nhân Viên");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(30, 41, 59)); // Slate 800
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controlPanel.setBackground(new Color(248, 250, 252));
        
        JLabel lblMonth = new JLabel("Tháng:");
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMonth.setForeground(new Color(71, 85, 105)); // Slate 500
        controlPanel.add(lblMonth);
        
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        cboMonth = new JComboBox<>(months);
        cboMonth.setSelectedItem(currentMonth);
        cboMonth.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Integer) {
                    setText("Tháng " + value);
                }
                return this;
            }
        });
        cboMonth.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboMonth.setBackground(Color.WHITE);
        cboMonth.setFocusable(false);
        cboMonth.addActionListener(e -> updateCalendar());
        controlPanel.add(cboMonth);

        JLabel lblYear = new JLabel("Năm:");
        lblYear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblYear.setForeground(new Color(71, 85, 105));
        controlPanel.add(lblYear);
        
        Integer[] years = new Integer[10];
        for (int i = 0; i < 10; i++) {
            years[i] = currentYear - 2 + i;
        }
        cboYear = new JComboBox<>(years);
        cboYear.setSelectedItem(currentYear);
        cboYear.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboYear.setBackground(Color.WHITE);
        cboYear.setFocusable(false);
        cboYear.addActionListener(e -> updateCalendar());
        controlPanel.add(cboYear);

        
        controlPanel.add(Box.createHorizontalStrut(10));

        headerPanel.add(controlPanel, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(headerPanel, BorderLayout.NORTH);

        // Calendar Panel Wrapper
        JPanel mainCalendarWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mainCalendarWrapper.setOpaque(false);
        mainCalendarWrapper.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        // Days of week header
        JPanel daysHeaderPanel = new JPanel(new GridLayout(1, 7)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(67, 142, 104)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.fillRect(0, 10, getWidth(), getHeight() - 10); // keep top rounded, bottom flat
                g2.dispose();
                super.paintComponent(g);
            }
        };
        daysHeaderPanel.setOpaque(false);
        
        String[] daysOfWeek = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        for (String day : daysOfWeek) {
            JLabel lblDay = new JLabel(day, SwingConstants.CENTER);
            lblDay.setForeground(Color.WHITE);
            lblDay.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblDay.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            daysHeaderPanel.add(lblDay);
        }

        calendarPanel = new JPanel(new GridLayout(0, 7)) {
            @Override
            public Dimension getPreferredSize() {
                return super.getPreferredSize();
            }
        };
        // We must implement Scrollable to force the panel to fit the viewport width
        class ScrollableCalendarPanel extends JPanel implements Scrollable {
            public ScrollableCalendarPanel(LayoutManager layout) {
                super(layout);
            }
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }
            @Override
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 16;
            }
            @Override
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
                return visibleRect.height;
            }
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true; // Force width to match viewport
            }
            @Override
            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
        }
        
        calendarPanel = new ScrollableCalendarPanel(new GridLayout(0, 7));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder());
        calendarPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(calendarPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Put the header inside the scroll pane's column header to ensure perfect alignment
        scrollPane.setColumnHeaderView(daysHeaderPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Fill the upper right corner (above the vertical scrollbar) with the header color
        JPanel cornerPanel = new JPanel();
        cornerPanel.setBackground(new Color(67, 142, 104));
        scrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, cornerPanel);

        mainCalendarWrapper.add(scrollPane, BorderLayout.CENTER);

        add(mainCalendarWrapper, BorderLayout.CENTER);
    }

    private void updateCalendar() {
        currentMonth = (Integer) cboMonth.getSelectedItem();
        currentYear = (Integer) cboYear.getSelectedItem();
        loadCalendar();
    }

    private void loadCalendar() {
        calendarPanel.removeAll();
        
        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        
        List<EmployeeScheduleModel> schedules = controller.getSchedulesByMonthYear(currentMonth, currentYear);

        // Fill empty cells before the 1st of the month
        for (int i = 1; i < dayOfWeekValue; i++) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(new Color(249, 250, 251)); // Gray 50
            emptyPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240))); // Slate 200
            calendarPanel.add(emptyPanel);
        }

        // Fill actual days
        for (int day = 1; day <= daysInMonth; day++) {
            final int currentDay = day;
            java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.of(currentYear, currentMonth, day));
            
            JPanel dayPanel = new JPanel(new BorderLayout());
            dayPanel.setBackground(Color.WHITE);
            dayPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
            
            // Shifts panel
            JPanel shiftsPanel = new JPanel();
            shiftsPanel.setLayout(new BoxLayout(shiftsPanel, BoxLayout.Y_AXIS));
            shiftsPanel.setBackground(Color.WHITE);
            shiftsPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 2, 6));
            
            // Morning Shift
            List<String> morningEmps = schedules.stream()
                .filter(s -> s.getWorkDate().equals(sqlDate) && s.getShiftType().equals("Sáng"))
                .map(EmployeeScheduleModel::getEmployeeName)
                .collect(Collectors.toList());
            
            Color morningBg = new Color(239, 246, 255); // Blue 50
            Color morningHover = new Color(219, 234, 254); // Blue 100
            Color morningTitle = new Color(29, 78, 216); // Blue 700
            JPanel morningPanel = createShiftPanel("Sáng", morningEmps, morningBg, morningTitle);
            morningPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { openAssignDialog(sqlDate, "Sáng"); }
                @Override
                public void mouseEntered(MouseEvent e) { morningPanel.repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { morningPanel.repaint(); }
            });
            shiftsPanel.add(morningPanel);
            shiftsPanel.add(Box.createVerticalStrut(6));
            
            // Afternoon Shift
            List<String> afternoonEmps = schedules.stream()
                .filter(s -> s.getWorkDate().equals(sqlDate) && s.getShiftType().equals("Chiều"))
                .map(EmployeeScheduleModel::getEmployeeName)
                .collect(Collectors.toList());
                
            Color afternoonBg = new Color(255, 247, 237); // Orange 50
            Color afternoonHover = new Color(255, 237, 213); // Orange 100
            Color afternoonTitle = new Color(194, 65, 12); // Orange 700
            JPanel afternoonPanel = createShiftPanel("Chiều", afternoonEmps, afternoonBg, afternoonTitle);
            afternoonPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { openAssignDialog(sqlDate, "Chiều"); }
                @Override
                public void mouseEntered(MouseEvent e) { afternoonPanel.repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { afternoonPanel.repaint(); }
            });
            shiftsPanel.add(afternoonPanel);
            
            dayPanel.add(shiftsPanel, BorderLayout.NORTH);
            
            // Day number label (SOUTH)
            JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
            southPanel.setBackground(Color.WHITE);

            JLabel lblDayNum = new JLabel(String.valueOf(day));
            lblDayNum.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblDayNum.setForeground(new Color(100, 116, 139)); // Slate 500
            
            // Highlight today
            if (sqlDate.toLocalDate().equals(LocalDate.now())) {
                lblDayNum.setForeground(Color.WHITE);
                lblDayNum.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                
                JPanel todayWrapper = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(16, 185, 129)); // Emerald 500
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                todayWrapper.setOpaque(false);
                todayWrapper.add(lblDayNum);
                southPanel.add(todayWrapper);
            } else {
                southPanel.add(lblDayNum);
            }
            
            dayPanel.add(southPanel, BorderLayout.SOUTH);

            calendarPanel.add(dayPanel);
        }

        // Fill empty cells after the last day
        int totalCells = dayOfWeekValue - 1 + daysInMonth;
        int remainingCells = (7 - (totalCells % 7)) % 7;
        for (int i = 0; i < remainingCells; i++) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(new Color(249, 250, 251));
            emptyPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
            calendarPanel.add(emptyPanel);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private JPanel createShiftPanel(String shiftName, List<String> employees, Color bgColor, Color titleColor) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); // Modern rounded corners
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblShiftName = new JLabel(shiftName);
        lblShiftName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblShiftName.setForeground(titleColor);
        lblShiftName.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblShiftName);
        
        if (!employees.isEmpty()) {
            panel.add(Box.createVerticalStrut(4));
        }

        for (String emp : employees) {
            JLabel lblEmp = new JLabel(emp);
            lblEmp.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblEmp.setForeground(new Color(71, 85, 105)); // Slate 500
            lblEmp.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(lblEmp);
        }
        
        return panel;
    }

    private void openAssignDialog(java.sql.Date workDate, String shiftType) {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame frame = (window instanceof Frame) ? (Frame) window : null;
        AssignShiftDialog dialog = new AssignShiftDialog(frame, workDate, shiftType);
        dialog.setVisible(true);
        // Refresh calendar after dialog closes
        loadCalendar();
    }
}
