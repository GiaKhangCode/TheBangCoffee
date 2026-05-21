package View;

import Common.ComponentUI;
import DatabaseAccessObject.ShiftSessionDAO;
import Model.ShiftSession;
import Service.AccountService;
import Model.AccountModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class ShiftMonitorPanel extends JPanel {

    private JTable shiftTable;
    private DefaultTableModel tableModel;
    private ShiftSessionDAO shiftSessionDAO;
    private AccountService accountService;

    public ShiftMonitorPanel() {
        shiftSessionDAO = new ShiftSessionDAO();
        accountService = new AccountService();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Table Panel
        String[] columns = {"Mã Phiên", "Trạng Thái Ca", "Tên Nhân Viên Mở", "Thời Gian Mở", "Trạng Thái Lịch", "Doanh Thu Tạm Tính", "Hành Động"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        shiftTable = new JTable(tableModel);
        ComponentUI.styleTable(shiftTable, Color.BLACK, Color.BLACK, new Color(67, 142, 104));
        
        // Căn chỉnh kích thước cột
        shiftTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        shiftTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        shiftTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        shiftTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        shiftTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        shiftTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        shiftTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        
        // Render nút Action dưới dạng một JButton thực sự
        shiftTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value != null && value.toString().equals("Ép đóng ca")) {
                    JButton btn = ComponentUI.createModernButton("Ép đóng ca", new Color(255, 59, 48), Color.WHITE);
                    btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                    panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    panel.add(btn);
                    return panel;
                } else {
                    JLabel lbl = new JLabel("");
                    lbl.setOpaque(true);
                    lbl.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    return lbl;
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(shiftTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        add(scrollPane, BorderLayout.CENTER);

        // Listeners
        shiftTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = shiftTable.rowAtPoint(e.getPoint());
                int col = shiftTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 6) {
                    Object val = tableModel.getValueAt(row, 6);
                    if (val != null && val.toString().equals("Ép đóng ca")) {
                        int maPhienCa = (int) tableModel.getValueAt(row, 0);
                        forceCloseShift(maPhienCa);
                    }
                }
            }
        });
    }

    private void forceCloseShift(int maPhienCa) {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn ép đóng ca này?\nHành động này không thể hoàn tác.", "Xác nhận ép đóng ca", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            double doanhThu = shiftSessionDAO.getDoanhThuTienMat(maPhienCa);
            double tienDauCa = shiftSessionDAO.getTienMatDauCa(maPhienCa);
            double tienHeThong = tienDauCa + doanhThu;

            // Khi ép đóng, tiền thực tế được ghi nhận bằng tiền hệ thống
            boolean success = shiftSessionDAO.dongCaToanDien(maPhienCa, Model.SessionManager.getAccountId(), "Quản lý ép đóng ca", tienHeThong, tienHeThong);
            if (success) {
                JOptionPane.showMessageDialog(this, "Ép đóng ca thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadData();
                
                // Nếu ca vừa ép đóng là ca đang mở của chính phiên đăng nhập hiện tại
                if (maPhienCa == Model.SessionManager.getCurrentMaPhienCa()) {
                    Model.SessionManager.setCurrentMaPhienCa(-1);
                    MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(this);
                    if (mainFrame != null) {
                        mainFrame.setShiftButtonState(false);
                        try {
                            mainFrame.setPageActive("Stats");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi ép đóng ca!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<ShiftSession> activeShifts = shiftSessionDAO.getTodayShifts();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        for (ShiftSession ca : activeShifts) {
            // Lấy tên người mở
            AccountModel acc = accountService.getAccountById(ca.getMaTaiKhoanMo());
            String tenNhanVien = acc != null ? acc.getUsername() : "Unknown (" + ca.getMaTaiKhoanMo() + ")";

            // Trạng thái lịch
            String lich = ca.getMaLich() != null ? "Đúng lịch (Mã: " + ca.getMaLich() + ")" : "Làm đột xuất/Làm thay";

            // Cột Doanh Thu Tạm Tính sẽ chỉ lấy Tổng doanh thu (không cộng tiền đầu ca)
            double tongDoanhThu = shiftSessionDAO.getTongDoanhThu(ca.getMaPhienCa());
            String doanhThuTamTinh = String.format("%,.0f VNĐ", tongDoanhThu);
            
            boolean isOpen = "Đang mở".equals(ca.getTrangThai());
            String trangThaiCa = isOpen ? "Đang mở" : "Đã kết thúc";
            String action = isOpen ? "Ép đóng ca" : "";

            tableModel.addRow(new Object[]{
                ca.getMaPhienCa(),
                trangThaiCa,
                tenNhanVien,
                sdf.format(ca.getThoiGianMo()),
                lich,
                doanhThuTamTinh,
                action
            });
        }
    }
}
