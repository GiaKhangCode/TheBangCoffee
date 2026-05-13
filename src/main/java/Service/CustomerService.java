package Service;

import DatabaseAccessObject.CustomerDAO;
import Model.CustomerModel;
import java.sql.SQLException;

public class CustomerService {
    private CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public CustomerModel processCustomerForOrder(String phone, String name) throws SQLException, ClassNotFoundException {
        // 1. Khách vãng lai (Không nhập SĐT)
        if (phone == null || phone.trim().isEmpty()) {
            return null; 
        }

        // 2. Kiểm tra xem khách đã tồn tại chưa
        CustomerModel existingCustomer = customerDAO.findCustomerByPhone(phone);
        if (existingCustomer != null) {
            return existingCustomer; // Trả về khách cũ
        }

        // 3. Khách mới: Yêu cầu phải có tên
        if (name == null || name.trim().isEmpty()) {
            name = "Khách Hàng"; // Tên mặc định nếu thu ngân quên nhập
        }

        CustomerModel newCustomer = new CustomerModel(0, phone, name, 0, "Đồng");
        int newId = customerDAO.insertAndGetId(newCustomer);
        
        if (newId > 0) {
            newCustomer.setMaKH(newId); // Cập nhật ID cho object
            return newCustomer;
        }

        return null; // Lỗi CSDL
    }
    
    // Thêm hàm này vào CustomerService.java
    public CustomerModel findCustomerByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        return customerDAO.findCustomerByPhone(phone);
    }
    
    // Thêm hàm này vào CustomerService.java
    public CustomerModel registerNewCustomer(String phone, String name) throws SQLException, ClassNotFoundException {
        if (phone == null || phone.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            return null;
        }
        
        // Tạo model khách hàng với điểm mặc định = 0, hạng "Mới"
        CustomerModel newCustomer = new CustomerModel(0, phone, name, 0, "Mới");
        int newId = customerDAO.insertAndGetId(newCustomer);
        
        if (newId > 0) {
            newCustomer.setMaKH(newId);
            return newCustomer;
        }
        return null;
    }
}