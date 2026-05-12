package Controller;

import Service.ReportService;
import View.DashboardPanel;
import View.MainFrame;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class DashboardController {
    
    private MainFrame mainFrame;
    private DashboardPanel dashboardPanel;
    private ReportService reportService;

    public DashboardController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.dashboardPanel = mainFrame.getDashboardPanel();
        this.reportService = new ReportService();
        
        this.dashboardPanel.addRevenueFilterListener(e -> {
            String selectedFilter = dashboardPanel.getSelectedRevenueFilter();
            
            if(selectedFilter.equals("Tùy chỉnh...")) {
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
        
        loadData();
    }

    private void loadData() {
        loadRevenueStats("Hôm nay");
        loadSalesStats();
        loadInventoryStats();
        // loadShiftStats()...
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
        
        // Sinh tiêu đề dựa trên bộ lọc
        String chartTitle = "Doanh Thu " + filterType; 
        
        // Gọi hàm update có truyền tiêu đề
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
    
    private void loadCustomRevenueStats(java.util.Date startDate, java.util.Date endDate) {
        // 1. Load Thẻ thống kê
        Object[] revData = reportService.getCustomRevenueStats(startDate, endDate);
        long totalRevenue = (long) revData[0];
        int totalOrders = (int) revData[1];
        long avgOrder = totalOrders > 0 ? (totalRevenue / totalOrders) : 0;

        String strTotalRev = String.format("%,d đ", totalRevenue);
        String strTotalOrders = String.valueOf(totalOrders);
        String strAvgOrder = String.format("%,d đ", avgOrder);

        dashboardPanel.updateRevenueCards(strTotalRev, strTotalOrders, strAvgOrder);

        // 2. Load Biểu đồ
        List<Object[]> chartData = reportService.getCustomRevenueChartData(startDate, endDate);
        
        // Ép kiểu ngày tháng để tạo câu Tiêu đề thật đẹp (VD: Doanh Thu Từ 01/05/2026 Đến 15/05/2026)
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String chartTitle = "Doanh Thu Từ " + sdf.format(startDate) + " Đến " + sdf.format(endDate);
        
        // Gọi hàm update có truyền tiêu đề
        dashboardPanel.updateRevenueChart(chartData, chartTitle);
    }
}