package Controller;

import Model.ToppingModel;
import Service.IngredientService;
import Service.ToppingService;
import View.ToppingManagementPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ToppingController {
    private ToppingManagementPanel view;
    private ToppingService toppingService;
    private IngredientService ingredientService;
    private List<ToppingModel> allToppings;

    public ToppingController(ToppingManagementPanel view) {
        this.view = view;
        this.toppingService = new ToppingService();
        this.ingredientService = new IngredientService();
        
        initListeners();
        loadData();
    }

    private void loadData() {
        allToppings = toppingService.getAllToppings();
        view.loadDataToTable(allToppings);
    }

    private void initListeners() {
        // --- 1. TÌM KIẾM ---
        view.addSearchListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String keyword = view.getSearchText().toLowerCase();
                if (keyword.equals("tìm kiếm topping...")) keyword = "";
                
                List<ToppingModel> filtered = new ArrayList<>();
                for (ToppingModel t : allToppings) {
                    if (t.getToppingName().toLowerCase().contains(keyword)) {
                        filtered.add(t);
                    }
                }
                view.loadDataToTable(filtered);
            }
        });

        // --- 2. THÊM TOPPING ---
        view.addAddButtonListener(e -> {
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            JTextField txtName = new JTextField();
            JTextField txtPrice = new JTextField("0"); 
            JComboBox<String> cbIng = new JComboBox<>();
            
            List<String> ingredients = ingredientService.getIngredientNames();
            for (String ing : ingredients) cbIng.addItem(ing);
            
            JTextField txtLoss = new JTextField("0");
            JTextField txtVat = new JTextField("8");
            
            panel.add(new JLabel("Tên Topping (VD: Trân châu trắng):")); panel.add(txtName);
            panel.add(new JLabel("Giá Bán (VNĐ):")); panel.add(txtPrice);
            panel.add(new JLabel("Nguyên liệu liên kết (Bị trừ khi bán):")); panel.add(cbIng);
            panel.add(new JLabel("Định lượng trừ (Hao hụt):")); panel.add(txtLoss);
            panel.add(new JLabel("Thuế VAT (%):")); panel.add(txtVat);

            int result = JOptionPane.showConfirmDialog(view, panel, "Thêm Topping Mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String topName = txtName.getText().trim();
                if (topName.isEmpty()) return;
                try {
                    long price = Long.parseLong(txtPrice.getText().trim().replace(",", ""));
                    double loss = Double.parseDouble(txtLoss.getText().trim().replace(",", ""));
                    double vat = Double.parseDouble(txtVat.getText().trim().replace(",", ""));
                    int ingId = ingredientService.getIngredientIdByName((String) cbIng.getSelectedItem());
                    
                    if (toppingService.addTopping(topName, price, ingId, loss, vat)) {
                        JOptionPane.showMessageDialog(view, "Thêm Topping thành công!");
                        loadData(); // Cập nhật lại bảng
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(view, "Giá, Hao hụt và Thuế phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- 3. SỬA / XÓA TOPPING TRÊN BẢNG ---
        view.setActionListener(new ToppingManagementPanel.ActionButtonListener() {
            @Override
            public void onEdit(int row) {
                int selectedId = view.getToppingIdAt(row);
                String currentName = view.getToppingNameAt(row);
                long currentPrice = view.getToppingPriceAt(row);
                int currentIngId = view.getIngredientIdAt(row);
                double currentLoss = view.getLossAmountAt(row);
                double currentVat = view.getVatAt(row);

                JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
                JTextField txtName = new JTextField(currentName);
                JTextField txtPrice = new JTextField(String.valueOf(currentPrice)); 
                JComboBox<String> cbIng = new JComboBox<>();
                
                String selectedIngName = null;
                List<String> ingredients = ingredientService.getIngredientNames();
                for (String ing : ingredients) {
                    cbIng.addItem(ing);
                    if (ingredientService.getIngredientIdByName(ing) == currentIngId) {
                        selectedIngName = ing;
                    }
                }
                if (selectedIngName != null) cbIng.setSelectedItem(selectedIngName);

                JTextField txtLoss = new JTextField(String.valueOf(currentLoss));
                JTextField txtVat = new JTextField(String.valueOf(currentVat));
                
                panel.add(new JLabel("Tên Topping (VD: Trân châu trắng):")); panel.add(txtName);
                panel.add(new JLabel("Giá Bán (VNĐ):")); panel.add(txtPrice);
                panel.add(new JLabel("Nguyên liệu liên kết (Bị trừ khi bán):")); panel.add(cbIng);
                panel.add(new JLabel("Định lượng trừ (Hao hụt):")); panel.add(txtLoss);
                panel.add(new JLabel("Thuế VAT (%):")); panel.add(txtVat);

                int result = JOptionPane.showConfirmDialog(view, panel, "Sửa Topping", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String topName = txtName.getText().trim();
                    if (topName.isEmpty()) {
                        JOptionPane.showMessageDialog(view, "Tên Topping không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        long price = Long.parseLong(txtPrice.getText().trim().replace(",", ""));
                        double loss = Double.parseDouble(txtLoss.getText().trim().replace(",", ""));
                        double vat = Double.parseDouble(txtVat.getText().trim().replace(",", ""));
                        int ingId = ingredientService.getIngredientIdByName((String) cbIng.getSelectedItem());
                        
                        if (toppingService.updateTopping(selectedId, topName, price, ingId, loss, vat)) {
                            JOptionPane.showMessageDialog(view, "Cập nhật Topping thành công!");
                            loadData();
                        } else {
                            JOptionPane.showMessageDialog(view, "Cập nhật thất bại do lỗi CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(view, "Giá bán, Hao hụt và Thuế phải là chữ số hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            @Override
            public void onDelete(int row) {
                int selectedId = view.getToppingIdAt(row);
                if (JOptionPane.showConfirmDialog(view, "Xóa Topping này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (toppingService.deleteTopping(selectedId)) {
                        JOptionPane.showMessageDialog(view, "Xóa thành công!"); 
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(view, "Lỗi! Topping này đang được sử dụng trong hóa đơn hoặc được liên kết với món ăn.", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }
}