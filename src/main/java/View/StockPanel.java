package View;

import Common.ComponentUI;
import Model.IngredientModel;
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
    private HistoryActionButtonListener historyActionListener;
    

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
        statsPanel.add(new StatCard("Tổng nguyên liệu", "...", "Loại"));
        statsPanel.add(new StatCard("Cần nhập hàng", "...", "Cảnh báo"));
        statsPanel.add(new StatCard("Giá trị kho", "... VND", "Ước tính"));

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
        String[] columns = {"ID", "Tên nguyên liệu", "ĐVT", "Tồn kho", "Ngưỡng", "Trạng thái", "Hành động"};
        inventoryModel = new DefaultTableModel(null, columns) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        inventoryTable = new JTable(inventoryModel);
        ComponentUI.styleTable(inventoryTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        
        // Thêm Buttons vào cột Hành động
        TableColumn actionCol = inventoryTable.getColumnModel().getColumn(6);
        actionCol.setCellRenderer(new ActionButtonRenderer(new ActionPanel()));
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
        }, new ActionPanel()));
        actionCol.setPreferredWidth(160);
        
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

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel title = new JLabel("Lập phiếu nhập kho");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);
        
        JButton btnBack = new JButton("← Quay lại");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(PRIMARY_COLOR);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "InventoryList"));

        header.add(title, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(30, 30, 30, 30));

        // 1. TẠO BẢNG
        String[] itemCols = {"Tên nguyên liệu", "ĐVT", "Số lượng", "Đơn giá", "Ngưỡng báo", "Nhà cung cấp", "Ngày", ""};
        itemModel = new DefaultTableModel(null, itemCols);
        JTable itemTable = new JTable(itemModel);
        ComponentUI.styleTable(itemTable, TEXT_DARK, TEXT_DARK, PRIMARY_COLOR);
        JScrollPane itemScroll = new JScrollPane(itemTable);
        itemScroll.setPreferredSize(new Dimension(0, 450));


        JPanel bottomActions = new JPanel(new BorderLayout());
        bottomActions.setOpaque(false);

        // 3. TẠO NÚT VÀ NHÃN HIỂN THỊ TIỀN (Dùng biến toàn cục)
        lblTotal = new JLabel("Tổng cộng: 0 VND");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(PRIMARY_COLOR);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        
        JButton btnAddRow = ComponentUI.createModernButton("+ Thêm dòng", new Color(240, 240, 240), TEXT_DARK);
        btnSubmit = ComponentUI.createModernButton("Hoàn tất nhập hàng", PRIMARY_COLOR, Color.WHITE);

        // 4. SỰ KIỆN KHI BẤM NÚT "+ THÊM DÒNG"
        btnAddRow.addActionListener(e -> {
            // Tạo 1 mảng đại diện cho 1 dòng trống (với số lượng và đơn giá mặc định là 0)
            Object[] emptyRow = {"", "", "0", "0", "0", "", java.time.LocalDate.now().toString(), "X"};
            itemModel.addRow(emptyRow);
        });

        btns.add(btnAddRow);
        btns.add(btnSubmit);

        bottomActions.add(lblTotal, BorderLayout.WEST);
        bottomActions.add(btns, BorderLayout.EAST);

        content.add(itemScroll, BorderLayout.CENTER);
        content.add(bottomActions, BorderLayout.SOUTH);

        receiptFormView.add(header, BorderLayout.NORTH);
        receiptFormView.add(content, BorderLayout.CENTER);
    }
    
    
    // --- ĐÃ SỬA: Hàm đổ dữ liệu theo format yêu cầu ---
    public void displayIngredientData(List<IngredientModel> danhSach) {
        // Lấy model của bảng inventoryTable (bảng quản lý kho của bạn)
        javax.swing.table.DefaultTableModel tableModel = (javax.swing.table.DefaultTableModel) inventoryTable.getModel();
        
        // Xóa sạch các dòng cũ trước khi đổ dữ liệu mới
        tableModel.setRowCount(0); 
        
        for (IngredientModel nl : danhSach) {
            Object[] rowData = {
                nl.getID(),           // Lấy mã nguyên liệu
                nl.getTenNguyenLieu(), // Lấy tên nguyên liệu
                nl.getDonViTinh(),    // Lấy đơn vị tính (KG, Lít...)
                nl.getTonKho(),       // Lấy số lượng tồn hiện tại
                nl.getNguong(),       // Lấy ngưỡng báo động
                nl.getTrangThai(),    // Gọi logic tự tính "Còn hàng/Hết hàng"
                "Sửa / Xóa"           // Cột hành động (Không có cũng không sao)
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
        actionCol.setCellRenderer(new ActionButtonRenderer(new HistoryActionPanel()));
        actionCol.setCellEditor(new HistoryActionButtonEditor(new HistoryActionButtonListener() {
            @Override
            public void onDetail(int row) {
                if (historyActionListener != null) {
                    historyActionListener.onDetail(row);
                }
            }
            
            @Override
            public void onEdit(int row) {
                if (historyActionListener != null) {
                    historyActionListener.onEdit(row);
                }
            }

            @Override
            public void onDelete(int row) {
                if (historyActionListener != null) {
                    historyActionListener.onDelete(row);
                }
            }
        }));
        
        actionCol.setPreferredWidth(160);

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
        public StatCard(String title, String value, String unit) {
            setLayout(new BorderLayout(0, 5));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblTitle.setForeground(TEXT_MUTED);

            JLabel lblValue = new JLabel(value);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblValue.setForeground(TEXT_DARK);

            JLabel lblUnit = new JLabel(unit);
            lblUnit.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblUnit.setForeground(TEXT_MUTED);

            add(lblTitle, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
            add(lblUnit, BorderLayout.SOUTH);
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

    // --- Action Components ---
    public interface ActionButtonListener {
        void onEdit(int row);
        void onDelete(int row);
    }

    class ActionPanel extends JPanel {
        protected JButton btnEdit = new JButton("Sửa");
        protected JButton btnDelete = new JButton("Xóa");

        public ActionPanel() {
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

    class ActionButtonRenderer implements TableCellRenderer {
        protected JPanel panel;

        public ActionButtonRenderer(JPanel panel) {
            this.panel = panel;
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

        // Truyền Panel và Listener từ ngoài vào
        public ActionButtonEditor(ActionButtonListener listener, ActionPanel panel) {
            super(new JCheckBox());
            this.listener = listener;
            this.panel = panel;

            // Cắm sự kiện chung (Sửa, Xóa)
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
    
    // --- Action Components ---
    public interface HistoryActionButtonListener extends ActionButtonListener {
        void onDetail(int row);
    }

    class HistoryActionPanel extends ActionPanel {
    protected JButton btnDetail = new JButton("Xem chi tiết");

    public HistoryActionPanel() {
        super(); 
        
        styleButton(btnDetail, new Color(0, 0, 0), 90, 30);
       
        add(btnDetail, 0); 
    }
}
    class HistoryActionButtonRenderer implements TableCellRenderer {
        private HistoryActionPanel panel = new HistoryActionPanel();
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return panel;
        }
    }

    class HistoryActionButtonEditor extends ActionButtonEditor {
        
        public HistoryActionButtonEditor(HistoryActionButtonListener listener) {
            // Truyền Listener và khởi tạo HistoryActionPanel đưa lên cho lớp cha
            super(listener, new HistoryActionPanel()); 

            // Ép kiểu panel về lại HistoryActionPanel để lấy được nút Chi tiết
            HistoryActionPanel hPanel = (HistoryActionPanel) this.panel;

            // Cắm sự kiện riêng cho nút Chi tiết
            hPanel.btnDetail.addActionListener(e -> { 
                stopCellEditing(); 
                listener.onDetail(currentRow); 
            });
        }
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
    public void clearReceiptForm() {
        itemModel.setRowCount(0); // Xóa sạch các dòng
        if (lblTotal != null) {
            lblTotal.setText("Tổng cộng: 0 VND");
        }
    }
    
        public void addHistoryButtonListener(ActionListener listener) {
            btnHistory.addActionListener(listener);
    }
        
    public void setHistoryActionListener(HistoryActionButtonListener listener) {
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
}


