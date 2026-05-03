package View;

import Model.CategoryModel;
import Model.ToppingModel;
import Model.ProductCategoryListModel;
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

public class ProductDetailDialog extends JDialog {

    private JTabbedPane tabbedPane;
    private JPanel tabInfo, tabCategory, tabTopping;

    private Color PRIMARY_COLOR = AppColor.PRIMARY;
    private Color TEXT_DARK = AppColor.TEXT_DARK;
    private Color TEXT_MUTED = AppColor.TEXT_MUTED;

    // [SỬA] Đổi tên biến và thêm 3 loại giá
    private JTextField txtProductName, txtDineInPrice, txtTakeawayPrice, txtHolidayPrice, txtVat; 
    private JComboBox<String> cbCategory;
    private JTextArea txtDescription;
    private JLabel lblImagePlaceholder;
    private JRadioButton rbOnSale, rbOutOfStock, rbStopSelling;
    
    // Table quản lý Size (Variant)
    private JTable variantTable;
    private DefaultTableModel variantModel;
    private JButton btnAddVariant;

    private JPanel toppingsPanel;
    private Map<Integer, JCheckBox> toppingCheckboxMap = new LinkedHashMap<>();

    private JButton btnSave, btnUpload;
    
    // Quản lý Danh mục và Topping
    private JTable categoryTable, toppingTable;
    private DefaultTableModel categoryModel, toppingModel;
    private JButton btnAddCategory, btnAddTopping;
    
    private ProductActionListener categoryTableListener;
    private ProductActionListener toppingTableListener;
    private DeleteActionListener variantDeleteListener;
    
    private boolean hasEditPermission = true;
    private boolean hasDeletePermission = true;

    public ProductDetailDialog(Frame parent) {
        super(parent, "QUẢN LÝ MÓN VÀ DANH MỤC", true);
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
        JLabel title = new JLabel("  QUẢN LÝ MÓN & DANH MỤC TÙY CHỌN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);
        
        initTabInfo();
        initTabCategory();
        initTabTopping(); 

        tabbedPane.addTab("Thông tin chung", tabInfo);
        tabbedPane.addTab("Quản lý Loại SP", tabCategory);
        tabbedPane.addTab("Quản lý Topping", tabTopping);
        
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

    private void initTabInfo() {
        tabInfo = new JPanel(new BorderLayout(20, 0));
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
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        
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
        
        JPanel imgPanel = new JPanel(new GridBagLayout());
        imgPanel.setOpaque(false);
        GridBagConstraints imgGbc = new GridBagConstraints();
        imgGbc.anchor = GridBagConstraints.WEST; imgGbc.insets = new Insets(0, 0, 0, 15);
        
        lblImagePlaceholder = new JLabel("Hình ảnh mẫu", SwingConstants.CENTER);
        lblImagePlaceholder.setPreferredSize(new Dimension(100, 140));
        lblImagePlaceholder.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        btnUpload = new JButton("Tải ảnh lên");
        
        imgGbc.gridx = 0; imgGbc.gridy = 0; imgPanel.add(lblImagePlaceholder, imgGbc);
        imgGbc.gridx = 1; imgGbc.gridy = 0; imgPanel.add(btnUpload, imgGbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0; leftPanel.add(new JLabel("Hình ảnh:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; leftPanel.add(imgPanel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.NORTHWEST;
        leftPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH; 
        leftPanel.add(new JScrollPane(txtDescription), gbc);

        // ==== RIGHT PANEL (Biến Thể & Topping) ====
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setOpaque(false);
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.fill = GridBagConstraints.BOTH; rightGbc.weightx = 1.0; rightGbc.insets = new Insets(0, 0, 10, 0);

        // 1. Quản lý Size (BIEN_THE)
        JPanel variantPanel = createSectionPanel("Biến thể (Size & 3 Loại Giá)");
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
            if (variantTable.isEditing()) {
                variantTable.getCellEditor().stopCellEditing();
            }
            if (variantDeleteListener != null) {
                variantDeleteListener.onDelete(row);
            } else {
                if (row >= 0 && row < variantModel.getRowCount()) variantModel.removeRow(row);
            }
        }, new DeleteActionPanel()));
        
        // Thêm dòng mặc định (Truyền đủ 6 giá trị)
        btnAddVariant.addActionListener(e -> variantModel.addRow(new Object[]{0, "", "0", "0", "0", "Xóa"}));
        
        JScrollPane scrollVar = new JScrollPane(variantTable);
        scrollVar.setPreferredSize(new Dimension(0, 120));
        variantPanel.add(varCtrlPanel, BorderLayout.NORTH);
        variantPanel.add(scrollVar, BorderLayout.CENTER);

        // 2. Chọn Topping
        toppingsPanel = createSectionPanel("Topping khả dụng");
        toppingsPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10)); 
        
        // 3. Trạng thái
        JPanel statusPanel = createSectionPanel("Trạng thái");
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 8));
        rbOnSale = new JRadioButton("Đang bán", true);
        rbOutOfStock = new JRadioButton("Tạm hết");
        rbStopSelling = new JRadioButton("Ngừng bán");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbOnSale); bg.add(rbOutOfStock); bg.add(rbStopSelling);
        statusPanel.add(rbOnSale); statusPanel.add(rbOutOfStock); statusPanel.add(rbStopSelling);

        rightGbc.gridy = 0; rightGbc.weighty = 0.4; rightContainer.add(variantPanel, rightGbc);
        rightGbc.gridy = 1; rightGbc.weighty = 0.5; rightContainer.add(new JScrollPane(toppingsPanel), rightGbc);
        rightGbc.gridy = 2; rightGbc.weighty = 0.1; rightGbc.insets = new Insets(0, 0, 0, 0); rightContainer.add(statusPanel, rightGbc);

        tabInfo.add(leftPanel, BorderLayout.WEST);
        tabInfo.add(rightContainer, BorderLayout.CENTER);
    }

    private void initTabCategory() {
        tabCategory = new JPanel(new BorderLayout(15, 15));
        tabCategory.setBackground(Color.WHITE); tabCategory.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlPanel.setOpaque(false);
        btnAddCategory = createModernButton("Thêm Loại SP", PRIMARY_COLOR, Color.WHITE);
        controlPanel.add(btnAddCategory);

        String[] cols = {"Mã Loại", "Tên Loại Sản Phẩm", "Trạng Thái", "Thuế Mặc định (%)", "Hành động"};
        categoryModel = new DefaultTableModel(cols, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return c == 4; } 
        };
        categoryTable = new JTable(categoryModel); 
        styleTable(categoryTable); categoryTable.setRowHeight(45); 

        TableColumn actionCol = categoryTable.getColumnModel().getColumn(4);
        actionCol.setCellRenderer(new ProductActionButtonRenderer(new ProductActionPanel()));
        actionCol.setCellEditor(new ProductActionButtonEditor(new ProductActionListener() {
            @Override public void onEdit(int row) { if (categoryTableListener != null) categoryTableListener.onEdit(row); }
            @Override public void onDelete(int row) { if (categoryTableListener != null) categoryTableListener.onDelete(row); }
        }, new ProductActionPanel()));

        tabCategory.add(controlPanel, BorderLayout.NORTH);
        tabCategory.add(new JScrollPane(categoryTable), BorderLayout.CENTER);
    }

    private void initTabTopping() {
        tabTopping = new JPanel(new BorderLayout(15, 15));
        tabTopping.setBackground(Color.WHITE); tabTopping.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlPanel.setOpaque(false);
        btnAddTopping = createModernButton("Thêm Topping", PRIMARY_COLOR, Color.WHITE);
        controlPanel.add(btnAddTopping);

        String[] cols = {"Mã", "Tên Topping", "Giá Bán", "Mã NL Trừ", "Hao hụt", "Thuế GTGT", "Hành động"};
        toppingModel = new DefaultTableModel(cols, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return c == 6; } 
        };
        toppingTable = new JTable(toppingModel); 
        styleTable(toppingTable); toppingTable.setRowHeight(45); 

        TableColumn actionCol = toppingTable.getColumnModel().getColumn(6);
        actionCol.setCellRenderer(new ProductActionButtonRenderer(new ProductActionPanel()));
        actionCol.setCellEditor(new ProductActionButtonEditor(new ProductActionListener() {
            @Override public void onEdit(int row) { if (toppingTableListener != null) toppingTableListener.onEdit(row); }
            @Override public void onDelete(int row) { if (toppingTableListener != null) toppingTableListener.onDelete(row); }
        }, new ProductActionPanel()));

        tabTopping.add(controlPanel, BorderLayout.NORTH);
        tabTopping.add(new JScrollPane(toppingTable), BorderLayout.CENTER);
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(); p.setBackground(Color.WHITE);
        p.setBorder(new TitledBorder(new LineBorder(new Color(230, 230, 230)), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));
        return p;
    }
    
    private JPanel createInputWrapper(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 5)); p.setOpaque(false);
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(l, BorderLayout.NORTH); p.add(comp, BorderLayout.CENTER);
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
                g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
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

    // ==== PUBLIC API DÀNH CHO CONTROLLER ====
    public void loadCategoryData(ProductCategoryListModel dataList) {
        categoryModel.setRowCount(0); cbCategory.removeAllItems();
        if (dataList != null && dataList.getProductCategoryList() != null) {
            for (CategoryModel cat : dataList.getProductCategoryList()) {
                categoryModel.addRow(new Object[]{
                    cat.getCategoryID(), cat.getCategoryName(), cat.getCategoryStatus(), cat.getDefaultVat(), "Sửa / Xóa"
                });
                cbCategory.addItem(cat.getCategoryName());
            }
        }
    }

    public void loadToppingData(ArrayList<ToppingModel> toppingList) {
        toppingModel.setRowCount(0);
        toppingsPanel.removeAll();
        toppingCheckboxMap.clear();

        if (toppingList != null) {
            for (ToppingModel top : toppingList) {
                toppingModel.addRow(new Object[]{ 
                    top.getToppingID(), top.getToppingName(), top.getPrice(), top.getIngredientID(), top.getLossAmount(), top.getVat(), "Sửa / Xóa" 
                });
                JCheckBox cb = new JCheckBox(top.getLabel());
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 13)); cb.setOpaque(false);
                toppingCheckboxMap.put(top.getToppingID(), cb);
                toppingsPanel.add(cb);
            }
        }
        toppingsPanel.revalidate(); toppingsPanel.repaint();
    }
    
    public void loadVariantData(List<VariantModel> variants) {
        variantModel.setRowCount(0);
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
            }
        }
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
            
            // 1. Lấy ID an toàn
            int id = 0;
            try {
                if (variantModel.getValueAt(i, 0) != null) {
                    id = Integer.parseInt(variantModel.getValueAt(i, 0).toString().trim());
                }
            } catch (Exception e) { id = 0; }
            
            // 2. Lấy Tên 
            String name = "";
            if (variantModel.getValueAt(i, 1) != null) {
                name = variantModel.getValueAt(i, 1).toString().trim();
            }
            
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

    public void setImage(ImageIcon icon) {
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(100, 140, Image.SCALE_SMOOTH);
            lblImagePlaceholder.setIcon(new ImageIcon(img)); lblImagePlaceholder.setText("");
        } else {
            lblImagePlaceholder.setIcon(null); lblImagePlaceholder.setText("Hình ảnh mẫu");
        }
    }
    
    public void removeVariantRow(int row) {
        if (row >= 0 && row < variantModel.getRowCount()) {
            variantModel.removeRow(row);
        }
    }

    // GETTERS & LISTENERS
    public void addChooseImageListener(ActionListener listener) { btnUpload.addActionListener(listener); }
    public void addSaveListener(ActionListener listener) { btnSave.addActionListener(listener); }
    public void addAddCategoryListener(ActionListener listener) { btnAddCategory.addActionListener(listener); }
    public void addAddToppingListener(ActionListener listener) { btnAddTopping.addActionListener(listener); }
    
    public void setCategoryTableListener(ProductActionListener listener) { this.categoryTableListener = listener; }
    public void setToppingTableListener(ProductActionListener listener) { this.toppingTableListener = listener; }
    
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
        if (rbOutOfStock.isSelected()) return "Tạm hết";
        return "Ngừng bán";
    }
    public String getDescription (){ return txtDescription.getText().trim(); }

    public int getCategoryIdAt(int row) { return (int) categoryModel.getValueAt(row, 0); }
    public String getCategoryNameAt(int row) { return (String) categoryModel.getValueAt(row, 1); }
    public int getToppingIdAt(int row) { return (int) toppingModel.getValueAt(row, 0); }
    public String getToppingNameAt(int row) { return (String) toppingModel.getValueAt(row, 1); }
    public long getToppingPriceAt(int row) { return (long) toppingModel.getValueAt(row, 2); }
    public int getToppingIngredientIdAt(int row) { return (int) toppingModel.getValueAt(row, 3); }
    public double getToppingLossAmountAt(int row) { return (double) toppingModel.getValueAt(row, 4); }
    public double getToppingVatAt(int row) { return (double) toppingModel.getValueAt(row, 5); }
    
    public String getVariantNameAt(int row) {
        Object val = variantModel.getValueAt(row, 1);
        return val != null ? val.toString().trim() : "";
    }
    
    class WrapLayout extends FlowLayout {
        // ... (Giữ nguyên WrapLayout của bạn)
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
        protected ProductActionPanel panel;
        protected ProductActionListener listener;
        protected int currentRow;
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
    
    public void clearForm() {
        if(txtProductName == null) return;
        
        txtProductName.setText(""); 
        
        // [SỬA] Xóa trắng 3 ô giá
        txtDineInPrice.setText("0"); 
        txtTakeawayPrice.setText("0"); 
        txtHolidayPrice.setText("0"); 
        
        txtVat.setText("8");
        txtDescription.setText("");
        
        if(cbCategory.getItemCount() > 0) cbCategory.setSelectedIndex(0); 
        rbOnSale.setSelected(true);
        setImage(null);
        
        if (variantModel != null) variantModel.setRowCount(0);
        if (toppingCheckboxMap != null) {
            for (JCheckBox cb : toppingCheckboxMap.values()) cb.setSelected(false);
        }
    }
    
    public void setActionPermissions(boolean canEdit, boolean canDelete) {
        this.hasEditPermission = canEdit;
        this.hasDeletePermission = canDelete;
        this.repaint(); 
    }
    
    public void setVariantDeleteListener(DeleteActionListener listener) {
        this.variantDeleteListener = listener;
    }
}