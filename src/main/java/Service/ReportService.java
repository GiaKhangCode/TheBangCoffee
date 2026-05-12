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
    // TAB 4: KHO
    // ==========================================
    // [MỚI THÊM] Hàm lấy 3 thông số tổng quan Kho
    public Object[] getInventoryOverviewStats() {
        return reportDAO.getInventoryOverviewStats();
    }

    public List<Object[]> getExpiringIngredients() {
        return reportDAO.getExpiringIngredients();
    }

    public List<Object[]> getMostUsedIngredients() {
        return reportDAO.getMostUsedIngredients();
    }
}