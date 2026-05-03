package View;

import Common.ComponentUI;
import Model.IngredientModel;
import Model.IngredientTypeModel;
import Model.WarehouseReceiptModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import com.toedter.calendar.JDateChooser;

/**
 * Phân hệ Quản lý Kho - The Bang Coffee.
 * Thiết kế cho phép quản lý tồn kho và nhập hàng ngay trong panel chính.
 */
public class StockPanel extends JPanel {

    // Colors (Đồng bộ với MainFrame)
    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(33, 37, 41);
    private final Color TEXT_MUTED = new Color(108, 117, 125);

    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    // Views
    private JPanel inventoryListView;
    private JPanel receiptFormView;
    private JPanel historyView;

    // Models
    private DefaultTableModel inventoryModel;
    private JTable inventoryTable;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private DefaultTableModel itemModel; // Model cho bảng nhập hàng
    private List<Receipt> historyList = new ArrayList<>();
    private Receipt editingReceipt = null; // Receipt đang được sửa
    private JLabel lblTotal;
    private JButton btnSubmit;
    private JButton btnHistory;
    
    // Listner
    private ActionButtonListener inventoryActionListener;
    private ActionButtonListener historyActionListener;
    
    // Form Inputs
    private JComboBox<IngredientTypeModel> cbCategory; 
    private JTextField txtIngredientName;
    private JComboBox<String> cbUnit;
    private JTextField txtUnitCapacity; 
    private JTextField txtQuantity;     
    private JTextField txtThreshold;
    private JTextField txtProvider;
    
    // [MỚI] Các trường liên quan đến Thuế
    private JTextField txtGiaTruocThue;   
    private JTextField txtThueGTGT;
    
    private JDateChooser jdImportDate;
    private JDateChooser jdExpiryDate;
    private JButton btnAddCategory;
    
    private StatCard cardTotalIngredients;
    private StatCard cardWarning;

    public StockPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setOpaque(false);

        initInventoryListView();
        
        initHistoryView(); 
        initReceiptFormView();

        mainContainer.add(inventoryListView, "InventoryList");
        mainContainer.add(historyView, "HistoryList");
        mainContainer.add(receiptFormView, "ReceiptForm");

        add(mainContainer, BorderLayout.CENTER);
    }

    private void initInventoryListView() {
        inventoryListView = new JPanel(new BorderLayout(0, 25));
        inventoryListView.setOpaque(false);

        // 1. Stats Row
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        
        // Khởi tạo và gán vào biến
        cardTotalIngredients = new StatCard("Tổng nguyên liệu", "0", "Loại");
        cardWarning = new StatCard("Cần nhập hàng", "0", "Cảnh báo");

        statsPanel.add(cardTotalIngredients);
        statsPanel.add(cardWarning);

        // 2. Action Bar
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);
        
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm nguyên liệu...");
        searchField.setPreferredSize(new Dimension(250, 40));
        leftActions.add(searchField);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        btnHistory = ComponentUI.createModernButton("Lịch sử nhập hàng", new Color(240, 240, 240), TEXT_DARK);
        
        JButton btnImport = ComponentUI.createModernButton("Nhập hàng mới +", PRIMARY_COLOR, Color.WHITE);
        
        btnHistory.addActionListener(e -> cardLayout.show(mainContainer, "HistoryList"));
        
        btnImport.addActionListener(e -> {
            editingReceipt = null; 
            cardLayout.show(mainContainer, "ReceiptForm");
        });

        rightActions.add(btnHistory);
        rightActions.add(btnImport);

        actionBar.add(leftActions, BorderLayout.WEST);
        actionBar.add(rightActions, BorderLayout.EAST);

        // 3. Table
        String[] columns = {"ID", "Tên nguyên liệu", "Tổng tồn kho hiện tại", "Ngưỡng cảnh báo", "Trạng thái", "Hành động"};
        inventoryModel = new DefaultTableModel(null, columns) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        inventoryTable = new JTable(inventoryModel);
        ComponentUI.styleTable(inventoryTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        
        TableColumn actionCol = inventoryTable.getColumnModel().getColumn(5);
        actionCol.setCellRenderer(new ActionButtonRenderer(true, true, true));
        actionCol.setCellEditor(new ActionButtonEditor(new ActionButtonListener() {
            @Override 
            public void onEdit(int row) { 
                if (inventoryActionListener != null) {
                    inventoryActionListener.onEdit(row);
                }
            }
            @Override 
            public void onDelete(int row) { 
                if (inventoryActionListener != null) {
                    inventoryActionListener.onDelete(row);
                }
            }
            @Override
            public void onDetail(int row) {
                if (inventoryActionListener != null) 
                    inventoryActionListener.onDetail(row);
            }
        }, true, true, true));
        actionCol.setPreferredWidth(240);
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        inventoryListView.add(statsPanel, BorderLayout.NORTH);
        
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setOpaque(false);
        centerWrapper.add(actionBar, BorderLayout.NORTH);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        
        inventoryListView.add(centerWrapper, BorderLayout.CENTER);
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

        // KHỞI TẠO CÁC Ô NHẬP LIỆU ĐỂ TRÁNH NULL POINTER EXCEPTION
        cbCategory = new JComboBox<>(); 
        cbCategory.setBackground(Color.WHITE);
        
        txtIngredientName = new JTextField();
        String[] units = {"kg", "gram", "lít", "ml"}; 
        cbUnit = new JComboBox<>(units); cbUnit.setBackground(Color.WHITE);
        
        txtUnitCapacity = new JTextField(); 
        txtQuantity = new JTextField();
        txtThreshold = new JTextField("0"); 
        txtProvider = new JTextField();
        
        txtGiaTruocThue = new JTextField();   
        txtThueGTGT = new JTextField("8"); // Mặc định 8%
        
        jdImportDate = new JDateChooser();
        jdImportDate.setDateFormatString("yyyy-MM-dd"); // Chuẩn format đẩy xuống DB
        jdImportDate.setDate(new java.util.Date()); 

        jdExpiryDate = new JDateChooser();
        jdExpiryDate.setDateFormatString("yyyy-MM-dd");

        // 1. CHỈNH LẠI LƯỚI: 4 dòng x 3 cột = 12 ô. (Sử dụng 11 ô, dư 1 ô)
        JPanel formPanel = new JPanel(new GridLayout(4, 3, 15, 15)); 
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết"));
        
        // 2. Khởi tạo nút + (Gọn gàng)
        btnAddCategory = new JButton("+");
        btnAddCategory.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnAddCategory.setBackground(PRIMARY_COLOR); // Hoặc màu (0, 122, 255) tùy bạn
        btnAddCategory.setForeground(Color.WHITE);
        btnAddCategory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddCategory.setPreferredSize(new Dimension(40, 30));
        btnAddCategory.setMargin(new Insets(0, 0, 0, 0));
        
        // 3. Gom Category Cbb + Btn thành 1 Panel nằm ngang
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 0)); // Khoảng cách 5px
        categoryPanel.setOpaque(false);
        categoryPanel.add(cbCategory, BorderLayout.CENTER);
        categoryPanel.add(btnAddCategory, BorderLayout.EAST);
        
        // 4. ADD COMPONENTS THEO ĐÚNG LƯỚI 4x3
        formPanel.add(createInputWrapper("Loại nguyên liệu:", categoryPanel)); // 1
        formPanel.add(createInputWrapper("Tên nguyên liệu:", txtIngredientName)); // 2
        formPanel.add(createInputWrapper("Đơn vị tính:", cbUnit)); // 3
        
        formPanel.add(createInputWrapper("Định lượng 1 bao/hộp:", txtUnitCapacity)); // 4
        formPanel.add(createInputWrapper("Số lượng (gói/hộp):", txtQuantity)); // 5
        formPanel.add(createInputWrapper("Giá trước thuế (Tổng):", txtGiaTruocThue)); // 6
        
        formPanel.add(createInputWrapper("Thuế GTGT (%):", txtThueGTGT)); // 7
        formPanel.add(createInputWrapper("Ngưỡng cảnh báo (gói/hộp):", txtThreshold)); // 8
        formPanel.add(createInputWrapper("Nhà cung cấp:", txtProvider)); // 9
        
        formPanel.add(createInputWrapper("Ngày nhập:", jdImportDate)); // 10
        formPanel.add(createInputWrapper("Hạn sử dụng:", jdExpiryDate)); // 11

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

        // [SỬA LẠI] Đủ 13 Cột để Controller lấy data map với DB
        String[] itemCols = {"Loại NL", "Tên NL", "ĐVT", "Đ.Lượng/Gói", "Số lượng", "Giá T.Thuế", "Thuế (%)", "Thành tiền", "Ngưỡng", "Nhà cung cấp", "Ngày nhập", "Hạn sử dụng", "Hành động"};
        itemModel = new DefaultTableModel(null, itemCols) {
            @Override 
            public boolean isCellEditable(int row, int column) { 
                return column == 12; // Cột hành động
            }
        };
        JTable itemTable = new JTable(itemModel);
        ComponentUI.styleTable(itemTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        JScrollPane itemScroll = new JScrollPane(itemTable); itemScroll.setPreferredSize(new Dimension(0, 300));

        TableColumn actionCol = itemTable.getColumnModel().getColumn(12);
        actionCol.setCellRenderer(new ActionButtonRenderer(false, true, true));
        actionCol.setCellEditor(new ActionButtonEditor(new ActionButtonListener() {
            @Override
            public void onEdit(int row) {
                // Mapping Index
                String catStr = itemModel.getValueAt(row, 0).toString();
                for(int i = 0; i < cbCategory.getItemCount(); i++) {
                    if(cbCategory.getItemAt(i).getTypeName().equals(catStr)) { 
                        cbCategory.setSelectedIndex(i); 
                        break; 
                    }
                }
                txtIngredientName.setText(itemModel.getValueAt(row, 1).toString());
                cbUnit.setSelectedItem(itemModel.getValueAt(row, 2).toString());
                
                txtUnitCapacity.setText(itemModel.getValueAt(row, 3).toString()); 
                txtQuantity.setText(itemModel.getValueAt(row, 4).toString());
                txtGiaTruocThue.setText(itemModel.getValueAt(row, 5).toString());
                txtThueGTGT.setText(itemModel.getValueAt(row, 6).toString());
                txtThreshold.setText(itemModel.getValueAt(row, 8).toString());
                txtProvider.setText(itemModel.getValueAt(row, 9).toString());
                
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    
                    String importStr = itemModel.getValueAt(row, 10).toString();
                    if (!importStr.isEmpty()) {
                        jdImportDate.setDate(sdf.parse(importStr));
                    }
                    
                    String expiryStr = itemModel.getValueAt(row, 11).toString();
                    if (!expiryStr.isEmpty()) {
                        jdExpiryDate.setDate(sdf.parse(expiryStr));
                    }
                } catch (java.text.ParseException ex) {
                    ex.printStackTrace();
                }
                
                itemModel.removeRow(row);
            }
            @Override
            public void onDelete(int row) { itemModel.removeRow(row); }

            @Override
            public void onDetail(int row) {}
        }, false, true, true));
        actionCol.setPreferredWidth(160);

        btnAddToList.addActionListener(e -> {
            try {
                if(cbCategory.getSelectedItem() == null) return;
                String category = ((IngredientTypeModel) cbCategory.getSelectedItem()).getTypeName();
                String name = txtIngredientName.getText().trim();
                String unit = cbUnit.getSelectedItem().toString();
                String capStr = txtUnitCapacity.getText().trim();
                String qtyStr = txtQuantity.getText().trim();
                String giaTruocThueStr = txtGiaTruocThue.getText().trim(); 
                String thueStr = txtThueGTGT.getText().trim(); 
                String thresStr = txtThreshold.getText().trim();
                String provider = txtProvider.getText().trim();
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                
                String importingDate = "";
                if (jdImportDate.getDate() != null) {
                    importingDate = sdf.format(jdImportDate.getDate());
                }
                
                String expiryDate = "";
                if (jdExpiryDate.getDate() != null) {
                    expiryDate = sdf.format(jdExpiryDate.getDate());
                }

                if (name.isEmpty() || capStr.isEmpty() || qtyStr.isEmpty() || giaTruocThueStr.isEmpty() || thueStr.isEmpty() || provider.isEmpty() || thresStr.isEmpty() || importingDate.isEmpty() || expiryDate.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin (Kể cả ngày HSD)!", "Lỗi", JOptionPane.WARNING_MESSAGE); return;
                }

                double dinhLuong1Goi = Double.parseDouble(capStr); 
                int soLuongGoi = Integer.parseInt(qtyStr);
                long giaTruocThue = Long.parseLong(giaTruocThueStr); 
                double thuePercent = Double.parseDouble(thueStr);
                int threshold = Integer.parseInt(thresStr);

                if (dinhLuong1Goi <= 0 || soLuongGoi <= 0 || giaTruocThue < 0 || thuePercent < 0 || threshold < 0) {
                    JOptionPane.showMessageDialog(this, "Số liệu phải hợp lệ (>=0)!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
                }

                // Tính toán
                long tienThue = Math.round(giaTruocThue * (thuePercent / 100.0));
                long thanhTien = giaTruocThue + tienThue;

                // Push xuống Table Model đúng vị trí
                itemModel.addRow(new Object[]{
                    category, name, unit, dinhLuong1Goi, soLuongGoi, giaTruocThue, thuePercent, thanhTien, threshold, provider, importingDate, expiryDate, "Sửa/Xóa"
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
    
    public void displayIngredientData(List<IngredientModel> danhSach) {
        javax.swing.table.DefaultTableModel tableModel = (javax.swing.table.DefaultTableModel) inventoryTable.getModel();
        tableModel.setRowCount(0); 
        
        for (IngredientModel nl : danhSach) {
            Object[] rowData = {
                nl.getIngredientID(),            
                nl.getIngredientName(), 
                String.format("%,d %s", nl.getInStock(), nl.getUnit()),
                String.format("%,d %s", nl.getThreshold(), nl.getUnit()),
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
                pnk.getTotal(),
                "Sửa / Xóa"         
            };
            tableModel.addRow(rowData);
        }
    }

    private void initHistoryView() {
        historyView = new JPanel(new BorderLayout(0, 20));
        historyView.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Lịch sử nhập hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);

        JButton btnBack = new JButton("← Quay lại kho");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(PRIMARY_COLOR);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "InventoryList"));

        header.add(title, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);

        String[] cols = {"Mã phiếu", "Ngày nhập", "Người nhập", "Tổng tiền", "Hành động"};
        historyModel = new DefaultTableModel(null, cols) {
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return c == 4; 
            }
        };
        historyTable = new JTable(historyModel);
        ComponentUI.styleTable(historyTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);

        TableColumn actionCol = historyTable.getColumnModel().getColumn(4);
        actionCol.setCellRenderer(new ActionButtonRenderer(true, false, true));
        actionCol.setCellEditor(new ActionButtonEditor(new ActionButtonListener() {
            @Override
            public void onDetail(int row) {
                if (historyActionListener != null) historyActionListener.onDetail(row);
            }
            @Override
            public void onEdit(int row) {
                if (historyActionListener != null) historyActionListener.onEdit(row);
            }
            @Override
            public void onDelete(int row) {
                if (historyActionListener != null) historyActionListener.onDelete(row);
            }
        }, true, false, true)); 
        
        actionCol.setPreferredWidth(170); 

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);

        historyView.add(header, BorderLayout.NORTH);
        historyView.add(scroll, BorderLayout.CENTER);
    }

    private void loadReceiptToForm(Receipt r) {
        itemModel.setRowCount(0);
        for (ReceiptItem item : r.items) {
            itemModel.addRow(new Object[]{item.name, item.unit, item.quantity, item.price, item.threshold, item.supplier, item.date, "X"});
        }
    }

    class StatCard extends JPanel {
        private JLabel lblValue;
        public StatCard(String title, String value, String unit) {
            setLayout(new BorderLayout(0, 5));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblTitle.setForeground(TEXT_MUTED);

            lblValue = new JLabel(value); 
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblValue.setForeground(TEXT_DARK);

            JLabel lblUnit = new JLabel(unit);
            lblUnit.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblUnit.setForeground(TEXT_MUTED);

            add(lblTitle, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
            add(lblUnit, BorderLayout.SOUTH);
        }
        
        public void setValue(String newValue) {
            if (this.lblValue != null) {
                this.lblValue.setText(newValue);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(new Color(0, 0, 0, 20));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            g2.dispose();
        }
        @Override public void setOpaque(boolean isOpaque) { super.setOpaque(false); }
    }

    public interface ActionButtonListener {
        void onDetail(int row);
        void onEdit(int row);
        void onDelete(int row);
    }

    class ActionPanel extends JPanel {
        protected JButton btnDetail = new JButton("Xem chi tiết");
        protected JButton btnEdit = new JButton("Sửa");
        protected JButton btnDelete = new JButton("Xóa");

        public ActionPanel(boolean showDetail, boolean showEdit, boolean showDelete) {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 8));
            setOpaque(true);
            setBackground(Color.WHITE);

            if (showDetail) {
                styleButton(btnDetail, new Color(0, 0, 0), 95, 30);
                add(btnDetail);
            }
            if (showEdit) {
                styleButton(btnEdit, new Color(0, 122, 255), 60, 30);
                add(btnEdit);
            }
            if (showDelete) {
                styleButton(btnDelete, new Color(255, 59, 48), 60, 30);
                add(btnDelete);
            }
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

    class ActionButtonRenderer implements TableCellRenderer {
        protected ActionPanel panel;

        public ActionButtonRenderer(boolean showDetail, boolean showEdit, boolean showDelete) {
            this.panel = new ActionPanel(showDetail, showEdit, showDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return panel;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        protected ActionPanel panel;
        protected ActionButtonListener listener;
        protected int currentRow;

        public ActionButtonEditor(ActionButtonListener listener, boolean showDetail, boolean showEdit, boolean showDelete) {
            super(new JCheckBox());
            this.listener = listener;
            this.panel = new ActionPanel(showDetail, showEdit, showDelete);

            if (showDetail) this.panel.btnDetail.addActionListener(e -> { stopCellEditing(); listener.onDetail(currentRow); });
            if (showEdit) this.panel.btnEdit.addActionListener(e -> { stopCellEditing(); listener.onEdit(currentRow); });
            if (showDelete) this.panel.btnDelete.addActionListener(e -> { stopCellEditing(); listener.onDelete(currentRow); });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override public Object getCellEditorValue() { return ""; }
    }

    public void addSubmitReceiptListener(ActionListener listener) {
        btnSubmit.addActionListener(listener);
    }
    
    private static class Receipt {
        String id;
        String date;
        String supplier;
        double totalAmount;
        List<ReceiptItem> items = new ArrayList<>();
    }

    private static class ReceiptItem {
        String name;
        String unit;
        double quantity;
        double price;
        double threshold;
        String supplier;
        String date;
    }
    
    public void setInventoryActionListener(ActionButtonListener listener) {
        this.inventoryActionListener = listener;
    }

    public JTable getInventoryTable() { return inventoryTable; }
    public DefaultTableModel getInventoryModel() { return inventoryModel; }
    
    public Object[] showEditDialog(String ten, String dvt, int ton, int nguong) {
        JTextField txtTen = new JTextField(ten);
        JTextField txtDVT = new JTextField(dvt);
        JTextField txtTon = new JTextField(String.valueOf(ton));
        JTextField txtNguong = new JTextField(String.valueOf(nguong));
        JTextField txtLyDo = new JTextField(); 

        Object[] message = {
            "Tên nguyên liệu:", txtTen,
            "Đơn vị tính:", txtDVT,
            "Số lượng tồn hiện tại:", txtTon,
            "Ngưỡng báo động:", txtNguong,
            "Lý do chỉnh sửa:", txtLyDo 
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Chỉnh sửa nguyên liệu", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            try {
                String lyDo = txtLyDo.getText().trim();
                if (lyDo.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập lý do chỉnh sửa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return null; 
                }

                return new Object[]{
                    txtTen.getText().trim(), 
                    txtDVT.getText().trim(), 
                    Integer.parseInt(txtTon.getText().trim()), 
                    Integer.parseInt(txtNguong.getText().trim()),
                    lyDo
                };
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số lượng và ngưỡng phải là con số!");
            }
        }
        return null; 
    }
    
    public DefaultTableModel getReceiptItemModel() {
        return itemModel;
    }
    
    public void loadIngredientTypesToComboBox(List<IngredientTypeModel> types) {
        cbCategory.removeAllItems();
        for (IngredientTypeModel type : types) { cbCategory.addItem(type); }
    }

    public void clearReceiptForm() {
        if (itemModel != null) { itemModel.setRowCount(0); itemModel.fireTableDataChanged(); }
        if (lblTotal != null) { lblTotal.setText("Tổng cộng: 0 VND"); }
        clearFormInputsOnly();
    }
    
    private void clearFormInputsOnly() {
        if (cbCategory != null && cbCategory.getItemCount() > 0) cbCategory.setSelectedIndex(0);
        if (txtIngredientName != null) txtIngredientName.setText("");
        if (cbUnit != null) cbUnit.setSelectedIndex(0);
        if (txtUnitCapacity != null) txtUnitCapacity.setText("");
        if (txtQuantity != null) txtQuantity.setText("");
        if (txtGiaTruocThue != null) txtGiaTruocThue.setText("");
        if (txtThueGTGT != null) txtThueGTGT.setText("8");
        if (txtThreshold != null) txtThreshold.setText("0"); 
        if (txtProvider != null) txtProvider.setText("");
        if (jdImportDate != null) jdImportDate.setDate(new java.util.Date()); 
        if (jdExpiryDate != null) jdExpiryDate.setDate(null); 
    }
    
    public void addHistoryButtonListener(ActionListener listener) {
        btnHistory.addActionListener(listener);
    }
        
    public void setHistoryActionListener(ActionButtonListener listener) {
        this.historyActionListener = listener;
    }

    public JTable getHistoryTable() { return historyTable; }
    public DefaultTableModel getHistoryModel() { return historyModel; }
    
    public void setTotalAmountLabel(long tongCong) {
        if (lblTotal != null) {
            lblTotal.setText("Tổng cộng: " + String.format("%,d", tongCong) + " VND");
        }
    }
    
    public void addCategoryButtonListener(ActionListener listener) {
        btnAddCategory.addActionListener(listener);
    }

    // 2. Bổ sung hàm hiển thị hộp thoại thêm danh mục
    public String showAddCategoryDialog() {
        return JOptionPane.showInputDialog(this, "Nhập tên loại nguyên liệu mới:", "Thêm Loại Nguyên Liệu", JOptionPane.QUESTION_MESSAGE);
    }
    
    public JComboBox<IngredientTypeModel> getCategoryComboBox() {
        return cbCategory;
    }
    
    public void updateDashboardStats(int totalTypes, int warningCount) {
        if (cardTotalIngredients != null) 
            cardTotalIngredients.setValue(String.valueOf(totalTypes));
        if (cardWarning != null) 
            cardWarning.setValue(String.valueOf(warningCount));
    }
}