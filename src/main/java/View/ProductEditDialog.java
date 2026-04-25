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
 * Giao diện Cập nhật / Xóa Sản Phẩm (màn hình chi tiết món).
 */
public class ProductEditDialog extends JDialog {

    private JTabbedPane tabbedPane;
    private JPanel tabInfo, tabRecipe;

    private Color PRIMARY_COLOR = AppColor.PRIMARY;
    private Color DANGER_COLOR  = new Color(231, 76, 60);
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

    // Tab Công thức
    private JComboBox<String> cbIngredient;
    private JComboBox<String> cbUnit;
    private JTextField txtQuantitative;
    private JTable recipeTable;
    private DefaultTableModel recipeModel;
    private JLabel lblTotalCost;
    private JButton btnAddRecipe;

    // Buttons Tab 1
    private JButton btnUpload;
    private JButton btnUpdate;
    private JButton btnDelete;

    // Listener cho bảng công thức
    private TableActionSupport.SplitActionListener recipeTableListener;

    public ProductEditDialog(Frame parent) {
        super(parent, "CHI TIẾT SẢN PHẨM", true);
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

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(getWidth(), 50));
        JLabel title = new JLabel("  THÔNG TIN CHI TIẾT & CẬP NHẬT MÓN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);

        initTabInfo();
        initTabRecipe();

        tabbedPane.addTab("Thông tin chung", tabInfo);
        tabbedPane.addTab("Công thức",       tabRecipe);
        add(tabbedPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.setBackground(new Color(245, 245, 245));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnCancel = createModernButton("Đóng", new Color(220, 220, 220), TEXT_DARK);
        btnCancel.addActionListener(e -> dispose());
        footer.add(btnCancel);
        add(footer, BorderLayout.SOUTH);
    }

    private void initTabInfo() {
        tabInfo = new JPanel(new BorderLayout(20, 15));
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

        btnUpload = new JButton("Đổi ảnh");
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

        // Footer của tab
        JPanel tabFooter = new JPanel(new BorderLayout());
        tabFooter.setOpaque(false);
        tabFooter.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
                new EmptyBorder(10, 0, 0, 0)));

        JPanel leftFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftFooter.setOpaque(false);
        btnDelete = createModernButton("Xóa sản phẩm", DANGER_COLOR, Color.WHITE);
        leftFooter.add(btnDelete);

        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightFooter.setOpaque(false);
        btnUpdate = createModernButton("Cập nhật thay đổi", PRIMARY_COLOR, Color.WHITE);
        rightFooter.add(btnUpdate);

        tabFooter.add(leftFooter,  BorderLayout.WEST);
        tabFooter.add(rightFooter, BorderLayout.EAST);

        tabInfo.add(leftPanel,     BorderLayout.WEST);
        tabInfo.add(rightContainer, BorderLayout.CENTER);
        tabInfo.add(tabFooter,     BorderLayout.SOUTH);
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

        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(248, 249, 250));
        inputPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 15, 15, 15)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        cbIngredient  = new JComboBox<>();
        cbUnit        = new JComboBox<>(new String[]{"kg", "gram", "lit", "ml"});
        cbUnit.setBackground(Color.WHITE);
        txtQuantitative = createStyledTextField("");
        btnAddRecipe    = createModernButton("Thêm", PRIMARY_COLOR, Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4; inputPanel.add(new JLabel("Nguyên liệu:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(cbIngredient, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.2; inputPanel.add(new JLabel("Đơn vị:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(cbUnit, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.2; inputPanel.add(new JLabel("Định lượng:"), gbc);
        gbc.gridx = 2; gbc.gridy = 1; inputPanel.add(txtQuantitative, gbc);

        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.2; inputPanel.add(btnAddRecipe, gbc);

        // Bảng công thức
        String[] cols = {"Mã NL", "Tên Nguyên Liệu", "Đơn vị", "Định lượng", "Thành tiền", "Hành động"};
        recipeModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        recipeTable = new JTable(recipeModel);
        recipeTable.setRowHeight(45);
        recipeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recipeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        recipeTable.getTableHeader().setBackground(new Color(242, 242, 242));
        recipeTable.getTableHeader().setForeground(TEXT_DARK);
        recipeTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        recipeTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        recipeTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        recipeTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        recipeTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        recipeTable.getColumnModel().getColumn(5).setPreferredWidth(140);

        TableColumn actionCol = recipeTable.getColumnModel().getColumn(5);
        actionCol.setCellRenderer(TableActionSupport.renderer());
        actionCol.setCellEditor(TableActionSupport.editor(new TableActionSupport.SplitActionListener() {
            @Override public void onEdit(int row)   { if (recipeTableListener != null) recipeTableListener.onEdit(row); }
            @Override public void onDelete(int row) { if (recipeTableListener != null) recipeTableListener.onDelete(row); }
        }));

        // Tổng giá vốn
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.setOpaque(false);
        lblTotalCost = new JLabel("Tổng giá vốn: 0 VNĐ");
        lblTotalCost.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalCost.setForeground(PRIMARY_COLOR);
        summaryPanel.add(lblTotalCost);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(inputPanel,                    BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(recipeTable),  BorderLayout.CENTER);
        centerPanel.add(summaryPanel,                  BorderLayout.SOUTH);

        tabRecipe.add(recipeHeader, BorderLayout.NORTH);
        tabRecipe.add(centerPanel,  BorderLayout.CENTER);
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
    
    private String formatCurrency(double amount) {
        java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // Dấu chấm cho hàng nghìn

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0", symbols);
        return df.format(amount) + " VNĐ";
    }

    // =====================================================================
    // PUBLIC API — NẠP DỮ LIỆU
    // =====================================================================

    public void setCategoryList(List<String> categories) {
        cbOption.removeAllItems();
        if (categories != null) {
            for (String cat : categories) cbOption.addItem(cat);
        }
    }

    public void setIngredientList(ArrayList<String> ingredients) {
        cbIngredient.removeAllItems();
        if (ingredients != null) {
            for (String name : ingredients) cbIngredient.addItem(name);
        }
    }

    public void loadOptionCheckboxes(HashMap<String, ArrayList<OptionModel>> optionsMap,
                                     ArrayList<OptionModel> optionSelected) {
        this.currentOptionGroups = optionsMap;
        optionsPanel.removeAll();
        optionCheckboxMap.clear();
        if (optionsMap == null) { optionsPanel.revalidate(); optionsPanel.repaint(); return; }

        GridBagConstraints optGbc = new GridBagConstraints();
        optGbc.fill   = GridBagConstraints.HORIZONTAL;
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
                cb.setSelected(optionSelected != null &&
                        optionSelected.stream().anyMatch(o -> o.getOptionName().equalsIgnoreCase(item.getOptionName())));
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

    public void loadRecipeData(List<Model.RecipeModel> recipes) {
        recipeModel.setRowCount(0);
        double totalCost = 0;
        if (recipes != null && !recipes.isEmpty()) {
            for (Model.RecipeModel recipe : recipes) {
                recipeModel.addRow(new Object[]{
                    recipe.getIngredientID(),
                    recipe.getIngredientName(),
                    recipe.getUnit(),
                    recipe.getQuantitative(),
                    formatCurrency(recipe.getPrice()),
                    "Sửa / Xóa"
                });
                totalCost += recipe.getPrice();
            }
        }
        lblTotalCost.setText("Tổng giá vốn: " + formatCurrency(totalCost));
    }

    public void setProductData(String name, String category, double price,
                               String status, String description, ImageIcon icon) {
        txtProductName.setText(name);
        cbOption.setSelectedItem(category);
        txtPrice.setText(String.format("%.0f", price));
        txtDescription.setText(description);
        setImage(icon);
        if      (status.equalsIgnoreCase("Đang bán")) rbOnSale.setSelected(true);
        else if (status.equalsIgnoreCase("Tạm hết"))  rbOutOfStock.setSelected(true);
        else                                           rbStopSelling.setSelected(true);
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

    // =====================================================================
    // GETTERS
    // =====================================================================

    public String getProductName()  { return txtProductName.getText().trim(); }
    public String getCategory()     { return (String) cbOption.getSelectedItem(); }
    public String getDescription()  { return txtDescription.getText().trim(); }
    public String getIngredientName() { return (String) cbIngredient.getSelectedItem(); }
    public String getUnit()           { return (String) cbUnit.getSelectedItem(); }

    public double getPrice() {
        try { return Double.parseDouble(txtPrice.getText().trim().replace(".", "").replace(",", "")); }
        catch (Exception e) { return 0; }
    }

    public double getQuantitative() {
        try { return Double.parseDouble(txtQuantitative.getText().trim().replace(".", "").replace(",", "")); }
        catch (Exception e) { return 0; }
    }

    public String getStatus() {
        if (rbOnSale.isSelected())     return "Đang bán";
        if (rbOutOfStock.isSelected()) return "Tạm hết";
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

    // Getters cho dòng bảng công thức
    public int    getRecipeIngredientIdAt(int row)   { return (int)    recipeModel.getValueAt(row, 0); }
    public String getRecipeIngredientNameAt(int row) { return (String) recipeModel.getValueAt(row, 1); }
    public String getRecipeUnitAt(int row)           { return (String) recipeModel.getValueAt(row, 2); }
    public double getRecipeQuantitativeAt(int row)   { return (double) recipeModel.getValueAt(row, 3); }

    // =====================================================================
    // LISTENERS CHO CONTROLLER
    // =====================================================================

    public void addChooseImageListener(ActionListener listener) { btnUpload.addActionListener(listener); }
    public void addUpdateListener(ActionListener listener)      { btnUpdate.addActionListener(listener); }
    public void addDeleteListener(ActionListener listener)      { btnDelete.addActionListener(listener); }
    public void addAddRecipeListener(ActionListener listener)   { btnAddRecipe.addActionListener(listener); }

    public void setRecipeTableListener(TableActionSupport.SplitActionListener listener) {
        this.recipeTableListener = listener;
    }

    // =====================================================================
    // DIALOG SỬA DỮ LIỆU — CHUẨN MVC (UI nằm trong View)
    // =====================================================================

    /** Sửa Công Thức — trả về [unit, qtyStr] hoặc null nếu hủy. */
    public Object[] showEditRecipeDialog(String ingName, String currentUnit, double currentQty) {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        // ===== TITLE =====
        JLabel title = new JLabel("Sửa định lượng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);
        gbc.gridy = 0;
        panel.add(title, gbc);

        // ===== INGREDIENT =====
        gbc.gridy++;
        panel.add(new JLabel("Nguyên liệu"), gbc);

        JTextField txtName = createStyledTextField(ingName);
        txtName.setEditable(false);
        txtName.setBackground(new Color(245, 245, 245));
        gbc.gridy++;
        panel.add(txtName, gbc);

        // ===== UNIT =====
        gbc.gridy++;
        panel.add(new JLabel("Đơn vị"), gbc);

        JComboBox<String> cbUnitDialog = new JComboBox<>(new String[]{"kg", "gram", "lit", "ml"});
        cbUnitDialog.setSelectedItem(currentUnit);
        cbUnitDialog.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy++;
        panel.add(cbUnitDialog, gbc);

        // ===== QUANTITY =====
        gbc.gridy++;
        panel.add(new JLabel("Định lượng"), gbc);

        JTextField txtQty = createStyledTextField(String.valueOf(currentQty));
        gbc.gridy++;
        panel.add(txtQty, gbc);

        // ===== SHOW DIALOG =====
        UIManager.put("OptionPane.okButtonText", "Lưu");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");

        int r = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Chỉnh sửa",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (r == JOptionPane.OK_OPTION) {
            return new Object[]{
                    cbUnitDialog.getSelectedItem(),
                    txtQty.getText().trim()
            };
        }

        return null;
    }
}