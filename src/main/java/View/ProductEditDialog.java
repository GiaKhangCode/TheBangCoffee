package View;

import Model.ToppingModel;
import Model.VariantModel;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductEditDialog extends JDialog {

    private JTabbedPane tabbedPane;
    private JPanel tabInfo, tabRecipe;

    private Color PRIMARY_COLOR = AppColor.PRIMARY;
    private Color DANGER_COLOR = new Color(231, 76, 60); 
    private Color TEXT_DARK = AppColor.TEXT_DARK;

    // [SỬA] Thêm 3 Textfield giá
    private JTextField txtProductName, txtDineInPrice, txtTakeawayPrice, txtHolidayPrice, txtVat;
    private JComboBox<String> cbCategory;
    private JTextArea txtDescription;
    private JLabel lblImagePlaceholder;
    private JRadioButton rbOnSale, rbOutOfStock, rbStopSelling;
    
    private JTable variantTable;
    private DefaultTableModel variantModel;
    private JButton btnAddVariant;

    private JPanel toppingsPanel;
    private Map<Integer, JCheckBox> toppingCheckboxMap = new LinkedHashMap<>();

    private JComboBox<VariantModel> cbVariantRecipe; 
    private JComboBox<String> cbIngredient;
    private ArrayList<String> allIngredientsCache = new ArrayList<>();
    private JTextField txtUnit; 
    private JTextField txtQuantitative;
    private JTable recipeTable;
    private DefaultTableModel recipeModel;
    private JButton btnAddRecipe;
    private JLabel lblTotalCost; 

    private JButton btnUpload, btnUpdate, btnDelete;
    
    private boolean hasEditPermission = true;
    private boolean hasDeletePermission = true;
    
    private ProductActionListener recipeTableListener; 
    private DeleteActionListener variantDeleteListener;

    public ProductEditDialog(Frame parent) {
        super(parent, "CHI TIẾT SẢN PHẨM", true);
        initComponents();
    }

    private void initComponents() {
        setSize(1100, 650);
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
        tabbedPane.addTab("Công thức", tabRecipe);
        
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

        JPanel leftPanel = createSectionPanel("Thông tin cơ bản");
        leftPanel.setPreferredSize(new Dimension(450, 0));
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        txtProductName = createStyledTextField("");
        cbCategory = new JComboBox<>();
        
        // [SỬA] Khởi tạo 3 ô nhập giá
        txtDineInPrice = createStyledTextField("0");
        txtTakeawayPrice = createStyledTextField("0");
        txtHolidayPrice = createStyledTextField("0");
        txtVat = createStyledTextField("8");
        
        txtDescription = new JTextArea(3, 20);
        txtDescription.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        txtDescription.setLineWrap(true); txtDescription.setWrapStyleWord(true);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; leftPanel.add(new JLabel("Tên món: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; leftPanel.add(txtProductName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; leftPanel.add(new JLabel("Danh mục: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; leftPanel.add(cbCategory, gbc);
        
        // [SỬA] Bố cục lại phần hiển thị Giá
        JPanel pricePanel1 = new JPanel(new GridLayout(1, 2, 10, 0));
        pricePanel1.setOpaque(false);
        pricePanel1.add(createInputWrapper("Giá tại quán:", txtDineInPrice));
        pricePanel1.add(createInputWrapper("Giá mang về:", txtTakeawayPrice));

        JPanel pricePanel2 = new JPanel(new GridLayout(1, 2, 10, 0));
        pricePanel2.setOpaque(false);
        pricePanel2.add(createInputWrapper("Giá ngày lễ:", txtHolidayPrice));
        pricePanel2.add(createInputWrapper("Thuế VAT (%):", txtVat));
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; leftPanel.add(pricePanel1, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; leftPanel.add(pricePanel2, gbc);
        
        JPanel imgPanel = new JPanel(new GridBagLayout()); imgPanel.setOpaque(false);
        GridBagConstraints imgGbc = new GridBagConstraints(); imgGbc.anchor = GridBagConstraints.WEST; imgGbc.insets = new Insets(0, 0, 0, 15);
        
        lblImagePlaceholder = new JLabel("Hình ảnh mẫu", SwingConstants.CENTER);
        lblImagePlaceholder.setPreferredSize(new Dimension(100, 100));
        lblImagePlaceholder.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        btnUpload = new JButton("Đổi ảnh");
        
        imgGbc.gridx = 0; imgGbc.gridy = 0; imgPanel.add(lblImagePlaceholder, imgGbc);
        imgGbc.gridx = 1; imgGbc.gridy = 0; imgPanel.add(btnUpload, imgGbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0; leftPanel.add(new JLabel("Hình ảnh:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; leftPanel.add(imgPanel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.NORTHWEST;
        leftPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH; 
        leftPanel.add(new JScrollPane(txtDescription), gbc);

        // ==== RIGHT PANEL ====
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setOpaque(false);
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.fill = GridBagConstraints.BOTH; rightGbc.weightx = 1.0; rightGbc.insets = new Insets(0, 0, 10, 0);

        // Biến thể
        JPanel variantPanel = createSectionPanel("Biến thể (Size & Giá)");
        variantPanel.setLayout(new BorderLayout(0, 5));
        btnAddVariant = new JButton("+ Thêm Size");
        JPanel varCtrlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        varCtrlPanel.setOpaque(false); varCtrlPanel.add(btnAddVariant);
        
        // [SỬA LẠI] Model có 6 cột: ID, Tên Size, Tại quán, Mang về, Ngày lễ, Xóa
        String[] varCols = {"ID", "Tên Size (M, L...)", "Tại quán", "Mang về", "Ngày lễ", "Xóa"};
        variantModel = new DefaultTableModel(varCols, 0) { 
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return c == 1 || c == 2 || c == 3 || c == 4 || c == 5; 
            } 
        };
        variantTable = new JTable(variantModel); 
        variantTable.setRowHeight(30);

        // Ẩn cột ID đi
        variantTable.removeColumn(variantTable.getColumnModel().getColumn(0));
        
        // [SỬA LẠI] Cột Xóa bây giờ là cột thứ 4 trên giao diện (sau khi ẩn ID)
        TableColumn delCol = variantTable.getColumnModel().getColumn(4);
        delCol.setCellRenderer(new DeleteActionButtonRenderer(new DeleteActionPanel()));
        delCol.setCellEditor(new DeleteActionButtonEditor(row -> {
            if (variantTable.isEditing()) variantTable.getCellEditor().stopCellEditing();
            if (variantDeleteListener != null) variantDeleteListener.onDelete(row);
            else if (row >= 0 && row < variantModel.getRowCount()) variantModel.removeRow(row);
        }, new DeleteActionPanel()));
        
        btnAddVariant.addActionListener(e -> variantModel.addRow(new Object[]{0, "", "0", "0", "0", "Xóa"}));
        
        JScrollPane scrollVar = new JScrollPane(variantTable); scrollVar.setPreferredSize(new Dimension(0, 120));
        variantPanel.add(varCtrlPanel, BorderLayout.NORTH); variantPanel.add(scrollVar, BorderLayout.CENTER);

        // Topping
        toppingsPanel = createSectionPanel("Topping khả dụng");
        toppingsPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10));
        
        // Status
        JPanel statusPanel = createSectionPanel("Trạng thái");
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 8));
        rbOnSale = new JRadioButton("Đang bán", true); rbOutOfStock = new JRadioButton("Tạm hết"); rbStopSelling = new JRadioButton("Ngừng bán");
        ButtonGroup bg = new ButtonGroup(); bg.add(rbOnSale); bg.add(rbOutOfStock); bg.add(rbStopSelling);
        statusPanel.add(rbOnSale); statusPanel.add(rbOutOfStock); statusPanel.add(rbStopSelling);

        rightGbc.gridy = 0; rightGbc.weighty = 0.4; rightContainer.add(variantPanel, rightGbc);
        rightGbc.gridy = 1; rightGbc.weighty = 0.5; rightContainer.add(new JScrollPane(toppingsPanel), rightGbc);
        rightGbc.gridy = 2; rightGbc.weighty = 0.1; rightGbc.insets = new Insets(0, 0, 0, 0); rightContainer.add(statusPanel, rightGbc);

        // Footer của Info Tab
        JPanel tabFooter = new JPanel(new BorderLayout()); tabFooter.setOpaque(false);
        tabFooter.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)), new EmptyBorder(10, 0, 0, 0)));
        JPanel leftFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); leftFooter.setOpaque(false);
        btnDelete = createModernButton("Xóa sản phẩm", DANGER_COLOR, Color.WHITE); leftFooter.add(btnDelete);
        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); rightFooter.setOpaque(false);
        btnUpdate = createModernButton("Cập nhật thay đổi", PRIMARY_COLOR, Color.WHITE); rightFooter.add(btnUpdate);

        tabFooter.add(leftFooter, BorderLayout.WEST); tabFooter.add(rightFooter, BorderLayout.EAST);

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
        JLabel lblTarget = new JLabel("Định lượng theo từng Size");
        lblTarget.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recipeHeader.add(lblTarget);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(248, 249, 250));
        inputPanel.setBorder(new CompoundBorder(new LineBorder(new Color(230, 230, 230)), new EmptyBorder(15, 15, 15, 15)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbVariantRecipe = new JComboBox<>();
        cbVariantRecipe.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof VariantModel) setText(((VariantModel) value).getSizeName());
                return this;
            }
        });

        cbIngredient = new JComboBox<>();
        txtUnit = createStyledTextField(""); txtUnit.setEditable(false);
        txtQuantitative = createStyledTextField("");
        btnAddRecipe = createModernButton("Lưu dòng này", PRIMARY_COLOR, Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2; inputPanel.add(new JLabel("Áp dụng cho Size:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(cbVariantRecipe, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.3; inputPanel.add(new JLabel("Nguyên liệu:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(cbIngredient, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.1; inputPanel.add(new JLabel("Đơn vị:"), gbc);
        gbc.gridx = 2; gbc.gridy = 1; inputPanel.add(txtUnit, gbc);
        
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.2; inputPanel.add(new JLabel("Định lượng:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; inputPanel.add(txtQuantitative, gbc);
        
        gbc.gridx = 4; gbc.gridy = 1; gbc.weightx = 0.2; inputPanel.add(btnAddRecipe, gbc);

        String[] cols = {"STT", "Mã NL", "Tên Nguyên Liệu", "Đơn vị", "Số lượng cần", "Hành động"};
        recipeModel = new DefaultTableModel(cols, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return c == 5; } 
        };
        recipeTable = new JTable(recipeModel);
        recipeTable.setRowHeight(40); recipeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recipeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        recipeTable.getTableHeader().setBackground(new Color(242, 242, 242));

        TableColumn actionCol = recipeTable.getColumnModel().getColumn(5);
        actionCol.setCellRenderer(new ProductActionButtonRenderer(new ProductActionPanel()));
        actionCol.setCellEditor(new ProductActionButtonEditor(new ProductActionListener() {
            @Override public void onEdit(int row) { if (recipeTableListener != null) recipeTableListener.onEdit(row); }
            @Override public void onDelete(int row) { if (recipeTableListener != null) recipeTableListener.onDelete(row); }
        }, new ProductActionPanel()));
        
        JPanel recipeFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        recipeFooter.setOpaque(false);
        lblTotalCost = new JLabel("Giá vốn ước tính: 0 đ");
        lblTotalCost.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalCost.setForeground(DANGER_COLOR);
        recipeFooter.add(lblTotalCost);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15)); 
        centerPanel.setOpaque(false);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(recipeTable), BorderLayout.CENTER);
        centerPanel.add(recipeFooter, BorderLayout.SOUTH); 
        
        tabRecipe.add(recipeHeader, BorderLayout.NORTH);
        tabRecipe.add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(); p.setBackground(Color.WHITE);
        p.setBorder(new TitledBorder(new LineBorder(new Color(230, 230, 230)), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));
        return p;
    }
    private JPanel createInputWrapper(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 5)); p.setOpaque(false);
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(l, BorderLayout.NORTH); p.add(comp, BorderLayout.CENTER); return p;
    }
    private JTextField createStyledTextField(String text) {
        JTextField tf = new JTextField(text, 15); tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200)), new EmptyBorder(5, 10, 5, 10))); return tf;
    }
    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); btn.setBackground(bg); btn.setForeground(fg);
        btn.setFocusPainted(false); btn.setBorder(new EmptyBorder(10, 20, 10, 20)); btn.setContentAreaFilled(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ==== PUBLIC API CHO CONTROLLER ====
    public void setCategoryList(List<String> categories) {
        cbCategory.removeAllItems();
        if (categories != null) for (String cat : categories) cbCategory.addItem(cat);
    }
    public void setIngredientList(ArrayList<String> ingredients){
        this.allIngredientsCache = ingredients;
        cbIngredient.removeAllItems();
        if(ingredients != null) {
            for (String name : ingredients) cbIngredient.addItem(name);
        }
    }
    
    public void loadVariantData(List<VariantModel> variants) {
        variantModel.setRowCount(0);
        if (cbVariantRecipe != null) cbVariantRecipe.removeAllItems();

        if(variants != null) {
            for(VariantModel v : variants) {
                // Đổ đủ 6 trường vào model
                variantModel.addRow(new Object[]{ 
                    v.getVariantID(), 
                    v.getSizeName(), 
                    String.format("%,d", v.getDineInPrice()), 
                    String.format("%,d", v.getTakeawayPrice()), 
                    String.format("%,d", v.getHolidayPrice()), 
                    "Xóa" 
                });
                
                if (cbVariantRecipe != null) cbVariantRecipe.addItem(v); 
            }
        }
    }

    public void loadToppingCheckboxes(ArrayList<ToppingModel> allToppings, List<ToppingModel> selectedToppings) {
        toppingsPanel.removeAll(); toppingCheckboxMap.clear();
        if (allToppings == null) return;

        for (ToppingModel top : allToppings) {
            JCheckBox cb = new JCheckBox(top.getLabel());
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 13)); cb.setOpaque(false);
            
            if(selectedToppings != null && selectedToppings.stream().anyMatch(t -> t.getToppingID() == top.getToppingID())) {
                cb.setSelected(true); 
            }
            toppingCheckboxMap.put(top.getToppingID(), cb);
            toppingsPanel.add(cb);
        }
        toppingsPanel.revalidate(); toppingsPanel.repaint();
    }
    
    public List<Integer> getSelectedToppingIds() {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, JCheckBox> entry : toppingCheckboxMap.entrySet()) {
            if (entry.getValue().isSelected()) result.add(entry.getKey());
        }
        return result;
    }
    
    public List<VariantModel> getVariantsFromTable() {
        List<VariantModel> list = new ArrayList<>();
        for(int i = 0; i < variantModel.getRowCount(); i++) {
            
            int id = 0;
            try {
                if (variantModel.getValueAt(i, 0) != null) id = Integer.parseInt(variantModel.getValueAt(i, 0).toString().trim());
            } catch (Exception e) { id = 0; }
            
            String name = "";
            if (variantModel.getValueAt(i, 1) != null) name = variantModel.getValueAt(i, 1).toString().trim();
            
            // 3. Lấy 3 loại giá
            long dineInPrice = 0, takeawayPrice = 0, holidayPrice = 0;
            try { 
                if (variantModel.getValueAt(i, 2) != null) dineInPrice = Long.parseLong(variantModel.getValueAt(i, 2).toString().trim().replace(",", "")); 
                if (variantModel.getValueAt(i, 3) != null) takeawayPrice = Long.parseLong(variantModel.getValueAt(i, 3).toString().trim().replace(",", "")); 
                if (variantModel.getValueAt(i, 4) != null) holidayPrice = Long.parseLong(variantModel.getValueAt(i, 4).toString().trim().replace(",", "")); 
            } catch(Exception e){}
            
            if(!name.isEmpty()) {
                list.add(new VariantModel(id, 0, name, dineInPrice, takeawayPrice, holidayPrice));
            }
        }
        return list;
    }

    public void loadRecipeData(List<Model.RecipeModel> recipes) {
        recipeModel.setRowCount(0); 
        List<String> usedIngredients = new ArrayList<>();

        if (recipes != null && !recipes.isEmpty()) {
            int stt = 1;
            for (Model.RecipeModel r : recipes) {
                recipeModel.addRow(new Object[]{ stt++, r.getIngredientID(), r.getIngredientName(), r.getUnit(), r.getQuantityRequired(), "Sửa / Xóa" });
                usedIngredients.add(r.getIngredientName());
            }
        }

        if (cbIngredient != null && allIngredientsCache != null) {
            cbIngredient.removeAllItems();
            for (String ing : allIngredientsCache) {
                if (!usedIngredients.contains(ing)) cbIngredient.addItem(ing);
            }
            if (cbIngredient.getItemCount() == 0) txtUnit.setText("");
        }
    }
    
    public void addIngredientSelectionListener(ItemListener listener) { cbIngredient.addItemListener(listener); }
    public void setUnitText(String unit) { txtUnit.setText(unit); }

    // [SỬA] Đổ 3 loại giá vào giao diện
    public void setProductData(String name, String category, long dineInPrice, long takeawayPrice, long holidayPrice, double vat, String status, String description, ImageIcon icon) {
        txtProductName.setText(name); cbCategory.setSelectedItem(category);
        txtDineInPrice.setText(String.format("%d", dineInPrice));
        txtTakeawayPrice.setText(String.format("%d", takeawayPrice));
        txtHolidayPrice.setText(String.format("%d", holidayPrice));
        txtVat.setText(String.valueOf(vat));
        txtDescription.setText(description); setImage(icon);

        if (status.equalsIgnoreCase("Đang bán")) rbOnSale.setSelected(true);
        else if (status.equalsIgnoreCase("Tạm hết")) rbOutOfStock.setSelected(true);
        else rbStopSelling.setSelected(true);
    }

    public void setImage(ImageIcon icon) {
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            lblImagePlaceholder.setIcon(new ImageIcon(img)); lblImagePlaceholder.setText("");
        } else { lblImagePlaceholder.setIcon(null); lblImagePlaceholder.setText("Hình ảnh mẫu"); }
    }
    
    public void setEstimatedCost(long totalCost) {
        lblTotalCost.setText(String.format("Giá vốn ước tính: %,d đ", totalCost));
    }
    
    public void removeVariantRow(int row) {
        if (row >= 0 && row < variantModel.getRowCount()) {
            variantModel.removeRow(row);
        }
    }

    public void addChooseImageListener(ActionListener listener) { btnUpload.addActionListener(listener); }
    public void addUpdateListener(ActionListener listener) { btnUpdate.addActionListener(listener); }
    public void addDeleteListener(ActionListener listener) { btnDelete.addActionListener(listener); }
    public void addAddRecipeListener(ActionListener listener) { btnAddRecipe.addActionListener(listener); }
    
    public void addVariantSelectionListener(ItemListener listener) { cbVariantRecipe.addItemListener(listener); }

    public String getProductName() { return txtProductName.getText().trim(); }
    
    // [SỬA] Đổi getPrice thành 3 hàm Getter cho từng loại giá
    public long getDineInPrice() {
        try { return Long.parseLong(txtDineInPrice.getText().trim().replace(".", "").replace(",", "")); } 
        catch (Exception e) { return 0; }
    }
    public long getTakeawayPrice() {
        try { return Long.parseLong(txtTakeawayPrice.getText().trim().replace(".", "").replace(",", "")); } 
        catch (Exception e) { return 0; }
    }
    public long getHolidayPrice() {
        try { return Long.parseLong(txtHolidayPrice.getText().trim().replace(".", "").replace(",", "")); } 
        catch (Exception e) { return 0; }
    }

    public double getVat() {
        try { return Double.parseDouble(txtVat.getText().trim()); } catch (Exception e) { return 8.0; }
    }
    public String getCategory() { return (String) cbCategory.getSelectedItem(); }
    public String getStatus() {
        if (rbOnSale.isSelected()) return "Đang bán";
        if (rbOutOfStock.isSelected()) return "Tạm hết"; return "Ngừng bán";
    }
    public String getDescription() { return txtDescription.getText().trim(); }
    
    public VariantModel getSelectedVariantForRecipe() { return (VariantModel) cbVariantRecipe.getSelectedItem(); }
    public String getIngredientName(){ return (String) cbIngredient.getSelectedItem(); }
    public double getQuantitative(){
        try { return Double.parseDouble(txtQuantitative.getText().trim().replace(".", "").replace(",", "")); } 
        catch (Exception e) { return 0; }
    }
    
    public void setRecipeTableListener(ProductActionListener listener) { this.recipeTableListener = listener; }
    public void setVariantDeleteListener(DeleteActionListener listener) { this.variantDeleteListener = listener; }
    
    public void setActionPermissions(boolean canEdit, boolean canDelete) {
        this.hasEditPermission = canEdit;
        this.hasDeletePermission = canDelete;
        this.repaint(); 
    }
    
    public int getRecipeIngredientIdAt(int row) { return (int) recipeModel.getValueAt(row, 1); }
    public String getRecipeIngredientNameAt(int row) { return (String) recipeModel.getValueAt(row, 2); }
    public double getRecipeQuantitativeAt(int row) { return (double) recipeModel.getValueAt(row, 4); }
    
    public int getVariantIdAt(int row) {
        try { return Integer.parseInt(variantModel.getValueAt(row, 0).toString()); } 
        catch (Exception e) { return 0; }
    }
    
    public String getVariantNameAt(int row) {
        Object val = variantModel.getValueAt(row, 1);
        return val != null ? val.toString() : "";
    }

    class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) { Dimension minimum = layoutSize(target, false); minimum.width -= (getHgap() + 1); return minimum; }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getParent().getWidth(); if (targetWidth == 0) targetWidth = 400; 
                int hgap = getHgap(); int vgap = getVgap(); Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2); int maxWidth = targetWidth - horizontalInsetsAndGap;
                Dimension dim = new Dimension(0, 0); int rowWidth = 0; int rowHeight = 0; int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) { dim.width = Math.max(dim.width, rowWidth); dim.height += rowHeight + vgap; rowWidth = 0; rowHeight = 0; }
                        rowWidth += d.width + hgap; rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rowWidth); dim.height += rowHeight + insets.top + insets.bottom + vgap * 2; return dim;
            }
        }
    }
    
    public interface DeleteActionListener { void onDelete(int row); }

    class DeleteActionPanel extends JPanel {
        protected JButton btnDelete = new JButton("Thu hồi");
        public DeleteActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 4)); setOpaque(true);
            btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 11)); 
            btnDelete.setForeground(new Color(255, 59, 48));
            btnDelete.setBackground(Color.WHITE);
            btnDelete.setBorder(BorderFactory.createLineBorder(new Color(255, 59, 48), 1));
            btnDelete.setFocusPainted(false); btnDelete.setPreferredSize(new Dimension(50, 20)); 
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            add(btnDelete);
        }
    }

    class DeleteActionButtonRenderer implements TableCellRenderer {
        protected DeleteActionPanel panel; 
        public DeleteActionButtonRenderer(DeleteActionPanel panel) { this.panel = panel; }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.btnDelete.setVisible(hasDeletePermission); return panel;
        }
    }

    class DeleteActionButtonEditor extends DefaultCellEditor {
        protected DeleteActionPanel panel; protected DeleteActionListener listener; protected int currentRow;
        public DeleteActionButtonEditor(DeleteActionListener listener, DeleteActionPanel panel) {
            super(new JCheckBox()); this.listener = listener; this.panel = panel;
            this.panel.btnDelete.addActionListener(e -> { stopCellEditing(); listener.onDelete(currentRow); });
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row; panel.setBackground(table.getSelectionBackground());
            panel.btnDelete.setVisible(hasDeletePermission); return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
    
    public interface ProductActionListener { void onEdit(int row); void onDelete(int row); }

    class ProductActionPanel extends JPanel {
        protected JButton btnEdit = new JButton("Sửa");
        protected JButton btnDelete = new JButton("Xóa");
        public ProductActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 8)); setOpaque(true);
            styleButton(btnEdit, new Color(0, 122, 255), 60, 30);
            styleButton(btnDelete, new Color(255, 59, 48), 60, 30);
            add(btnEdit); add(btnDelete);
        }
        protected void styleButton(JButton btn, Color color, int width, int height) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12)); btn.setForeground(color);
            btn.setBackground(Color.WHITE); btn.setBorder(BorderFactory.createLineBorder(color, 1));
            btn.setFocusPainted(false); btn.setPreferredSize(new Dimension(width, height));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    class ProductActionButtonRenderer implements TableCellRenderer {
        protected JPanel panel;
        public ProductActionButtonRenderer(JPanel panel) { this.panel = panel; }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE); return panel;
        }
    }

    class ProductActionButtonEditor extends DefaultCellEditor {
        protected ProductActionPanel panel; protected ProductActionListener listener; protected int currentRow;
        public ProductActionButtonEditor(ProductActionListener listener, ProductActionPanel panel) {
            super(new JCheckBox()); this.listener = listener; this.panel = panel;
            this.panel.btnEdit.addActionListener(e -> { stopCellEditing(); listener.onEdit(currentRow); });
            this.panel.btnDelete.addActionListener(e -> { stopCellEditing(); listener.onDelete(currentRow); });
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row; panel.setBackground(table.getSelectionBackground()); return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
}