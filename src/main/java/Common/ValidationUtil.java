/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Common;

import Model.SessionManager;
import Service.SessionService;
import View.ProductEditDialog;
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
            JOptionPane.showMessageDialog(stockPanelView, "Phiếu nhập trống!"); return false;
        }
 
        for (int i = 0; i < rowCount; i++) {
            String category = String.valueOf(itemModel.getValueAt(i, 0)).trim();
            String name = String.valueOf(itemModel.getValueAt(i, 1)).trim();
            String unit = String.valueOf(itemModel.getValueAt(i, 2)).trim(); //
            String strCapacity = String.valueOf(itemModel.getValueAt(i, 3)).trim();
            String strQuantity = String.valueOf(itemModel.getValueAt(i, 4)).trim();
            String strTotal = String.valueOf(itemModel.getValueAt(i, 5)).trim();
            String strThres = String.valueOf(itemModel.getValueAt(i, 6)).trim();
            String provider = String.valueOf(itemModel.getValueAt(i, 7)).trim();
            String importingDate = String.valueOf(itemModel.getValueAt(i, 8)).trim();
            String expiryDate = String.valueOf(itemModel.getValueAt(i, 9)).trim();
 
            // <-- ĐÃ THÊM !validateString(unit) vào điều kiện kiểm tra rỗng
            if (!validateString(category) || !validateString(name) || !validateString(unit) || !validateString(provider) || !validateString(importingDate) || !validateString(expiryDate)) {
                JOptionPane.showMessageDialog(stockPanelView, "Vui lòng điền đủ thông tin chữ ở dòng " + (i+1), "Lỗi", JOptionPane.ERROR_MESSAGE); return false;
            }
 
            try {
                int capacity = Integer.parseInt(strCapacity);
                int quantity = Integer.parseInt(strQuantity);
                long totalPrice = Long.parseLong(strTotal);
                int threshold = Integer.parseInt(strThres);
 
                if (capacity <= 0 || quantity <= 0 || threshold < 0 || totalPrice < 0) {
                    JOptionPane.showMessageDialog(stockPanelView, "Các số liệu ở dòng " + (i+1) + " phải hợp lệ (>0)!", "Lỗi logic", JOptionPane.ERROR_MESSAGE); return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(stockPanelView, "Dòng " + (i+1) + " chứa định dạng số không hợp lệ!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE); return false;
            }
            try { 
                LocalDate.parse(importingDate); 
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(stockPanelView, "Ngày nhập ở dòng " + (i+1) + " phải là định dạng yyyy-MM-dd!", "Lỗi ngày", JOptionPane.ERROR_MESSAGE); return false;
            }
            try { 
                LocalDate.parse(expiryDate); 
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(stockPanelView, "Hạn sử dụng ở dòng " + (i+1) + " phải là định dạng yyyy-MM-dd!", "Lỗi ngày", JOptionPane.ERROR_MESSAGE); return false;
            }
        }
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
    public static boolean validateAddCategory(String newCategoryName){
        return newCategoryName != null && !newCategoryName.trim().isEmpty();
    }
    
    public static boolean validateAddOptionGroup(String newGroupName){
        return (newGroupName != null && !newGroupName.trim().isEmpty());
    }
    
    public static void validateAddRecipe(String ingName, double quantitative, ProductEditDialog editDialog){
        if (ingName == null || ingName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(editDialog, "Vui lòng chọn nguyên liệu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (quantitative <= 0) {
            JOptionPane.showMessageDialog(editDialog, "Định lượng phải là số lớn hơn 0!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
    }
}
