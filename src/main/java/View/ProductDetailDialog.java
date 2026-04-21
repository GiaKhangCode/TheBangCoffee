package View;

import Model.CategoryModel;
import Model.OptionModel;
import Model.ProductCategoryListModel;
import Model.OptionGroupModel;
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

public class ProductDetailDialog extends JDialog {

    private JTabbedPane tabbedPane;
    private JPanel tabInfo, tabRecipe, tabCategory, tabOptionGroup, tabOption;

    private Color PRIMARY_COLOR = AppColor.PRIMARY;
    private Color TEXT_DARK = AppColor.TEXT_DARK;
    private Color TEXT_MUTED = AppColor.TEXT_MUTED;

    private JTextField txtProductName, txtPrice;
    private JComboBox<String> cbOption;
    private JTextArea txtDescription;
    private JLabel lblImagePlaceholder;
    private JRadioButton rbOnSale, rbOutOfStock, rbStopSelling;
    private JPanel optionsPanel;
    private Map<Integer, JCheckBox> optionCheckboxMap = new LinkedHashMap<>();
    private HashMap<String, ArrayList<OptionModel>> currentOptionGroups = new HashMap<>();

    private JButton btnSave, btnUpload;
    private JComboBox<String> cbIngredient;
    private JTextField txtUnit, txtQuantitative;
    
    private JTable recipeTable, categoryTable, optionGroupTable, optionTable;
    private DefaultTableModel recipeModel, categoryModel, optionGroupModel, optionModel;
    private JLabel lblTotalCost;
    
    private JButton btnAddCategory, btnEditCategory, btnDeleteCategory;
    private JButton btnAddOptionGroup, btnEditOptionGroup, btnDeleteOptionGroup;
    private JButton btnAddOption, btnEditOption, btnDeleteOption;

    public ProductDetailDialog(Frame parent) {
        super(parent, "QUẢN LÝ MÓN VÀ DANH MỤC", true);
        initComponents();
    }

    private void initComponents() {
        setSize(1050, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(getWidth(), 50));
        JLabel title = new JLabel("  QUẢN LÝ MÓN & DANH MỤC TÙY CHỌN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);
        
        initTabInfo();
        initTabRecipe();
        initTabCategory();
        initTabOptionGroup();
        initTabOption();

        tabbedPane.addTab("Thông tin chung", tabInfo);
        tabbedPane.addTab("Công thức", tabRecipe);
        tabbedPane.addTab("Quản lý Loại SP", tabCategory);
        tabbedPane.addTab("Quản lý Nhóm Tùy Chọn", tabOptionGroup);
        tabbedPane.addTab("Quản lý Tùy Chọn", tabOption);
        
        add(tabbedPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.setBackground(new Color(245, 245, 245));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        btnSave = createModernButton("Lưu sản phẩm", PRIMARY_COLOR, Color.WHITE);
        JButton btnCancel = createModernButton("Đóng", new Color(220, 220, 220), TEXT_DARK);
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnSave);
        footer.add(btnCancel);
        add(footer, BorderLayout.SOUTH);
    }

    // =====================================================================
    // ĐÃ FIX LAYOUT: Sử dụng BorderLayout để khóa cứng Panel trái
    // =====================================================================
    private void initTabInfo() {
        // Dùng BorderLayout thay vì GridBagLayout để phân chia rõ 2 cột
        tabInfo = new JPanel(new BorderLayout(20, 0)); // 20px là khoảng cách giữa 2 cột
        tabInfo.setBackground(Color.WHITE);
        tabInfo.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ============ LEFT PANEL (Cột trái khóa cứng 400px) ============
        JPanel leftPanel = createSectionPanel("Thông tin cơ bản");
        leftPanel.setPreferredSize(new Dimension(400, 0)); // Khóa cứng chiều rộng 400px
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        txtProductName = createStyledTextField("");
        cbOption = new JComboBox<>();
        txtPrice = createStyledTextField("");
        txtDescription = new JTextArea(4, 20);
        txtDescription.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        
        // Hàng 1
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        leftPanel.add(new JLabel("Tên món: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        leftPanel.add(txtProductName, gbc);
        
        // Hàng 2
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        leftPanel.add(new JLabel("Danh mục: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        leftPanel.add(cbOption, gbc);
        
        // Hàng 3
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        leftPanel.add(new JLabel("Giá bán: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        leftPanel.add(txtPrice, gbc);
        
        // Hàng 4 (Khung ảnh)
        JPanel imgPanel = new JPanel(new GridBagLayout());
        imgPanel.setOpaque(false);
        GridBagConstraints imgGbc = new GridBagConstraints();
        imgGbc.anchor = GridBagConstraints.WEST;
        imgGbc.insets = new Insets(0, 0, 0, 15);
        
        lblImagePlaceholder = new JLabel("Hình ảnh mẫu", SwingConstants.CENTER);
        lblImagePlaceholder.setPreferredSize(new Dimension(100, 100));
        lblImagePlaceholder.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        btnUpload = new JButton("Tải ảnh lên");
        btnUpload.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUpload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        imgGbc.gridx = 0; imgGbc.gridy = 0;
        imgPanel.add(lblImagePlaceholder, imgGbc);
        imgGbc.gridx = 1; imgGbc.gridy = 0;
        imgPanel.add(btnUpload, imgGbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        leftPanel.add(new JLabel("Hình ảnh:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        leftPanel.add(imgPanel, gbc);
        
        // Hàng 5 (Mô tả)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0; gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Ép chữ "Mô tả:" lên góc trên cùng
        leftPanel.add(new JLabel("Mô tả:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; gbc.weighty = 1.0; 
        gbc.fill = GridBagConstraints.BOTH; 
        leftPanel.add(new JScrollPane(txtDescription), gbc);

        // ============ RIGHT PANEL (Cột phải chiếm hết không gian còn lại) ============
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setOpaque(false);
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.fill = GridBagConstraints.BOTH;
        rightGbc.weightx = 1.0;
        rightGbc.insets = new Insets(0, 0, 10, 0);

        optionsPanel = createSectionPanel("Tùy chọn & Biến thể");
        optionsPanel.setLayout(new GridBagLayout());
        
        JPanel statusPanel = createSectionPanel("Trạng thái");
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 8));
        rbOnSale = new JRadioButton("Đang bán", true);
        rbOutOfStock = new JRadioButton("Tạm hết");
        rbStopSelling = new JRadioButton("Ngừng bán");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbOnSale); bg.add(rbOutOfStock); bg.add(rbStopSelling);
        statusPanel.add(rbOnSale); statusPanel.add(rbOutOfStock); statusPanel.add(rbStopSelling);

        rightGbc.gridy = 0; rightGbc.weighty = 1.0; 
        rightContainer.add(optionsPanel, rightGbc);
        rightGbc.gridy = 1; rightGbc.weighty = 0.0; 
        rightGbc.insets = new Insets(0, 0, 0, 0);
        rightContainer.add(statusPanel, rightGbc);

        // Nạp 2 cột vào Tab chính (Tây và Trung tâm)
        tabInfo.add(leftPanel, BorderLayout.WEST);
        tabInfo.add(rightContainer, BorderLayout.CENTER);
    }

    private void initTabRecipe() {
        tabRecipe = new JPanel(new BorderLayout(0, 20));
        tabRecipe.setBackground(Color.WHITE);
        tabRecipe.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel recipeHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recipeHeader.setOpaque(false);
        JLabel lblTarget = new JLabel("Thiết lập định lượng");
        lblTarget.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recipeHeader.add(lblTarget);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(248, 249, 250));
        inputPanel.setBorder(new CompoundBorder(new LineBorder(new Color(230, 230, 230)), new EmptyBorder(15, 15, 15, 15)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbIngredient = new JComboBox<>();
        txtUnit = createStyledTextField(""); txtUnit.setEditable(false);
        txtQuantitative = createStyledTextField("");
        JButton btnAdd = createModernButton(" ➕ Thêm", PRIMARY_COLOR, Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4; inputPanel.add(new JLabel("Nguyên liệu:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(cbIngredient, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.2; inputPanel.add(new JLabel("Đơn vị:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(txtUnit, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.2; inputPanel.add(new JLabel("Định lượng:"), gbc);
        gbc.gridx = 2; gbc.gridy = 1; inputPanel.add(txtQuantitative, gbc);
        
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.2; inputPanel.add(btnAdd, gbc);

        String[] cols = {"STT", "Mã NL", "Tên Nguyên Liệu", "Đơn vị", "Định lượng", "Thành tiền"};
        recipeModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        recipeTable = new JTable(recipeModel); styleTable(recipeTable);
        
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); summaryPanel.setOpaque(false);
        lblTotalCost = new JLabel("Tổng giá vốn: 0 VND"); lblTotalCost.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalCost.setForeground(PRIMARY_COLOR); summaryPanel.add(lblTotalCost);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15)); centerPanel.setOpaque(false);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(recipeTable), BorderLayout.CENTER);
        centerPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        tabRecipe.add(recipeHeader, BorderLayout.NORTH);
        tabRecipe.add(centerPanel, BorderLayout.CENTER);
    }

    private void initTabCategory() {
        tabCategory = new JPanel(new BorderLayout(15, 15));
        tabCategory.setBackground(Color.WHITE);
        tabCategory.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlPanel.setOpaque(false);
        btnAddCategory = createModernButton("Thêm Loại SP", PRIMARY_COLOR, Color.WHITE);
        btnEditCategory = createModernButton("Sửa", new Color(241, 196, 15), Color.BLACK);
        btnDeleteCategory = createModernButton("Xóa", new Color(231, 76, 60), Color.WHITE);
        controlPanel.add(btnAddCategory); controlPanel.add(btnEditCategory); controlPanel.add(btnDeleteCategory);

        String[] cols = {"Mã Loại", "Tên Loại Sản Phẩm", "Trạng Thái"};
        categoryModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        categoryTable = new JTable(categoryModel); styleTable(categoryTable);
        
        tabCategory.add(controlPanel, BorderLayout.NORTH);
        tabCategory.add(new JScrollPane(categoryTable), BorderLayout.CENTER);
    }

    private void initTabOptionGroup() {
        tabOptionGroup = new JPanel(new BorderLayout(15, 15));
        tabOptionGroup.setBackground(Color.WHITE);
        tabOptionGroup.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlPanel.setOpaque(false);
        btnAddOptionGroup = createModernButton("Thêm Nhóm", PRIMARY_COLOR, Color.WHITE);
        btnEditOptionGroup = createModernButton("Sửa", new Color(241, 196, 15), Color.BLACK);
        btnDeleteOptionGroup = createModernButton("Xóa", new Color(231, 76, 60), Color.WHITE);
        controlPanel.add(btnAddOptionGroup); controlPanel.add(btnEditOptionGroup); controlPanel.add(btnDeleteOptionGroup);

        String[] cols = {"Mã Nhóm", "Tên Nhóm Tùy Chọn"};
        optionGroupModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        optionGroupTable = new JTable(optionGroupModel); styleTable(optionGroupTable);
        
        tabOptionGroup.add(controlPanel, BorderLayout.NORTH);
        tabOptionGroup.add(new JScrollPane(optionGroupTable), BorderLayout.CENTER);
    }

    private void initTabOption() {
        tabOption = new JPanel(new BorderLayout(15, 15));
        tabOption.setBackground(Color.WHITE);
        tabOption.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlPanel.setOpaque(false);
        btnAddOption = createModernButton("Thêm Tùy Chọn", PRIMARY_COLOR, Color.WHITE);
        btnEditOption = createModernButton("Sửa", new Color(241, 196, 15), Color.BLACK);
        btnDeleteOption = createModernButton("Xóa", new Color(231, 76, 60), Color.WHITE);
        controlPanel.add(btnAddOption); controlPanel.add(btnEditOption); controlPanel.add(btnDeleteOption);

        String[] cols = {"Mã Tùy Chọn", "Thuộc Nhóm", "Tên Tùy Chọn", "Phụ Thu", "Trạng Thái"};
        optionModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        optionTable = new JTable(optionModel); styleTable(optionTable);
        
        tabOption.add(controlPanel, BorderLayout.NORTH);
        tabOption.add(new JScrollPane(optionTable), BorderLayout.CENTER);
    }

    // =====================================================================
    // HÀM UI HELPER
    // =====================================================================
    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(); p.setBackground(Color.WHITE);
        p.setBorder(new TitledBorder(new LineBorder(new Color(230, 230, 230)), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));
        return p;
    }

    private JTextField createStyledTextField(String text) {
        JTextField tf = new JTextField(text, 15); tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200)), new EmptyBorder(5, 10, 5, 10)));
        return tf;
    }

    private void addComponent(JPanel p, Component c, int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x; gbc.gridy = y; p.add(c, gbc);
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); btn.setBackground(bg); btn.setForeground(fg);
        btn.setFocusPainted(false); btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setContentAreaFilled(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(40); table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(242, 242, 242));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30));
    }

    // =====================================================================
    // PUBLIC API - VIEW TỰ XỬ LÝ DATA TỪ CONTROLLER
    // =====================================================================

    public void loadCategoryData(ProductCategoryListModel dataList) {
        categoryModel.setRowCount(0);
        cbOption.removeAllItems();
        
        if (dataList != null && dataList.getProductCategoryList() != null) {
            for (CategoryModel cat : dataList.getProductCategoryList()) {
                categoryModel.addRow(new Object[]{
                    cat.getCategoryID(),
                    cat.getCategoryName(),
                    cat.getCategoryStatus()
                });
                cbOption.addItem(cat.getCategoryName());
            }
        }
    }

    public void loadOptionGroupData(ArrayList<OptionGroupModel> groupList) {
        optionGroupModel.setRowCount(0);
        if (groupList != null) {
            for (OptionGroupModel group : groupList) {
                optionGroupModel.addRow(new Object[]{ 
                    group.getOptionGroupID(),
                    group.getOptionGroupName()
                });
            }
        }
    }

    public void loadOptionData(HashMap<String, ArrayList<OptionModel>> optionsMap) {
        optionModel.setRowCount(0);
        if (optionsMap != null) {
            for (Map.Entry<String, ArrayList<OptionModel>> entry : optionsMap.entrySet()) {
                String groupName = entry.getKey();
                for (OptionModel item : entry.getValue()) {
                    String phuThuFormatted = String.format("%,.0f VNĐ", item.getExtraPrice());
                    optionModel.addRow(new Object[]{
                        item.getOptionID(),
                        groupName,
                        item.getOptionName(),
                        phuThuFormatted,
                        item.getOptionStatus()
                    });
                }
            }
        }
        
        this.currentOptionGroups = optionsMap;
        optionsPanel.removeAll();
        optionCheckboxMap.clear();

        if (optionsMap == null) return;
        GridBagConstraints optGbc = new GridBagConstraints();
        optGbc.fill = GridBagConstraints.HORIZONTAL;
        optGbc.insets = new Insets(8, 10, 8, 10);
        optGbc.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;
        for (Map.Entry<String, ArrayList<OptionModel>> entry : optionsMap.entrySet()) {
            
            optGbc.gridx = 0; optGbc.gridy = row; optGbc.weightx = 0.0;
            JLabel lblGroup = new JLabel(entry.getKey() + ":");
            lblGroup.setFont(new Font("Segoe UI", Font.BOLD, 13));
            optionsPanel.add(lblGroup, optGbc);

            JPanel itemsPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5));
            itemsPanel.setOpaque(false);

            for (OptionModel item : entry.getValue()) {
                JCheckBox cb = new JCheckBox(item.getLabel());
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 13)); cb.setOpaque(false);
                cb.setSelected("Đang sử dụng".equals(item.getOptionStatus())); 
                optionCheckboxMap.put(item.getOptionID(), cb);
                itemsPanel.add(cb);
            }

            optGbc.gridx = 1; optGbc.weightx = 1.0;
            optionsPanel.add(itemsPanel, optGbc);
            row++;
        }

        optGbc.gridx = 0; optGbc.gridy = row; optGbc.gridwidth = 2; optGbc.weighty = 1.0;
        optionsPanel.add(Box.createVerticalGlue(), optGbc);
        optionsPanel.revalidate(); optionsPanel.repaint();
    }

    public HashMap<String, List<String>> getSelectedOptionNamesByGroup() {
        HashMap<String, List<String>> result = new HashMap<>();
        if (currentOptionGroups == null || currentOptionGroups.isEmpty()) return result;

        for (Map.Entry<String, ArrayList<OptionModel>> entry : currentOptionGroups.entrySet()) {
            String groupName = entry.getKey();
            List<String> selectedNamesInGroup = new ArrayList<>();

            for (OptionModel item : entry.getValue()) {
                JCheckBox cb = optionCheckboxMap.get(item.getOptionID());
                if (cb != null && cb.isSelected()) {
                    selectedNamesInGroup.add(item.getOptionName());
                }
            }
            if (!selectedNamesInGroup.isEmpty()) result.put(groupName, selectedNamesInGroup);
        }
        return result;
    }

    public void setImage(ImageIcon icon) {
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            lblImagePlaceholder.setIcon(new ImageIcon(img));
            lblImagePlaceholder.setText("");
        } else {
            lblImagePlaceholder.setIcon(null); lblImagePlaceholder.setText("Hình ảnh mẫu");
        }
    }

    // =====================================================================
    // CÁC HÀM GETTERS / LISTENERS
    // =====================================================================
    public void addChooseImageListener(ActionListener listener) { btnUpload.addActionListener(listener); }
    public void addSaveListener(ActionListener listener) { btnSave.addActionListener(listener); }
    public void addAddCategoryListener(ActionListener listener) { btnAddCategory.addActionListener(listener); }
    public void addAddOptionGroupListener(ActionListener listener) { btnAddOptionGroup.addActionListener(listener); }
    public void addAddOptionListener(ActionListener listener) { btnAddOption.addActionListener(listener); }
    public void addDeleteCategoryListener(ActionListener listener) { btnDeleteCategory.addActionListener(listener); }
    public void addDeleteOptionGroupListener(ActionListener listener) { btnDeleteOptionGroup.addActionListener(listener); }
    public void addDeleteOptionListener(ActionListener listener) { btnDeleteOption.addActionListener(listener); }

    public String getProductName() { return txtProductName.getText().trim(); }
    public double getPrice() {
        try { return Double.parseDouble(txtPrice.getText().trim().replace(".", "").replace(",", "")); } 
        catch (Exception e) { return 0; }
    }
    public String getCategory() { return (String) cbOption.getSelectedItem(); }
    public String getStatus() {
        if (rbOnSale.isSelected()) return "Đang bán";
        if (rbOutOfStock.isSelected()) return "Tạm hết";
        return "Ngừng bán";
    }
    
    public int getSelectedCategoryId() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (int) categoryModel.getValueAt(selectedRow, 0);
        }
        return -1;
    }

    public String getSelectedOptionGroupName() {
        int selectedRow = optionGroupTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (String) optionGroupModel.getValueAt(selectedRow, 0);
        }
        return null;
    }
    
    public int getSelectedOptionDetailId() {
        int selectedRow = optionTable.getSelectedRow();
        if (selectedRow >= 0) {
            return (int) optionModel.getValueAt(selectedRow, 0);
        }
        return -1;
    }

    public void clearForm() {
        if(txtProductName == null) return;
        txtProductName.setText(""); txtPrice.setText(""); txtDescription.setText("");
        if(cbOption.getItemCount() > 0) cbOption.setSelectedIndex(0); 
        rbOnSale.setSelected(true);
        lblImagePlaceholder.setIcon(null); lblImagePlaceholder.setText("Hình ảnh mẫu");
        for (JCheckBox cb : optionCheckboxMap.values()) cb.setSelected(false);
        recipeModel.setRowCount(0); lblTotalCost.setText("Tổng giá vốn ước tính: 0 VND");
    }
    
    class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getParent().getWidth();
                if (targetWidth == 0) targetWidth = 400; // fallback hợp lý

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.width = Math.max(dim.width, rowWidth);
                            dim.height += rowHeight + vgap;
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        rowWidth += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight + insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }
    }
    public interface RoleActionListener {
        void onEdit(int row);
        void onDelete(int row);
    }

    class RoleActionPanel extends JPanel {
        protected JButton btnEdit = new JButton("Sửa");
        protected JButton btnDelete = new JButton("Xóa");

        public RoleActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 8));
            setOpaque(true);
            styleButton(btnEdit, new Color(0, 122, 255), 60, 30);
            styleButton(btnDelete, new Color(255, 59, 48), 60, 30);
            add(btnEdit);
            add(btnDelete);
        }

        protected void styleButton(JButton btn, Color color, int width, int height) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(color);
            btn.setBackground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(color, 1));
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(width, height));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    class RoleActionButtonRenderer implements TableCellRenderer {
        protected JPanel panel;

        public RoleActionButtonRenderer(JPanel panel) {
            this.panel = panel;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return panel;
        }
    }

    class RoleActionButtonEditor extends DefaultCellEditor {
        protected RoleActionPanel panel;
        protected RoleActionListener listener;
        protected int currentRow;

        public RoleActionButtonEditor(RoleActionListener listener, RoleActionPanel panel) {
            super(new JCheckBox());
            this.listener = listener;
            this.panel = panel;

            this.panel.btnEdit.addActionListener(e -> { stopCellEditing(); listener.onEdit(currentRow); });
            this.panel.btnDelete.addActionListener(e -> { stopCellEditing(); listener.onDelete(currentRow); });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override public Object getCellEditorValue() { return ""; }
    }
}