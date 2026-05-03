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

/**
 * Giao diện chính cao cấp (Modern Management Dashboard) - The Bang Coffee.
 * Thiết kế theo phong cách Apple/Material, đồng bộ với LoginFrame.
 * 
 * @author Antigravity
 */
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
    
    // [MỚI] Thêm OrderPanel để quản lý hóa đơn
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

        // Nền chính
        getContentPane().setBackground(AppColor.BG_LIGHT);
        setLayout(new BorderLayout(0, 0));

        // 1. Sidebar Panel (Light Minimalist)
        sidebar = new SidebarPanel();
        int sidebarWidth = (int) (screenWidth * 0.18); 
        sidebar.setPreferredSize(new Dimension(sidebarWidth, screenHeight)); 
        
        // Logo Section
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

        // Menu Nút điều hướng
        navButtons = new HashMap<>();
        addMenuButton("Tạo đơn", "POS", "Order");
        addMenuButton("Quản lý đơn hàng", "BILL", "OrderList");
        addMenuButton("Menu đồ uống", "MENU", "Menu");
        addMenuButton("Quản lý kho", "STOCK", "Stock");
        addMenuButton("Quản lý nhân viên", "STAFF", "Staff");
        addMenuButton("Phân quyền", "ROLE", "Role");
        addMenuButton("Báo cáo & Thống kê", "CHART", "Stats");
        addMenuButton("Cài đặt", "SETTINGS", "Settings");
        
        sidebar.add(Box.createVerticalGlue());
        addMenuButton("Đăng xuất", "EXIT", "Logout");
        
        int bottomGap = (int) (screenHeight * 0.029); 
        sidebar.add(Box.createRigidArea(new Dimension(0, bottomGap)));

        // 2. Main Container
        mainContainer = new JPanel(new BorderLayout());
        mainContainer.setOpaque(false);
        
        int mainPaddingTop = (int) (screenHeight * 0.023); 
        int mainPaddingSide = (int) (screenWidth * 0.020); 
        int mainPaddingBottom = (int) (screenHeight * 0.035); 
        mainContainer.setBorder(new EmptyBorder(mainPaddingTop, mainPaddingSide, mainPaddingBottom, mainPaddingSide));


        // Content Area
        cardLayout = new CardLayout(0, 0);
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        // Khởi tạo các Panel chức năng
        contentArea.add(new DashboardPanel(), "Stats"); 
        
        this.posPanel = new PosPanel();
        contentArea.add(this.posPanel, "Order");
        
        // [MỚI] Khởi tạo và gắn OrderPanel vào CardLayout
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

        // 3. Lắp ráp
        add(sidebar, BorderLayout.WEST);
        add(mainContainer, BorderLayout.CENTER);
        
        // Mặc định chọn Dashboard (Stats)
        setPageActive("Stats");
    }

    private void addMenuButton(String text, String icon, String cardName) {
        NavButton btn = new NavButton(text, icon, cardName);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT); 
        navButtons.put(cardName, btn);
        sidebar.add(btn);
        
        int gapSmall = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.006); 
        sidebar.add(Box.createRigidArea(new Dimension(0, gapSmall)));
        
        if (!cardName.equals("Logout")) {
            btn.addActionListener(e -> {
                try {
                    setPageActive(cardName);
                } catch (SQLException ex) {
                    System.getLogger(MainFrame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            });
        }
    }

    private void setPageActive(String cardName) throws SQLException {
        if (!cardName.equals("Logout") && SessionManager.isLoggedIn() && !ValidationUtil.validateSession()) {
            SessionManager.clear();
            this.setVisible(false);
            new AccountController();
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

    public static void main(String[] args) {}
    
    public StockPanel getStockPanel(){
        return stockPanel;
    }
    
    public MenuPanel getMenuPanel(){
        return menuPanel;
    }
    
    public RolePanel getRolePanel(){
        return rolePanel;
    }
    
    public PosPanel getPosPanel(){
        return posPanel;
    }
    
    public EmployeeSchedulePanel getShiftPanel(){
        return shiftPanel;
    }
    
    // [MỚI] Getter để Controller có thể truy cập OrderPanel
    public OrderPanel getOrderPanel() {
        return orderPanel;
    }
    
    public void setRoleMenuVisible(boolean isVisible) {
        if (navButtons != null && navButtons.containsKey("Role")) {
            NavButton roleBtn = navButtons.get("Role");
            roleBtn.setVisible(isVisible);
            
            sidebar.revalidate();
            sidebar.repaint();
        }
    }
}