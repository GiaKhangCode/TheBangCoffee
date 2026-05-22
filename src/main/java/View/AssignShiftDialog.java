package View;

import Controller.EmployeeScheduleController;
import Model.AccountModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignShiftDialog extends JDialog {
    private EmployeeScheduleController controller;
    private Date workDate;
    private String shiftType;
    
    private Map<JCheckBox, Integer> checkboxAccountMap;

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(30, 41, 59); // Slate 800

    public AssignShiftDialog(Frame owner, Date workDate, String shiftType) {
        super(owner, "Phân ca: " + shiftType + " - " + new SimpleDateFormat("dd/MM/yyyy").format(workDate), true);
        this.controller = new EmployeeScheduleController();
        this.workDate = workDate;
        this.shiftType = shiftType;
        this.checkboxAccountMap = new HashMap<>();

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparent background for rounded corners
        
        initComponents();
        loadData();
    }

    private void initComponents() {
        setSize(450, 550);
        setLocationRelativeTo(getOwner());

        JPanel containerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                // Border
                g2.setColor(PRIMARY_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        containerPanel.setOpaque(false);
        containerPanel.setLayout(new BorderLayout(0, 15));
        containerPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("XẾP LỊCH NHÂN VIÊN", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_COLOR);

        String dateStr = new SimpleDateFormat("dd/MM/yyyy").format(workDate);
        JLabel subLabel = new JLabel("Ca " + shiftType + " - Ngày " + dateStr, JLabel.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subLabel.setForeground(new Color(100, 116, 139)); // Slate 500

        headerPanel.add(titleLabel);
        headerPanel.add(subLabel);
        containerPanel.add(headerPanel, BorderLayout.NORTH);

        // --- CENTER: Employee List ---
        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(10, 10, 10, 10));

        List<AccountModel> accounts = controller.getAllAccounts();
        for (AccountModel acc : accounts) {
            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            itemPanel.setOpaque(false);
            
            JCheckBox chk = new JCheckBox(acc.getFullName() + " (" + acc.getUsername() + ")");
            chk.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            chk.setForeground(TEXT_DARK);
            chk.setFocusPainted(false);
            chk.setOpaque(false);
            chk.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            itemPanel.add(chk);
            checkboxAccountMap.put(chk, acc.getAccountID());
            
            pnlCenter.add(itemPanel);
            pnlCenter.add(Box.createVerticalStrut(5));
        }

        JScrollPane scrollPane = new JScrollPane(pnlCenter);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240))); // Slate 200
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        containerPanel.add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM: Buttons ---
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnCancel = createButton("Hủy Bỏ", new Color(241, 245, 249), new Color(71, 85, 105)); // Gray
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = createButton("Lưu Thay Đổi", PRIMARY_COLOR, Color.WHITE);
        btnSave.addActionListener(e -> save());

        pnlBottom.add(btnCancel);
        pnlBottom.add(btnSave);
        containerPanel.add(pnlBottom, BorderLayout.SOUTH);

        setContentPane(containerPanel);
    }

    private JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 40));
        return btn;
    }

    private void loadData() {
        List<Integer> assignedIds = controller.getAssignedAccountIds(workDate, shiftType);
        for (Map.Entry<JCheckBox, Integer> entry : checkboxAccountMap.entrySet()) {
            if (assignedIds.contains(entry.getValue())) {
                entry.getKey().setSelected(true);
            }
        }
    }

    private void save() {
        List<Integer> selectedAccountIds = new ArrayList<>();
        for (Map.Entry<JCheckBox, Integer> entry : checkboxAccountMap.entrySet()) {
            if (entry.getKey().isSelected()) {
                selectedAccountIds.add(entry.getValue());
            }
        }

        boolean success = controller.saveSchedules(workDate, shiftType, selectedAccountIds);
        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu phân ca thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu phân ca.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
