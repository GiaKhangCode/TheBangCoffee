package Controller;

import Model.CategoryModel;
import Service.CategoryService;
import View.CategoryManagementPanel;
import View.MainFrame;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class CategoryController {
    private CategoryManagementPanel view;
    private CategoryService service;
    private List<CategoryModel> currentList;
    private MainFrame mainFrame; 

    public CategoryController(CategoryManagementPanel view, MainFrame mainFrame) {
        this.view = view;
        this.service = new CategoryService();
        this.mainFrame = mainFrame; 

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
        // --- 1. Lắng nghe ô Tìm kiếm ---
        view.addSearchListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String keyword = view.getSearchText().toLowerCase();
                if (keyword.equals("tìm kiếm danh mục...")) keyword = "";
                
                List<CategoryModel> filtered = new ArrayList<>();
                for (CategoryModel c : currentList) {
                    if (c.getCategoryName().toLowerCase().contains(keyword)) {
                        filtered.add(c);
                    }
                }
                view.loadDataToTable(filtered);
            }
        });

        // --- 2. Thêm mới ---
        view.setAddAction((name, vat) -> {
            try {
                String msg = service.addCategory(name, vat);
                if (msg.equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(view, "Thêm danh mục thành công!");
                    loadData();
                    triggerPosReload(); 
                } else {
                    JOptionPane.showMessageDialog(view, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        // --- 3. Sửa danh mục ---
        view.setEditAction((id, newName, newVat) -> {
            try {
                String msg = service.updateCategory(id, newName, newVat);
                if (msg.equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(view, "Cập nhật danh mục thành công!");
                    loadData();
                    triggerPosReload(); 
                } else {
                    JOptionPane.showMessageDialog(view, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        // --- 4. Ẩn / Hiện danh mục ---
        view.setToggleStatusAction((id, currentStatus) -> {
            String actionName = currentStatus.equals("Đang sử dụng") ? "Tạm ngừng sử dụng" : "Sử dụng lại";
            int confirm = JOptionPane.showConfirmDialog(view, 
                "Bạn có chắc muốn " + actionName + " danh mục này không?", 
                "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String msg = service.toggleCategoryStatus(id, currentStatus);
                    if (msg.equals("SUCCESS")) {
                        loadData(); 
                        triggerPosReload(); 
                    } else {
                        JOptionPane.showMessageDialog(view, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void triggerPosReload() {
        if (mainFrame != null && mainFrame.getPosController() != null) {
            mainFrame.getPosController().reloadPosData();
        }
    }
}