package View;

import Model.AccountModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.List;

public class ShiftSessionCloseDialog extends JDialog {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104); // Đổi sang Xanh Lá
    private final Color ORANGE_COLOR = new Color(255, 140, 0); 
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color BORDER_COLOR = new Color(230, 230, 230);
    private final Color BG_LIGHT = new Color(248, 249, 250);

    private JTextField txtTienMatThucTe;
    private JLabel lblChenhLechVal, lblTienBanGiaoVal;
    private JComboBox<String> cbNhanBanGiao;
    private JTextArea txtGhiChu;
    private JButton btnCancel, btnCloseShift, btnCloseAndPrint;
    
    private boolean isConfirmed = false;
    private double tienMatThucTe = 0.0, tienHeThong;
    private String ghiChu = "";
    private Integer maTaiKhoanNhan = null;
    private List<AccountModel> accounts;
    private DecimalFormat formatter = new DecimalFormat("#,###");
    private int pX, pY;

    public ShiftSessionCloseDialog(Frame parent, double tienDauCa, double doanhThu, int hdThanhToan, int hdChuaThanhToan, List<AccountModel> accounts) {
        super(parent, "Đóng ca làm việc", true);
        this.tienHeThong = tienDauCa + doanhThu;
        this.accounts = accounts;
        initComponents(tienDauCa, doanhThu, hdThanhToan, hdChuaThanhToan);
        setupEvents();
    }

    private void initComponents(double tienDauCa, double doanhThu, int hdThanhToan, int hdChuaThanhToan) {
        setUndecorated(true);
        setSize(850, 750); // Tăng chiều cao để không bị cuộn
        setLocationRelativeTo(getParent());

        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));

        // HEADER (Xanh lá)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 50));
        JLabel lblTitle = new JLabel("Đóng ca làm việc");
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
        contentPanel.setBorder(new EmptyBorder(10, 30, 10, 30));

        contentPanel.add(createSummaryRow("Đầu ca két tiền", formatter.format(tienDauCa) + "đ", false));
        contentPanel.add(createSummaryRow("Bán hàng", formatter.format(doanhThu) + "đ", false));
        contentPanel.add(createSummaryRow("Thu khác", "0đ", false));
        contentPanel.add(createSummaryRow("Chi khác", "0đ", false));
        contentPanel.add(createSummaryRow("Cuối ca (Hệ thống)", formatter.format(tienHeThong) + "đ", true));

        // Nhập thực tế
        JPanel pnlInput = new JPanel(new BorderLayout());
        pnlInput.setBackground(Color.WHITE);
        pnlInput.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0, BORDER_COLOR), new EmptyBorder(10,5,10,5)));
        JLabel lblT = new JLabel("<html><b>Thực tế trong két (Tiền mặt)</b></html>");
        txtTienMatThucTe = new JTextField("0");
        txtTienMatThucTe.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtTienMatThucTe.setHorizontalAlignment(JTextField.RIGHT);
        txtTienMatThucTe.setPreferredSize(new Dimension(250, 40));
        pnlInput.add(lblT, BorderLayout.WEST);
        pnlInput.add(txtTienMatThucTe, BorderLayout.EAST);
        contentPanel.add(pnlInput);

        JPanel rowChenhLech = createSummaryRow("Chênh lệch", "- " + formatter.format(tienHeThong) + "đ", true);
        lblChenhLechVal = (JLabel) ((BorderLayout)rowChenhLech.getLayout()).getLayoutComponent(BorderLayout.EAST);
        contentPanel.add(rowChenhLech);

        JPanel rowBanGiao = createSummaryRow("Tiền bàn giao", "0đ", true);
        lblTienBanGiaoVal = (JLabel) ((BorderLayout)rowBanGiao.getLayout()).getLayoutComponent(BorderLayout.EAST);
        contentPanel.add(rowBanGiao);

        // Grid 3 cột hóa đơn
        JPanel gridHD = new JPanel(new GridLayout(1, 3, 15, 0));
        gridHD.setBackground(Color.WHITE);
        gridHD.setBorder(new EmptyBorder(15, 0, 15, 0));
        gridHD.add(createColumnBox("Đã thanh toán", String.valueOf(hdThanhToan)));
        gridHD.add(createColumnBox("Chưa thanh toán", String.valueOf(hdChuaThanhToan)));
        gridHD.add(createColumnBox("Hóa đơn nợ", "0"));
        contentPanel.add(gridHD);

        // Nhận bàn giao (Map Data)
        contentPanel.add(new JLabel("<html><b>Nhận bàn giao ca sau:</b></html>"));
        cbNhanBanGiao = new JComboBox<>();
        cbNhanBanGiao.addItem("--- Chọn nhân viên nhận ca ---");
        if(accounts != null) for(AccountModel a : accounts) cbNhanBanGiao.addItem(a.getFullName());
        cbNhanBanGiao.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        contentPanel.add(cbNhanBanGiao);
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(new JLabel("<html><b>Ghi chú:</b></html>"));
        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        contentPanel.add(new JScrollPane(txtGhiChu));

        formPanel.add(contentPanel, BorderLayout.CENTER);

        // BUTTONS
        JPanel btnPnl = new JPanel(new GridLayout(1, 3, 10, 0));
        btnPnl.setBorder(new EmptyBorder(15, 20, 20, 20));
        btnCancel = createBtn("Huỷ bỏ", Color.LIGHT_GRAY, TEXT_DARK);
        btnCloseShift = createBtn("Đóng ca", PRIMARY_COLOR, Color.WHITE);
        btnCloseAndPrint = createBtn("Đóng ca và in", ORANGE_COLOR, Color.WHITE);
        btnPnl.add(btnCancel); btnPnl.add(btnCloseShift); btnPnl.add(btnCloseAndPrint);
        formPanel.add(btnPnl, BorderLayout.SOUTH);

        setContentPane(formPanel);
    }

    private JPanel createSummaryRow(String t, String v, boolean b) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0, BORDER_COLOR), new EmptyBorder(12,5,12,5)));
        JLabel lT = new JLabel(t); JLabel lV = new JLabel(v);
        Font f = new Font("Segoe UI", b ? Font.BOLD : Font.PLAIN, 15);
        lT.setFont(f); lV.setFont(f);
        p.add(lT, BorderLayout.WEST); p.add(lV, BorderLayout.EAST);
        return p;
    }

    private JPanel createColumnBox(String t, String v) {
        JPanel p = new JPanel(new BorderLayout(0, 5)); p.setBackground(BG_LIGHT);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(10,10,10,10)));
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel val = new JLabel(v); val.setFont(new Font("Segoe UI", Font.BOLD, 16)); val.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(l, BorderLayout.NORTH); p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JButton createBtn(String t, Color b, Color f) {
        JButton btn = new JButton(t); btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(b); btn.setForeground(f); btn.setFocusPainted(false); btn.setBorderPainted(false);
        return btn;
    }

    private void setupEvents() {
        btnCancel.addActionListener(e -> dispose());
        txtTienMatThucTe.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        double val = Double.parseDouble(txtTienMatThucTe.getText().replaceAll("[,.]", ""));
                        double diff = val - tienHeThong;
                        lblChenhLechVal.setText(formatter.format(diff) + "đ");
                        lblChenhLechVal.setForeground(diff < 0 ? Color.RED : PRIMARY_COLOR);
                        lblTienBanGiaoVal.setText(formatter.format(val) + "đ");
                    } catch (Exception ex) {}
                });
            }
        });
        
        ActionListener confirm = e -> {
            try {
                tienMatThucTe = Double.parseDouble(txtTienMatThucTe.getText().replaceAll("[,.]", ""));
                ghiChu = txtGhiChu.getText().trim();
                if(cbNhanBanGiao.getSelectedIndex() > 0) maTaiKhoanNhan = accounts.get(cbNhanBanGiao.getSelectedIndex()-1).getAccountID();
                isConfirmed = true; dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Nhập số tiền hợp lệ!"); }
        };
        btnCloseShift.addActionListener(confirm);
        btnCloseAndPrint.addActionListener(confirm);
    }

    public boolean isConfirmed() { return isConfirmed; }
    public double getTienMatThucTe() { return tienMatThucTe; }
    public String getGhiChu() { return ghiChu; }
    public Integer getMaTaiKhoanNhan() { return maTaiKhoanNhan; }
}