package Controller;

import Service.ReportService;
import View.DashboardPanel;
import View.MainFrame;
import java.util.List;

public class DashboardController {
    
    private MainFrame mainFrame;
    private DashboardPanel dashboardPanel;
    private ReportService reportService;

    public DashboardController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.dashboardPanel = mainFrame.getDashboardPanel();
        this.reportService = new ReportService();
        
        // --- SỰ KIỆN LỌC DOANH THU ---
        this.dashboardPanel.addRevenueFilterListener(e -> {
            String selectedFilter = dashboardPanel.getSelectedRevenueFilter();
            
            if(selectedFilter.contains("Tùy chỉnh")) {
                // Gọi hộp thoại chọn ngày lên
                java.util.Date[] dates = dashboardPanel.showCustomDateDialog();
                if (dates != null) {
                    // Nếu người dùng chọn ngày thành công, truyền vào hàm vẽ biểu đồ Custom
                    loadCustomRevenueStats(dates[0], dates[1]);
                }
            } else {
                // Nếu chọn Hôm nay/Tuần này/Tháng này thì chạy hàm mặc định
                loadRevenueStats(selectedFilter);
            }
        });
        
        // --- SỰ KIỆN LỌC CA LÀM VIỆC ---
        this.dashboardPanel.addShiftFilterListener(e -> {
            String selectedFilter = dashboardPanel.getSelectedShiftFilter();
            
            if(selectedFilter.contains("Tùy chỉnh")) {
                java.util.Date[] dates = dashboardPanel.showCustomDateDialog();
                if (dates != null) {
                    loadCustomShiftStats(dates[0], dates[1]);
                }
            } else {
                loadShiftStats(selectedFilter);
            }
        });
        
        // --- SỰ KIỆN LỌC KHÁCH HÀNG (MỚI) ---
        this.dashboardPanel.addCustomerFilterListener(e -> {
            String selectedFilter = dashboardPanel.getSelectedCustomerFilter();
            
            if(selectedFilter.contains("Tùy chỉnh")) {
                java.util.Date[] dates = dashboardPanel.showCustomDateDialog();
                if (dates != null) {
                    loadCustomCustomerStats(dates[0], dates[1]);
                }
            } else {
                loadCustomerStats(selectedFilter);
            }
        });
        
        loadData();
    }

    private void loadData() {
        loadRevenueStats("Hôm nay");
        loadSalesStats();
        loadInventoryStats();
        loadShiftStats("Tuần này"); // Mở khóa dòng này để gọi Load dữ liệu Ca làm việc
        loadCustomerStats("Tuần này"); 
    }

    private void loadRevenueStats(String filterType) {
        // 1. Load Thẻ thống kê
        Object[] revData = reportService.getRevenueStats(filterType);
        long totalRevenue = (long) revData[0];
        int totalOrders = (int) revData[1];
        long avgOrder = totalOrders > 0 ? (totalRevenue / totalOrders) : 0;

        String strTotalRev = String.format("%,d đ", totalRevenue);
        String strTotalOrders = String.valueOf(totalOrders);
        String strAvgOrder = String.format("%,d đ", avgOrder);

        dashboardPanel.updateRevenueCards(strTotalRev, strTotalOrders, strAvgOrder);

        // 2. Load Biểu đồ và cập nhật Tiêu đề
        List<Object[]> chartData = reportService.getRevenueChartData(filterType);
        String chartTitle = "Doanh Thu " + filterType; 
        
        dashboardPanel.updateRevenueChart(chartData, chartTitle);
    }

    private void loadSalesStats() {
        // 1. Load Bảng Top Món
        List<Object[]> topProducts = reportService.getTopSellingProducts();
        dashboardPanel.updateTopSellingTable(topProducts);
        
        // 2. Load Biểu đồ tròn Danh mục
        List<Object[]> categorySales = reportService.getSalesByCategory();
        dashboardPanel.updateCategoryPieChart(categorySales);
    }
    
    private void loadInventoryStats() {
        // 1. Cập nhật 3 Thẻ thống kê
        Object[] invStats = reportService.getInventoryOverviewStats();
        String strTotalInv = String.format("%,d đ", (Long) invStats[0]);
        String strLowStock = String.valueOf((Integer) invStats[1]);
        String strImportMonth = String.format("%,d đ", (Long) invStats[2]);
        
        dashboardPanel.updateInventoryCards(strTotalInv, strLowStock, strImportMonth);

        // 2. Cập nhật Bảng Nguyên liệu gần hết hạn (Sắp xếp theo Lô)
        List<Object[]> expiringData = reportService.getExpiringIngredients();
        dashboardPanel.updateExpiringTable(expiringData);

        // 3. Cập nhật Bảng Nguyên liệu sử dụng nhiều nhất (Tháng)
        List<Object[]> mostUsedData = reportService.getMostUsedIngredients();
        dashboardPanel.updateMostUsedTable(mostUsedData);
    }
    
    // =========================================================================
    // XỬ LÝ DỮ LIỆU TAB KHÁCH HÀNG (MỚI)
    // =========================================================================
    private void loadCustomerStats(String filterType) {
        // 1. Lấy dữ liệu 4 Thẻ thống kê
        Object[] cusData = reportService.getCustomerOverviewStats(filterType);
        int newCus = (int) cusData[0];
        double retention = (double) cusData[1];
        long arpu = (long) cusData[2];
        long totalPoints = (long) cusData[3];

        String strNewCus = String.format("%,d", newCus);
        String strRetention = String.format("%.2f%%", retention); // Format phần trăm
        String strArpu = String.format("%,d đ", arpu);
        String strTotalPoints = String.format("%,d", totalPoints);

        dashboardPanel.updateCustomerCards(strNewCus, strRetention, strArpu, strTotalPoints);

        // 2. Cập nhật Biểu đồ đường
        List<Object[]> chartData = reportService.getCustomerGrowthChartData(filterType);
        String chartTitle = "Tốc Độ Tăng Trưởng Khách Hàng - " + filterType; 
        
        dashboardPanel.updateCustomerGrowthChart(chartData, chartTitle);
    }

    // =========================================================================
    // XỬ LÝ LỌC CUSTOM (THEO KHOẢNG NGÀY)
    // =========================================================================
    private void loadCustomRevenueStats(java.util.Date startDate, java.util.Date endDate) {
        Object[] revData = reportService.getCustomRevenueStats(startDate, endDate);
        long totalRevenue = (long) revData[0];
        int totalOrders = (int) revData[1];
        long avgOrder = totalOrders > 0 ? (totalRevenue / totalOrders) : 0;

        String strTotalRev = String.format("%,d đ", totalRevenue);
        String strTotalOrders = String.valueOf(totalOrders);
        String strAvgOrder = String.format("%,d đ", avgOrder);

        dashboardPanel.updateRevenueCards(strTotalRev, strTotalOrders, strAvgOrder);

        List<Object[]> chartData = reportService.getCustomRevenueChartData(startDate, endDate);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String chartTitle = "Doanh Thu Từ " + sdf.format(startDate) + " Đến " + sdf.format(endDate);
        
        dashboardPanel.updateRevenueChart(chartData, chartTitle);
    }
    
    private void loadCustomCustomerStats(java.util.Date startDate, java.util.Date endDate) {
        Object[] cusData = reportService.getCustomCustomerOverviewStats(startDate, endDate);
        int newCus = (int) cusData[0];
        double retention = (double) cusData[1];
        long arpu = (long) cusData[2];
        long totalPoints = (long) cusData[3]; // Tổng điểm luôn được lấy thực tế trong kho không qua Date

        String strNewCus = String.format("%,d", newCus);
        String strRetention = String.format("%.2f%%", retention);
        String strArpu = String.format("%,d đ", arpu);
        String strTotalPoints = String.format("%,d", totalPoints);

        dashboardPanel.updateCustomerCards(strNewCus, strRetention, strArpu, strTotalPoints);

        List<Object[]> chartData = reportService.getCustomCustomerGrowthChartData(startDate, endDate);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String chartTitle = "Tăng Trưởng Khách Hàng Từ " + sdf.format(startDate) + " Đến " + sdf.format(endDate);
        
        dashboardPanel.updateCustomerGrowthChart(chartData, chartTitle);
    }
    
    // =========================================================================
    // XỬ LÝ DỮ LIỆU TAB CA LÀM VIỆC
    // =========================================================================
    private void loadShiftStats(String filterType) {
        // 1. Thẻ thống kê
        Object[] stats = reportService.getShiftOverviewStats(filterType);
        String strShifts = String.format("%,d", (Integer) stats[0]);
        String strHours = String.format("%.1f", (Double) stats[1]);
        String strAvgRev = String.format("%,d đ", (Long) stats[2]);
        String strCanceled = String.format("%,d", (Integer) stats[3]);

        dashboardPanel.updateShiftCards(strShifts, strHours, strAvgRev, strCanceled);

        // 2. Biểu đồ cột (Doanh thu theo mẫu Ca)
        List<Object[]> barData = reportService.getShiftRevenueChartData(filterType);
        dashboardPanel.updateShiftRevenueChart(barData, "Doanh Thu Theo Mẫu Ca - " + filterType);

        // 3. Biểu đồ đường (Giờ làm việc)
        List<Object[]> lineData = reportService.getWorkingHoursChartData(filterType);
        dashboardPanel.updateWorkingHoursChart(lineData, "Xu Hướng Giờ Làm Việc - " + filterType);
    }

    private void loadCustomShiftStats(java.util.Date startDate, java.util.Date endDate) {
        Object[] stats = reportService.getCustomShiftOverviewStats(startDate, endDate);
        String strShifts = String.format("%,d", (Integer) stats[0]);
        String strHours = String.format("%.1f", (Double) stats[1]);
        String strAvgRev = String.format("%,d đ", (Long) stats[2]);
        String strCanceled = String.format("%,d", (Integer) stats[3]);

        dashboardPanel.updateShiftCards(strShifts, strHours, strAvgRev, strCanceled);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String dateSuffix = "Từ " + sdf.format(startDate) + " Đến " + sdf.format(endDate);

        List<Object[]> barData = reportService.getCustomShiftRevenueChartData(startDate, endDate);
        dashboardPanel.updateShiftRevenueChart(barData, "Doanh Thu Theo Mẫu Ca " + dateSuffix);

        List<Object[]> lineData = reportService.getCustomWorkingHoursChartData(startDate, endDate);
        dashboardPanel.updateWorkingHoursChart(lineData, "Xu Hướng Giờ Làm Việc " + dateSuffix);
    }
}