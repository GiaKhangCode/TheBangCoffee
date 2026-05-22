package Service;

import DatabaseAccessObject.ReportDAO;
import java.util.List;

public class ReportService {
    private ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    // ==========================================
    // TAB 1: DOANH THU
    // ==========================================
    public Object[] getRevenueStats(String filterType) {
        return reportDAO.getRevenueStats(filterType);
    }
    public List<Object[]> getRevenueChartData(String filterType) {
        return reportDAO.getRevenueChartData(filterType);
    }
    
    public Object[] getCustomRevenueStats(java.util.Date startDate, java.util.Date endDate) {
        return reportDAO.getCustomRevenueStats(startDate, endDate);
    }

    public List<Object[]> getCustomRevenueChartData(java.util.Date startDate, java.util.Date endDate) {
        return reportDAO.getCustomRevenueChartData(startDate, endDate);
    }

    // ==========================================
    // TAB 2: BÁN HÀNG
    // ==========================================
    public List<Object[]> getTopSellingProducts() {
        return reportDAO.getTopSellingProducts();
    }

    public List<Object[]> getSalesByCategory() {
        return reportDAO.getSalesByCategory();
    }

    // ==========================================
    // TAB 3: CA LÀM VIỆC VÀ NHÂN VIÊN
    // ==========================================
    public List<Object[]> getEmployeePerformance() {
        return reportDAO.getEmployeePerformance();
    }
    


    // ==========================================
    // TAB 4: KHO
    // ==========================================
    public Object[] getInventoryOverviewStats() {
        return reportDAO.getInventoryOverviewStats();
    }

    public List<Object[]> getExpiringIngredients() {
        return reportDAO.getExpiringIngredients();
    }

    public List<Object[]> getMostUsedIngredients() {
        return reportDAO.getMostUsedIngredients();
    }
    
    // ==========================================
    // TAB 5: KHÁCH HÀNG (MỚI)
    // ==========================================
    public Object[] getCustomerOverviewStats(String filterType) {
        return reportDAO.getCustomerOverviewStats(filterType);
    }

    public Object[] getCustomCustomerOverviewStats(java.util.Date startDate, java.util.Date endDate) {
        return reportDAO.getCustomCustomerOverviewStats(startDate, endDate);
    }

    public List<Object[]> getCustomerGrowthChartData(String filterType) {
        return reportDAO.getCustomerGrowthChartData(filterType);
    }

    public List<Object[]> getCustomCustomerGrowthChartData(java.util.Date startDate, java.util.Date endDate) {
        return reportDAO.getCustomCustomerGrowthChartData(startDate, endDate);
    }
}