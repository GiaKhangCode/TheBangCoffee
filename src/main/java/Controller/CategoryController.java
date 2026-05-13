package Controller;

import Model.CategoryModel;
import Service.CategoryService;
import View.CategoryManagementPanel;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;

public class CategoryController {
    private CategoryManagementPanel view;
    private CategoryService service;
    private List<CategoryModel> currentList;

    public CategoryController(CategoryManagementPanel view) {
        this.view = view;
        this.service = new CategoryService();

        initListeners();
        loadData();
    }

    private void loadData() {
        try {
            currentList = service.getAllCategories();
            view.loadDataToTable(currentList);
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi tải dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initListeners() {
        // 1. Xử lý sự kiện "Thêm Mới"
        view.setAddAction((name, vat) -> {
            try {
                String msg = service.addCategory(name, vat);
                if (msg.equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(view, "Thêm danh mục thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(view, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        // 2. Xử lý sự kiện "Cập nhật (Sửa)" từ nút trên bảng
        view.setEditAction((id, newName, newVat) -> {
            try {
                String msg = service.updateCategory(id, newName, newVat);
                if (msg.equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(view, "Cập nhật danh mục thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(view, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        // 3. Xử lý sự kiện "Đổi trạng thái (Ẩn/Sử dụng lại)" từ nút trên bảng
        view.setToggleStatusAction((id, currentStatus) -> {
            String actionName = currentStatus.equals("Đang sử dụng") ? "Tạm ngừng sử dụng" : "Sử dụng lại";
            int confirm = JOptionPane.showConfirmDialog(view, 
                "Bạn có chắc muốn " + actionName + " danh mục này không?", 
                "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String msg = service.toggleCategoryStatus(id, currentStatus);
                    if (msg.equals("SUCCESS")) {
                        loadData(); // Tải lại bảng để cập nhật màu sắc/vị trí dòng
                    } else {
                        JOptionPane.showMessageDialog(view, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}