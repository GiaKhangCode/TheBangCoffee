package Service;

import DatabaseAccessObject.OrderDAO;
import Model.CartItemModel;
import Model.IngredientModel;
import Model.OrderDetailModel;
import Model.OrderModel;
import Model.RecipeModel;
import Model.ToppingModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {
    
    private OrderDAO orderDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
    }

    // [SỬA] Thêm isTakeaway và isHoliday vào tham số
    public boolean createOrder(int accountId, List<CartItemModel> cart, long finalTotal, double totalVat, String status, boolean isTakeaway, boolean isHoliday) {
        // Có thể thêm các rule Validate ở đây trước khi đẩy xuống DAO
        if (cart == null || cart.isEmpty()) {
            return false;
        }
        
        return orderDAO.createOrder(accountId, cart, finalTotal, totalVat, status, isTakeaway, isHoliday);
    }
    
    public String validateInventory(List<CartItemModel> cart) {
        // Map 1: Gom nhóm ID Nguyên Liệu -> Tổng số lượng cần thiết
        java.util.Map<Integer, Double> requiredIngredients = new java.util.HashMap<>();
        
        // Map 2: Gom nhóm ID Nguyên Liệu -> Danh sách TÊN MÓN bị ảnh hưởng
        java.util.Map<Integer, java.util.Set<String>> ingredientToProductsMap = new java.util.HashMap<>();
        
        Service.RecipeService recipeService = new Service.RecipeService();
        DatabaseAccessObject.IngredientDAO ingredientDAO = new DatabaseAccessObject.IngredientDAO();

        // 1. Duyệt qua từng món trong giỏ hàng để cộng dồn định lượng và ghi nhận Tên món
        for (CartItemModel item : cart) {
            int qty = item.getQuantity(); 
            String productName = item.getProduct().getProductName(); // Lấy tên món nước

            // A. Tính nguyên liệu từ Công thức của Món chính
            int variantId = item.getSelectedVariant() != null ? item.getSelectedVariant().getVariantID() : 0;
            if (variantId > 0) {
                List<Model.RecipeModel> recipes = recipeService.getRecipeByVariantId(variantId);
                if (recipes != null) {
                    for (Model.RecipeModel r : recipes) {
                        int ingId = r.getIngredientID();
                        double needed = r.getQuantityRequired() * qty;
                        
                        // Cộng dồn số lượng
                        requiredIngredients.put(ingId, requiredIngredients.getOrDefault(ingId, 0.0) + needed);
                        
                        // Ghi nhận món nước này sử dụng nguyên liệu ingId
                        ingredientToProductsMap.computeIfAbsent(ingId, k -> new java.util.HashSet<>()).add(productName);
                    }
                }
            }

            // B. Tính nguyên liệu hao hụt từ Topping
            if (item.getSelectedToppings() != null) {
                for (Model.ToppingModel top : item.getSelectedToppings()) {
                    int ingId = top.getIngredientID(); 
                    double loss = top.getLossAmount(); 
                    
                    if (ingId > 0 && loss > 0) {
                        double needed = loss * qty;
                        
                        // Cộng dồn số lượng
                        requiredIngredients.put(ingId, requiredIngredients.getOrDefault(ingId, 0.0) + needed);
                        
                        // Ghi nhận Topping của món này sử dụng nguyên liệu ingId
                        String affectedItem = productName + " (Thêm " + top.getLabel() + ")";
                        ingredientToProductsMap.computeIfAbsent(ingId, k -> new java.util.HashSet<>()).add(affectedItem);
                    }
                }
            }
        }

        // 2. Đối chiếu giỏ hàng với Tồn kho thực tế và xây dựng thông báo lỗi
        StringBuilder errorMsg = new StringBuilder();
        for (java.util.Map.Entry<Integer, Double> entry : requiredIngredients.entrySet()) {
            int ingId = entry.getKey();
            double totalNeeded = entry.getValue();

            Model.IngredientModel ing = ingredientDAO.getIngredientById(ingId);
            if (ing != null) {
                // Nếu nhu cầu > Tồn kho -> Phát hiện thiếu hàng
                if (totalNeeded > ing.getInStock()) {
                    // Rút trích danh sách Tên món bị ảnh hưởng
                    java.util.Set<String> affectedProducts = ingredientToProductsMap.get(ingId);
                    String productsStr = String.join(", ", affectedProducts); // Nối các món bằng dấu phẩy

                    errorMsg.append("Thiếu [").append(ing.getIngredientName()).append("]\n")
                            .append("   - Tồn kho chỉ còn: ").append(ing.getInStock()).append(" ").append(ing.getUnit()).append("\n")
                            .append("   - Tổng cần cho đơn: ").append(totalNeeded).append(" ").append(ing.getUnit()).append("\n")
                            .append(" Không đủ nguyên liệu để pha chế: ").append(productsStr).append("\n\n");
                }
            }
        }

        return errorMsg.length() > 0 ? errorMsg.toString() : null;
    }
    
    public List<OrderModel> getAllOrders(String statusFilter, String keyword) {
        return orderDAO.getAllOrders(statusFilter, keyword);
    }

    public List<OrderDetailModel> getOrderDetailsByOrderId(int orderId) {
        return orderDAO.getOrderDetailsByOrderId(orderId);
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        return orderDAO.updateStatus(orderId, newStatus);
    }

    // Hàm then chốt: Vừa đổi trạng thái vừa trừ kho
    public boolean completeOrderAndDeductInventory(int orderId) {
        return orderDAO.completeAndDeductStock(orderId);
    }
    
    // Thêm hàm bổ trợ lấy 1 đơn hàng từ list (để đỡ phải query DB nhiều lần)
    public OrderModel getOrderFromList(List<OrderModel> list, int id) {
        return list.stream().filter(o -> o.getOrderId() == id).findFirst().orElse(null);
    }
    
    // Lấy 1 đơn hàng theo ID
    public OrderModel getOrderById(int orderId) {
        return orderDAO.getOrderById(orderId);
    }
}