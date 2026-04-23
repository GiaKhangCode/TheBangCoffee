package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EmployeeSchedulePanel extends JPanel {
    private JComboBox<String> cbMonth;
    private JSpinner spYear;
    private JPanel calendarContainer;

    public EmployeeSchedulePanel() {
        initComponents();
        updateCalendar();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Lịch Làm Việc Nhân Viên");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(AppColor.TEXT_DARK);
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Bộ lọc tháng năm
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblMonth = new JLabel("Tháng:");
        lblMonth.setFont(new Font("SansSerif", Font.PLAIN, 14));
        filterPanel.add(lblMonth);

        String[] months = new String[12];
        for (int i = 0; i < 12; i++) months[i] = "Tháng " + (i + 1);
        cbMonth = new JComboBox<>(months);
        cbMonth.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        cbMonth.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cbMonth.setBackground(Color.WHITE);
        cbMonth.addActionListener(e -> updateCalendar());
        filterPanel.add(cbMonth);

        JLabel lblYear = new JLabel("Năm:");
        lblYear.setFont(new Font("SansSerif", Font.PLAIN, 14));
        filterPanel.add(lblYear);

        spYear = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2050, 1));
        spYear.setEditor(new JSpinner.NumberEditor(spYear, "#"));
        spYear.setFont(new Font("SansSerif", Font.PLAIN, 14));
        // Đặt màu nền cho bộ quay năm
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor)spYear.getEditor();
        spinnerEditor.getTextField().setBackground(Color.WHITE);
        spYear.addChangeListener(e -> updateCalendar());
        filterPanel.add(spYear);

        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Vùng chứa Lịch
        calendarContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }

            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape clip = new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16);
                g2.clip(clip);
                super.paintChildren(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColor.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        calendarContainer.setOpaque(false);
        calendarContainer.setBackground(Color.WHITE);
        calendarContainer.setBorder(new EmptyBorder(1, 1, 1, 1));

        // Tiêu đề các thứ trong tuần
        JPanel dowPanel = new JPanel(new GridLayout(1, 7));
        dowPanel.setBackground(AppColor.PRIMARY);
        String[] dows = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        for (String dow : dows) {
            JLabel lbl = new JLabel(dow, SwingConstants.CENTER);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
            lbl.setBorder(new EmptyBorder(12, 0, 12, 0));
            dowPanel.add(lbl);
        }
        calendarContainer.add(dowPanel, BorderLayout.NORTH);

        add(calendarContainer, BorderLayout.CENTER);
    }

    private void updateCalendar() {
        if (cbMonth == null || spYear == null) return;
        
        int year = (Integer) spYear.getValue();
        int month = cbMonth.getSelectedIndex() + 1;

        Map<LocalDate, DaySchedule> data = generateDummyData(year, month);

        JPanel gridPanel = new JPanel(new GridLayout(0, 7, 1, 1)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension pref = super.getPreferredSize();
                if (getParent() instanceof JViewport) {
                    pref.height = Math.max(pref.height, ((JViewport)getParent()).getHeight());
                }
                return pref;
            }
        };
        gridPanel.setBackground(AppColor.LINE_LIGHT); // Tạo viền bằng khoảng cách nền

        YearMonth ym = YearMonth.of(year, month);
        int daysInMonth = ym.lengthOfMonth();
        DayOfWeek firstDay = ym.atDay(1).getDayOfWeek();
        int startOffset = firstDay.getValue() - 1; // Thứ 2 = 0

        // Các ô trống của tháng trước
        for (int i = 0; i < startOffset; i++) {
            JPanel empty = new JPanel();
            empty.setBackground(AppColor.BG_LIGHT);
            gridPanel.add(empty);
        }

        // Các ngày trong tháng
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = ym.atDay(i);
            DayCellPanel cell = new DayCellPanel(date, data.get(date));
            gridPanel.add(cell);
        }

        // Các ô trống của tháng sau
        int remaining = (startOffset + daysInMonth) % 7;
        if (remaining > 0) {
            for (int i = remaining; i < 7; i++) {
                JPanel empty = new JPanel();
                empty.setBackground(AppColor.BG_LIGHT);
                gridPanel.add(empty);
            }
        }

        BorderLayout layout = (BorderLayout) calendarContainer.getLayout();
        Component center = layout.getLayoutComponent(BorderLayout.CENTER);
        if (center != null) {
            calendarContainer.remove(center);
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        calendarContainer.add(scrollPane, BorderLayout.CENTER);
        calendarContainer.revalidate();
        calendarContainer.repaint();
    }

    // -- DATA CLASSES --
    public static class ShiftDetail {
        String employeeName;
        double hours;

        public ShiftDetail(String name, double h) {
            this.employeeName = name;
            this.hours = h;
        }
    }

    public static class DaySchedule {
        List<ShiftDetail> morningShifts = new ArrayList<>();
        List<ShiftDetail> afternoonShifts = new ArrayList<>();
    }

    // TẠO DỮ LIỆU MẪU ĐỂ CHẠY THỬ
    private Map<LocalDate, DaySchedule> generateDummyData(int year, int month) {
        Map<LocalDate, DaySchedule> data = new HashMap<>();
        YearMonth ym = YearMonth.of(year, month);
        // Sử dụng seed để dữ liệu không bị nhảy lung tung mỗi khi refresh lại tháng
        Random r = new Random(year * 100 + month); 
        String[] employees = {"Nguyễn Văn A", "Trần Thị Cẩm", "Lê Phương C", "Phạm Quốc Dũng", "Hoàng Văn Tiến", "Vũ Thị Nhi"};

        for (int i = 1; i <= ym.lengthOfMonth(); i++) {
            LocalDate date = ym.atDay(i);
            
            // Random cho hiển thị khoảng 80% có ca làm việc
            if (r.nextDouble() > 0.2) { 
                DaySchedule ds = new DaySchedule();
                
                // Ca sáng
                int numMorning = 1 + r.nextInt(2); // 1-2 NV
                for (int m = 0; m < numMorning; m++) {
                    ds.morningShifts.add(new ShiftDetail(employees[r.nextInt(employees.length)], 4.0 + r.nextInt(2)));
                }

                // Ca chiều
                int numAfternoon = 1 + r.nextInt(2); // 1-2 NV
                for (int a = 0; a < numAfternoon; a++) {
                    ds.afternoonShifts.add(new ShiftDetail(employees[r.nextInt(employees.length)], 4.0 + r.nextInt(2)));
                }
                data.put(date, ds);
            }
        }
        return data;
    }

    // -- INNER UI CLASSES --
    class DayCellPanel extends JPanel {
        public DayCellPanel(LocalDate date, DaySchedule schedule) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);

            // Ngày
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            headerPanel.setOpaque(false);
            headerPanel.setBorder(new EmptyBorder(3, 3, 0, 3));
            
            JLabel lblDate = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (date.equals(LocalDate.now())) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(AppColor.ACCENT_GREEN);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            lblDate.setFont(new Font("SansSerif", Font.BOLD, 14));
            
            if (date.equals(LocalDate.now())) {
                // Highlight hôm nay
                lblDate.setForeground(Color.WHITE);
                lblDate.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8)); // padding cho border box
            } else {
                lblDate.setForeground(AppColor.TEXT_DARK);
                lblDate.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            }
            
            headerPanel.add(lblDate);
            add(headerPanel, BorderLayout.NORTH);

            // Container ca làm việc
            JPanel shiftsPanel = new JPanel();
            shiftsPanel.setLayout(new BoxLayout(shiftsPanel, BoxLayout.Y_AXIS));
            shiftsPanel.setOpaque(false);
            shiftsPanel.setBorder(new EmptyBorder(2, 2, 2, 2));

            if (schedule != null) {
                if (!schedule.morningShifts.isEmpty()) {
                    // Ca Sáng: nền xanh dương nhẹ
                    Color bgMorning = new Color(225, 240, 255);
                    Color fgMorning = new Color(0, 80, 160);
                    shiftsPanel.add(createShiftPanel("Sáng", schedule.morningShifts, bgMorning, fgMorning, date));
                    shiftsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
                }
                if (!schedule.afternoonShifts.isEmpty()) {
                    // Ca Chiều: nền cam nhạt
                    Color bgAfternoon = new Color(255, 245, 225);
                    Color fgAfternoon = new Color(180, 100, 0);
                    shiftsPanel.add(createShiftPanel("Chiều", schedule.afternoonShifts, bgAfternoon, fgAfternoon, date));
                }
            }

            add(shiftsPanel, BorderLayout.CENTER);
        }

        private JPanel createShiftPanel(String title, List<ShiftDetail> shifts, Color bgColor, Color fgColor, LocalDate date) {
            JPanel p = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bgColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(3, 4, 3, 4));
            p.setToolTipText("Nhấp để xem chi tiết ca " + title.toLowerCase());

            StringBuilder names = new StringBuilder("<html><b style='color: rgb(" + fgColor.getRed() + "," + fgColor.getGreen() + "," + fgColor.getBlue() + ")'>" + title + ":</b><br/>");
            for (int i = 0; i < shifts.size(); i++) {
                names.append("<span style='color: " + toHex(AppColor.TEXT_DARK) + "'>• ").append(shifts.get(i).employeeName).append("</span>");
                if (i < shifts.size() - 1) names.append("<br/>");
            }
            names.append("</html>");

            JLabel lbl = new JLabel(names.toString());
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            p.add(lbl, BorderLayout.CENTER);

            p.setCursor(new Cursor(Cursor.HAND_CURSOR));
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showDetailDialog(title, date, shifts);
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                     p.setBorder(BorderFactory.createCompoundBorder(
                         BorderFactory.createLineBorder(fgColor, 1), 
                         new EmptyBorder(2, 3, 2, 3)
                     ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                     p.setBorder(new EmptyBorder(3, 4, 3, 4));
                }
            });

            return p;
        }

        // Helper method cho màu html
        private String toHex(Color c) {
            return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
        }

        private void showDetailDialog(String shiftName, LocalDate date, List<ShiftDetail> shifts) {
            Window parent = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parent instanceof Frame ? (Frame) parent : null, "Chi tiết " + shiftName + " - " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.getContentPane().setBackground(Color.WHITE);
            dialog.getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15));

            JLabel titleLabel = new JLabel("Chi tiết thời gian làm việc ca " + shiftName + " ngày " + date.format(DateTimeFormatter.ofPattern("dd/MM")));
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            titleLabel.setForeground(AppColor.TEXT_DARK);
            titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
            dialog.add(titleLabel, BorderLayout.NORTH);

            String[] columns = {"Tên nhân viên", "Số giờ làm thực tế"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            for (ShiftDetail sd : shifts) {
                model.addRow(new Object[]{sd.employeeName, sd.hours + " giờ"});
            }

            JTable table = new JTable(model);
            table.setRowHeight(35); // Tăng chiều cao để đọc dễ hơn
            table.setFont(new Font("SansSerif", Font.PLAIN, 14));
            
            JTableHeader header = table.getTableHeader();
            header.setFont(new Font("SansSerif", Font.BOLD, 14));
            header.setBackground(AppColor.BG_LIGHT);
            header.setOpaque(true);
            
            JScrollPane scrollSpace = new JScrollPane(table);
            scrollSpace.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
            scrollSpace.getViewport().setBackground(Color.WHITE);
            dialog.add(scrollSpace, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(Color.WHITE);
            JButton btnClose = new JButton("Đóng") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btnClose.setContentAreaFilled(false);
            btnClose.setFont(new Font("SansSerif", Font.BOLD, 14));
            btnClose.setBackground(AppColor.TEXT_MUTED);
            btnClose.setForeground(Color.WHITE);
            btnClose.setFocusPainted(false);
            btnClose.setBorder(new EmptyBorder(8, 20, 8, 20));
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.addActionListener(e -> dialog.dispose());
            bottomPanel.add(btnClose);
            
            dialog.add(bottomPanel, BorderLayout.SOUTH);

            dialog.setSize(500, 350);
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        }
    }

    // HÀM MAIN ĐỂ CHẠY THỬ ĐỘC LẬP THEO YÊU CẦU
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Giao Diện Quản Lý Nhân Viên - Lịch Làm Việc");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 750);
            frame.setLocationRelativeTo(null);
            
            frame.add(new EmployeeSchedulePanel());
            frame.setVisible(true);
        });
    }
}
