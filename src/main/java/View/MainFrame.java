package View;

import Model.SessionManager;
import Common.ValidationUtil;
import Controller.AccountController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private SidebarPanel sidebar;
    private JPanel mainContainer; 
    private JPanel contentArea;
    private CardLayout cardLayout;
    private StockPanel stockPanel;
    private MenuPanel menuPanel;
    private Map<String, NavButton> navButtons;
    private NavButton activeButton;
    private RolePanel rolePanel;
    private EmployeeSchedulePanel shiftPanel;
    private PosPanel posPanel; 
    private DashboardPanel dashboardPanel;
    private OrderPanel orderPanel; 
    
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

        navButtons = new HashMap<>();
        addMenuButton("Tạo đơn", "POS", "Order");
        addMenuButton("Quản lý đơn hàng", "BILL", "OrderList");
        addMenuButton("Menu đồ uống", "MENU", "Menu");
        addMenuButton("Nhập kho", "STOCK", "Stock");
        addMenuButton("Quản lý ca làm việc", "STAFF", "Staff");
        addMenuButton("Tài khoản & Phân quyền", "ROLE", "Role");
        addMenuButton("Báo cáo & Thống kê", "CHART", "Stats");
        addMenuButton("Cài đặt", "SETTINGS", "Settings");
        
        sidebar.add(Box.createVerticalGlue());
        
        // [MỚI] Khởi tạo nút Mở/Đóng ca. Mặc định là Mở ca. Controller sẽ đổi tên sau.
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

        this.dashboardPanel = new DashboardPanel();
        contentArea.add(this.dashboardPanel, "Stats");
        
        this.posPanel = new PosPanel();
        contentArea.add(this.posPanel, "Order");
        
        this.orderPanel = new OrderPanel();
        contentArea.add(this.orderPanel, "OrderList");
        
        this.menuPanel = new MenuPanel();
        contentArea.add(this.menuPanel, "Menu"); 
        
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
        
        // [QUAN TRỌNG] KHÓA TAB POS KHI CHƯA MỞ CA
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
    
    // [MỚI] Hàm cập nhật chữ của nút (Mở ca / Đóng ca)
    public void setShiftButtonState(boolean hasOpenShift) {
        if (navButtons != null && navButtons.containsKey("ShiftToggle")) {
            navButtons.get("ShiftToggle").setText(hasOpenShift ? "Đóng ca" : "Mở ca");
        }
    }

    // [MỚI] Lắng nghe sự kiện của nút Mở/Đóng ca
    public void addShiftToggleListener(ActionListener listener) {
        if (navButtons != null && navButtons.containsKey("ShiftToggle")) {
            navButtons.get("ShiftToggle").addActionListener(listener);
        }
    }

    public static void main(String[] args) {}
    
    public StockPanel getStockPanel(){ return stockPanel; }
    public MenuPanel getMenuPanel(){ return menuPanel; }
    public RolePanel getRolePanel(){ return rolePanel; }
    public PosPanel getPosPanel(){ return posPanel; }
    public EmployeeSchedulePanel getShiftPanel(){ return shiftPanel; }
    public OrderPanel getOrderPanel() { return orderPanel; }
    
    public void setRoleMenuVisible(boolean isVisible) {
        if (navButtons != null && navButtons.containsKey("Role")) {
            NavButton roleBtn = navButtons.get("Role");
            roleBtn.setVisible(isVisible);
            sidebar.revalidate();
            sidebar.repaint();
        }
    }
        
    public DashboardPanel getDashboardPanel(){ return dashboardPanel; }
}