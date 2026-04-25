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
    private HeaderPanel header;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private StockPanel stockPanel;
    private MenuPanel menuPanel;
    private Map<String, NavButton> navButtons;
    private NavButton activeButton;
    private RolePanel rolePanel;
    private EmployeeSchedulePanel shiftPanel;
    
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
        int sidebarWidth = (int) (screenWidth * 0.18); // Khoảng 280px
        sidebar.setPreferredSize(new Dimension(sidebarWidth, screenHeight)); 
        
        // Logo Section
        JLabel logoTitle = new JLabel("The Bang Coffee");
        int logoFontSize = Math.max(18, (int) (screenWidth * 0.014)); // Khoảng 22px
        logoTitle.setFont(new Font("Segoe UI", Font.BOLD, logoFontSize));
        logoTitle.setForeground(AppColor.TEXT_DARK);
        
        int logoPaddingTop = (int) (screenHeight * 0.046); // Khoảng 40px
        int logoPaddingLeft = (int) (screenWidth * 0.016); // Khoảng 25px
        int logoPaddingBottom = (int) (screenHeight * 0.035); // Khoảng 30px
        logoTitle.setBorder(new EmptyBorder(logoPaddingTop, logoPaddingLeft, logoPaddingBottom, 0)); 
        logoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sidebar.add(logoTitle);

        // Menu Nút điều hướng
        navButtons = new HashMap<>();
        addMenuButton("Bán hàng", "POS", "Order");
        addMenuButton("Menu đồ uống", "MENU", "Menu");
        addMenuButton("Quản lý kho", "STOCK", "Stock");
        addMenuButton("Quản lý nhân viên", "STAFF", "Staff");
        addMenuButton("Phân quyền", "ROLE", "Role");
        addMenuButton("Báo cáo & Thống kê", "CHART", "Stats");
        addMenuButton("Cài đặt", "SETTINGS", "Settings");
        
        sidebar.add(Box.createVerticalGlue());
        addMenuButton("Đăng xuất", "EXIT", "Logout");
        
        int bottomGap = (int) (screenHeight * 0.029); // Khoảng 25px
        sidebar.add(Box.createRigidArea(new Dimension(0, bottomGap)));

        // 2. Main Container
        mainContainer = new JPanel(new BorderLayout());
        mainContainer.setOpaque(false);
        
        int mainPaddingTop = (int) (screenHeight * 0.023); // Khoảng 20px
        int mainPaddingSide = (int) (screenWidth * 0.020); // Khoảng 30px
        int mainPaddingBottom = (int) (screenHeight * 0.035); // Khoảng 30px
        mainContainer.setBorder(new EmptyBorder(mainPaddingTop, mainPaddingSide, mainPaddingBottom, mainPaddingSide));

        // Header
        header = new HeaderPanel("Tổng quan hôm nay");
        mainContainer.add(header, BorderLayout.NORTH);

        // Content Area
        cardLayout = new CardLayout(0, 0);
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        // Thêm các Panel chức năng
        contentArea.add(new DashboardPanel(), "Stats"); 
        contentArea.add(new ContentBasePanel("Giao diện Bán hàng", "Thực hiện order và thanh toán."), "Order");
        contentArea.add(new MenuPanel(), "Menu");
        //------------------------------------------------------------------------
        this.stockPanel = new StockPanel(); 
        this.menuPanel = new MenuPanel();
        this.rolePanel = new RolePanel();
        contentArea.add(this.stockPanel, "Stock"); 
        contentArea.add(this.menuPanel, "Menu"); 
//        try {
//            DatabaseAccessObject.NguyenLieuDAO dao = new DatabaseAccessObject.NguyenLieuDAO();
//            java.util.List<Model.NguyenLieu> data = dao.getNguyenLieu();
//            this.stockPanel.displayData(data);
//        } catch (Exception e) {
//            System.out.println("Loi: " + e.getMessage());
//        }
        
//        DatabaseAccessObject.PhieuNhapKhoDAO phieuDao = new DatabaseAccessObject.PhieuNhapKhoDAO();
//            java.util.List<Model.PhieuNhapKho> lsPhieu = phieuDao.getPhieuNhapKho();
//            this.stockPanel.displayPhieuNhapKhoData(lsPhieu);
        
        //------------------------------------------------------------------------
        this.shiftPanel = new EmployeeSchedulePanel();
        contentArea.add(this.shiftPanel, "Staff");
        contentArea.add(this.rolePanel, "Role"); // Giao diện Phân quyền
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
        
        int gapSmall = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.006); // Khoảng 5px
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
            String title = activeButton.getText();
            if (cardName.equals("Stats")) title = "Tổng quan hôm nay";
            header.setTitle(title);
            cardLayout.show(contentArea, cardName);
        }
    }

    /**
     * Đăng ký lắng nghe sự kiện Đăng xuất.
     * (Chỉ duy nhất một phương thức này để tránh lỗi Duplicate)
     */
    public void addLogoutListener(ActionListener listener) {
        if (navButtons != null && navButtons.containsKey("Logout")) {
            navButtons.get("Logout").addActionListener(listener);
        }
    }

    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception ignored) {}
//
//        SwingUtilities.invokeLater(() -> {
//            new MainFrame().setVisible(true);
//        });
    }
    
    public StockPanel getStockPanel(){
        return stockPanel;
    }
    
    public MenuPanel getMenuPanel(){
        return menuPanel;
    }
    public RolePanel getRolePanel(){
        return rolePanel;
    }
    
    public EmployeeSchedulePanel getShiftPanel(){
        return shiftPanel;
    }
    
    public void setRoleMenuVisible(boolean isVisible) {
        if (navButtons != null && navButtons.containsKey("Role")) {
            NavButton roleBtn = navButtons.get("Role");
            roleBtn.setVisible(isVisible);
            
            // Ép sidebar vẽ lại để cập nhật các khoảng trống nếu nút bị ẩn
            sidebar.revalidate();
            sidebar.repaint();
        }
    }
}
