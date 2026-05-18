package View;

import Model.SessionManager;
import Common.ValidationUtil;
import Controller.AccountController;
import Controller.CustomerController;
import Controller.PosController; // Import PosController để sử dụng
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private SidebarPanel sidebar;
    private JPanel mainContainer; 
    private JPanel contentArea;
    private CardLayout cardLayout;
    
    // Các Panel cũ
    private StockPanel stockPanel;
    private MenuPanel menuPanel;
    private RolePanel rolePanel;
    private EmployeeSchedulePanel shiftPanel;
    private PosPanel posPanel; 
    private DashboardPanel dashboardPanel;
    private OrderPanel orderPanel; 
    
    // [MỚI] Khai báo 2 Panel Khách hàng và Tích điểm
    private CustomerManagementPanel customerPanel;
    private LoyaltyManagementPanel loyaltyPanel;
    
    private Map<String, NavButton> navButtons;
    private NavButton activeButton;
    
    // Khai báo PosController để các Controller khác có thể gọi hàm reload
    private PosController posController;
    
    public MainFrame() throws SQLException {
        initComponents();
    }

    private void initComponents() throws SQLException {
        setTitle("Quản Lý Hệ Thống - The Bang Coffee");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        
        setSize(screenWidth, screenHeight);
        setLocationRelativeTo(null);

        getContentPane().setBackground(AppColor.BG_LIGHT);
        setLayout(new BorderLayout(0, 0));

        sidebar = new SidebarPanel();
        int sidebarWidth = (int) (screenWidth * 0.18); 
        sidebar.setPreferredSize(new Dimension(sidebarWidth, screenHeight)); 
        
        JLabel logoTitle = new JLabel("The Bang Coffee");
        int logoFontSize = Math.max(18, (int) (screenWidth * 0.014)); 
        logoTitle.setFont(new Font("Segoe UI", Font.BOLD, logoFontSize));
        logoTitle.setForeground(AppColor.TEXT_DARK);
        
        int logoPaddingTop = (int) (screenHeight * 0.046); 
        int logoPaddingLeft = (int) (screenWidth * 0.016); 
        int logoPaddingBottom = (int) (screenHeight * 0.035); 
        logoTitle.setBorder(new EmptyBorder(logoPaddingTop, logoPaddingLeft, logoPaddingBottom, 0)); 
        logoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sidebar.add(logoTitle);

        URL cartIcon = getClass().getResource("/images/cart_icon.png");
        URL invoiceIcon = getClass().getResource("/images/invoice_icon.png");
        URL glassIcon = getClass().getResource("/images/glass_icon.png");
        URL warehouseIcon = getClass().getResource("/images/warehouse_icon.png");
        URL staffIcon = getClass().getResource("/images/staff_icon.png");
        URL settingIcon = getClass().getResource("/images/setting_icon.png");
        URL statisticIcon = getClass().getResource("/images/statistic_icon.png");
        URL customerIcon = getClass().getResource("/images/customer_icon.png");
        URL promotionIcon = getClass().getResource("/images/promotion_icon.png");
        URL finishIcon = getClass().getResource("/images/finish_icon.png");
        URL logoutIcon = getClass().getResource("/images/logout_icon.png");
        
        navButtons = new HashMap<>();
        
        // --- DANH SÁCH MENU SIDEBAR ---
        addMenuButton("<html><img src='" + cartIcon + "' width='25' height='25'>    Tạo đơn</html>", "POS", "Order");
        addMenuButton("<html><img src='" + invoiceIcon + "' width='25' height='25'>    Quản lý đơn hàng</html>", "BILL", "OrderList");
        addMenuButton("<html><img src='" + glassIcon + "' width='25' height='25'>    Menu đồ uống</html>", "MENU", "Menu");
        
        // [MỚI] Thêm 2 nút vào Sidebar (Không gộp chung)
        
        addMenuButton("<html><img src='" + customerIcon + "' width='25' height='25'>    Khách hàng</html>", "USER", "Customer");
        addMenuButton("<html><img src='" + promotionIcon + "' width='25' height='25'>    Cấu hình Tích điểm</html>", "GIFT", "Loyalty");
        
        
        addMenuButton("<html><img src='" + warehouseIcon + "' width='25' height='25'>    Nhập kho</html>", "STOCK", "Stock");
        addMenuButton("<html><img src='" + staffIcon + "' width='25' height='25'>    Quản lý ca làm việc</html>", "STAFF", "Staff");
        addMenuButton("<html><img src='" + settingIcon + "' width='25' height='25'>    Tài khoản và phân quyền</html>", "ROLE", "Role");
        addMenuButton("<html><img src='" + statisticIcon + "' width='25' height='25'>    Báo cáo & Thống kê</html>", "CHART", "Stats");
        
        sidebar.add(Box.createVerticalGlue());
        
        // Khởi tạo nút Mở/Đóng ca. Mặc định là Mở ca. Controller sẽ đổi tên sau.
        addMenuButton("Mở ca", "LOCK", "ShiftToggle"); 
        addMenuButton("Đăng xuất", "EXIT", "Logout");
        
        int bottomGap = (int) (screenHeight * 0.029); 
        sidebar.add(Box.createRigidArea(new Dimension(0, bottomGap)));

        mainContainer = new JPanel(new BorderLayout());
        mainContainer.setOpaque(false);
        
        int mainPaddingTop = (int) (screenHeight * 0.023); 
        int mainPaddingSide = (int) (screenWidth * 0.020); 
        int mainPaddingBottom = (int) (screenHeight * 0.035); 
        mainContainer.setBorder(new EmptyBorder(mainPaddingTop, mainPaddingSide, mainPaddingBottom, mainPaddingSide));

        cardLayout = new CardLayout(0, 0);
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        // --- KHỞI TẠO VÀ THÊM CÁC PANEL VÀO CARDLAYOUT ---
        this.dashboardPanel = new DashboardPanel();
        contentArea.add(this.dashboardPanel, "Stats");
        
        this.posPanel = new PosPanel();
        contentArea.add(this.posPanel, "Order");
        
        this.orderPanel = new OrderPanel();
        contentArea.add(this.orderPanel, "OrderList");
        
        this.menuPanel = new MenuPanel();
        this.menuPanel.setupPanels(this); 
        contentArea.add(this.menuPanel, "Menu"); 
        
        // [MỚI] Khởi tạo 2 Panel mới và thêm vào ContentArea
        this.customerPanel = new CustomerManagementPanel();
        contentArea.add(this.customerPanel, "Customer");
        
        this.loyaltyPanel = new LoyaltyManagementPanel();
        contentArea.add(this.loyaltyPanel, "Loyalty");
        
        new CustomerController(this);
        
        this.stockPanel = new StockPanel(); 
        contentArea.add(this.stockPanel, "Stock"); 
        
        this.rolePanel = new RolePanel();
        contentArea.add(this.rolePanel, "Role"); 
        
        this.shiftPanel = new EmployeeSchedulePanel();
        contentArea.add(this.shiftPanel, "Staff");
        
        contentArea.add(new ContentBasePanel("Cài đặt Hệ thống", "Tùy chỉnh các tham số vận hành."), "Settings");

        mainContainer.add(contentArea, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainContainer, BorderLayout.CENTER);
        
        setPageActive("Stats");
    }

    private void addMenuButton(String text, String icon, String cardName) {
        NavButton btn = new NavButton(text, icon, cardName);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT); 
        navButtons.put(cardName, btn);
        sidebar.add(btn);
        
        int gapSmall = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.006); 
        sidebar.add(Box.createRigidArea(new Dimension(0, gapSmall)));
        
        if (!cardName.equals("Logout") && !cardName.equals("ShiftToggle")) {
            btn.addActionListener(e -> {
                try {
                    setPageActive(cardName);
                } catch (SQLException ex) {
                    System.getLogger(MainFrame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            });
        }
    }

    public void setPageActive(String cardName) throws SQLException {
        if (!cardName.equals("Logout") && SessionManager.isLoggedIn() && !ValidationUtil.validateSession()) {
            SessionManager.clear();
            this.setVisible(false);
            new AccountController();
            return;
        }
        
        // KHÓA TAB POS KHI CHƯA MỞ CA
        if (cardName.equals("Order") && !SessionManager.hasOpenShift()) {
            JOptionPane.showMessageDialog(this, "Bạn chưa mở ca! Vui lòng Mở ca làm việc trước khi vào chức năng Bán hàng.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (activeButton != null) {
            activeButton.setActive(false);
        }
        
        activeButton = navButtons.get(cardName);
        if (activeButton != null) {
            activeButton.setActive(true);
            cardLayout.show(contentArea, cardName);
        }
    }

    public void addLogoutListener(ActionListener listener) {
        if (navButtons != null && navButtons.containsKey("Logout")) {
            navButtons.get("Logout").addActionListener(listener);
        }
    }
    
    // Hàm cập nhật chữ của nút (Mở ca / Đóng ca)
    public void setShiftButtonState(boolean hasOpenShift) {
        if (navButtons != null && navButtons.containsKey("ShiftToggle")) {
            URL finishIcon = getClass().getResource("/images/finish_icon.png");
            navButtons.get("ShiftToggle").setText(hasOpenShift ? "<html><img src='" + finishIcon + "' width='25' height='25'>    Đóng ca</html>" : "<html><img src='" + finishIcon + "' width='25' height='25'>    Mở ca</html>");
        }
    }

    // Lắng nghe sự kiện của nút Mở/Đóng ca
    public void addShiftToggleListener(ActionListener listener) {
        if (navButtons != null && navButtons.containsKey("ShiftToggle")) {
            navButtons.get("ShiftToggle").addActionListener(listener);
        }
    }

    public static void main(String[] args) {}
    
    // --- Các Getter Panel ---
    public StockPanel getStockPanel(){ return stockPanel; }
    public MenuPanel getMenuPanel(){ return menuPanel; }
    public RolePanel getRolePanel(){ return rolePanel; }
    public PosPanel getPosPanel(){ return posPanel; }
    public EmployeeSchedulePanel getShiftPanel(){ return shiftPanel; }
    public OrderPanel getOrderPanel() { return orderPanel; }
    public DashboardPanel getDashboardPanel(){ return dashboardPanel; }
    
    // [MỚI] Getter cho 2 Panel mới để Controller gọi tới
    public CustomerManagementPanel getCustomerPanel() { return customerPanel; }
    public LoyaltyManagementPanel getLoyaltyPanel() { return loyaltyPanel; }

    // Getter & Setter cho PosController để reload data từ xa
    public PosController getPosController() { return posController; }
    public void setPosController(PosController posController) { this.posController = posController; }

    public void setMenuVisible(String menuKey, boolean isVisible) {
        if (navButtons != null && navButtons.containsKey(menuKey)) {
            NavButton btn = navButtons.get(menuKey);
            btn.setVisible(isVisible);
            sidebar.revalidate();
            sidebar.repaint();
        }
    }
    
    // --- PHÂN QUYỀN ĐỘNG (DÙNG CHUNG) ---
    private java.util.List<Runnable> permissionReloaders = new java.util.ArrayList<>();
    
    public void registerPermissionReloader(Runnable reloader) {
        permissionReloaders.add(reloader);
    }
    
    public void reloadPermissions() {
        for (Runnable reloader : permissionReloaders) {
            try {
                reloader.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}