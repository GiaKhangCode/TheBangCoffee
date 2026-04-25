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
    private StockPanel stockPanel;
    
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
    
    // Thêm vào để reset khi chuyển giao diện
    private JComboBox<IngredientTypeModel> cbCategory; 
    private JTextField txtIngredientName;
    private JComboBox<String> cbUnit;
    private JTextField txtUnitCapacity; 
    private JTextField txtQuantity;     
    private JTextField txtTotalPrice;   
    private JTextField txtThreshold;
    private JTextField txtProvider;
    private JTextField txtTotalCapacity; 
    private com.toedter.calendar.JDateChooser jdImportDate;
    private com.toedter.calendar.JDateChooser jdExpiryDate;
    private JButton btnAddCategory;
    
    private StatCard cardTotalIngredients;
    private StatCard cardWarning;
    //private StatCard cardTotalValue;

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
        //cardTotalValue = new StatCard("Giá trị kho", "0 VND", "Ước tính");

        statsPanel.add(cardTotalIngredients);
        statsPanel.add(cardWarning);
        //statsPanel.add(cardTotalValue);

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
        
        // Thêm Buttons vào cột Hành động
        TableColumn actionCol = inventoryTable.getColumnModel().getColumn(5);
        actionCol.setCellRenderer(new ActionButtonRenderer(true, true, true));
        actionCol.setCellEditor(new ActionButtonEditor(new ActionButtonListener() {
            @Override 
            public void onEdit(int row) { 
                // View chỉ báo: "Ê, có người bấm Sửa ở dòng này nè"
                if (inventoryActionListener != null) {
                    inventoryActionListener.onEdit(row);
                }
            }
            @Override 
            public void onDelete(int row) { 
                // View chỉ báo: "Ê, có người bấm Xóa ở dòng này nè"
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
        receiptFormView = new JPanel(new BorderLayout(0, 20)); receiptFormView.setOpaque(false); receiptFormView.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel title = new JLabel("Lập phiếu nhập kho"); title.setFont(new Font("Segoe UI", Font.BOLD, 24)); title.setForeground(TEXT_DARK);
        JButton btnBack = new JButton("← Quay lại"); btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnBack.setForeground(PRIMARY_COLOR);
        btnBack.setContentAreaFilled(false); btnBack.setBorderPainted(false); btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "InventoryList"));
        header.add(title, BorderLayout.WEST); header.add(btnBack, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 15)) { /* paintComponent... */ };
        content.setOpaque(false); content.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel formPanel = new JPanel(new GridLayout(3, 4, 15, 15)); formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết"));
        
        cbCategory = new JComboBox<>(); 
        cbCategory.setBackground(Color.WHITE);
        
        //Thêm dấu + vào ô Loại nguyên liệu
        btnAddCategory = new JButton("+");
        btnAddCategory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddCategory.setBackground(PRIMARY_COLOR);
        btnAddCategory.setForeground(Color.WHITE);
        btnAddCategory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddCategory.setPreferredSize(new Dimension(40, 30)); // Nút vuông nhỏ gọn
        
        btnAddCategory.setMargin(new Insets(0, 0, 0, 0));
        
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 0)); // Khoảng cách 5px
        categoryPanel.setOpaque(false);
        categoryPanel.add(cbCategory, BorderLayout.CENTER);
        categoryPanel.add(btnAddCategory, BorderLayout.EAST);
        
        txtIngredientName = new JTextField();
        String[] units = {"kg", "gram", "lít", "ml"}; 
        cbUnit = new JComboBox<>(units); cbUnit.setBackground(Color.WHITE);
        txtUnitCapacity = new JTextField(); 
        txtQuantity = new JTextField();
        txtTotalPrice = new JTextField();   
        txtThreshold = new JTextField("0"); 
        txtProvider = new JTextField();
        
        jdImportDate = new com.toedter.calendar.JDateChooser();
        jdImportDate.setDateFormatString("dd/MM/yyyy"); // Giao diện hiện kiểu Việt Nam
        jdImportDate.setDate(new java.util.Date()); // Mặc định là ngày hôm nay

        jdExpiryDate = new com.toedter.calendar.JDateChooser();
        jdExpiryDate.setDateFormatString("dd/MM/yyyy");
        
        formPanel.add(createInputWrapper("Loại nguyên liệu:", categoryPanel));
        formPanel.add(createInputWrapper("Tên nguyên liệu:", txtIngredientName));
        formPanel.add(createInputWrapper("Đơn vị tính:", cbUnit));
        formPanel.add(createInputWrapper("Định lượng:", txtUnitCapacity));
        formPanel.add(createInputWrapper("Số lượng (gói/hộp):", txtQuantity));
        formPanel.add(createInputWrapper("Thành tiền (VND):", txtTotalPrice));
        formPanel.add(createInputWrapper("Ngưỡng cảnh báo (gói/hộp):", txtThreshold));
        formPanel.add(createInputWrapper("Nhà cung cấp:", txtProvider));
        formPanel.add(createInputWrapper("Ngày nhập:", jdImportDate));
        formPanel.add(createInputWrapper("Hạn sử dụng:", jdExpiryDate));

        JButton btnAddToList = ComponentUI.createModernButton("+ Thêm vào bảng", new Color(0, 122, 255), Color.WHITE);
        btnAddToList.setPreferredSize(new Dimension(160, 35)); // Định hình lại chiều dài/rộng cho đẹp
        
        // Dùng FlowLayout.RIGHT để ép nút sát về lề phải
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); 
        btnPanel.setOpaque(false); 
        btnPanel.setBorder(new EmptyBorder(15, 0, 5, 0)); // Tạo khoảng cách (margin) đẩy nút xuống 1 dòng
        btnPanel.add(btnAddToList);

        // Gói cả cái form nhập liệu (formPanel) và cái nút (btnPanel) vào chung 1 khối (topSection)
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(formPanel, BorderLayout.CENTER);
        topSection.add(btnPanel, BorderLayout.SOUTH);

        String[] itemCols = {"Loại NL", "Tên NL", "ĐVT", "Tổng định lượng", "Số lượng", "Thành tiền", "Ngưỡng", "Nhà cung cấp", "Ngày nhập", "Hạn sử dụng","Hành động"};
        itemModel = new DefaultTableModel(null, itemCols) {
            @Override 
            public boolean isCellEditable(int row, int column) { 
                            return column == 10; 
            }
        };
        JTable itemTable = new JTable(itemModel);
        ComponentUI.styleTable(itemTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        JScrollPane itemScroll = new JScrollPane(itemTable); itemScroll.setPreferredSize(new Dimension(0, 300));

        TableColumn actionCol = itemTable.getColumnModel().getColumn(10);
        // ĐÃ SỬA CÚ PHÁP CŨ: Truyền false, true, true cho bảng nhập hàng (Chỉ bật Sửa, Xóa)
        actionCol.setCellRenderer(new ActionButtonRenderer(false, true, true));
        actionCol.setCellEditor(new ActionButtonEditor(new ActionButtonListener() {
            @Override
            public void onEdit(int row) {
                String catStr = itemModel.getValueAt(row, 0).toString();
                for(int i = 0; i < cbCategory.getItemCount(); i++) {
                    if(cbCategory.getItemAt(i).getTypeName().equals(catStr)) { 
                        cbCategory.setSelectedIndex(i); 
                        break; 
                    }
                }
                txtIngredientName.setText(itemModel.getValueAt(row, 1).toString());
                cbUnit.setSelectedItem(itemModel.getValueAt(row, 2).toString());
                
                // LOGIC CHIA NGƯỢC LẠI KHI BẤM SỬA
                int tongDinhLuong = Integer.parseInt(itemModel.getValueAt(row, 3).toString());
                int soLuongGoi = Integer.parseInt(itemModel.getValueAt(row, 4).toString());
                int dinhLuong1Goi = tongDinhLuong / soLuongGoi; 
                
                txtUnitCapacity.setText(String.valueOf(dinhLuong1Goi)); 
                txtQuantity.setText(String.valueOf(soLuongGoi));
                txtTotalPrice.setText(itemModel.getValueAt(row, 5).toString());
                txtThreshold.setText(itemModel.getValueAt(row, 6).toString());
                txtProvider.setText(itemModel.getValueAt(row, 7).toString());
                
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    
                    String importStr = itemModel.getValueAt(row, 8).toString();
                    if (!importStr.isEmpty()) {
                        jdImportDate.setDate(sdf.parse(importStr));
                    }
                    
                    String expiryStr = itemModel.getValueAt(row, 9).toString();
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
            public void onDetail(int row) {
                // Không dùng ở bảng này nhưng bắt buộc phải khai báo
            }
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
                String priceStr = txtTotalPrice.getText().trim();
                String thresStr = txtThreshold.getText().trim();
                String provider = txtProvider.getText().trim();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                
                
                String importingDate = "";
                if (jdImportDate.getDate() != null) {
                    importingDate = sdf.format(jdImportDate.getDate());
                }
                
                String expiryDate = "";
                if (jdExpiryDate.getDate() != null) {
                    expiryDate = sdf.format(jdExpiryDate.getDate());
                }

                if (name.isEmpty() || capStr.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty() || provider.isEmpty() || thresStr.isEmpty() || importingDate.isEmpty() || expiryDate.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!", "Lỗi", JOptionPane.WARNING_MESSAGE); return;
                }

                int capacity = Integer.parseInt(capStr); 
                int quantity = Integer.parseInt(qtyStr);
                long totalPrice = Long.parseLong(priceStr); 
                int threshold = Integer.parseInt(thresStr);

                if (capacity <= 0 || quantity <= 0 || totalPrice < 0 || threshold < 0) {
                    JOptionPane.showMessageDialog(this, "Số liệu phải hợp lệ (>0)!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
                }

                // LOGIC NHÂN KHI THÊM VÀO BẢNG
                int tongDinhLuong = capacity * quantity; // 25 * 20 = 500

                // Đẩy Tổng định lượng (500) vào bảng
                itemModel.addRow(new Object[]{category, name, unit, tongDinhLuong, quantity, totalPrice, threshold, provider, importingDate, expiryDate, "Sửa/Xóa"});
                
                clearFormInputsOnly();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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

// Đổi tham số thứ 2 thành JComponent
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
    
    
    // --- ĐÃ SỬA: Hàm đổ dữ liệu theo format yêu cầu ---
    public void displayIngredientData(List<IngredientModel> danhSach) {
        // Lấy model của bảng inventoryTable (bảng quản lý kho của bạn)
        javax.swing.table.DefaultTableModel tableModel = (javax.swing.table.DefaultTableModel) inventoryTable.getModel();
        
        // Xóa sạch các dòng cũ trước khi đổ dữ liệu mới
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
    
    // --- Hàm đổ dữ liệu vào bảng Lịch sử nhập hàng ---
    public void displayWarehouseReceiptData(List<WarehouseReceiptModel> list) {
        // Lấy model của bảng inventoryTable (bảng quản lý kho của bạn)
        javax.swing.table.DefaultTableModel tableModel = (javax.swing.table.DefaultTableModel) historyTable.getModel();
        
        // Xóa sạch các dòng cũ trước khi đổ dữ liệu mới
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

        // 1. Tạo Header
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

        // 2. Tạo Bảng Lịch Sử (Lưu ý: cột Hành động là cột số 4)
        String[] cols = {"Mã phiếu", "Ngày nhập", "Người nhập", "Tổng tiền", "Hành động"};
        historyModel = new DefaultTableModel(null, cols) {
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return c == 4; // BẮT BUỘC TRẢ VỀ TRUE Ở CỘT NÚT BẤM (CỘT 4) ĐỂ NHẬN CLICK
            }
        };
        historyTable = new JTable(historyModel);
        ComponentUI.styleTable(historyTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);

        // 3. GẮN RENDERER VÀ EDITOR ĐỂ BIẾN CHỮ THÀNH NÚT
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
        }, true, false, true)); // TẮT Sửa ở Editor
        
        actionCol.setPreferredWidth(170); // Cột này chỉ có 2 nút nên để 170 là vừa đẹp

        // 4. Đưa bảng vào ScrollPane
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


//    private void styleTable(JTable table) {
//        table.setRowHeight(50);
//        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
//        table.getTableHeader().setBackground(new Color(242, 242, 242));
//        table.getTableHeader().setForeground(TEXT_DARK);
//        table.setShowGrid(false);
//        table.setIntercellSpacing(new Dimension(0, 0));
//        table.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30));
//        table.setSelectionForeground(TEXT_DARK);
//    }

    class StatCard extends JPanel {
        private JLabel lblValue;
        public StatCard(String title, String value, String unit) {
            setLayout(new BorderLayout(0, 5));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblTitle.setForeground(TEXT_MUTED);

            lblValue = new JLabel(value); // Gán vào biến
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

    // --- Action Components (Dynamic) ---
    public interface ActionButtonListener {
        void onDetail(int row);
        void onEdit(int row);
        void onDelete(int row);
    }

    class ActionPanel extends JPanel {
        protected JButton btnDetail = new JButton("Xem chi tiết");
        protected JButton btnEdit = new JButton("Sửa");
        protected JButton btnDelete = new JButton("Xóa");

        // Nhận 3 biến boolean để quyết định vẽ nút nào
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

            // Chỉ cắm sự kiện cho những nút được hiển thị
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
    
    // có thể mang qua model
    private static class Receipt {
        String id;
        String date;
        String supplier;
        double totalAmount;
        List<ReceiptItem> items = new ArrayList<>();
    }

    // có thể mang qua model
    private static class ReceiptItem {
        String name;
        String unit;
        double quantity;
        double price;
        double threshold;
        String supplier;
        String date;
    }
    
    // Hàm này để Controller cắm dây vào lắng nghe nút bấm
    public void setInventoryActionListener(ActionButtonListener listener) {
        this.inventoryActionListener = listener;
    }

    // Hai hàm này để Controller lấy cái Bảng và Khay dữ liệu
    public JTable getInventoryTable() { return inventoryTable; }
    public DefaultTableModel getInventoryModel() { return inventoryModel; }
    
    // Hàm này giúp Controller hiện hộp thoại sửa
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
                    return null; // Bắt buộc nhập lý do
                }

                // Trả về mảng 5 phần tử, có thêm Lý Do
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
    
    // Hàm này giúp Controller lấy danh sách các nguyên liệu đang có trên form nhập hàng
    public DefaultTableModel getReceiptItemModel() {
        return itemModel;
    }
    
    // (Tùy chọn) Hàm này giúp dọn dẹp form sau khi nhập thành công
//    public void clearReceiptForm() {
//        itemModel.setRowCount(0); // Xóa sạch các dòng trong bảng
//        if (lblTotal != null) {
//            lblTotal.setText("Tổng cộng: 0 VND");
//        }
//        
//        // --- THÊM ĐOẠN NÀY: Xóa sạch các ô nhập liệu ---
//        if (txtTenNL != null) txtTenNL.setText("");
//        if (cbDVT != null) cbDVT.setSelectedIndex(0); // Reset về "Kg"
//        if (txtSoLuong != null) txtSoLuong.setText("");
//        if (txtDonGia != null) txtDonGia.setText("");
//        if (txtNguong != null) txtNguong.setText("1"); // Hoặc "0" tùy bạn
//        if (txtNhaCungCap != null) txtNhaCungCap.setText("");
//        if (txtNgay != null) txtNgay.setText(java.time.LocalDate.now().toString()); // Set lại ngày hôm nay
//    }
    // --- HÀM XÓA TRẮNG FORM ---
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
        if (txtTotalPrice != null) txtTotalPrice.setText("");
        if (txtThreshold != null) txtThreshold.setText("0"); 
        if (txtProvider != null) txtProvider.setText("");
        if (jdImportDate != null) jdImportDate.setDate(new java.util.Date()); 
        if (jdExpiryDate != null) jdExpiryDate.setDate(null); // Xóa trắng
    }
    
        public void addHistoryButtonListener(ActionListener listener) {
            btnHistory.addActionListener(listener);
    }
        
    public void setHistoryActionListener(ActionButtonListener listener) {
        this.historyActionListener = listener;
    }

    // Cho phép Controller lấy dữ liệu từ bảng Lịch sử
    public JTable getHistoryTable() { 
        return historyTable; 
    }
    
    // Cho phép Controller xóa dòng trên giao diện
    public DefaultTableModel getHistoryModel() { 
        return historyModel; 
    }
    
    // Hàm này để Controller gọi sau khi đã tính toán xong tổng tiền
    public void setTotalAmountLabel(long tongCong) {
        if (lblTotal != null) {
            lblTotal.setText("Tổng cộng: " + String.format("%,d", tongCong) + " VND");
        }
    }
    
    // Cho Controller lắng nghe nút [+]
    public void addAddCategoryListener(ActionListener listener) {
        btnAddCategory.addActionListener(listener);
    }
    
    // Lấy ComboBox ra để Controller chọn cái vừa mới thêm
    public JComboBox<IngredientTypeModel> getCategoryComboBox() {
        return cbCategory;
    }
    
    // Hàm này mở cửa cho Controller truyền 3 con số vào
    public void updateDashboardStats(int totalTypes, int warningCount) {
        if (cardTotalIngredients != null) 
            cardTotalIngredients.setValue(String.valueOf(totalTypes));
        if (cardWarning != null) 
            cardWarning.setValue(String.valueOf(warningCount));
//        if (cardTotalValue != null) 
//            cardTotalValue.setValue(String.format("%,d", totalValue) + " VND");
    }
}