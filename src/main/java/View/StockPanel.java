package View;

import Common.AutoCompleteComboBox;
import Common.ComponentUI;
import Model.IngredientModel;
import Model.IngredientTypeModel;
import Model.WarehouseReceiptModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import com.toedter.calendar.JDateChooser;

public class StockPanel extends JPanel {

    // Colors
    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);
    private final Color PRINT_COLOR = new Color(142, 68, 173); // [MỚI] Màu tím cho nút in

    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    // Views
    private JPanel inventoryListView;
    private JPanel receiptFormView;
    private JPanel historyView;
    private JPanel categoryView; 

    // Models
    private DefaultTableModel inventoryModel;
    private JTable inventoryTable;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private DefaultTableModel itemModel; 
    
    // Model & Table cho Loại NL
    private JTable categoryTable;
    private DefaultTableModel categoryModel;
    
    private List<Receipt> historyList = new ArrayList<>();
    private Receipt editingReceipt = null; 
    private JLabel lblTotal;
    private JButton btnSubmit;
    private JButton btnHistory;
    
    private JButton btnManageCategory; 
    private JButton btnAddNewIngredient;
    private JButton btnImport;
    
    // Listeners
    private ActionButtonListener inventoryActionListener;
    private ActionButtonListener historyActionListener;
    private ActionButtonListener categoryActionListener; 
    
    // --- Form Phiếu Nhập Kho ---
    private AutoCompleteComboBox<IngredientTypeModel> cbCategory; 
    private AutoCompleteComboBox<String> cbIngredient;
    private JButton btnAddCategory; 
    private JTextField txtUnit; 
    private JTextField txtUnitCapacity; 
    private JTextField txtQuantity;     
    private JTextField txtProvider;
    private JTextField txtGiaTruocThue;   
    private JTextField txtThueGTGT;
    private JDateChooser jdImportDate;
    private JDateChooser jdExpiryDate;
    
    // --- Stats ---
    private JTextField txtSearch; // [MỚI] Ô tìm kiếm nguyên liệu
    private JToggleButton btnFilterWarning; // [MỚI] Nút lọc những nguyên liệu cần nhập (hết hàng/sắp hết)

    public StockPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setOpaque(false);

        initInventoryListView();
        initHistoryView(); 
        initReceiptFormView();
        initCategoryView(); 

        mainContainer.add(inventoryListView, "InventoryList");
        mainContainer.add(historyView, "HistoryList");
        mainContainer.add(receiptFormView, "ReceiptForm");
        mainContainer.add(categoryView, "CategoryList"); 

        add(mainContainer, BorderLayout.CENTER);
    }

    private void initInventoryListView() {
        inventoryListView = new JPanel(new BorderLayout(0, 25));
        inventoryListView.setOpaque(false);

        // [CẬP NHẬT] Đã loại bỏ hoàn toàn StatCard "Cần nhập hàng"

        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);
        
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(250, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setText("Tìm kiếm nguyên liệu...");
        txtSearch.setForeground(Color.GRAY);

        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Tìm kiếm nguyên liệu...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(TEXT_DARK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setText("Tìm kiếm nguyên liệu...");
                }
            }
        });
        leftActions.add(txtSearch);

        // [MỚI] Khởi tạo và cấu hình JToggleButton lọc các nguyên liệu cần nhập
        btnFilterWarning = new JToggleButton("Cần nhập hàng");
        btnFilterWarning.setPreferredSize(new Dimension(140, 40));
        btnFilterWarning.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnFilterWarning.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFilterWarning.setFocusPainted(false);
        btnFilterWarning.setBackground(Color.WHITE);
        btnFilterWarning.setForeground(new Color(220, 53, 69)); // Màu chữ đỏ cảnh báo
        btnFilterWarning.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 1)); // Viền đỏ

        btnFilterWarning.addActionListener(e -> {
            if (btnFilterWarning.isSelected()) {
                btnFilterWarning.setBackground(new Color(255, 230, 230)); // Đỏ nhạt đồng bộ khi được chọn
            } else {
                btnFilterWarning.setBackground(Color.WHITE);
            }
        });
        leftActions.add(btnFilterWarning);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        btnHistory = ComponentUI.createModernButton("Lịch sử nhập hàng", new Color(240, 240, 240), TEXT_DARK);
        btnManageCategory = ComponentUI.createModernButton("Quản lý loại NL", new Color(240, 240, 240), TEXT_DARK); 
        
        cbCategory = new AutoCompleteComboBox<>(); 
        
        btnAddNewIngredient = ComponentUI.createModernButton("Thêm Nguyên Liệu", new Color(0, 122, 255), Color.WHITE);
        btnImport = ComponentUI.createModernButton("Nhập Hàng", PRIMARY_COLOR, Color.WHITE);
        
        btnHistory.addActionListener(e -> cardLayout.show(mainContainer, "HistoryList"));
        btnManageCategory.addActionListener(e -> cardLayout.show(mainContainer, "CategoryList")); 
        btnImport.addActionListener(e -> {
            editingReceipt = null; 
            cardLayout.show(mainContainer, "ReceiptForm");
        });

        rightActions.add(btnManageCategory); 
        rightActions.add(btnHistory);
        rightActions.add(btnAddNewIngredient);
        rightActions.add(btnImport);

        actionBar.add(leftActions, BorderLayout.WEST);
        actionBar.add(rightActions, BorderLayout.EAST);

        String[] columns = {"ID", "Tên nguyên liệu", "ĐVT", "Tồn kho hiện tại", "Ngưỡng cảnh báo", "Trạng thái", "Hành động"};
        inventoryModel = new DefaultTableModel(null, columns) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; } 
        };
        inventoryTable = new JTable(inventoryModel);
        ComponentUI.styleTable(inventoryTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        
        TableColumnModel tcm = inventoryTable.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(50);  tcm.getColumn(0).setMaxWidth(80); 
        tcm.getColumn(1).setPreferredWidth(250); 
        tcm.getColumn(2).setPreferredWidth(80);  
        tcm.getColumn(3).setPreferredWidth(120); 
        tcm.getColumn(4).setPreferredWidth(120); 
        tcm.getColumn(5).setPreferredWidth(120); 
        
        InventoryStatusRenderer customRenderer = new InventoryStatusRenderer();
        for (int i = 0; i < inventoryTable.getColumnCount() - 1; i++) {
            inventoryTable.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
        
        TableColumn actionCol = inventoryTable.getColumnModel().getColumn(6); 
        actionCol.setCellRenderer(new ActionButtonRenderer(true, true, true));
        actionCol.setCellEditor(new ActionButtonEditor(new ActionButtonListener() {
            @Override public void onEdit(int row) { if (inventoryActionListener != null) inventoryActionListener.onEdit(row); }
            @Override public void onDelete(int row) { if (inventoryActionListener != null) inventoryActionListener.onDelete(row); }
            @Override public void onDetail(int row) { if (inventoryActionListener != null) inventoryActionListener.onDetail(row); }
        }, true, true, true));
        actionCol.setPreferredWidth(240);
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // [CẬP NHẬT] Đã loại bỏ statsPanel khỏi layout
        
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setOpaque(false);
        centerWrapper.add(actionBar, BorderLayout.NORTH);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        
        inventoryListView.add(centerWrapper, BorderLayout.CENTER);
    }
    
    private void initCategoryView() {
        categoryView = new JPanel(new BorderLayout(0, 20));
        categoryView.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel title = new JLabel("Danh sách Loại Nguyên Liệu"); title.setFont(new Font("Segoe UI", Font.BOLD, 24)); title.setForeground(TEXT_DARK);
        
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        headerActions.setOpaque(false);
        
        btnAddCategory = ComponentUI.createModernButton("+ Thêm loại mới", PRIMARY_COLOR, Color.WHITE);
        
        JButton btnBack = new JButton("← Quay lại kho"); btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnBack.setForeground(PRIMARY_COLOR);
        btnBack.setContentAreaFilled(false); btnBack.setBorderPainted(false); btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "InventoryList"));
        
        headerActions.add(btnAddCategory);
        headerActions.add(btnBack);
        
        header.add(title, BorderLayout.WEST); 
        header.add(headerActions, BorderLayout.EAST);

        String[] cols = {"Mã Loại", "Tên Loại Nguyên Liệu", "Hành động"};
        categoryModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2; }
        };
        categoryTable = new JTable(categoryModel);
        ComponentUI.styleTable(categoryTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);

        TableColumnModel tcmCat = categoryTable.getColumnModel();
        tcmCat.getColumn(0).setPreferredWidth(100); tcmCat.getColumn(0).setMaxWidth(150);
        tcmCat.getColumn(1).setPreferredWidth(300);
        
        CenterRenderer centerRenderer = new CenterRenderer();
        for (int i = 0; i < categoryTable.getColumnCount() - 1; i++) {
            tcmCat.getColumn(i).setCellRenderer(centerRenderer);
        }

        TableColumn actionCol = categoryTable.getColumnModel().getColumn(2);
        actionCol.setCellRenderer(new ActionButtonRenderer(false, true, false)); 
        actionCol.setCellEditor(new ActionButtonEditor(new ActionButtonListener() {
            @Override public void onEdit(int row) { if (categoryActionListener != null) categoryActionListener.onEdit(row); }
            @Override public void onDelete(int row) {}
            @Override public void onDetail(int row) {}
        }, false, true, false)); 
        actionCol.setPreferredWidth(100); 
        
        JScrollPane scroll = new JScrollPane(categoryTable); scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230))); scroll.getViewport().setBackground(Color.WHITE);
        categoryView.add(header, BorderLayout.NORTH); categoryView.add(scroll, BorderLayout.CENTER);
    }
    
    public DefaultTableModel getCategoryModel() { return categoryModel; }
    public JTable getCategoryTable() { return categoryTable; }
    
    public void displayCategoryData(List<IngredientTypeModel> types) {
        categoryModel.setRowCount(0);
        for (IngredientTypeModel type : types) {
            categoryModel.addRow(new Object[]{ type.getTypeID(), type.getTypeName(), "Sửa" });
        }
    }
    
    public void setCategoryActionListener(ActionButtonListener listener) { this.categoryActionListener = listener; }
    
    public String showEditCategoryDialog(String currentName) {
        return (String) JOptionPane.showInputDialog(this, "Chỉnh sửa tên loại nguyên liệu:", "Sửa loại", JOptionPane.PLAIN_MESSAGE, null, null, currentName);
    }

    private void initReceiptFormView() {
        receiptFormView = new JPanel(new BorderLayout(0, 20)); 
        receiptFormView.setOpaque(false); 
        receiptFormView.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel title = new JLabel("Lập phiếu nhập kho"); title.setFont(new Font("Segoe UI", Font.BOLD, 24)); title.setForeground(TEXT_DARK);
        JButton btnBack = new JButton("← Quay lại"); btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnBack.setForeground(PRIMARY_COLOR);
        btnBack.setContentAreaFilled(false); btnBack.setBorderPainted(false); btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "InventoryList"));
        header.add(title, BorderLayout.WEST); header.add(btnBack, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setOpaque(false); content.setBorder(new EmptyBorder(20, 30, 20, 30));

        cbIngredient = new AutoCompleteComboBox<>();
        cbIngredient.setBackground(Color.WHITE);
        
        txtUnit = new JTextField(); 
        txtUnit.setEditable(false); 
        txtUnit.setBackground(new Color(245, 245, 245));
        
        txtUnitCapacity = new JTextField(); 
        txtQuantity = new JTextField();
        txtProvider = new JTextField();
        
        txtGiaTruocThue = new JTextField();   
        txtThueGTGT = new JTextField("8"); 
        
        jdImportDate = new JDateChooser();
        jdImportDate.setDateFormatString("yyyy-MM-dd"); 
        jdImportDate.setDate(new java.util.Date()); 

        jdExpiryDate = new JDateChooser();
        jdExpiryDate.setDateFormatString("yyyy-MM-dd");

        JPanel formPanel = new JPanel(new GridLayout(3, 3, 15, 15)); 
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết Lô hàng nhập"));
        
        formPanel.add(createInputWrapper("Chọn Nguyên liệu:", cbIngredient));
        formPanel.add(createInputWrapper("Đơn vị tính:", txtUnit)); 
        formPanel.add(createInputWrapper("Định lượng 1 bao/hộp:", txtUnitCapacity)); 
        
        formPanel.add(createInputWrapper("Số lượng (gói/hộp):", txtQuantity)); 
        formPanel.add(createInputWrapper("Giá trước thuế (Tổng):", txtGiaTruocThue)); 
        formPanel.add(createInputWrapper("Thuế GTGT (%):", txtThueGTGT)); 
        
        formPanel.add(createInputWrapper("Nhà cung cấp:", txtProvider)); 
        formPanel.add(createInputWrapper("Ngày nhập:", jdImportDate)); 
        formPanel.add(createInputWrapper("Hạn sử dụng:", jdExpiryDate)); 

        JButton btnAddToList = ComponentUI.createModernButton("+ Thêm vào bảng", new Color(0, 122, 255), Color.WHITE);
        btnAddToList.setPreferredSize(new Dimension(160, 35)); 
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); 
        btnPanel.setOpaque(false); 
        btnPanel.setBorder(new EmptyBorder(15, 0, 5, 0)); 
        btnPanel.add(btnAddToList);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(formPanel, BorderLayout.CENTER);
        topSection.add(btnPanel, BorderLayout.SOUTH);

        // BẢNG CHI TIẾT PHIẾU NHẬP
        String[] itemCols = {"Tên NL", "ĐVT", "Đ.Lượng/Gói", "Số lượng", "Giá T.Thuế", "Thuế (%)", "Thành tiền", "Nhà cung cấp", "Ngày nhập", "Hạn sử dụng", "Hành động"};
        itemModel = new DefaultTableModel(null, itemCols) {
            @Override public boolean isCellEditable(int row, int column) { return column == 10; }
        };
        JTable itemTable = new JTable(itemModel);
        ComponentUI.styleTable(itemTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        
        TableColumnModel tcmItem = itemTable.getColumnModel();
        tcmItem.getColumn(0).setPreferredWidth(150); 
        tcmItem.getColumn(1).setPreferredWidth(50);  
        tcmItem.getColumn(2).setPreferredWidth(80);  
        tcmItem.getColumn(3).setPreferredWidth(60);  
        tcmItem.getColumn(4).setPreferredWidth(100); 
        tcmItem.getColumn(5).setPreferredWidth(60);  
        tcmItem.getColumn(6).setPreferredWidth(100); 
        tcmItem.getColumn(7).setPreferredWidth(120); 
        tcmItem.getColumn(8).setPreferredWidth(90);  
        tcmItem.getColumn(9).setPreferredWidth(90);  
        
        CenterRenderer centerRenderer = new CenterRenderer();
        for (int i = 0; i < itemTable.getColumnCount() - 1; i++) {
            tcmItem.getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane itemScroll = new JScrollPane(itemTable); itemScroll.setPreferredSize(new Dimension(0, 300));

        TableColumn actionCol = itemTable.getColumnModel().getColumn(10);
        actionCol.setCellRenderer(new ActionButtonRenderer(false, false, true));
        actionCol.setCellEditor(new ActionButtonEditor(new ActionButtonListener() {
            @Override
            public void onEdit(int row) {}
            @Override public void onDelete(int row) {
                cbIngredient.setSelectedItem(itemModel.getValueAt(row, 0).toString());
                txtUnit.setText(itemModel.getValueAt(row, 1).toString());
                txtUnitCapacity.setText(itemModel.getValueAt(row, 2).toString()); 
                txtQuantity.setText(itemModel.getValueAt(row, 3).toString());
                txtGiaTruocThue.setText(itemModel.getValueAt(row, 4).toString());
                txtThueGTGT.setText(itemModel.getValueAt(row, 5).toString());
                txtProvider.setText(itemModel.getValueAt(row, 7).toString());
                
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String importStr = itemModel.getValueAt(row, 8).toString();
                    if (!importStr.isEmpty()) jdImportDate.setDate(sdf.parse(importStr));
                    
                    String expiryStr = itemModel.getValueAt(row, 9).toString();
                    if (!expiryStr.isEmpty()) jdExpiryDate.setDate(sdf.parse(expiryStr));
                } catch (java.text.ParseException ex) { ex.printStackTrace(); }
                
                itemModel.removeRow(row);
            }
            @Override public void onDetail(int row) {}
        }, false, false, true));
        actionCol.setPreferredWidth(90);

        btnAddToList.addActionListener(e -> {
            try {
                if(cbIngredient.getSelectedItem() == null) return;
                String name = cbIngredient.getSelectedItem().toString();
                String unit = txtUnit.getText().trim();
                String capStr = txtUnitCapacity.getText().trim();
                String qtyStr = txtQuantity.getText().trim();
                String giaTruocThueStr = txtGiaTruocThue.getText().trim(); 
                String thueStr = txtThueGTGT.getText().trim(); 
                String provider = txtProvider.getText().trim();
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String importingDate = (jdImportDate.getDate() != null) ? sdf.format(jdImportDate.getDate()) : "";
                String expiryDate = (jdExpiryDate.getDate() != null) ? sdf.format(jdExpiryDate.getDate()) : "";

                if (name.isEmpty() || capStr.isEmpty() || qtyStr.isEmpty() || giaTruocThueStr.isEmpty() || thueStr.isEmpty() || provider.isEmpty() || importingDate.isEmpty() || expiryDate.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin (Kể cả ngày HSD)!", "Lỗi", JOptionPane.WARNING_MESSAGE); return;
                }

                double dinhLuong1Goi = Double.parseDouble(capStr); 
                int soLuongGoi = Integer.parseInt(qtyStr);
                long giaTruocThue = Long.parseLong(giaTruocThueStr); 
                double thuePercent = Double.parseDouble(thueStr);

                if (dinhLuong1Goi <= 0 || soLuongGoi <= 0 || giaTruocThue < 0 || thuePercent < 0) {
                    JOptionPane.showMessageDialog(this, "Số liệu phải hợp lệ (>=0)!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
                }

                long tienThue = Math.round(giaTruocThue * (thuePercent / 100.0));
                long thanhTien = giaTruocThue + tienThue;

                itemModel.addRow(new Object[]{
                    name, unit, dinhLuong1Goi, soLuongGoi, giaTruocThue, thuePercent, thanhTien, provider, importingDate, expiryDate, "Xóa"
                });
                
                clearFormInputsOnly();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ ở các ô Giá, Lượng, Thuế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomActions = new JPanel(new BorderLayout()); bottomActions.setOpaque(false);
        lblTotal = new JLabel("Tổng cộng: 0 VND"); lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20)); lblTotal.setForeground(PRIMARY_COLOR);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); btns.setOpaque(false);
        btnSubmit = ComponentUI.createModernButton("Hoàn tất nhập hàng", PRIMARY_COLOR, Color.WHITE); btns.add(btnSubmit);

        bottomActions.add(lblTotal, BorderLayout.WEST); bottomActions.add(btns, BorderLayout.EAST);
        content.add(topSection, BorderLayout.NORTH); content.add(itemScroll, BorderLayout.CENTER); content.add(bottomActions, BorderLayout.SOUTH);
        receiptFormView.add(header, BorderLayout.NORTH); receiptFormView.add(content, BorderLayout.CENTER);
    }

    private JPanel createInputWrapper(String labelText, JComponent inputField) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        inputField.setPreferredSize(new Dimension(0, 30));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(inputField, BorderLayout.CENTER);
        return panel;
    }
    
    public void addAddNewIngredientListener(ActionListener listener) {
        btnAddNewIngredient.addActionListener(listener);
    }
    
    public Object[] showAddIngredientDialog(List<IngredientTypeModel> categories) {
        JTextField txtName = new JTextField();
        JComboBox<String> cbUom = new JComboBox<>(new String[]{"kg", "gram", "lít", "ml", "cái"});
        JTextField txtThres = new JTextField("0");

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Loại nguyên liệu:")); 
        panel.add(cbCategory); 
        panel.add(new JLabel("Tên nguyên liệu:")); panel.add(txtName);
        panel.add(new JLabel("Đơn vị tính:")); panel.add(cbUom);
        panel.add(new JLabel("Ngưỡng cảnh báo hết hàng:")); panel.add(txtThres);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm Dữ Liệu Nguyên Liệu Gốc", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int thres = Integer.parseInt(txtThres.getText().trim());
                if (txtName.getText().trim().isEmpty() || thres < 0) throw new Exception();
                
                return new Object[]{
                    cbCategory.getSelectedItem(),
                    txtName.getText().trim(),
                    cbUom.getSelectedItem().toString(),
                    thres
                };
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }
    
    public void loadIngredientsToComboBox(List<IngredientModel> ingredients) {
        if (cbIngredient == null) cbIngredient = new AutoCompleteComboBox<>();
        List<String> names = new ArrayList<>();
        for (IngredientModel ing : ingredients) {
            names.add(ing.getIngredientName());
        }
        cbIngredient.setData(names); 
    }
    
    public void loadIngredientTypesToComboBox(List<IngredientTypeModel> types) {
        if (cbCategory == null) cbCategory = new AutoCompleteComboBox<>();
        cbCategory.setData(types); 
    }
    
    public AutoCompleteComboBox<String> getIngredientComboBox() { return cbIngredient; }
    public AutoCompleteComboBox<IngredientTypeModel> getCategoryComboBox() { return cbCategory; }
    public void setUnitText(String unit) { txtUnit.setText(unit); }

    public void displayIngredientData(List<IngredientModel> danhSach) {
        javax.swing.table.DefaultTableModel tableModel = (javax.swing.table.DefaultTableModel) inventoryTable.getModel();
        tableModel.setRowCount(0); 
        for (IngredientModel nl : danhSach) {
            Object[] rowData = {
                nl.getIngredientID(),            
                nl.getIngredientName(), 
                nl.getUnit(), 
                String.format("%,d", nl.getInStock()), 
                String.format("%,d", nl.getThreshold()), 
                nl.getStatus(),    
                "Sửa / Xóa" 
            };
            tableModel.addRow(rowData);
        }
    }
    
    public void displayWarehouseReceiptData(List<WarehouseReceiptModel> list) {
        javax.swing.table.DefaultTableModel tableModel = (javax.swing.table.DefaultTableModel) historyTable.getModel();
        tableModel.setRowCount(0); 
        for (WarehouseReceiptModel pnk : list) {
            Object[] rowData = {
                pnk.getReceiptID(),          
                pnk.getImportingDate(), 
                pnk.getUserName(),    
                String.format("%,d đ", pnk.getTotal()),
                "Sửa / Xóa"         
            };
            tableModel.addRow(rowData);
        }
    }

    private void initHistoryView() {
        historyView = new JPanel(new BorderLayout(0, 20));
        historyView.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel title = new JLabel("Lịch sử nhập hàng"); title.setFont(new Font("Segoe UI", Font.BOLD, 24)); title.setForeground(TEXT_DARK);
        JButton btnBack = new JButton("← Quay lại kho"); btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnBack.setForeground(PRIMARY_COLOR);
        btnBack.setContentAreaFilled(false); btnBack.setBorderPainted(false); btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "InventoryList"));
        header.add(title, BorderLayout.WEST); header.add(btnBack, BorderLayout.EAST);

        String[] cols = {"Mã phiếu", "Ngày nhập", "Người nhập", "Tổng tiền", "Hành động"};
        historyModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        historyTable = new JTable(historyModel);
        ComponentUI.styleTable(historyTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);

        TableColumnModel tcmHistory = historyTable.getColumnModel();
        tcmHistory.getColumn(0).setPreferredWidth(80); tcmHistory.getColumn(0).setMaxWidth(100);
        tcmHistory.getColumn(1).setPreferredWidth(120);
        tcmHistory.getColumn(2).setPreferredWidth(200);
        tcmHistory.getColumn(3).setPreferredWidth(120);
        
        CenterRenderer centerRenderer = new CenterRenderer();
        for (int i = 0; i < historyTable.getColumnCount() - 1; i++) {
            tcmHistory.getColumn(i).setCellRenderer(centerRenderer);
        }

        TableColumn actionCol = historyTable.getColumnModel().getColumn(4);
        actionCol.setCellRenderer(new ActionButtonRenderer(true, false, true));
        actionCol.setCellEditor(new ActionButtonEditor(new ActionButtonListener() {
            @Override public void onDetail(int row) { if (historyActionListener != null) historyActionListener.onDetail(row); }
            @Override public void onEdit(int row) { if (historyActionListener != null) historyActionListener.onEdit(row); }
            @Override public void onDelete(int row) { if (historyActionListener != null) historyActionListener.onDelete(row); }
        }, true, false, true)); 
        
        actionCol.setPreferredWidth(170); 
        JScrollPane scroll = new JScrollPane(historyTable); scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230))); scroll.getViewport().setBackground(Color.WHITE);
        historyView.add(header, BorderLayout.NORTH); historyView.add(scroll, BorderLayout.CENTER);
    }

    class StatCard extends JPanel {
        private JLabel lblValue;
        public StatCard(String title, String value, String unit) {
            setLayout(new BorderLayout(0, 5)); setBackground(Color.WHITE); setBorder(new EmptyBorder(20, 20, 20, 20));
            JLabel lblTitle = new JLabel(title); lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lblTitle.setForeground(TEXT_MUTED);
            lblValue = new JLabel(value);  lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24)); lblValue.setForeground(TEXT_DARK);
            JLabel lblUnit = new JLabel(unit); lblUnit.setFont(new Font("Segoe UI", Font.ITALIC, 12)); lblUnit.setForeground(TEXT_MUTED);
            add(lblTitle, BorderLayout.NORTH); add(lblValue, BorderLayout.CENTER); add(lblUnit, BorderLayout.SOUTH);
        }
        public void setValue(String newValue) { if (this.lblValue != null) this.lblValue.setText(newValue); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(new Color(0, 0, 0, 20)); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15); g2.dispose();
        }
        @Override public void setOpaque(boolean isOpaque) { super.setOpaque(false); }
    }

    class CenterRenderer extends DefaultTableCellRenderer {
        public CenterRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    class InventoryStatusRenderer extends DefaultTableCellRenderer {
        private final Color WARNING_COLOR = new Color(255, 230, 230); 
        private final Color WARNING_TEXT_COLOR = new Color(220, 53, 69); 
        private final Color NORMAL_BG = Color.WHITE;
        private final Color NORMAL_TEXT = TEXT_DARK;
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = table.getValueAt(row, 5).toString(); 
            if (!isSelected) { 
                if (status.equalsIgnoreCase("Hết hàng") || status.equalsIgnoreCase("Sắp hết")) {
                    c.setBackground(WARNING_COLOR);
                    if(column == 5) c.setForeground(WARNING_TEXT_COLOR); else c.setForeground(NORMAL_TEXT);
                } else { c.setBackground(NORMAL_BG); c.setForeground(NORMAL_TEXT); }
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 5, 0, 5)); return c;
        }
    }

    public interface ActionButtonListener { void onDetail(int row); void onEdit(int row); void onDelete(int row); }

    class ActionPanel extends JPanel {
        URL editIconUrl = getClass().getResource("/images/edit-247.png");
        URL deleteIconUrl = getClass().getResource("/images/delete-icon.png");
        protected JButton btnDetail = new JButton("Xem chi tiết"); 
        protected JButton btnEdit = new JButton("<html><img src='" + editIconUrl + "' width='12' height='12'> Sửa</html>"); 
        protected JButton btnDelete = new JButton("<html><img src='" + deleteIconUrl + "' width='12' height='12'> Xóa</html>");
        public ActionPanel(boolean showDetail, boolean showEdit, boolean showDelete) {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 8)); setOpaque(true); setBackground(Color.WHITE);
            if (showDetail) { styleButton(btnDetail, new Color(0, 0, 0), 95, 30); add(btnDetail); }
            if (showEdit) { styleButton(btnEdit, new Color(0, 122, 255), 75, 30); add(btnEdit); }
            if (showDelete) { styleButton(btnDelete, new Color(255, 59, 48), 75, 30); add(btnDelete); }
        }
        protected void styleButton(JButton btn, Color color, int width, int height) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12)); btn.setForeground(color); btn.setBackground(Color.WHITE); btn.setBorder(BorderFactory.createLineBorder(color, 1));
            btn.setFocusPainted(false); btn.setPreferredSize(new Dimension(width, height)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    class ActionButtonRenderer implements TableCellRenderer {
        protected ActionPanel panel;
        public ActionButtonRenderer(boolean showDetail, boolean showEdit, boolean showDelete) { this.panel = new ActionPanel(showDetail, showEdit, showDelete); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE); 
            if (panel.btnEdit != null) panel.btnEdit.setVisible(hasEditPermission);
            if (panel.btnDelete != null) panel.btnDelete.setVisible(hasDeletePermission);
            return panel;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        protected ActionPanel panel; protected ActionButtonListener listener; protected int currentRow;
        public ActionButtonEditor(ActionButtonListener listener, boolean showDetail, boolean showEdit, boolean showDelete) {
            super(new JCheckBox()); this.listener = listener; this.panel = new ActionPanel(showDetail, showEdit, showDelete);
            if (showDetail) this.panel.btnDetail.addActionListener(e -> { stopCellEditing(); listener.onDetail(currentRow); });
            if (showEdit) this.panel.btnEdit.addActionListener(e -> { stopCellEditing(); listener.onEdit(currentRow); });
            if (showDelete) this.panel.btnDelete.addActionListener(e -> { stopCellEditing(); listener.onDelete(currentRow); });
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row; panel.setBackground(table.getSelectionBackground()); 
            if (panel.btnEdit != null) panel.btnEdit.setVisible(hasEditPermission);
            if (panel.btnDelete != null) panel.btnDelete.setVisible(hasDeletePermission);
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }

    public void addSubmitReceiptListener(ActionListener listener) { btnSubmit.addActionListener(listener); }
    private static class Receipt { String id; String date; String supplier; double totalAmount; List<ReceiptItem> items = new ArrayList<>(); }
    private static class ReceiptItem { String name; String unit; double quantity; double price; double threshold; String supplier; String date; }
    public void setInventoryActionListener(ActionButtonListener listener) { this.inventoryActionListener = listener; }
    public JTable getInventoryTable() { return inventoryTable; }
    public DefaultTableModel getInventoryModel() { return inventoryModel; }
    
    public Object[] showEditDialog(String ten, String dvt, int ton, int nguong) {
        JTextField txtTen = new JTextField(ten); JTextField txtDVT = new JTextField(dvt); 
        JTextField txtTon = new JTextField(String.valueOf(ton));
        txtTon.setEditable(false); // [MỚI] Vô hiệu hóa việc sửa số lượng tồn kho trực tiếp
        
        JTextField txtNguong = new JTextField(String.valueOf(nguong)); JTextField txtLyDo = new JTextField(); 
        Object[] message = { "Tên nguyên liệu:", txtTen, "Đơn vị tính:", txtDVT, "Số lượng tồn hiện tại (Không thể sửa trực tiếp):", txtTon, "Ngưỡng báo động:", txtNguong, "Lý do chỉnh sửa:", txtLyDo };
        int option = JOptionPane.showConfirmDialog(this, message, "Chỉnh sửa nguyên liệu", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String lyDo = txtLyDo.getText().trim();
                if (lyDo.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập lý do chỉnh sửa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return null; }
                return new Object[]{ txtTen.getText().trim(), txtDVT.getText().trim(), Integer.parseInt(txtTon.getText().trim()), Integer.parseInt(txtNguong.getText().trim()), lyDo };
            } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Số lượng và ngưỡng phải là con số!"); }
        }
        return null; 
    }
    
    public DefaultTableModel getReceiptItemModel() { return itemModel; }
    public void clearReceiptForm() {
        if (itemModel != null) { itemModel.setRowCount(0); itemModel.fireTableDataChanged(); }
        if (lblTotal != null) { lblTotal.setText("Tổng cộng: 0 VND"); }
        clearFormInputsOnly();
    }
    
    private void clearFormInputsOnly() {
        if (cbIngredient != null && cbIngredient.getItemCount() > 0) cbIngredient.setSelectedIndex(0);
        if (txtUnitCapacity != null) txtUnitCapacity.setText("");
        if (txtQuantity != null) txtQuantity.setText("");
        if (txtGiaTruocThue != null) txtGiaTruocThue.setText("");
        if (txtThueGTGT != null) txtThueGTGT.setText("8");
        if (txtProvider != null) txtProvider.setText("");
        if (jdImportDate != null) jdImportDate.setDate(new java.util.Date()); 
        if (jdExpiryDate != null) jdExpiryDate.setDate(null); 
    }
    
    public void addHistoryButtonListener(ActionListener listener) { btnHistory.addActionListener(listener); }
    public void addCategoryButtonListener(ActionListener listener) {
        btnAddCategory.addActionListener(listener);
    }
    public void setHistoryActionListener(ActionButtonListener listener) { this.historyActionListener = listener; }
    public JTable getHistoryTable() { return historyTable; }
    public DefaultTableModel getHistoryModel() { return historyModel; }
    public void setTotalAmountLabel(long tongCong) { if (lblTotal != null) lblTotal.setText("Tổng cộng: " + String.format("%,d", tongCong) + " VND"); }
    // [CẬP NHẬT] Đã xóa StatCard, giữ lại hàm trống để tương thích ngược với Controller
    public void updateDashboardStats(int warningCount) {
    }

    // [CẬP NHẬT] Đã xóa StatCard, giữ lại hàm trống để tương thích ngược với Controller
    public void updateDashboardStats(int totalTypes, int warningCount) {
    }

    // --- PHÂN QUYỀN ---
    private boolean hasEditPermission = true;
    private boolean hasDeletePermission = true;
    
    public void setActionPermissions(boolean canEdit, boolean canDelete) {
        this.hasEditPermission = canEdit;
        this.hasDeletePermission = canDelete;
        this.repaint();
    }
    
    public JButton getBtnAddNewIngredient() { return btnAddNewIngredient; }
    public JButton getBtnImport() { return btnImport; }
    public JButton getBtnAddCategory() { return btnAddCategory; }
    
    public String showAddCategoryDialog() {
        return JOptionPane.showInputDialog(this, "Nhập tên loại nguyên liệu mới:", "Thêm Loại Nguyên Liệu", JOptionPane.QUESTION_MESSAGE);
    }
    
    // [MỚI] Interface cho tính năng in phiếu nhập kho
    public interface PrintReceiptListener {
        void onPrint(int receiptID);
    }

    // [CẬP NHẬT] Thêm tham số printListener và chèn nút "In phiếu nhập" vào Footer của hộp thoại
    public void showReceiptDetailTableDialog(int receiptID, List<Object[]> details, boolean canPrint, PrintReceiptListener printListener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết phiếu nhập #" + receiptID, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(850, 450);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"Tên Nguyên Liệu", "SL Nhập", "Định Lượng", "ĐVT", "Hạn Sử Dụng", "Thành Tiền"};
        DefaultTableModel model = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        long total = 0;
        for (Object[] row : details) {
            model.addRow(row);
            total += (long) row[5]; 
        }

        JTable table = new JTable(model);
        ComponentUI.styleTable(table, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);

        // Bố cục mới cho Footer: In ở bên trái, Tổng tiền ở bên phải
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        footer.setOpaque(false);
        
        JButton btnPrint = ComponentUI.createModernButton("In phiếu nhập", PRINT_COLOR, Color.WHITE);
        btnPrint.setVisible(canPrint);
        btnPrint.addActionListener(e -> {
            if (printListener != null) {
                printListener.onPrint(receiptID);
            }
            dialog.dispose();
        });
        
        JLabel lblTotal = new JLabel("Tổng giá trị phiếu: " + String.format("%,d VNĐ", total));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(new Color(231, 76, 60));
        
        footer.add(btnPrint, BorderLayout.WEST);
        footer.add(lblTotal, BorderLayout.EAST);

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
    
    public void showBatchDetailDialog(String tenNL, List<Object[]> batches, BatchDisposeListener listener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết các lô hàng: " + tenNL, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"Mã Lô", "Ngày Nhập", "Tồn Lô", "Hạn Sử Dụng", "Hành động"};
        DefaultTableModel model = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };

        for (Object[] b : batches) { model.addRow(new Object[]{ b[0], b[1], b[2], b[3], "Xuất hủy" }); }

        JTable table = new JTable(model);
        ComponentUI.styleTable(table, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        
        TableColumn actionCol = table.getColumnModel().getColumn(4);
        actionCol.setCellRenderer(new ActionButtonRenderer(false, false, true)); 
        
        actionCol.setCellEditor(new ActionButtonEditor(new ActionButtonListener() {
            @Override public void onDelete(int row) {
                int maLo = (int) table.getValueAt(row, 0);
                double tonHienTai = (double) table.getValueAt(row, 2);
                
                JTextField txtQty = new JTextField();
                JTextField txtReason = new JTextField();
                Object[] message = { "Số lượng hủy:", txtQty, "Lý do hủy:", txtReason };
                
                int option = JOptionPane.showConfirmDialog(dialog, message, "Xác nhận xuất hủy lô #" + maLo, JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        double qty = Double.parseDouble(txtQty.getText());
                        String reason = txtReason.getText().trim();
                        if (qty <= 0 || qty > tonHienTai || reason.isEmpty()) {
                            JOptionPane.showMessageDialog(dialog, "Dữ liệu không hợp lệ hoặc vượt quá tồn lô!");
                            return;
                        }
                        listener.onDispose(maLo, qty, reason);
                        dialog.dispose(); 
                    } catch (Exception e) { JOptionPane.showMessageDialog(dialog, "Vui lòng nhập số hợp lệ!"); }
                }
            }
            @Override public void onEdit(int row) {} @Override public void onDetail(int row) {}
        }, false, false, true));

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // [MỚI] Lấy từ khóa tìm kiếm hiện tại từ ô txtSearch, bỏ qua text mặc định gợi ý
    public String getSearchText() {
        String text = txtSearch.getText().trim();
        if (text.equals("Tìm kiếm nguyên liệu...")) {
            return "";
        }
        return text;
    }

    // [MỚI] Đăng ký bộ lắng nghe sự kiện gõ phím trên ô tìm kiếm
    public void addSearchListener(java.awt.event.KeyListener listener) {
        txtSearch.addKeyListener(listener);
    }

    // [MỚI] Kiểm tra xem nút lọc nguyên liệu cần nhập có đang được chọn hay không
    public boolean isFilterWarningSelected() {
        return btnFilterWarning.isSelected();
    }

    // [MỚI] Đăng ký bộ lắng nghe sự kiện khi click nút lọc nguyên liệu cần nhập
    public void addFilterWarningListener(java.awt.event.ActionListener listener) {
        btnFilterWarning.addActionListener(listener);
    }

    public interface BatchDisposeListener { void onDispose(int maLo, double qty, String reason); }
}