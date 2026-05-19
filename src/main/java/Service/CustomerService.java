package Service;

import DatabaseAccessObject.CustomerDAO;
import Model.CustomerModel;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerService {
    private CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public CustomerModel processCustomerForOrder(String phone, String name) throws SQLException, ClassNotFoundException {
        if (phone == null || phone.trim().isEmpty()) {
            return null; 
        }

        CustomerModel existingCustomer = customerDAO.findCustomerByPhone(phone);
        if (existingCustomer != null) {
            return existingCustomer; 
        }

        if (name == null || name.trim().isEmpty()) {
            name = "Khách Hàng"; 
        }

        CustomerModel newCustomer = new CustomerModel(0, phone, name, 0, 0, "Mới");
        int newId = customerDAO.insertAndGetId(newCustomer);
        
        if (newId > 0) {
            newCustomer.setMaKH(newId); 
            return newCustomer;
        }
        return null; 
    }
    
    public CustomerModel findCustomerByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        return customerDAO.findCustomerByPhone(phone);
    }
    
    public CustomerModel registerNewCustomer(String phone, String name) throws SQLException, ClassNotFoundException {
        if (phone == null || phone.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            return null;
        }
        
        CustomerModel newCustomer = new CustomerModel(0, phone, name, 0, 0, "Mới");
        int newId = customerDAO.insertAndGetId(newCustomer);
        
        if (newId > 0) {
            newCustomer.setMaKH(newId);
            return newCustomer;
        }
        return null;
    }

    public void addPointsToCustomerByOrderId(int orderId, int pointsToAdd) {
        customerDAO.addPointsToCustomerByOrderId(orderId, pointsToAdd);
    }

    public void refundPointsToCustomerByOrderId(int orderId, int pointsToRefund) {
        customerDAO.refundPointsToCustomerByOrderId(orderId, pointsToRefund);
    }

    public ResultSet getAllTiers() throws SQLException, ClassNotFoundException {
        return customerDAO.getAllTiers();
    }

    public void saveTier(int maHang, String tenHang, int diemYeuCau) throws SQLException, ClassNotFoundException {
        customerDAO.saveTier(maHang, tenHang, diemYeuCau);
    }

    public String deleteTier(int maHang) throws SQLException, ClassNotFoundException {
        return customerDAO.deleteTier(maHang);
    }

    public String syncTiers() throws SQLException, ClassNotFoundException {
        return customerDAO.syncTiers();
    }

    public int[] getPointRule() throws SQLException, ClassNotFoundException {
        return customerDAO.getPointRule();
    }

    // [CẬP NHẬT] Thêm tham số diemDoiLy
    public void updatePointRule(int tienTich, int giaTri, int diemDoiLy) throws SQLException, ClassNotFoundException {
        customerDAO.updatePointRule(tienTich, giaTri, diemDoiLy);
    }
    
    public ResultSet getAllCustomers() throws SQLException, ClassNotFoundException {
        return customerDAO.getAllCustomers();
    }
}