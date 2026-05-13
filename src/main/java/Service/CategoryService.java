package Service;

import DatabaseAccessObject.CategoryDAO;
import Model.CategoryModel;
import java.sql.SQLException;
import java.util.List;

public class CategoryService {
    private CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    public List<CategoryModel> getAllCategories() throws SQLException, ClassNotFoundException {
        List<CategoryModel> list = categoryDAO.getAllCategories();
        
        list.sort((c1, c2) -> {
            int status1 = c1.getCategoryStatus().equals("Đang sử dụng") ? 0 : 1;
            int status2 = c2.getCategoryStatus().equals("Đang sử dụng") ? 0 : 1;
            return Integer.compare(status1, status2);
        });
        
        return list;
    }

    public String addCategory(String name, double vat) throws SQLException, ClassNotFoundException {
        if (name == null || name.trim().isEmpty()) return "Tên danh mục không được để trống!";
        if (vat < 0 || vat > 100) return "Thuế VAT phải từ 0 - 100%!";
        if (categoryDAO.checkDuplicateName(name.trim(), -1)) return "Tên danh mục đã tồn tại!";
        
        CategoryModel cat = new CategoryModel(0, name.trim(), "Đang sử dụng", vat);
        if (categoryDAO.addCategory(cat)) return "SUCCESS";
        return "Lỗi hệ thống khi thêm danh mục!";
    }

    public String updateCategory(int id, String name, double vat) throws SQLException, ClassNotFoundException {
        if (id <= 0) return "Lỗi định dạng ID!";
        if (name == null || name.trim().isEmpty()) return "Tên danh mục không được để trống!";
        if (vat < 0 || vat > 100) return "Thuế VAT phải từ 0 - 100%!";
        if (categoryDAO.checkDuplicateName(name.trim(), id)) return "Tên danh mục đã tồn tại!";
        
        CategoryModel cat = new CategoryModel(id, name.trim(), "", vat);
        if (categoryDAO.updateCategory(cat)) return "SUCCESS";
        return "Lỗi hệ thống khi cập nhật danh mục!";
    }

    public String toggleCategoryStatus(int id, String currentStatus) throws SQLException, ClassNotFoundException {
        if (id <= 0) return "Lỗi định dạng ID!";
        
        // Trả lại trạng thái chuẩn là "Đã ẩn" (Bỏ dấu ngoặc dư ở lần fix nhầm trước)
        String newStatus = currentStatus.equals("Đang sử dụng") ? "Đã ẩn" : "Đang sử dụng";
        
        if (categoryDAO.updateCategoryStatus(id, newStatus)) return "SUCCESS";
        return "Lỗi hệ thống khi cập nhật trạng thái!";
    }
}