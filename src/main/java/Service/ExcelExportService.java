package Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

public class ExcelExportService {

    public boolean exportDashboardDataToExcel(
            String filePath,
            String revFilter,
            String cusFilter,
            ReportService reportService,
            Date revStartDate, Date revEndDate,
            Date cusStartDate, Date cusEndDate) {
        
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // --- TẠO CÁC STYLE ĐỊNH DẠNG ---
            XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
            
            // Màu chủ đạo: 67, 142, 104
            byte[] primaryRgb = new byte[]{(byte)67, (byte)142, (byte)104};
            org.apache.poi.xssf.usermodel.XSSFColor primaryColor = new org.apache.poi.xssf.usermodel.XSSFColor(primaryRgb, null);
            
            // 1. Title Style (Tiêu đề lớn)
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            // Có thể dùng màu chủ đạo cho chữ tiêu đề
            ((org.apache.poi.xssf.usermodel.XSSFFont)titleFont).setColor(primaryColor);
            titleStyle.setFont(titleFont);
            
            // 2. Header Style (Dòng tiêu đề bảng)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            ((org.apache.poi.xssf.usermodel.XSSFCellStyle)headerStyle).setFillForegroundColor(primaryColor);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(headerStyle);

            // 3. Data Style (Dữ liệu chữ thông thường)
            CellStyle dataStyle = workbook.createCellStyle();
            setBorders(dataStyle);
            
            // 4. Number Style (Dữ liệu số thông thường)
            CellStyle numberStyle = workbook.createCellStyle();
            setBorders(numberStyle);
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0"));
            
            // 5. Percent Style (Dữ liệu phần trăm)
            CellStyle percentStyle = workbook.createCellStyle();
            setBorders(percentStyle);
            percentStyle.setDataFormat(format.getFormat("0.00%"));

            // --- Sheet 1: Doanh Thu ---
            exportRevenueTab(workbook, titleStyle, headerStyle, dataStyle, numberStyle, revFilter, revStartDate, revEndDate, reportService);

            // --- Sheet 2: Bán Hàng ---
            exportSalesTab(workbook, titleStyle, headerStyle, dataStyle, numberStyle, reportService);

            // --- Sheet 3: Kho ---
            exportInventoryTab(workbook, titleStyle, headerStyle, dataStyle, numberStyle, reportService);

            // --- Sheet 4: Khách Hàng ---
            exportCustomerTab(workbook, titleStyle, headerStyle, dataStyle, numberStyle, percentStyle, cusFilter, cusStartDate, cusEndDate, reportService);

            // Ghi ra file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void setBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void exportRevenueTab(Workbook workbook, CellStyle titleStyle, CellStyle headerStyle, CellStyle dataStyle, CellStyle numberStyle, String revFilter, Date revStartDate, Date revEndDate, ReportService reportService) {
        Sheet sheet = workbook.createSheet("Doanh Thu");
        int rowNum = 0;

        Object[] revStats;
        List<Object[]> chartData;
        if (revFilter.contains("Tùy chỉnh") && revStartDate != null && revEndDate != null) {
            revStats = reportService.getCustomRevenueStats(revStartDate, revEndDate);
            chartData = reportService.getCustomRevenueChartData(revStartDate, revEndDate);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            createRow(sheet, rowNum++, titleStyle, null, null, "Thống kê từ: " + sdf.format(revStartDate) + " đến " + sdf.format(revEndDate));
        } else {
            revStats = reportService.getRevenueStats(revFilter);
            chartData = reportService.getRevenueChartData(revFilter);
            createRow(sheet, rowNum++, titleStyle, null, null, "Thống kê: " + revFilter);
        }

        long totalRevenue = (long) revStats[0];
        int totalOrders = (int) revStats[1];
        long avgOrder = totalOrders > 0 ? (totalRevenue / totalOrders) : 0;

        createRow(sheet, rowNum++, dataStyle, numberStyle, null, "Tổng Doanh Thu (VNĐ):", totalRevenue);
        createRow(sheet, rowNum++, dataStyle, numberStyle, null, "Tổng Số Đơn:", totalOrders);
        createRow(sheet, rowNum++, dataStyle, numberStyle, null, "Doanh Thu Trung Bình/Đơn (VNĐ):", avgOrder);
        
        rowNum++; // Empty row
        
        createRow(sheet, rowNum++, titleStyle, null, null, "Chi tiết Doanh Thu Theo Thời Gian");
        createRow(sheet, rowNum++, headerStyle, null, null, "Thời Gian", "Doanh Thu (VNĐ)");
        
        for (Object[] row : chartData) {
            createRow(sheet, rowNum++, dataStyle, numberStyle, null, (String) row[0], (Long) row[1]);
        }
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void exportSalesTab(Workbook workbook, CellStyle titleStyle, CellStyle headerStyle, CellStyle dataStyle, CellStyle numberStyle, ReportService reportService) {
        Sheet sheet = workbook.createSheet("Bán Hàng");
        int rowNum = 0;

        createRow(sheet, rowNum++, titleStyle, null, null, "Top Món Bán Chạy");
        createRow(sheet, rowNum++, headerStyle, null, null, "STT", "Tên Món Nước", "Danh Mục", "Số Lượng Bán", "Doanh Thu Mang Lại (VNĐ)");

        List<Object[]> topProducts = reportService.getTopSellingProducts();
        int stt = 1;
        for (Object[] row : topProducts) {
            createRow(sheet, rowNum++, dataStyle, numberStyle, null, stt++, (String) row[0], (String) row[1], (Integer) row[2], (Long) row[3]);
        }

        rowNum++;
        
        createRow(sheet, rowNum++, titleStyle, null, null, "Sản Phẩm Theo Danh Mục");
        createRow(sheet, rowNum++, headerStyle, null, null, "Danh Mục", "Số Lượng Bán");
        
        List<Object[]> categorySales = reportService.getSalesByCategory();
        for (Object[] row : categorySales) {
            createRow(sheet, rowNum++, dataStyle, numberStyle, null, (String) row[0], (Integer) row[1]);
        }
        
        for (int i = 0; i < 5; i++) sheet.autoSizeColumn(i);
    }

    private void exportInventoryTab(Workbook workbook, CellStyle titleStyle, CellStyle headerStyle, CellStyle dataStyle, CellStyle numberStyle, ReportService reportService) {
        Sheet sheet = workbook.createSheet("Kho");
        int rowNum = 0;

        Object[] invStats = reportService.getInventoryOverviewStats();
        createRow(sheet, rowNum++, titleStyle, null, null, "Tổng Quan Kho");
        createRow(sheet, rowNum++, dataStyle, numberStyle, null, "Tổng Vốn Tồn Kho (VNĐ):", (Long) invStats[0]);
        createRow(sheet, rowNum++, dataStyle, numberStyle, null, "Nguyên Liệu Sắp Hết:", (Integer) invStats[1]);
        createRow(sheet, rowNum++, dataStyle, numberStyle, null, "Tiền Đã Nhập (Tháng) (VNĐ):", (Long) invStats[2]);
        
        rowNum++;
        
        createRow(sheet, rowNum++, titleStyle, null, null, "Nguyên Liệu Sắp Hết Hạn (<= 7 ngày)");
        createRow(sheet, rowNum++, headerStyle, null, null, "STT", "Mã Lô", "Tên Nguyên Liệu", "Còn Lại", "Hạn Sử Dụng");
        
        List<Object[]> expiring = reportService.getExpiringIngredients();
        int stt = 1;
        for (Object[] row : expiring) {
            createRow(sheet, rowNum++, dataStyle, numberStyle, null, stt++, (Integer) row[0], (String) row[1], (String) row[2], (String) row[3]);
        }
        
        rowNum++;
        
        createRow(sheet, rowNum++, titleStyle, null, null, "Nguyên Liệu Tiêu Hao Nhiều Nhất");
        createRow(sheet, rowNum++, headerStyle, null, null, "STT", "Tên Nguyên Liệu", "Tổng Tiêu Hao");
        
        List<Object[]> mostUsed = reportService.getMostUsedIngredients();
        stt = 1;
        for (Object[] row : mostUsed) {
            createRow(sheet, rowNum++, dataStyle, numberStyle, null, stt++, (String) row[0], (String) row[1]);
        }
        
        for (int i = 0; i < 5; i++) sheet.autoSizeColumn(i);
    }

    private void exportCustomerTab(Workbook workbook, CellStyle titleStyle, CellStyle headerStyle, CellStyle dataStyle, CellStyle numberStyle, CellStyle percentStyle, String cusFilter, Date cusStartDate, Date cusEndDate, ReportService reportService) {
        Sheet sheet = workbook.createSheet("Khách Hàng");
        int rowNum = 0;

        Object[] cusStats;
        List<Object[]> chartData;
        if (cusFilter.contains("Tùy chỉnh") && cusStartDate != null && cusEndDate != null) {
            cusStats = reportService.getCustomCustomerOverviewStats(cusStartDate, cusEndDate);
            chartData = reportService.getCustomCustomerGrowthChartData(cusStartDate, cusEndDate);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            createRow(sheet, rowNum++, titleStyle, null, null, "Thống kê từ: " + sdf.format(cusStartDate) + " đến " + sdf.format(cusEndDate));
        } else {
            cusStats = reportService.getCustomerOverviewStats(cusFilter);
            chartData = reportService.getCustomerGrowthChartData(cusFilter);
            createRow(sheet, rowNum++, titleStyle, null, null, "Thống kê: " + cusFilter);
        }

        createRow(sheet, rowNum++, dataStyle, numberStyle, null, "Khách Hàng Mới:", (Integer) cusStats[0]);
        createRow(sheet, rowNum++, dataStyle, percentStyle, percentStyle, "Tỷ Lệ Quay Lại:", (Double) cusStats[1] / 100.0); // Chuyển thành % Excel
        createRow(sheet, rowNum++, dataStyle, numberStyle, null, "ARPU (VNĐ):", (Long) cusStats[2]);
        createRow(sheet, rowNum++, dataStyle, numberStyle, null, "Tổng Điểm Tích Lũy:", (Long) cusStats[3]);
        
        rowNum++;
        
        createRow(sheet, rowNum++, titleStyle, null, null, "Tốc Độ Tăng Trưởng Khách Hàng Mới");
        createRow(sheet, rowNum++, headerStyle, null, null, "Thời Gian", "Khách Mới (Người)");
        
        for (Object[] row : chartData) {
            createRow(sheet, rowNum++, dataStyle, numberStyle, null, (String) row[0], (Integer) row[1]);
        }
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createRow(Sheet sheet, int rowNum, CellStyle dataStyle, CellStyle numberStyle, CellStyle overrideStyle, Object... values) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            Object val = values[i];
            
            CellStyle currentStyle = dataStyle;
            
            if (val != null) {
                if (val instanceof String) {
                    cell.setCellValue((String) val);
                } else if (val instanceof Number) {
                    cell.setCellValue(((Number) val).doubleValue());
                    currentStyle = numberStyle;
                } else if (val instanceof Boolean) {
                    cell.setCellValue((Boolean) val);
                } else if (val instanceof Date) {
                    cell.setCellValue((Date) val);
                } else {
                    cell.setCellValue(val.toString());
                }
            }
            
            // Nếu có override (dành cho %, header, title)
            if (overrideStyle != null) {
                currentStyle = overrideStyle;
            } else if (dataStyle != null && currentStyle == null) {
                currentStyle = dataStyle;
            }
            
            if (currentStyle != null) {
                cell.setCellStyle(currentStyle);
            }
        }
    }
}
