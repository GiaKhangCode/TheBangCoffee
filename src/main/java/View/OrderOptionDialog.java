package View;

import Model.ProductModel;
import Model.ToppingModel;
import Model.VariantModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OrderOptionDialog extends JDialog {

    private final Color PRIMARY_COLOR = new Color(67, 142, 104);
    private final Color TEXT_DARK = new Color(33, 37, 41);

    private ProductModel product;
    private List<VariantModel> availableVariants;
    private List<ToppingModel> availableToppings;

    // Trạng thái đơn hàng
    private boolean isTakeaway;
    private boolean isHoliday;

    // Kết quả thu được sau khi chọn
    private boolean isConfirmed = false;
    private VariantModel selectedVariant = null;
    private List<ToppingModel> selectedToppings = new ArrayList<>();
    private int quantity = 1;
    
    // [MỚI] Biến chứa chuỗi Ghi chú tổng hợp
    private String finalNote = "";

    // UI Components
    private ButtonGroup sizeGroup;
    private List<JRadioButton> sizeRadios = new ArrayList<>();
    private List<JCheckBox> toppingCheckboxes = new ArrayList<>();
    private JLabel lblQuantity;
    
    // [MỚI] UI Components cho Đá, Đường, Ghi chú
    private ButtonGroup sugarGroup;
    private ButtonGroup iceGroup;
    private JTextField txtCustomNote;

    public OrderOptionDialog(Frame owner, ProductModel product, List<VariantModel> variants, List<ToppingModel> toppings, boolean isTakeaway, boolean isHoliday) {
        super(owner, "Tùy chọn: " + product.getProductName(), true);
        this.product = product;
        this.availableVariants = variants != null ? variants : new ArrayList<>();
        this.availableToppings = toppings != null ? toppings : new ArrayList<>();
        this.isTakeaway = isTakeaway;
        this.isHoliday = isHoliday;
        
        initComponents();
    }

    private void initComponents() {
        setSize(700, 700); // Tăng chiều cao để chứa thêm form
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- BODY ---
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. Tên món
        JLabel lblProductName = new JLabel(product.getProductName());
        lblProductName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblProductName.setForeground(PRIMARY_COLOR);
        lblProductName.setAlignmentX(Component.CENTER_ALIGNMENT);
        bodyPanel.add(lblProductName);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2. Tùy chọn Size
        if (!availableVariants.isEmpty()) {
            JPanel sizePanel = createSectionPanel("Chọn Size");
            sizePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
            sizeGroup = new ButtonGroup();
            
            for (int i = 0; i < availableVariants.size(); i++) {
                VariantModel v = availableVariants.get(i);
                
                long displayPrice = v.getDineInPrice();
                if (isHoliday) displayPrice = v.getHolidayPrice();
                else if (isTakeaway) displayPrice = v.getTakeawayPrice();
                
                String labelText = String.format("%s (+%,d đ)", v.getSizeName(), displayPrice);
                JRadioButton rb = new JRadioButton(labelText);
                rb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                rb.setBackground(Color.WHITE);
                rb.setFocusPainted(false);
                
                if (i == 0) {
                    rb.setSelected(true);
                    selectedVariant = v;
                }
                
                rb.putClientProperty("variant", v);
                rb.addActionListener(e -> selectedVariant = (VariantModel) rb.getClientProperty("variant"));
                
                sizeGroup.add(rb);
                sizeRadios.add(rb);
                sizePanel.add(rb);
            }
            bodyPanel.add(sizePanel);
            bodyPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        // 3. Tùy chọn Topping
        if (!availableToppings.isEmpty()) {
            JPanel toppingPanel = createSectionPanel("Chọn Topping");
            toppingPanel.setLayout(new GridLayout(0, 2, 10, 10)); 
            
            for (ToppingModel t : availableToppings) {
                String labelText = String.format("%s (+%,d đ)", t.getToppingName(), t.getPrice());
                JCheckBox cb = new JCheckBox(labelText);
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                cb.setBackground(Color.WHITE);
                cb.setFocusPainted(false);
                
                cb.putClientProperty("topping", t);
                toppingCheckboxes.add(cb);
                toppingPanel.add(cb);
            }
            bodyPanel.add(toppingPanel);
            bodyPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }
        
        // [MỚI] 4. Tùy chọn Mức Đường
        JPanel sugarPanel = createSectionPanel("Mức Đường");
        sugarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        sugarGroup = new ButtonGroup();
        String[] sugarLevels = {"100% (Bình thường)", "70% (Vừa)", "50% (Ít)", "30% (Rất ít)", "0% (Không đường)"};
        for (int i = 0; i < sugarLevels.length; i++) {
            JRadioButton rb = new JRadioButton(sugarLevels[i]);
            rb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            rb.setBackground(Color.WHITE);
            rb.setActionCommand(sugarLevels[i]); // Dùng để lấy giá trị sau này
            if (i == 0) rb.setSelected(true); // Mặc định 100%
            sugarGroup.add(rb);
            sugarPanel.add(rb);
        }
        bodyPanel.add(sugarPanel);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // [MỚI] 5. Tùy chọn Mức Đá
        JPanel icePanel = createSectionPanel("Mức Đá");
        icePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        iceGroup = new ButtonGroup();
        String[] iceLevels = {"100% (Bình thường)", "70% (Vừa)", "50% (Ít)", "0% (Không đá)", "Nóng"};
        for (int i = 0; i < iceLevels.length; i++) {
            JRadioButton rb = new JRadioButton(iceLevels[i]);
            rb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            rb.setBackground(Color.WHITE);
            rb.setActionCommand(iceLevels[i]);
            if (i == 0) rb.setSelected(true);
            iceGroup.add(rb);
            icePanel.add(rb);
        }
        bodyPanel.add(icePanel);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // [MỚI] 6. Ghi chú thêm
        JPanel notePanel = createSectionPanel("Ghi chú đặc biệt");
        notePanel.setLayout(new BorderLayout());
        txtCustomNote = new JTextField();
        txtCustomNote.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCustomNote.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220)), 
            new EmptyBorder(8, 10, 8, 10)
        ));
        notePanel.add(txtCustomNote, BorderLayout.CENTER);
        bodyPanel.add(notePanel);

        add(new JScrollPane(bodyPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        // --- FOOTER (Số lượng & Action) ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(248, 249, 250));
        footerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qtyPanel.setOpaque(false);
        qtyPanel.add(new JLabel("Số lượng: "));
        
        JButton btnMinus = createModernButton("-", Color.WHITE, TEXT_DARK);
        btnMinus.setPreferredSize(new Dimension(40, 35));
        
        lblQuantity = new JLabel("1", SwingConstants.CENTER);
        lblQuantity.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblQuantity.setPreferredSize(new Dimension(30, 35));
        
        JButton btnPlus = createModernButton("+", Color.WHITE, TEXT_DARK);
        btnPlus.setPreferredSize(new Dimension(40, 35));

        btnMinus.addActionListener(e -> updateQuantity(-1));
        btnPlus.addActionListener(e -> updateQuantity(1));

        qtyPanel.add(btnMinus);
        qtyPanel.add(lblQuantity);
        qtyPanel.add(btnPlus);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        
        JButton btnCancel = createModernButton("Hủy", new Color(220, 220, 220), TEXT_DARK);
        JButton btnConfirm = createModernButton("Thêm vào giỏ", PRIMARY_COLOR, Color.WHITE);
        
        btnCancel.addActionListener(e -> dispose());
        btnConfirm.addActionListener(e -> {
            gatherData();
            isConfirmed = true;
            dispose();
        });

        actionPanel.add(btnCancel);
        actionPanel.add(btnConfirm);

        footerPanel.add(qtyPanel, BorderLayout.WEST);
        footerPanel.add(actionPanel, BorderLayout.EAST);
        
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void updateQuantity(int delta) {
        if (quantity + delta >= 1) {
            quantity += delta;
            lblQuantity.setText(String.valueOf(quantity));
        }
    }

    // [MỚI] Hàm gom dữ liệu (Topping và Ghi chú Đá/Đường)
    private void gatherData() {
        // 1. Lấy Topping
        selectedToppings.clear();
        for (JCheckBox cb : toppingCheckboxes) {
            if (cb.isSelected()) {
                selectedToppings.add((ToppingModel) cb.getClientProperty("topping"));
            }
        }
        
        // 2. Lắp ráp chuỗi Ghi chú
        StringBuilder noteBuilder = new StringBuilder();
        
        String sugar = sugarGroup.getSelection().getActionCommand();
        String ice = iceGroup.getSelection().getActionCommand();
        
        // Nếu khác "Bình thường" thì mới ghi vào để đỡ rối mắt
        if (!sugar.contains("100%")) {
            noteBuilder.append("Đường: ").append(sugar.split(" ")[0]).append(" ");
        }
        if (!ice.contains("100%")) {
            if (noteBuilder.length() > 0) noteBuilder.append(" | ");
            noteBuilder.append("Đá: ").append(ice.split(" ")[0]).append(" ");
        }
        
        String custom = txtCustomNote.getText().trim();
        if (!custom.isEmpty()) {
            if (noteBuilder.length() > 0) noteBuilder.append(" | ");
            noteBuilder.append("Ghi chú: ").append(custom);
        }
        
        finalNote = noteBuilder.toString().trim();
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(); 
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(new Color(230, 230, 230)), title, 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), PRIMARY_COLOR
        ));
        return p;
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
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
        if (text.equals("+") || text.equals("-")) {
            btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        } else {
            btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        }
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Getters for Controller
    public boolean isConfirmed() { return isConfirmed; }
    public VariantModel getSelectedVariant() { return selectedVariant; }
    public List<ToppingModel> getSelectedToppings() { return selectedToppings; }
    public int getQuantity() { return quantity; }
    public String getFinalNote() { return finalNote; } // Lấy chuỗi ghi chú ra
}