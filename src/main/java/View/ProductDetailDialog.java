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
    private JPanel tabInfo, tabCategory, tabOptionGroup, tabOption;

    private Color PRIMARY_COLOR = AppColor.PRIMARY;
    private Color TEXT_DARK     = AppColor.TEXT_DARK;

    // Tab Thông tin chung
    private JTextField txtProductName, txtPrice;
    private JComboBox<String> cbOption;
    private JTextArea txtDescription;
    private JLabel lblImagePlaceholder;
    private JRadioButton rbOnSale, rbOutOfStock, rbStopSelling;
    private JPanel optionsPanel;
    private Map<Integer, JCheckBox> optionCheckboxMap = new LinkedHashMap<>();
    private HashMap<String, ArrayList<OptionModel>> currentOptionGroups = new HashMap<>();

    // Buttons
    private JButton btnSave, btnUpload;
    private JButton btnAddCategory, btnAddOptionGroup, btnAddOption;

    // Tables
    private JTable categoryTable, optionGroupTable, optionTable;
    private DefaultTableModel categoryModel, optionGroupModel, optionModel;

    // Listeners từ Controller cho các bảng
    private TableActionSupport.SplitActionListener categoryTableListener;
    private TableActionSupport.SplitActionListener optionGroupTableListener;
    private TableActionSupport.SplitActionListener optionTableListener;

    public ProductDetailDialog(Frame parent) {
        super(parent, "QUẢN LÝ MÓN VÀ DANH MỤC", true);
        initComponents();
    }

    // =====================================================================
    // KHỞI TẠO GIAO DIỆN
    // =====================================================================

    private void initComponents() {
        setSize(1050, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(getWidth(), 50));
        JLabel title = new JLabel("  QUẢN LÝ MÓN & DANH MỤC TÙY CHỌN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);

        initTabInfo();
        initTabCategory();
        initTabOptionGroup();
        initTabOption();

        tabbedPane.addTab("Thông tin chung",        tabInfo);
        tabbedPane.addTab("Quản lý Loại SP",         tabCategory);
        tabbedPane.addTab("Quản lý Nhóm Tùy Chọn",  tabOptionGroup);
        tabbedPane.addTab("Quản lý Tùy Chọn",        tabOption);
        add(tabbedPane, BorderLayout.CENTER);

        // Footer
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

    private void initTabInfo() {
        tabInfo = new JPanel(new BorderLayout(20, 0));
        tabInfo.setBackground(Color.WHITE);
        tabInfo.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel trái
        JPanel leftPanel = createSectionPanel("Thông tin cơ bản");
        leftPanel.setPreferredSize(new Dimension(400, 0));
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        txtProductName = createStyledTextField("");
        cbOption       = new JComboBox<>();
        txtPrice       = createStyledTextField("");
        txtDescription = new JTextArea(4, 20);
        txtDescription.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; leftPanel.add(new JLabel("Tên món: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; leftPanel.add(txtProductName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; leftPanel.add(new JLabel("Danh mục: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; leftPanel.add(cbOption, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; leftPanel.add(new JLabel("Giá bán: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; leftPanel.add(txtPrice, gbc);

        // Ảnh
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

        imgGbc.gridx = 0; imgGbc.gridy = 0; imgPanel.add(lblImagePlaceholder, imgGbc);
        imgGbc.gridx = 1; imgGbc.gridy = 0; imgPanel.add(btnUpload, imgGbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; leftPanel.add(new JLabel("Hình ảnh:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; leftPanel.add(imgPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0; gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        leftPanel.add(new JLabel("Mô tả:"), gbc);

        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        leftPanel.add(new JScrollPane(txtDescription), gbc);

        // Panel phải
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setOpaque(false);
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.fill    = GridBagConstraints.BOTH;
        rightGbc.weightx = 1.0;
        rightGbc.insets  = new Insets(0, 0, 10, 0);

        optionsPanel = createSectionPanel("Tùy chọn & Biến thể");
        optionsPanel.setLayout(new GridBagLayout());

        JPanel statusPanel = createSectionPanel("Trạng thái");
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 8));
        rbOnSale      = new JRadioButton("Đang bán", true);
        rbOutOfStock  = new JRadioButton("Tạm hết");
        rbStopSelling = new JRadioButton("Ngừng bán");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbOnSale); bg.add(rbOutOfStock); bg.add(rbStopSelling);
        statusPanel.add(rbOnSale); statusPanel.add(rbOutOfStock); statusPanel.add(rbStopSelling);

        rightGbc.gridy = 0; rightGbc.weighty = 1.0; rightContainer.add(optionsPanel, rightGbc);
        rightGbc.gridy = 1; rightGbc.weighty = 0.0; rightGbc.insets = new Insets(0, 0, 0, 0);
        rightContainer.add(statusPanel, rightGbc);

        tabInfo.add(leftPanel,     BorderLayout.WEST);
        tabInfo.add(rightContainer, BorderLayout.CENTER);
    }

    private void initTabCategory() {
        tabCategory = new JPanel(new BorderLayout(15, 15));
        tabCategory.setBackground(Color.WHITE);
        tabCategory.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlPanel.setOpaque(false);
        btnAddCategory = createModernButton("Thêm Loại SP", PRIMARY_COLOR, Color.WHITE);
        controlPanel.add(btnAddCategory);

        String[] cols = {"Mã Loại", "Tên Loại Sản Phẩm", "Trạng Thái", "Hành động"};
        categoryModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        categoryTable = new JTable(categoryModel);
        styleTable(categoryTable);
        categoryTable.setRowHeight(45);
        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        categoryTable.getColumnModel().getColumn(3).setPreferredWidth(140);

        TableColumn actionCol = categoryTable.getColumnModel().getColumn(3);
        actionCol.setCellRenderer(TableActionSupport.renderer());
        actionCol.setCellEditor(TableActionSupport.editor(new TableActionSupport.SplitActionListener() {
            @Override public void onEdit(int row)   { if (categoryTableListener != null) categoryTableListener.onEdit(row); }
            @Override public void onDelete(int row) { if (categoryTableListener != null) categoryTableListener.onDelete(row); }
        }));

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
        controlPanel.add(btnAddOptionGroup);

        String[] cols = {"Mã Nhóm", "Tên Nhóm Tùy Chọn", "Hành động"};
        optionGroupModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2; }
        };
        optionGroupTable = new JTable(optionGroupModel);
        styleTable(optionGroupTable);
        optionGroupTable.setRowHeight(45);
        optionGroupTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        optionGroupTable.getColumnModel().getColumn(1).setPreferredWidth(600);
        optionGroupTable.getColumnModel().getColumn(2).setPreferredWidth(140);

        TableColumn actionCol = optionGroupTable.getColumnModel().getColumn(2);
        actionCol.setCellRenderer(TableActionSupport.renderer());
        actionCol.setCellEditor(TableActionSupport.editor(new TableActionSupport.SplitActionListener() {
            @Override public void onEdit(int row)   { if (optionGroupTableListener != null) optionGroupTableListener.onEdit(row); }
            @Override public void onDelete(int row) { if (optionGroupTableListener != null) optionGroupTableListener.onDelete(row); }
        }));

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
        controlPanel.add(btnAddOption);

        String[] cols = {"Mã Tùy Chọn", "Thuộc Nhóm", "Tên Tùy Chọn", "Phụ Thu", "Trạng Thái", "Hành động"};
        optionModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        optionTable = new JTable(optionModel);
        styleTable(optionTable);
        optionTable.setRowHeight(45);
        optionTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        optionTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        optionTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        optionTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        optionTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        optionTable.getColumnModel().getColumn(5).setPreferredWidth(140);

        TableColumn actionCol = optionTable.getColumnModel().getColumn(5);
        actionCol.setCellRenderer(TableActionSupport.renderer());
        actionCol.setCellEditor(TableActionSupport.editor(new TableActionSupport.SplitActionListener() {
            @Override public void onEdit(int row)   { if (optionTableListener != null) optionTableListener.onEdit(row); }
            @Override public void onDelete(int row) { if (optionTableListener != null) optionTableListener.onDelete(row); }
        }));

        tabOption.add(controlPanel, BorderLayout.NORTH);
        tabOption.add(new JScrollPane(optionTable), BorderLayout.CENTER);
    }

    // =====================================================================
    // UI HELPER
    // =====================================================================

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new TitledBorder(
                new LineBorder(new Color(230, 230, 230)), title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)));
        return p;
    }

    private JTextField createStyledTextField(String text) {
        JTextField tf = new JTextField(text, 15);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)));
        return tf;
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
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
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(Color.WHITE);
        header.setForeground(TEXT_DARK);
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.setSelectionBackground(new Color(0, 122, 255, 40));
        table.setSelectionForeground(TEXT_DARK);

        // Zebra rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean isSelected,
                    boolean hasFocus, int row, int col) {

                Component c = super.getTableCellRendererComponent(
                        t, val, isSelected, hasFocus, row, col);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    // =====================================================================
    // PUBLIC API — NẠP DỮ LIỆU
    // =====================================================================

    public void loadCategoryData(ProductCategoryListModel dataList) {
        categoryModel.setRowCount(0);
        cbOption.removeAllItems();
        if (dataList != null && dataList.getProductCategoryList() != null) {
            for (CategoryModel cat : dataList.getProductCategoryList()) {
                categoryModel.addRow(new Object[]{
                    cat.getCategoryID(),
                    cat.getCategoryName(),
                    cat.getCategoryStatus(),
                    "Sửa / Xóa"
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
                    group.getOptionGroupName(),
                    "Sửa / Xóa"
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
                    optionModel.addRow(new Object[]{
                        item.getOptionID(),
                        groupName,
                        item.getOptionName(),
                        String.format("%,.0f VNĐ", item.getExtraPrice()),
                        item.getOptionStatus(),
                        "Sửa / Xóa"
                    });
                }
            }
        }

        // Cập nhật checkbox panel ở Tab Thông tin chung
        this.currentOptionGroups = optionsMap;
        optionsPanel.removeAll();
        optionCheckboxMap.clear();
        if (optionsMap == null) { optionsPanel.revalidate(); optionsPanel.repaint(); return; }

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
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                cb.setOpaque(false);
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
        optionsPanel.revalidate();
        optionsPanel.repaint();
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

    public void clearForm() {
        if (txtProductName == null) return;
        txtProductName.setText("");
        txtPrice.setText("");
        txtDescription.setText("");
        if (cbOption.getItemCount() > 0) cbOption.setSelectedIndex(0);
        rbOnSale.setSelected(true);
        lblImagePlaceholder.setIcon(null);
        lblImagePlaceholder.setText("Hình ảnh mẫu");
        for (JCheckBox cb : optionCheckboxMap.values()) cb.setSelected(false);
    }

    // =====================================================================
    // GETTERS
    // =====================================================================

    public String getProductName()  { return txtProductName.getText().trim(); }
    public String getCategory()     { return (String) cbOption.getSelectedItem(); }
    public String getDescription()  { return txtDescription.getText().trim(); }

    public double getPrice() {
        try { return Double.parseDouble(txtPrice.getText().trim().replace(".", "").replace(",", "")); }
        catch (Exception e) { return 0; }
    }

    public String getStatus() {
        if (rbOnSale.isSelected())      return "Đang bán";
        if (rbOutOfStock.isSelected())  return "Tạm hết";
        return "Ngừng bán";
    }

    public HashMap<String, List<String>> getSelectedOptionNamesByGroup() {
        HashMap<String, List<String>> result = new HashMap<>();
        if (currentOptionGroups == null || currentOptionGroups.isEmpty()) return result;
        for (Map.Entry<String, ArrayList<OptionModel>> entry : currentOptionGroups.entrySet()) {
            List<String> selected = new ArrayList<>();
            for (OptionModel item : entry.getValue()) {
                JCheckBox cb = optionCheckboxMap.get(item.getOptionID());
                if (cb != null && cb.isSelected()) selected.add(item.getOptionName());
            }
            if (!selected.isEmpty()) result.put(entry.getKey(), selected);
        }
        return result;
    }

    // Getters cho từng dòng bảng
    public int    getCategoryIdAt(int row)       { return (int)    categoryModel.getValueAt(row, 0); }
    public String getCategoryNameAt(int row)     { return (String) categoryModel.getValueAt(row, 1); }

    public int    getOptionGroupIdAt(int row)    { return (int)    optionGroupModel.getValueAt(row, 0); }
    public String getOptionGroupNameAt(int row)  { return (String) optionGroupModel.getValueAt(row, 1); }

    public int    getOptionIdAt(int row)         { return (int)    optionModel.getValueAt(row, 0); }
    public String getOptionNameAt(int row)       { return (String) optionModel.getValueAt(row, 2); }

    // =====================================================================
    // LISTENERS CHO CONTROLLER
    // =====================================================================

    public void addChooseImageListener(ActionListener listener)   { btnUpload.addActionListener(listener); }
    public void addSaveListener(ActionListener listener)          { btnSave.addActionListener(listener); }
    public void addAddCategoryListener(ActionListener listener)   { btnAddCategory.addActionListener(listener); }
    public void addAddOptionGroupListener(ActionListener listener){ btnAddOptionGroup.addActionListener(listener); }
    public void addAddOptionListener(ActionListener listener)     { btnAddOption.addActionListener(listener); }

    public void setCategoryTableListener(TableActionSupport.SplitActionListener listener)    { this.categoryTableListener    = listener; }
    public void setOptionGroupTableListener(TableActionSupport.SplitActionListener listener) { this.optionGroupTableListener = listener; }
    public void setOptionTableListener(TableActionSupport.SplitActionListener listener)      { this.optionTableListener      = listener; }

    // =====================================================================
    // DIALOG SỬA / THÊM DỮ LIỆU — CHUẨN MVC (UI nằm trong View)
    // =====================================================================

    /** Thêm Tùy Chọn mới — trả về [groupName, optionName, priceStr] hoặc null nếu hủy. */
    public Object[] showAddOptionDialog(ArrayList<OptionGroupModel> groups) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JComboBox<String> cbGroup = new JComboBox<>();
        if (groups != null) {
            for (OptionGroupModel g : groups) cbGroup.addItem(g.getOptionGroupName());
        }
        JTextField txtOptionName  = new JTextField();
        JTextField txtExtraPrice  = new JTextField("0");

        panel.add(new JLabel("Chọn Nhóm Tùy Chọn:"));
        panel.add(cbGroup);
        panel.add(new JLabel("Tên Tùy Chọn (VD: Size L, Trân châu đen):"));
        panel.add(txtOptionName);
        panel.add(new JLabel("Giá Phụ Thu (VNĐ):"));
        panel.add(txtExtraPrice);

        int r = JOptionPane.showConfirmDialog(this, panel, "Thêm Tùy Chọn Mới",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            return new Object[]{ cbGroup.getSelectedItem(), txtOptionName.getText().trim(), txtExtraPrice.getText().trim() };
        }
        return null;
    }

    /** Sửa Loại Sản Phẩm — trả về [newName, status] hoặc null nếu hủy. */
    public Object[] showEditCategoryDialog(String currentName) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtName = new JTextField(currentName);
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đang sử dụng", "Chưa sử dụng"});

        panel.add(new JLabel("Tên Loại Sản Phẩm:")); panel.add(txtName);
        panel.add(new JLabel("Trạng thái:"));         panel.add(cbStatus);

        int r = JOptionPane.showConfirmDialog(this, panel, "Sửa Loại Sản Phẩm",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            return new Object[]{ txtName.getText().trim(), cbStatus.getSelectedItem() };
        }
        return null;
    }

    /** Sửa Nhóm Tùy Chọn — trả về tên mới hoặc null nếu hủy / để trống. */
    public String showEditOptionGroupDialog(String currentName) {
        String newName = JOptionPane.showInputDialog(this, "Sửa Tên Nhóm Tùy Chọn:", currentName);
        return (newName != null && !newName.trim().isEmpty()) ? newName.trim() : null;
    }

    /** Sửa Tùy Chọn Chi Tiết — trả về [groupName, name, priceStr, status] hoặc null nếu hủy. */
    public Object[] showEditOptionDialog(String currentName, ArrayList<OptionGroupModel> groups) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JComboBox<String> cbGroup = new JComboBox<>();
        if (groups != null) {
            for (OptionGroupModel g : groups) cbGroup.addItem(g.getOptionGroupName());
        }
        JTextField txtName   = new JTextField(currentName);
        JTextField txtPhuThu = new JTextField("0");
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đang sử dụng", "Chưa sử dụng"});

        panel.add(new JLabel("Thuộc Nhóm:"));        panel.add(cbGroup);
        panel.add(new JLabel("Tên Tùy Chọn:"));      panel.add(txtName);
        panel.add(new JLabel("Giá Phụ Thu (VNĐ):")); panel.add(txtPhuThu);
        panel.add(new JLabel("Trạng Thái:"));         panel.add(cbStatus);

        int r = JOptionPane.showConfirmDialog(this, panel, "Sửa Tùy Chọn",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            return new Object[]{ cbGroup.getSelectedItem(), txtName.getText().trim(), txtPhuThu.getText().trim(), cbStatus.getSelectedItem() };
        }
        return null;
    }
}