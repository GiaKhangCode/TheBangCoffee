package View;

import Model.OptionModel;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Tạo món mới và Thiết lập công thức - The Bang Coffee.
 * Thiết kế hiện đại, đồng bộ với phong cách hệ thống.
 * Đã nâng cấp: Render Tùy chọn (Checkbox) ĐỘNG giữ nguyên Layout.
 */
public class ProductDetailDialog extends JDialog {

    private JTabbedPane tabbedPane;
    private JPanel tabInfo, tabRecipe;
    private Color PRIMARY_COLOR = AppColor.PRIMARY;
    private Color TEXT_DARK = AppColor.TEXT_DARK;
    private Color TEXT_MUTED = AppColor.TEXT_MUTED;

    // Components Tab 1
    private JTextField txtTenMon, txtGiaBan;
    private JComboBox<String> cbDanhMuc;
    private JTextArea txtMoTa;
    private JLabel lblImagePlaceholder;
    private JRadioButton rbDangBan, rbTamHet, rbNgungBan;

    // Biến toàn cục cho Panel Tùy Chọn Động
    private JPanel optionsPanel;
    private Map<Integer, JCheckBox> optionCheckboxMap = new LinkedHashMap<>();

    // Các nút bấm để Controller có thể gắn Listener
    private JButton btnSave;
    private JButton btnUpload;

    // Components Tab 2
    private JComboBox<String> cbNguyenLieu;
    private JTextField txtDonVi, txtDinhLuong;
    private JTable recipeTable;
    private DefaultTableModel recipeModel;
    private JLabel lblTotalCost;

    public ProductDetailDialog(Frame parent) {
        super(parent, "TẠO MÓN NƯỚC MỚI", true);
        initComponents();
    }

    private void initComponents() {
        setSize(1050, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(getWidth(), 50));
        JLabel title = new JLabel("  TẠO MÓN NƯỚC MỚI");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);
        
        initTabInfo();
        initTabRecipe();

        tabbedPane.addTab("Thông tin chung", tabInfo);
        tabbedPane.addTab("Công thức (Định lượng)", tabRecipe);
        
        add(tabbedPane, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.setBackground(new Color(245, 245, 245));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        btnSave = createModernButton("Lưu sản phẩm", PRIMARY_COLOR, Color.WHITE);
        JButton btnCancel = createModernButton("Hủy bỏ", new Color(220, 220, 220), TEXT_DARK);
        
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnSave);
        footer.add(btnCancel);
        add(footer, BorderLayout.SOUTH);
    }

    private void initTabInfo() {
        tabInfo = new JPanel(new GridBagLayout());
        tabInfo.setBackground(Color.WHITE);
        tabInfo.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.fill = GridBagConstraints.BOTH;
        mainGbc.weighty = 1.0;

        // LEFT: Basic Info
        JPanel leftPanel = createSectionPanel("Thông tin cơ bản");
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        txtTenMon = createStyledTextField("");
        cbDanhMuc = new JComboBox<>();
        cbDanhMuc.setPreferredSize(new Dimension(200, 35));
        txtGiaBan = createStyledTextField("");
        txtMoTa = new JTextArea(3, 20);
        txtMoTa.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        txtMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        
        gbc.weightx = 0.3; addComponent(leftPanel, new JLabel("Tên món: *"), 0, 1, gbc);
        gbc.weightx = 0.7; addComponent(leftPanel, txtTenMon, 1, 1, gbc);
        
        gbc.weightx = 0.3; addComponent(leftPanel, new JLabel("Danh mục món: *"), 0, 2, gbc);
        gbc.weightx = 0.7; addComponent(leftPanel, cbDanhMuc, 1, 2, gbc);
        
        gbc.weightx = 0.3; addComponent(leftPanel, new JLabel("Giá bán tiêu chuẩn: *"), 0, 3, gbc);
        gbc.weightx = 0.7; addComponent(leftPanel, txtGiaBan, 1, 3, gbc);
        
        // Image Section
        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        imgPanel.setOpaque(false);
        lblImagePlaceholder = new JLabel("Hình ảnh mẫu", SwingConstants.CENTER);
        lblImagePlaceholder.setPreferredSize(new Dimension(100, 100));
        lblImagePlaceholder.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        btnUpload = new JButton("Tải ảnh lên");
        btnUpload.setFont(new Font("Segoe UI", Font.BOLD, 12));
        imgPanel.add(lblImagePlaceholder);
        imgPanel.add(btnUpload);
        
        gbc.gridwidth = 2; gbc.weightx = 1.0;
        addComponent(leftPanel, new JLabel("Hình ảnh:"), 0, 4, gbc);
        addComponent(leftPanel, imgPanel, 0, 5, gbc);
        addComponent(leftPanel, new JLabel("Mô tả món:"), 0, 6, gbc);
        gbc.weighty = 0.1; addComponent(leftPanel, new JScrollPane(txtMoTa), 0, 7, gbc);
        gbc.weighty = 0;

        // RIGHT: Options & Status
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setOpaque(false);
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.fill = GridBagConstraints.BOTH;
        rightGbc.weightx = 1.0;
        rightGbc.insets = new Insets(0, 10, 0, 0);

        // LOGIC MỚI: Khởi tạo optionsPanel trống, chuẩn bị để load dữ liệu động
        optionsPanel = createSectionPanel("Tùy chọn & Biến thể");
        optionsPanel.setLayout(new GridBagLayout());
        
        // Status Panel
        JPanel statusPanel = createSectionPanel("Trạng thái");
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 8));
        rbDangBan = new JRadioButton("Đang bán", true);
        rbTamHet = new JRadioButton("Tạm hết");
        rbNgungBan = new JRadioButton("Ngừng bán");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbDangBan); bg.add(rbTamHet); bg.add(rbNgungBan);
        statusPanel.add(rbDangBan); statusPanel.add(rbTamHet); statusPanel.add(rbNgungBan);

        rightGbc.gridy = 0; rightGbc.weighty = 0.8;
        rightContainer.add(optionsPanel, rightGbc);
        rightGbc.gridy = 1; rightGbc.weighty = 0.2;
        rightContainer.add(statusPanel, rightGbc);

        mainGbc.gridx = 0; mainGbc.weightx = 0.6;
        tabInfo.add(leftPanel, mainGbc);
        mainGbc.gridx = 1; mainGbc.weightx = 0.4;
        tabInfo.add(rightContainer, mainGbc);
    }

    private void initTabRecipe() {
        tabRecipe = new JPanel(new BorderLayout(0, 20));
        tabRecipe.setBackground(Color.WHITE);
        tabRecipe.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Info
        JPanel recipeHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recipeHeader.setOpaque(false);
        JLabel lblTarget = new JLabel("Đang tạo món: ---");
        lblTarget.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recipeHeader.add(lblTarget);

        // Input Area
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(248, 249, 250));
        inputPanel.setBorder(new CompoundBorder(new LineBorder(new Color(230, 230, 230)), new EmptyBorder(15, 15, 15, 15)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        cbNguyenLieu = new JComboBox<>();
        txtDonVi = createStyledTextField("");
        txtDonVi.setEditable(false);
        txtDinhLuong = createStyledTextField("");
        JButton btnAdd = createModernButton(" ➕ Thêm vào công thức", PRIMARY_COLOR, Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("Nguyên liệu:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(cbNguyenLieu, gbc);
        gbc.gridx = 1; gbc.gridy = 0; inputPanel.add(new JLabel("Đơn vị:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(txtDonVi, gbc);
        gbc.gridx = 2; gbc.gridy = 0; inputPanel.add(new JLabel("Định lượng (Lượng):"), gbc);
        gbc.gridx = 2; gbc.gridy = 1; inputPanel.add(txtDinhLuong, gbc);
        gbc.gridx = 3; gbc.gridy = 1; inputPanel.add(btnAdd, gbc);

        // Table
        String[] cols = {"STT", "Mã NL", "Tên Nguyên Liệu", "Đơn vị", "Định lượng", "Giá vốn ước tính", "Thành tiền", "Thao tác"};
        recipeModel = new DefaultTableModel(cols, 0);
        recipeTable = new JTable(recipeModel);
        styleTable(recipeTable);
        
        JScrollPane scroll = new JScrollPane(recipeTable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));

        // Summary
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.setOpaque(false);
        lblTotalCost = new JLabel("Tổng giá vốn ước tính: 0 VND");
        lblTotalCost.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalCost.setForeground(PRIMARY_COLOR);
        summaryPanel.add(lblTotalCost);

        tabRecipe.add(recipeHeader, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);
        centerPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        tabRecipe.add(centerPanel, BorderLayout.CENTER);
    }

    // --- Helper Methods ---

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new TitledBorder(new LineBorder(new Color(230, 230, 230)), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));
        return p;
    }

    private JTextField createStyledTextField(String text) {
        JTextField tf = new JTextField(text, 15);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200)), new EmptyBorder(5, 10, 5, 10)));
        return tf;
    }

    private void addComponent(JPanel p, Component c, int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x; gbc.gridy = y;
        p.add(c, gbc);
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(242, 242, 242));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30));
        table.setSelectionForeground(TEXT_DARK);
    }

    // =====================================================================
    // PUBLIC API - ĐÃ NÂNG CẤP XỬ LÝ CHECKBOX ĐỘNG
    // =====================================================================

    /**
     * Hàm render giao diện Checkbox Động thay thế cho code cứng.
     * Áp dụng nguyên lý removeAll -> loop -> revalidate & repaint.
     */
    public void setOptionGroups(HashMap<String, ArrayList<OptionModel>> groups) {
        optionsPanel.removeAll();
        optionCheckboxMap.clear();

        GridBagConstraints optGbc = new GridBagConstraints();
        optGbc.fill = GridBagConstraints.HORIZONTAL;
        optGbc.insets = new Insets(5, 5, 5, 5);
        optGbc.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;
        for (Map.Entry<String, ArrayList<OptionModel>> entry : groups.entrySet()) {
            
            // 1. Cột trái (30%): Label Tên Nhóm
            optGbc.gridx = 0; 
            optGbc.gridy = row; 
            optGbc.weightx = 0.3;
            optionsPanel.add(new JLabel(entry.getKey() + ":"), optGbc);

            // 2. Cột phải (70%): Danh sách Checkbox
            JPanel itemsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            itemsPanel.setOpaque(false);

            for (OptionModel item : entry.getValue()) {
                JCheckBox cb = new JCheckBox(item.getLabel());
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                cb.setOpaque(false);
                
                // Mặc định tích nếu trong database set là "Dang su dung"
                cb.setSelected("Đang sử dụng".equals(item.getTrangThai())); 
                
                optionCheckboxMap.put(item.getMaTuyChon(), cb);
                itemsPanel.add(cb);
            }

            optGbc.gridx = 1; 
            optGbc.weightx = 0.7;
            optionsPanel.add(itemsPanel, optGbc);
            
            row++;
        }

        // Đẩy toàn bộ nội dung lên trên
        optGbc.gridx = 0; 
        optGbc.gridy = row; 
        optGbc.gridwidth = 2; 
        optGbc.weighty = 1.0;
        optionsPanel.add(Box.createVerticalGlue(), optGbc);

        // Làm mới giao diện
        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    /**
     * Hàm lấy danh sách các mã tùy chọn (ID) đã được người dùng tích chọn
     */
    public List<Integer> getSelectedOptionIds() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, JCheckBox> entry : optionCheckboxMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public void setCategoryList(List<String> categories) {
        cbDanhMuc.setModel(new DefaultComboBoxModel<>(categories.toArray(new String[0])));
    }

    public void setIngredientList(List<String> ingredients) {
        cbNguyenLieu.setModel(new DefaultComboBoxModel<>(ingredients.toArray(new String[0])));
    }

    public void setImage(ImageIcon icon) {
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            lblImagePlaceholder.setIcon(new ImageIcon(img));
            lblImagePlaceholder.setText("");
        } else {
            lblImagePlaceholder.setIcon(null);
            lblImagePlaceholder.setText("Hình ảnh mẫu");
        }
    }

    public void addChooseImageListener(ActionListener listener) {
        btnUpload.addActionListener(listener);
    }

    public void addSaveListener(ActionListener listener) {
        btnSave.addActionListener(listener);
    }

    public String getTenSanPham() {
        return txtTenMon.getText().trim();
    }

    public double getGiaBan() {
        try {
            return Double.parseDouble(txtGiaBan.getText().trim().replace(".", "").replace(",", ""));
        } catch (Exception e) { 
            return 0; 
        }
    }

    public String getLoaiSanPham() {
        return (String) cbDanhMuc.getSelectedItem();
    }

    public String getTrangThai() {
        if (rbDangBan.isSelected()) return "Đang sử dụng";
        if (rbTamHet.isSelected()) return "Tạm hết";
        return "Chưa sử dụng";
    }
    
    public void clearForm() {
        // ===== TAB 1: Thông tin =====
        if(txtTenMon == null) return;
        txtTenMon.setText("");
        txtGiaBan.setText("");
        txtMoTa.setText("");

        cbDanhMuc.setSelectedIndex(-1);

        // Reset trạng thái
        rbDangBan.setSelected(true);

        // Reset hình ảnh
        lblImagePlaceholder.setIcon(null);
        lblImagePlaceholder.setText("Hình ảnh mẫu");

        // ===== CHECKBOX OPTIONS =====
        for (JCheckBox cb : optionCheckboxMap.values()) {
            cb.setSelected(false);
        }

        // ===== TAB 2: Công thức =====
        cbNguyenLieu.setSelectedIndex(-1);
        txtDonVi.setText("");
        txtDinhLuong.setText("");

        // Xóa bảng công thức
        recipeModel.setRowCount(0);

        // Reset tổng tiền
        lblTotalCost.setText("Tổng giá vốn ước tính: 0 VND");
    }

    public static void main(String[] args) {
        // Hàm Main để Test độc lập
    }
}