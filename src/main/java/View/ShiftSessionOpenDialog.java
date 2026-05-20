package View;

import Model.SessionManager;
import Model.ShiftModel;
import Model.ShiftSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.List;

public class ShiftSessionOpenDialog extends JDialog {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color BORDER_COLOR = new Color(200, 200, 200);
    private final Color BG_LIGHT = new Color(248, 249, 250);

    private JComboBox<String> cbCaLamViec;
    private JLabel lblThoiGian;
    private JCheckBox chkLayBanGiao;
    private JComboBox<String> cbNguoiBanGiao;
    private JTextField txtTienMatDauCa;
    private JTextField txtHoaDonChuaThanhToan;
    private JTable tblTonKho;
    private JButton btnConfirm;
    private JButton btnCancel;
    
    private boolean isConfirmed = false;
    private double tienMatDauCa = 0.0;
    private ShiftSession shiftSessionModel;
    
    private List<ShiftModel> activeShifts;
    private Object[] handoverInfo;
    private int unpaidCount;
    private List<Object[]> inventory;

    private int pX, pY;

    public ShiftSessionOpenDialog(Frame parent, ShiftSession shiftSessionModel, List<ShiftModel> activeShifts, Object[] handoverInfo, int unpaidCount, List<Object[]> inventory) {
        super(parent, "Mở ca làm việc", true);
        this.shiftSessionModel = shiftSessionModel;
        this.activeShifts = activeShifts;
        this.handoverInfo = handoverInfo;
        this.unpaidCount = unpaidCount;
        this.inventory = inventory;
        
        initComponents();
        setupEvents();
        loadInitialData();
    }

    private void initComponents() {
        setUndecorated(true);
        setSize(850, 720); 
        setLocationRelativeTo(getParent());

        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));

        // HEADER (Draggable)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 50));
        JLabel lblTitle = new JLabel("Mở ca làm việc");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        formPanel.add(headerPanel, BorderLayout.NORTH);

        headerPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) { pX = evt.getX(); pY = evt.getY(); }
        });
        headerPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent evt) { setLocation(getLocation().x + evt.getX() - pX, getLocation().y + evt.getY() - pY); }
        });

        // CONTENT
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        contentPanel.add(createLabel("Chọn ca làm việc"));
        cbCaLamViec = new JComboBox<>();
        cbCaLamViec.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbCaLamViec.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        contentPanel.add(cbCaLamViec);
        
        lblThoiGian = new JLabel("Từ: --:--   -  Đến: --:--");
        lblThoiGian.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblThoiGian.setForeground(Color.GRAY);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(lblThoiGian);
        contentPanel.add(Box.createVerticalStrut(15));

        chkLayBanGiao = new JCheckBox("Lấy từ sổ bàn giao ca trước");
        chkLayBanGiao.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chkLayBanGiao.setBackground(Color.WHITE);
        contentPanel.add(chkLayBanGiao);
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createLabel("Người bàn giao"));
        cbNguoiBanGiao = new JComboBox<>();
        cbNguoiBanGiao.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbNguoiBanGiao.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        cbNguoiBanGiao.setEnabled(false); 
        contentPanel.add(cbNguoiBanGiao);
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createLabel("Đầu ca két tiền: (Tiền mặt)"));
        txtTienMatDauCa = new JTextField("0");
        txtTienMatDauCa.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtTienMatDauCa.setForeground(PRIMARY_COLOR);
        txtTienMatDauCa.setMaximumSize(new Dimension(Short.MAX_VALUE, 45));
        txtTienMatDauCa.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(5, 10, 5, 10)));
        contentPanel.add(txtTienMatDauCa);
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createLabel("Hoá đơn chưa thanh toán (Từ ca trước)"));
        txtHoaDonChuaThanhToan = new JTextField("0");
        txtHoaDonChuaThanhToan.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtHoaDonChuaThanhToan.setEditable(false);
        txtHoaDonChuaThanhToan.setBackground(BG_LIGHT);
        txtHoaDonChuaThanhToan.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        txtHoaDonChuaThanhToan.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(5, 10, 5, 10)));
        contentPanel.add(txtHoaDonChuaThanhToan);
        contentPanel.add(Box.createVerticalStrut(20));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER_COLOR), "Quản lý tồn kho đầu ca", TitledBorder.CENTER, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), TEXT_DARK));

        String[] cols = {"Tên sản phẩm / Nguyên liệu", "Tồn thực tế"};
        DefaultTableModel tblModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int column) { return column == 1; }
        };
        tblTonKho = new JTable(tblModel);
        tblTonKho.setRowHeight(35);
        tblTonKho.setGridColor(BORDER_COLOR);
        JScrollPane scrollTable = new JScrollPane(tblTonKho);
        scrollTable.setPreferredSize(new Dimension(0, 150));
        tablePanel.add(scrollTable, BorderLayout.CENTER);
        contentPanel.add(tablePanel);

        formPanel.add(contentPanel, BorderLayout.CENTER);

        // BUTTONS
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        buttonPanel.setPreferredSize(new Dimension(getWidth(), 55));
        btnCancel = createButton("Huỷ bỏ", Color.LIGHT_GRAY, TEXT_DARK);
        btnConfirm = createButton("Mở ca", PRIMARY_COLOR, Color.WHITE);
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnConfirm);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(formPanel);
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    private void loadInitialData() {
        cbCaLamViec.removeAllItems();
        if (activeShifts != null) {
            for (ShiftModel shift : activeShifts) cbCaLamViec.addItem(shift.getTenCa());
        }
        if (handoverInfo != null) cbNguoiBanGiao.addItem(handoverInfo[0].toString() + " (Ca trước)");
        txtHoaDonChuaThanhToan.setText(String.valueOf(unpaidCount));
        DefaultTableModel model = (DefaultTableModel) tblTonKho.getModel();
        if (inventory != null) {
            for (Object[] row : inventory) model.addRow(new Object[]{row[0], row[1] + " " + row[2]});
        }
    }

    private void setupEvents() {
        btnCancel.addActionListener(e -> { isConfirmed = false; dispose(); });
        btnConfirm.addActionListener(e -> {
            try {
                tienMatDauCa = Double.parseDouble(txtTienMatDauCa.getText().replaceAll("[,.]", ""));
                isConfirmed = true;
                dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!"); }
        });
        chkLayBanGiao.addActionListener(e -> {
            cbNguoiBanGiao.setEnabled(chkLayBanGiao.isSelected());
            if (chkLayBanGiao.isSelected() && handoverInfo != null) {
                txtTienMatDauCa.setText(new DecimalFormat("#,###").format((Double) handoverInfo[1]));
            }
        });
    }

    public boolean isConfirmed() { return isConfirmed; }
    public double getTienMatDauCa() { return tienMatDauCa; }
    public ShiftSession getShiftSessionModel() { return shiftSessionModel; }
}