package View;

import Common.AutoCompleteComboBox;
import Model.ToppingModel;
import Model.VariantModel;
import Model.RecipeModel; 
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
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

    private JTextField txtProductName, txtVat;
    private JComboBox<String> cbCategory;
    private JTextArea txtDescription;
    private JLabel lblImagePlaceholder;
    private JRadioButton rbOnSale, rbOutOfStock, rbStopSelling;
    
    private JTable variantTable;
    private DefaultTableModel variantModel;
    private JButton btnAddVariant;

    private JPanel toppingsPanel;
    private Map<Integer, JCheckBox> toppingCheckboxMap = new LinkedHashMap<>();

    private AutoCompleteComboBox<VariantModel> cbVariantRecipe; 
    private AutoCompleteComboBox<String> cbIngredient;
    
    private ArrayList<String> allIngredientsCache = new ArrayList<>();
    private JTextField txtUnit; 
    private JTextField txtQuantitative;
    private JTable recipeTable;
    private DefaultTableModel recipeModel;
    
    private JButton btnAddRecipeToTable;
    private JButton btnSaveRecipe;
    private JLabel lblTotalCost; 

    private JButton btnUpload, btnUpdate;
    private JLabel lblTitle;
    
    private boolean isCreateMode; // Biến xác định chế độ: True = Thêm mới, False = Sửa
    private boolean hasEditPermission = true;
    private boolean hasDeletePermission = true;
    
    private ProductActionListener recipeTableListener; 
    private DeleteActionListener variantDeleteListener;

    public ProductEditDialog(Frame parent, boolean isCreateMode) {
        super(parent, isCreateMode ? "THÊM MỚI SẢN PHẨM" : "CHI TIẾT SẢN PHẨM", true);
        this.isCreateMode = isCreateMode;
        initComponents();
        setupMode();
    }

    private void initComponents() {
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(getWidth(), 50));
        
        lblTitle = new JLabel("  THÔNG TIN CHI TIẾT SẢN PHẨM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);
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
    
    private void setupMode() {
        if (isCreateMode) {
            lblTitle.setText("  THÊM MỚI SẢN PHẨM");
            btnUpdate.setText("Tạo Sản Phẩm");
            
            // Xóa rỗng form
            clearForm();
        } else {
            lblTitle.setText("  THÔNG TIN CHI TIẾT & CẬP NHẬT MÓN");
            btnUpdate.setText("Cập nhật toàn bộ SP");
        }
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
        
        txtVat = createStyledTextField("8");
        
        txtDescription = new JTextArea(3, 20);
        txtDescription.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        txtDescription.setLineWrap(true); txtDescription.setWrapStyleWord(true);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; leftPanel.add(new JLabel("Tên món: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; leftPanel.add(txtProductName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; leftPanel.add(new JLabel("Danh mục: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; leftPanel.add(cbCategory, gbc);

        JPanel vatPanel = new JPanel(new GridLayout(1, 1, 10, 0));
        vatPanel.setOpaque(false);
        vatPanel.add(createInputWrapper("Thuế VAT (%):", txtVat));
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; leftPanel.add(vatPanel, gbc);
        
        JPanel imgPanel = new JPanel(new GridBagLayout()); imgPanel.setOpaque(false);
        GridBagConstraints imgGbc = new GridBagConstraints(); imgGbc.anchor = GridBagConstraints.WEST; imgGbc.insets = new Insets(0, 0, 0, 15);
        
        lblImagePlaceholder = new JLabel("Hình ảnh mẫu", SwingConstants.CENTER);
        lblImagePlaceholder.setPreferredSize(new Dimension(100, 100));
        lblImagePlaceholder.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        btnUpload = new JButton("Đổi ảnh");
        
        imgGbc.gridx = 0; imgGbc.gridy = 0; imgPanel.add(lblImagePlaceholder, imgGbc);
        imgGbc.gridx = 1; imgGbc.gridy = 0; imgPanel.add(btnUpload, imgGbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; leftPanel.add(new JLabel("Hình ảnh:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; leftPanel.add(imgPanel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.NORTHWEST;
        leftPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH; 
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
        
        String[] varCols = {"ID", "Tên Size (M, L...)", "Tại quán", "Mang về", "Ngày lễ", "Xóa"};
        variantModel = new DefaultTableModel(varCols, 0) { 
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return c == 1 || c == 2 || c == 3 || c == 4 || c == 5; 
            } 
        };
        variantTable = new JTable(variantModel); 
        variantTable.setRowHeight(30);

        variantTable.removeColumn(variantTable.getColumnModel().getColumn(0));
        
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
        
        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); rightFooter.setOpaque(false);
        
        btnUpdate = createModernButton("Cập nhật toàn bộ SP", PRIMARY_COLOR, Color.WHITE); 
        rightFooter.add(btnUpdate);

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

        cbVariantRecipe = new AutoCompleteComboBox<>();
        cbIngredient = new AutoCompleteComboBox<>();
        
        txtUnit = createStyledTextField(""); txtUnit.setEditable(false);
        txtQuantitative = createStyledTextField("");
        
        btnAddRecipeToTable = createModernButton("+ Thêm vào bảng", PRIMARY_COLOR, Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2; inputPanel.add(new JLabel("Áp dụng cho Size:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(cbVariantRecipe, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.3; inputPanel.add(new JLabel("Nguyên liệu:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(cbIngredient, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.1; inputPanel.add(new JLabel("Đơn vị:"), gbc);
        gbc.gridx = 2; gbc.gridy = 1; inputPanel.add(txtUnit, gbc);
        
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.2; inputPanel.add(new JLabel("Định lượng:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; inputPanel.add(txtQuantitative, gbc);
        
        gbc.gridx = 4; gbc.gridy = 1; gbc.weightx = 0.2; inputPanel.add(btnAddRecipeToTable, gbc);

        String[] cols = {"STT", "Mã NL", "Tên Nguyên Liệu", "Đơn vị", "Số lượng cần", "Hành động", "Trạng Thái Dòng"};
        recipeModel = new DefaultTableModel(cols, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return c == 5; } 
        };
        recipeTable = new JTable(recipeModel);
        recipeTable.setRowHeight(40); recipeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recipeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        recipeTable.getTableHeader().setBackground(new Color(242, 242, 242));
        
        recipeTable.removeColumn(recipeTable.getColumnModel().getColumn(6));

        TableColumn actionCol = recipeTable.getColumnModel().getColumn(5);
        actionCol.setCellRenderer(new ProductActionButtonRenderer(new ProductActionPanel()));
        actionCol.setCellEditor(new ProductActionButtonEditor(new ProductActionListener() {
            @Override public void onEdit(int row) { if (recipeTableListener != null) recipeTableListener.onEdit(row); }
            @Override public void onDelete(int row) { if (recipeTableListener != null) recipeTableListener.onDelete(row); }
        }, new ProductActionPanel()));
        
        JPanel recipeFooter = new JPanel(new BorderLayout());
        recipeFooter.setOpaque(false);
        recipeFooter.setBorder(new EmptyBorder(10, 0, 0, 0)); 

        lblTotalCost = new JLabel("Giá vốn ước tính: 0 đ");
        lblTotalCost.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalCost.setForeground(DANGER_COLOR);
        
        btnSaveRecipe = createModernButton("Lưu công thức Size này", PRIMARY_COLOR, Color.WHITE);
        
        recipeFooter.add(lblTotalCost, BorderLayout.WEST);
        recipeFooter.add(btnSaveRecipe, BorderLayout.EAST);

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
        if(ingredients != null) {
            cbIngredient.setData(ingredients); 
        }
    }
    
    public void loadVariantData(List<VariantModel> variants) {
        variantModel.setRowCount(0);
        List<VariantModel> cbList = new ArrayList<>();

        if(variants != null) {
            for(VariantModel v : variants) {
                variantModel.addRow(new Object[]{ 
                    v.getVariantID(), 
                    v.getSizeName(), 
                    String.format("%,d", v.getDineInPrice()), 
                    String.format("%,d", v.getTakeawayPrice()), 
                    String.format("%,d", v.getHolidayPrice()), 
                    "Xóa" 
                });
                
                cbList.add(v);
            }
            if (cbVariantRecipe != null) cbVariantRecipe.setData(cbList); 
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

        if (recipes != null && !recipes.isEmpty()) {
            int stt = 1;
            for (Model.RecipeModel r : recipes) {
                recipeModel.addRow(new Object[]{ stt++, r.getIngredientID(), r.getIngredientName(), r.getUnit(), r.getQuantityRequired(), "Sửa / Xóa", "OLD" });
            }
        }
    }
    
    public List<RecipeModel> getRecipesFromTable() {
        List<RecipeModel> list = new ArrayList<>();
        for(int i = 0; i < recipeModel.getRowCount(); i++) {
            try {
                int ingId = Integer.parseInt(recipeModel.getValueAt(i, 1).toString());
                double qty = Double.parseDouble(recipeModel.getValueAt(i, 4).toString());
                String status = recipeModel.getValueAt(i, 6).toString();
                
                RecipeModel rec = new RecipeModel(0, ingId, qty);
                rec.setIngredientName(status); 
                list.add(rec);
            } catch(Exception e) {}
        }
        return list;
    }

    public void addRecipeRow(int ingId, String ingName, String unit, double qty) {
        int rowCount = recipeModel.getRowCount();
        recipeModel.addRow(new Object[]{ rowCount + 1, ingId, ingName, unit, qty, "Sửa / Xóa", "NEW" });
    }
    
    public void markRecipeRowAsEdited(int row, double newQty) {
        recipeModel.setValueAt(newQty, row, 4);
        recipeModel.setValueAt("EDITED", row, 6);
    }
    
    public void removeRecipeRow(int row) {
        recipeModel.removeRow(row);
        for (int i = 0; i < recipeModel.getRowCount(); i++) {
            recipeModel.setValueAt(i + 1, i, 0);
        }
    }
    
    public void addIngredientSelectionListener(ItemListener listener) { cbIngredient.addItemListener(listener); }
    public void setUnitText(String unit) { txtUnit.setText(unit); }

    public void setProductData(String name, String category, double vat, String status, String description, ImageIcon icon) {
        txtProductName.setText(name); cbCategory.setSelectedItem(category);
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
    
    public void clearForm() {
        if(txtProductName == null) return;
        txtProductName.setText(""); 
        txtVat.setText("8");
        txtDescription.setText("");
        
        if(cbCategory.getItemCount() > 0) cbCategory.setSelectedIndex(0); 
        rbOnSale.setSelected(true);
        setImage(null);
        
        if (variantModel != null) variantModel.setRowCount(0);
        if (toppingCheckboxMap != null) {
            for (JCheckBox cb : toppingCheckboxMap.values()) cb.setSelected(false);
        }
        if (recipeModel != null) recipeModel.setRowCount(0);
        setEstimatedCost(0);
    }

    public void addChooseImageListener(ActionListener listener) { btnUpload.addActionListener(listener); }
    public void addUpdateListener(ActionListener listener) { btnUpdate.addActionListener(listener); }
    
    public void addAddRecipeToTableListener(ActionListener listener) { btnAddRecipeToTable.addActionListener(listener); }
    public void addVariantSelectionListener(ItemListener listener) { cbVariantRecipe.addItemListener(listener); }

    public String getProductName() { return txtProductName.getText().trim(); }
    

    public double getVat() {
        try { return Double.parseDouble(txtVat.getText().trim()); } catch (Exception e) { return 8.0; }
    }
    public String getCategory() { return (String) cbCategory.getSelectedItem(); }
    public String getStatus() {
        if (rbOnSale.isSelected()) return "Đang bán";
        if (rbOutOfStock.isSelected()) return "Tạm hết"; return "Ngừng bán";
    }
    public String getDescription() { return txtDescription.getText().trim(); }
    
    public VariantModel getSelectedVariantForRecipe() { 
        Object selected = cbVariantRecipe.getSelectedItem();
        if(selected instanceof VariantModel) return (VariantModel) selected;
        return null;
    }
    public String getIngredientName(){ 
        Object selected = cbIngredient.getSelectedItem();
        return selected != null ? selected.toString() : "";
    }
    public double getQuantitative(){
        try { return Double.parseDouble(txtQuantitative.getText().trim().replace(".", "").replace(",", "")); } 
        catch (Exception e) { return 0; }
    }
    
    public void addSaveRecipeListener(ActionListener listener) { btnSaveRecipe.addActionListener(listener); }
    
    public void setRecipeTableListener(ProductActionListener listener) { this.recipeTableListener = listener; }
    public void setVariantDeleteListener(DeleteActionListener listener) { this.variantDeleteListener = listener; }
    
    public void setActionPermissions(boolean canEdit, boolean canDelete) {
        this.hasEditPermission = canEdit;
        this.hasDeletePermission = canDelete;
        
        // Khóa/Mở khóa thông tin cơ bản
        txtProductName.setEditable(canEdit);
        txtVat.setEditable(canEdit);
        txtDescription.setEditable(canEdit);
        
        cbCategory.setEnabled(canEdit);
        rbOnSale.setEnabled(canEdit);
        rbOutOfStock.setEnabled(canEdit);
        rbStopSelling.setEnabled(canEdit);
        
        btnUpload.setVisible(canEdit);
        btnAddVariant.setVisible(canEdit);
        
        // Ẩn/Hiện nút Cập nhật sản phẩm
        
        btnUpdate.setVisible(canEdit);
        
        // Khóa/Mở khóa các ô cấu hình công thức (Tab 2)
        cbVariantRecipe.setEnabled(canEdit);
        cbIngredient.setEnabled(canEdit);
        txtQuantitative.setEditable(canEdit);
        btnAddRecipeToTable.setVisible(canEdit);
        btnSaveRecipe.setVisible(canEdit);
        
        // Vẽ lại bảng biến thể và bảng công thức để renderer cập nhật trạng thái các nút hành động dòng
        if (variantTable != null) variantTable.repaint();
        if (recipeTable != null) recipeTable.repaint();
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
        URL editIconUrl = getClass().getResource("/images/edit-247.png");
        URL deleteIconUrl = getClass().getResource("/images/delete-icon.png");
        protected JButton btnEdit = new JButton("<html><img src='" + editIconUrl + "' width='12' height='12'> Sửa</html>");
        protected JButton btnDelete = new JButton("<html><img src='" + deleteIconUrl + "' width='12' height='12'> Xóa</html>");
        public ProductActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 8)); setOpaque(true);
            styleButton(btnEdit, new Color(0, 122, 255), 75, 30);
            styleButton(btnDelete, new Color(255, 59, 48), 75, 30);
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
        protected ProductActionPanel panel;
        public ProductActionButtonRenderer(ProductActionPanel panel) { this.panel = panel; }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.btnEdit.setEnabled(hasEditPermission);
            panel.btnDelete.setEnabled(hasEditPermission);
            return panel;
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
            currentRow = row; panel.setBackground(table.getSelectionBackground());
            panel.btnEdit.setEnabled(hasEditPermission);
            panel.btnDelete.setEnabled(hasEditPermission);
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
}