package View;

import static Common.ComponentUI.createModernButton;
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
import java.util.List;
import java.util.Map;

public class EmployeeSchedulePanel extends JPanel {
    private final Color PRIMARY_COLOR = new Color(67, 142, 104); 
    private final Color LIGHT_PRIMARY_COLOR = new Color(161, 198, 179); 
    
    private JComboBox<String> cbMonth;
    private JSpinner spYear;
    private JPanel calendarContainer;
    
    // --- KHAI BÁO CÁC INTERFACE LẮNG NGHE SỰ KIỆN CHO CONTROLLER ---
    private AddShiftListener addShiftListener;
    private FilterChangeListener filterChangeListener;
    private ShiftActionUpdateListener shiftActionUpdateListener;
    
    public interface AddShiftListener {
        void onAddShift(LocalDate date, String shiftType);
    }
   
    public interface FilterChangeListener {
        void onFilterChanged(int year, int month);
    }

    public void setAddShiftListener(AddShiftListener listener) {
        this.addShiftListener = listener;
    }

    public void setFilterChangeListener(FilterChangeListener listener) {
        this.filterChangeListener = listener;
    }
    
    public interface ShiftActionUpdateListener {
        void onEditShift(int maCa, int currentMaTaiKhoan, LocalDate date, String shiftType);
        void onDeleteShift(int maCa);
        void onAddMoreEmployee(LocalDate date, String shiftType); 
    }

    public void setShiftActionUpdateListener(ShiftActionUpdateListener listener) {
        this.shiftActionUpdateListener = listener;
    }
    // ---------------------------------------------------------------

    public EmployeeSchedulePanel() {
        initComponents();
        // Không tự động render ở đây nữa, Controller sẽ gọi renderCalendar() khi khởi tạo
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColor.BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("Lịch Làm Việc Nhân Viên");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(AppColor.TEXT_DARK);
        
        JLabel noteLabel = new JLabel("(Sáng: 08:00 - 12:00 | Chiều: 13:00 - 17:00)");
        noteLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        noteLabel.setForeground(new Color(150, 150, 150)); 

        titleContainer.add(titleLabel);
        titleContainer.add(noteLabel);
        topPanel.add(titleContainer, BorderLayout.WEST);

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
        // Báo cho Controller khi đổi Tháng
        cbMonth.addActionListener(e -> {
            if (filterChangeListener != null) {
                filterChangeListener.onFilterChanged((Integer) spYear.getValue(), cbMonth.getSelectedIndex() + 1);
            }
        });
        filterPanel.add(cbMonth);

        JLabel lblYear = new JLabel("Năm:");
        lblYear.setFont(new Font("SansSerif", Font.PLAIN, 14));
        filterPanel.add(lblYear);
        
        spYear = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2050, 1));
        spYear.setEditor(new JSpinner.NumberEditor(spYear, "#"));
        spYear.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor)spYear.getEditor();
        spinnerEditor.getTextField().setBackground(Color.WHITE);
        // Báo cho Controller khi đổi Năm
        spYear.addChangeListener(e -> {
            if (filterChangeListener != null) {
                filterChangeListener.onFilterChanged((Integer) spYear.getValue(), cbMonth.getSelectedIndex() + 1);
            }
        });
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

    // --- HÀM NÀY ĐƯỢC CONTROLLER GỌI ĐỂ VẼ LỊCH VỚI DỮ LIỆU ĐÃ XỬ LÝ ---
    public void renderCalendar(int year, int month, Map<LocalDate, DaySchedule> data) {
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
        gridPanel.setBackground(AppColor.LINE_LIGHT); 

        YearMonth ym = YearMonth.of(year, month);
        int daysInMonth = ym.lengthOfMonth();
        DayOfWeek firstDay = ym.atDay(1).getDayOfWeek();
        int startOffset = firstDay.getValue() - 1; 

        for (int i = 0; i < startOffset; i++) {
            JPanel empty = new JPanel();
            empty.setBackground(AppColor.BG_LIGHT);
            gridPanel.add(empty);
        }

        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = ym.atDay(i);
            DayCellPanel cell = new DayCellPanel(date, data.get(date));
            gridPanel.add(cell);
        }

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
        public int maCa;           // Thêm mã ca
        public int maTaiKhoan;     // Thêm mã tài khoản
        public String employeeName;
        public double hours;

        public ShiftDetail(int maCa, int maTaiKhoan, String name, double h) {
            this.maCa = maCa;
            this.maTaiKhoan = maTaiKhoan;
            this.employeeName = name;
            this.hours = h;
        }
    }

    public static class DaySchedule {
        public List<ShiftDetail> morningShifts = new ArrayList<>();
        public List<ShiftDetail> afternoonShifts = new ArrayList<>();
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
                lblDate.setForeground(Color.WHITE);
                lblDate.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8)); 
            } else {
                lblDate.setForeground(AppColor.TEXT_DARK);
                lblDate.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            }
            headerPanel.add(lblDate);
            add(headerPanel, BorderLayout.NORTH);
            
            // Container ca làm việc (GridLayout chia 2 nửa đều nhau)
            JPanel shiftsPanel = new JPanel();
            shiftsPanel.setLayout(new GridLayout(2, 1, 0, 4));
            shiftsPanel.setOpaque(false);
            shiftsPanel.setBorder(new EmptyBorder(2, 4, 4, 4));

            Color bgMorning = new Color(225, 240, 255);
            Color fgMorning = new Color(0, 80, 160);
            Color bgAfternoon = new Color(255, 245, 225);
            Color fgAfternoon = new Color(180, 100, 0);
            
            // --- XỬ LÝ KHUNG CA SÁNG ---
            if (schedule != null && !schedule.morningShifts.isEmpty()) {
                shiftsPanel.add(createShiftPanel("Sáng", schedule.morningShifts, bgMorning, fgMorning, date));
            } else {
                JButton btnAddMorning = createModernButton("+ Thêm Ca Sáng", bgMorning, fgMorning);
                btnAddMorning.setFont(new Font("SansSerif", Font.PLAIN, 11));
                btnAddMorning.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnAddMorning.addActionListener(e -> {
                    if (addShiftListener != null) addShiftListener.onAddShift(date, "Sáng");
                });
                shiftsPanel.add(btnAddMorning);
            }
            
            // --- XỬ LÝ KHUNG CA CHIỀU ---
            if (schedule != null && !schedule.afternoonShifts.isEmpty()) {
                shiftsPanel.add(createShiftPanel("Chiều", schedule.afternoonShifts, bgAfternoon, fgAfternoon, date));
            } else {
                JButton btnAddAfternoon = createModernButton("+ Thêm Ca Chiều", bgAfternoon, fgAfternoon);
                btnAddAfternoon.setFont(new Font("SansSerif", Font.PLAIN, 11));
                btnAddAfternoon.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnAddAfternoon.addActionListener(e -> {
                    if (addShiftListener != null) addShiftListener.onAddShift(date, "Chiều");
                });
                shiftsPanel.add(btnAddAfternoon);
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
                names.append("<span style='color: ").append(toHex(AppColor.TEXT_DARK)).append("'>• ").append(shifts.get(i).employeeName).append("</span>");
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
            table.setRowHeight(35); 
            table.setFont(new Font("SansSerif", Font.PLAIN, 14));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho chọn 1 dòng
            
            JTableHeader header = table.getTableHeader();
            header.setFont(new Font("SansSerif", Font.BOLD, 14));
            header.setBackground(AppColor.BG_LIGHT);
            header.setOpaque(true);
            
            JScrollPane scrollSpace = new JScrollPane(table);
            scrollSpace.setBorder(BorderFactory.createLineBorder(AppColor.BORDER));
            scrollSpace.getViewport().setBackground(Color.WHITE);
            dialog.add(scrollSpace, BorderLayout.CENTER);

           // --- THÊM PHẦN NÚT BẤM DƯỚI CÙNG ---
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            bottomPanel.setBackground(Color.WHITE);

            JButton btnAddMore = new JButton("+ Thêm NV");
            btnAddMore.setBackground(new Color(40, 167, 69)); // Xanh lá
            btnAddMore.setForeground(Color.WHITE);
            btnAddMore.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JButton btnEdit = new JButton("Sửa");
            btnEdit.setBackground(new Color(255, 193, 7)); // Vàng
            btnEdit.setForeground(Color.BLACK);
            btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnEdit.setEnabled(false);

            JButton btnDelete = new JButton("Xóa");
            btnDelete.setBackground(new Color(220, 53, 69)); // Đỏ
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDelete.setEnabled(false);

            JButton btnClose = new JButton("Đóng");
            btnClose.setBackground(AppColor.TEXT_MUTED);
            btnClose.setForeground(Color.WHITE);
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.addActionListener(e -> dialog.dispose());

            // Mở khóa Sửa/Xóa khi click vào bảng
            table.getSelectionModel().addListSelectionListener(e -> {
                boolean hasSelection = table.getSelectedRow() != -1;
                btnEdit.setEnabled(hasSelection);
                btnDelete.setEnabled(hasSelection);
            });

            // Sự kiện Xóa
            btnDelete.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0 && shiftActionUpdateListener != null) {
                    dialog.dispose(); 
                    shiftActionUpdateListener.onDeleteShift(shifts.get(row).maCa);
                }
            });

            // Sự kiện Sửa
            btnEdit.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0 && shiftActionUpdateListener != null) {
                    ShiftDetail sd = shifts.get(row);
                    dialog.dispose(); 
                    shiftActionUpdateListener.onEditShift(sd.maCa, sd.maTaiKhoan, date, shiftName);
                }
            });

            // Sự kiện Thêm NV mới vào ca này
            btnAddMore.addActionListener(e -> {
                dialog.dispose(); 
                if (shiftActionUpdateListener != null) {
                    shiftActionUpdateListener.onAddMoreEmployee(date, shiftName);
                }
            });

            bottomPanel.add(btnAddMore);
            bottomPanel.add(btnDelete);
            bottomPanel.add(btnEdit);
            bottomPanel.add(btnClose);
            
            dialog.add(bottomPanel, BorderLayout.SOUTH);
            dialog.setSize(550, 400); 
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        }
    }
}