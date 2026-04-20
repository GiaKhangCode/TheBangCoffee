/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Common;

import Model.SessionManager;
import Service.SessionService;
import View.StockPanel;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FAKK
 */
public class ValidationUtil {
    private static SessionService sessionService = new SessionService();
    public static String checkValidateLogin(String fullName, String username, String password, String phoneNumber, String email){
        if(fullName.isEmpty()){
            return "Họ tên không được để trống";
        }
            
        if(email.isEmpty()){
            return"Email không được để trống";
        }
            
        if(phoneNumber.isEmpty()){
            return"Số điện thoại không được để trống";    
        }
        
        if(username.isEmpty()){
            return"Tên đăng nhập không được để trống"; 
        }
        
        if(password.isEmpty()){
            return"Mật khẩu không được để trống"; 
        }
        
        if(!isValidatePhoneNumber(phoneNumber)){
            return "Số điện thoại không hợp lệ";
        }
        return "Đăng nhập hợp lệ";
    }
    public static boolean isValidatePhoneNumber(String phone){
        if (phone == null) return false;
        return phone.matches("\\d{10}");
    }
    
       
    public static boolean validateString(String stringNeedValidate){
        if (stringNeedValidate == null || stringNeedValidate.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    // Đổi thành static boolean
    public static boolean validateAttributesOfWarehouseReceipt(DefaultTableModel itemModel, StockPanel stockPanelView){
        int rowCount = itemModel.getRowCount();
        if (rowCount == 0) {
            JOptionPane.showMessageDialog(stockPanelView, "Phiếu nhập đang trống, vui lòng thêm nguyên liệu!");
            return false;
        }

        for (int i = 0; i < rowCount; i++) {
            String tenNguyenLieu = String.valueOf(itemModel.getValueAt(i, 0)).trim();
            String donViTinh = String.valueOf(itemModel.getValueAt(i, 1)).trim();
            String strSoLuong = String.valueOf(itemModel.getValueAt(i, 2)).trim();
            String strDonGia = String.valueOf(itemModel.getValueAt(i, 3)).trim();
            String strNguong = String.valueOf(itemModel.getValueAt(i, 4)).trim();
            String nhaCungCap = String.valueOf(itemModel.getValueAt(i, 5)).trim();
            String strNgayNhap = String.valueOf(itemModel.getValueAt(i, 6)).trim();

            // --- A. KIỂM TRA RỖNG ---
            if (!validateString(tenNguyenLieu)){
                JOptionPane.showMessageDialog(stockPanelView, "Tên nguyên liệu ở dòng " + (i + 1) + " không được để trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return false; // Dừng lại và báo false
            }
            if (!validateString(donViTinh)) {
                JOptionPane.showMessageDialog(stockPanelView, "Đơn vị tính ở dòng " + (i + 1) + " không được để trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (!validateString(nhaCungCap)) {
                JOptionPane.showMessageDialog(stockPanelView, "Nhà cung cấp ở dòng " + (i + 1) + " không được để trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (!validateString(strNgayNhap)) {
                JOptionPane.showMessageDialog(stockPanelView, "Ngày nhập ở dòng " + (i + 1) + " không được để trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (!validateString(strSoLuong)) {
                JOptionPane.showMessageDialog(stockPanelView, "Số lượng ở dòng " + (i + 1) + " không được để trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (!validateString(strDonGia)) {
                JOptionPane.showMessageDialog(stockPanelView, "Đơn giá ở dòng " + (i + 1) + " không được để trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (!validateString(strNguong)) {
                JOptionPane.showMessageDialog(stockPanelView, "Ngưỡng cảnh báo ở dòng " + (i + 1) + " không được để trống!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // --- B. KIỂM TRA ĐỊNH DẠNG SỐ ---
            try {
                int soLuong = Integer.parseInt(strSoLuong);
                if (soLuong <= 0) {
                    JOptionPane.showMessageDialog(stockPanelView, "Số lượng ở dòng " + (i + 1) + " phải lớn hơn 0!", "Lỗi logic", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(stockPanelView, "Số lượng ở dòng " + (i + 1) + " phải là số nguyên!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            try {
                long donGia = Long.parseLong(strDonGia);
                if (donGia < 0) { 
                    JOptionPane.showMessageDialog(stockPanelView, "Đơn giá ở dòng " + (i + 1) + " không được âm!", "Lỗi logic", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(stockPanelView, "Đơn giá ở dòng " + (i + 1) + " phải là một con số hợp lệ!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            try {
                int nguong = Integer.parseInt(strNguong);
                if (nguong <= 0) { // Sửa lại logic chỗ này của bạn (soLuong <= 0 thành nguong <= 0)
                    JOptionPane.showMessageDialog(stockPanelView, "Ngưỡng ở dòng " + (i + 1) + " phải lớn hơn 0!", "Lỗi logic", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(stockPanelView, "Ngưỡng ở dòng " + (i + 1) + " phải là số nguyên!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            try {
                LocalDate.parse(strNgayNhap.trim());
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(stockPanelView, "Ngày nhập ở dòng " + (i + 1) + " sai định dạng yyyy-MM-dd!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return false; 
            }
        }

        // Vượt qua vòng lặp mà không có lỗi nào -> Trả về true
        return true; 
    }
    
    public static boolean validateSession() {
        if (!SessionManager.isLoggedIn()) {
            JOptionPane.showMessageDialog(null, "Chưa đăng nhập!");
            return false;
        }

        if (!sessionService.isValid(SessionManager.getToken())) {
            JOptionPane.showMessageDialog(null, "Phiên đăng nhập đã hết hạn!");
            SessionManager.clear();
            return false;
        }

        return true;
    }
    
    public static String validateProductDetail(String productName, double basicPrice, String category, String status){
        if (productName == null || productName.equals("")) {
            return "Tên sản phẩm không được để trống";
        }
        if (category == null || category.equals("")) {
            return "Loại sản phẩm không được để trống";
        }
        if (status == null || status.equals("")) {
            return "Trạng thái không được để trống";
        }
        if(basicPrice == 0.0){
            return "Giá tiền cơ bản không được để trống";
        }
        return "Hợp lệ";
    }
}
