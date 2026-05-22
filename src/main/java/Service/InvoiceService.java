package Service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class InvoiceService {

    /**
     * Hàm hiển thị hoặc in hóa đơn
     * @param maDonHang Mã đơn hàng cần in
     * @param isPrintDirectly true: In ra máy in luôn, false: Mở cửa sổ xem trước
     */
    public void printInvoice(int maDonHang, boolean isPrintDirectly) {
        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection()) {
            
            // 1. Đọc file thiết kế XML từ thư mục resources
            InputStream reportStream = getClass().getResourceAsStream("/reports/Invoice.jrxml");
            if (reportStream == null) {
                JOptionPane.showMessageDialog(null, "Không tìm thấy file mẫu hóa đơn (Invoice.jrxml)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Biên dịch file .jrxml thành cấu trúc JasperReport có thể chạy được
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 3. Truyền tham số p_MaDonHang vào trong báo cáo
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("p_MaDonHang", maDonHang);

            // 4. Đổ dữ liệu từ Database vào Báo cáo
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);

            // 5. Hiển thị hoặc In
            if (isPrintDirectly) {
                // In thẳng ra máy in mặc định của hệ thống
                JasperPrintManager.printReport(jasperPrint, false);
            } else {
                // Mở cửa sổ JasperViewer để xem trước
                // Tham số "false" rất quan trọng: Nó giúp khi bấm dấu [X] đóng cửa sổ hóa đơn thì không bị tắt luôn toàn bộ phần mềm
                JasperViewer.viewReport(jasperPrint, false);
            }

        } catch (JRException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi tạo hóa đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi kết nối CSDL: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * (Tùy chọn) Hàm xuất hóa đơn ra file PDF
     */
    public void exportInvoiceToPDF(int maDonHang, String filePath) {
        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection()) {
            InputStream reportStream = getClass().getResourceAsStream("C:\\Users\\SONY\\Documents\\NetBeansProjects\\TheBangCoffee\\src\\main\\resources\\reports\\Invoice.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("p_MaDonHang", maDonHang);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
            
            // Dùng JasperExportManager để xuất file
            JasperExportManager.exportReportToPdfFile(jasperPrint, filePath);
            
            JOptionPane.showMessageDialog(null, "Đã xuất PDF thành công tại:\n" + filePath, "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi xuất PDF: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Hàm hiển thị/in Phiếu nhập kho
     * @param maPhieuNhap Mã phiếu nhập cần in
     */
    public void printWarehouseReceipt(int maPhieuNhap) {
        try (Connection conn = ConnectDatabase.ConnectionUtils.getMyConnection()) {
            
            // Lấy mẫu thiết kế Phiếu nhập kho
            java.io.InputStream reportStream = getClass().getResourceAsStream("/reports/WarehouseReceipt.jrxml");
            if (reportStream == null) {
                javax.swing.JOptionPane.showMessageDialog(null, "Không tìm thấy mẫu (PhieuNhapKho.jrxml)!");
                return;
            }

            net.sf.jasperreports.engine.JasperReport jasperReport = net.sf.jasperreports.engine.JasperCompileManager.compileReport(reportStream);

            // Gắn tham số p_MaPhieuNhap cho SQL
            java.util.Map<String, Object> parameters = new java.util.HashMap<>();
            parameters.put("p_MaPhieuNhap", maPhieuNhap);

            net.sf.jasperreports.engine.JasperPrint jasperPrint = net.sf.jasperreports.engine.JasperFillManager.fillReport(jasperReport, parameters, conn);

            // Mở cửa sổ xem trước (Preview)
            net.sf.jasperreports.view.JasperViewer.viewReport(jasperPrint, false);

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "Lỗi khi in Phiếu Nhập Kho: " + e.getMessage());
        }
    }
    

}