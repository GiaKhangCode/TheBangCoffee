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
 * Giao diện Cập nhật / Xóa Sản Phẩm (Dành riêng cho màn hình chi tiết món)
 */
public class ProductEditDialog extends JDialog {

    private JTabbedPane tabbedPane;
    private JPanel tabInfo, tabRecipe;

    private Color PRIMARY_COLOR = AppColor.PRIMARY;
    private Color DANGER_COLOR = new Color(231, 76, 60); // Màu đỏ cho nút Xóa
    private Color TEXT_DARK = AppColor.TEXT_DARK;

    // Tab 1 Components
    private JTextField txtProductName, txtPrice;
    private JComboBox<String> cbOption;
    private JTextArea txtDescription;
    private JLabel lblImagePlaceholder;
    private JRadioButton rbOnSale, rbOutOfStock, rbStopSelling;
    private JPanel optionsPanel;
    private Map<Integer, JCheckBox> optionCheckboxMap = new LinkedHashMap<>();
    private HashMap<String, ArrayList<OptionModel>> currentOptionGroups = new HashMap<>();

    // Tab 2 Components
    private JComboBox<String> cbIngredient;
    private JComboBox<String> cbUnit;
    private JTextField txtQuantitative;
    private JTable recipeTable;
    private DefaultTableModel recipeModel;
    private JLabel lblTotalCost;
    private JButton btnAddRecipe;

    // Buttons
    private JButton btnUpload;
    private JButton btnUpdate;
    private JButton btnDelete;

    public ProductEditDialog(Frame parent) {
        super(parent, "CHI TIẾT SẢN PHẨM", true);
        initComponents();
    }

    private void initComponents() {
        setSize(1050, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(getWidth(), 50));
        JLabel title = new JLabel("  THÔNG TIN CHI TIẾT & CẬP NHẬT MÓN");
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
        tabbedPane.addTab("Công thức", tabRecipe);
        
        add(tabbedPane, BorderLayout.CENTER);

        // Footer của Cửa sổ CHỈ CÒN nút ĐÓNG
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.setBackground(new Color(245, 245, 245));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnCancel = createModernButton("Đóng", new Color(220, 220, 220), TEXT_DARK);
        btnCancel.addActionListener(e -> dispose());
        
        footer.add(btnCancel);
        add(footer, BorderLayout.SOUTH);
    }

    private void initTabInfo() {
        // Tăng khoảng cách vertical gap lên 15 để Footer của Tab cách nội dung một chút
        tabInfo = new JPanel(new BorderLayout(20, 15)); 
        tabInfo.setBackground(Color.WHITE);
        tabInfo.setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT PANEL (400px)
        JPanel leftPanel = createSectionPanel("Thông tin cơ bản");
        leftPanel.setPreferredSize(new Dimension(400, 0));
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
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; leftPanel.add(new JLabel("Tên món: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; leftPanel.add(txtProductName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; leftPanel.add(new JLabel("Danh mục: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; leftPanel.add(cbOption, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; leftPanel.add(new JLabel("Giá bán: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; leftPanel.add(txtPrice, gbc);
        
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
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0; gbc.weighty = 0.0; gbc.anchor = GridBagConstraints.NORTHWEST;
        leftPanel.add(new JLabel("Mô tả:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH; 
        leftPanel.add(new JScrollPane(txtDescription), gbc);

        // RIGHT PANEL
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

        rightGbc.gridy = 0; rightGbc.weighty = 1.0; rightContainer.add(optionsPanel, rightGbc);
        rightGbc.gridy = 1; rightGbc.weighty = 0.0; rightGbc.insets = new Insets(0, 0, 0, 0);
        rightContainer.add(statusPanel, rightGbc);

        // ========================================================
        // TAB INFO FOOTER (Chứa nút Xóa và Cập nhật - Gắn riêng vào Tab)
        // ========================================================
        JPanel tabFooter = new JPanel(new BorderLayout());
        tabFooter.setOpaque(false);
        // Đường kẻ xám nhỏ cách điệu phần nút bấm với phần nội dung
        tabFooter.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)), 
                new EmptyBorder(10, 0, 0, 0)
        ));

        JPanel leftFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftFooter.setOpaque(false);
        btnDelete = createModernButton("Xóa sản phẩm", DANGER_COLOR, Color.WHITE);
        leftFooter.add(btnDelete);

        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightFooter.setOpaque(false);
        btnUpdate = createModernButton("Cập nhật thay đổi", PRIMARY_COLOR, Color.WHITE);
        rightFooter.add(btnUpdate);

        tabFooter.add(leftFooter, BorderLayout.WEST);
        tabFooter.add(rightFooter, BorderLayout.EAST);

        // Gắn các khối vào Tab Info
        tabInfo.add(leftPanel, BorderLayout.WEST);
        tabInfo.add(rightContainer, BorderLayout.CENTER);
        tabInfo.add(tabFooter, BorderLayout.SOUTH); 
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
        String[] units = {"kg", "gram", "lit", "ml"}; 
        cbUnit = new JComboBox<>(units); cbUnit.setBackground(Color.WHITE);
        txtQuantitative = createStyledTextField("");
        btnAddRecipe = createModernButton("Thêm", PRIMARY_COLOR, Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4; inputPanel.add(new JLabel("Nguyên liệu:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(cbIngredient, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.2; inputPanel.add(new JLabel("Đơn vị:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(cbUnit, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.2; inputPanel.add(new JLabel("Định lượng:"), gbc);
        gbc.gridx = 2; gbc.gridy = 1; inputPanel.add(txtQuantitative, gbc);
        
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.2; inputPanel.add(btnAddRecipe, gbc);

        String[] cols = {"STT", "Mã NL", "Tên Nguyên Liệu", "Đơn vị", "Định lượng", "Thành tiền"};
        recipeModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        recipeTable = new JTable(recipeModel); 
        
        recipeTable.setRowHeight(40); recipeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recipeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        recipeTable.getTableHeader().setBackground(new Color(242, 242, 242));
        recipeTable.getTableHeader().setForeground(TEXT_DARK);
        
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

    // =====================================================================
    // PUBLIC API - ĐỔ DỮ LIỆU & LẤY DỮ LIỆU
    // =====================================================================

    public void setCategoryList(List<String> categories) {
        cbOption.removeAllItems();
        if (categories != null) {
            for (String cat : categories) {
                cbOption.addItem(cat);
            }
        }
    }
    
    public void setIngredientList(ArrayList<String> ingredients){
        cbIngredient.removeAllItems();
        if(ingredients != null){
            for (String name : ingredients){
                cbIngredient.addItem(name);
            }
        }
    }

    public void loadOptionCheckboxes(HashMap<String, ArrayList<OptionModel>> optionsMap, ArrayList<OptionModel> optionSelected) {
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
                if(optionSelected.stream().anyMatch(option -> option.getOptionName().equalsIgnoreCase(item.getOptionName()))){
                    cb.setSelected(true); 
                }
                else cb.setSelected(false); 
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
    
    public void loadRecipeData(List<Model.RecipeModel> recipes) {
        recipeModel.setRowCount(0); // Xóa dữ liệu cũ
        double totalCost = 0;
        
        if (recipes != null && !recipes.isEmpty()) {
            int stt = 1;
            for (Model.RecipeModel recipe : recipes) {
                String thanhTienFormatted = String.format("%,.0f VNĐ", recipe.getPrice());
                recipeModel.addRow(new Object[]{
                    stt++,
                    recipe.getIngredientID(),
                    recipe.getIngredientName(),
                    recipe.getUnit(),
                    recipe.getQuantitative(),
                    thanhTienFormatted
                });
                totalCost += recipe.getPrice();
            }
        }
        // Cập nhật nhãn Tổng giá vốn dưới góc phải bảng
        lblTotalCost.setText("Tổng giá vốn: " + String.format("%,.0f VNĐ", totalCost));
    }

    public void setProductData(String name, String category, double price, String status, String description, ImageIcon icon) {
        txtProductName.setText(name);
        cbOption.setSelectedItem(category);
        txtPrice.setText(String.format("%.0f", price));
        txtDescription.setText(description);
        setImage(icon);

        if (status.equalsIgnoreCase("Đang bán")) rbOnSale.setSelected(true);
        else if (status.equalsIgnoreCase("Tạm hết")) rbOutOfStock.setSelected(true);
        else rbStopSelling.setSelected(true);
    }

    public void setSelectedOptions(List<Integer> selectedIds) {
        for (Integer id : selectedIds) {
            if (optionCheckboxMap.containsKey(id)) {
                optionCheckboxMap.get(id).setSelected(true);
            }
        }
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

    // =====================================================================
    // GETTERS / LISTENERS CHO CONTROLLER
    // =====================================================================
    public void addChooseImageListener(ActionListener listener) { btnUpload.addActionListener(listener); }
    public void addUpdateListener(ActionListener listener) { btnUpdate.addActionListener(listener); }
    public void addDeleteListener(ActionListener listener) { btnDelete.addActionListener(listener); }
    public void addAddRecipeListener(ActionListener listener) { 
        btnAddRecipe.addActionListener(listener); 
    }
    public String getProductName() { return txtProductName.getText().trim(); }
    public double getPrice() {
        try { 
            return Double.parseDouble(txtPrice.getText().trim().replace(".", "").replace(",", "")); 
        } 
        catch (Exception e) {
            return 0; 
        }
    }
    public String getCategory() { return (String) cbOption.getSelectedItem(); }
    public String getStatus() {
        if (rbOnSale.isSelected()) return "Đang bán";
        if (rbOutOfStock.isSelected()) return "Tạm hết";
        return "Ngừng bán";
    }
    public String getDescription() { return txtDescription.getText().trim(); }
    
    public String getIngredientName(){
        return (String) cbIngredient.getSelectedItem();
    }
    
    public String getUnit(){
        return (String) cbUnit.getSelectedItem();
    }
    
    public double getQuantitative(){
        try { 
            return Double.parseDouble(txtQuantitative.getText().trim().replace(".", "").replace(",", "")); 
        } 
        catch (Exception e) {
            return 0; 
        }
    }

    // =====================================================================
    // CLASS HỖ TRỢ BÊN TRONG
    // =====================================================================
    class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false); minimum.width -= (getHgap() + 1); return minimum;
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getParent().getWidth();
                if (targetWidth == 0) targetWidth = 400; 
                int hgap = getHgap(); int vgap = getVgap(); Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;
                Dimension dim = new Dimension(0, 0); int rowWidth = 0; int rowHeight = 0;
                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.width = Math.max(dim.width, rowWidth); dim.height += rowHeight + vgap;
                            rowWidth = 0; rowHeight = 0;
                        }
                        rowWidth += d.width + hgap; rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight + insets.top + insets.bottom + vgap * 2; return dim;
            }
        }
    }
}