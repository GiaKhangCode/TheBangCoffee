package Service;

import DatabaseAccessObject.OrderDAO;
import Model.CartItemModel;
import Model.CustomerModel;
import Model.DiscountResultModel;
import Model.IngredientModel;
import Model.OrderDetailModel;
import Model.OrderModel;
import Model.RecipeModel;
import Model.ToppingModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {
    
    private OrderDAO orderDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
    }

    public int createOrder(int accountId, Integer maKhachHang, List<CartItemModel> cart, long finalTotal, double totalVat, String prepStatus, String payStatus, boolean isTakeaway, boolean isHoliday, int pointsEarned, int pointsUsed, long discountAmount) {
        if (cart == null || cart.isEmpty()) {
            return -1;
        }
 
        return orderDAO.createOrder(accountId, maKhachHang, cart, finalTotal, totalVat, prepStatus, payStatus, isTakeaway, isHoliday, pointsEarned, pointsUsed, discountAmount);
    }
    
    public DiscountResultModel calculateOrderDiscount(String pointsText, CustomerModel currentCustomer, long totalBill, int diemDoiMotLy, int giaTriMotDiem, int tienTichMotDiem, List<CartItemModel> cart) {
        DiscountResultModel result = new DiscountResultModel();
        
        long discountAmount = 0;
        int pointsUsed = 0;
        long tierDiscountAmount = 0;
        int earnedPoints = 0;

        if (pointsText != null && !pointsText.trim().isEmpty() && currentCustomer != null) {
            try {
                int p = Integer.parseInt(pointsText.trim());
                if (p <= 0) {
                    result.setErrorMessage("Vui lòng nhập số > 0");
                } else if (p > currentCustomer.getDiemHienTai()) {
                    result.setErrorMessage("Vượt quá số điểm hiện tại!");
                } else {
                    List<Long> eligiblePrices = new ArrayList<>();
                    if (cart != null) {
                        for (CartItemModel item : cart) {
                            if (item.getTotalPrice() > 0 && item.getQuantity() > 0) {
                                long unitPrice = item.getUnitPrice();
                                for (int i = 0; i < item.getQuantity(); i++) {
                                    eligiblePrices.add(unitPrice);
                                }
                            }
                        }
                        Collections.sort(eligiblePrices);
                    }
                    
                    int maxDrinksInCart = eligiblePrices.size();
                    int potentialFreeDrinks = p / diemDoiMotLy;
                    int actualFreeDrinks = Math.min(potentialFreeDrinks, maxDrinksInCart);
                    
                    int pointsUsedForDrinks = actualFreeDrinks * diemDoiMotLy;
                    int leftoverPoints = p - pointsUsedForDrinks;
                    
                    long drinksDiscount = 0;
                    for (int i = 0; i < actualFreeDrinks; i++) {
                        drinksDiscount += eligiblePrices.get(i);
                    }
                    
                    long cashDiscount = (long) leftoverPoints * giaTriMotDiem;
                    long discount = drinksDiscount + cashDiscount;
                    
                    if (discount > totalBill) {
                        discount = totalBill;
                    }
                    
                    pointsUsed = p;
                    discountAmount = discount;
                    
                    if (actualFreeDrinks > 0 || cashDiscount > 0) {
                        String msg = "";
                        if (actualFreeDrinks > 0) msg += "Quy đổi: " + actualFreeDrinks + " ly miễn phí";
                        if (cashDiscount > 0) msg += (msg.isEmpty() ? "Giảm: " : " và giảm thêm ") + String.format("%,d đ", cashDiscount);
                        result.setSuccessMessage(msg);
                    }
                }
            } catch (NumberFormatException ex) {
                result.setErrorMessage("Dữ liệu không hợp lệ!");
            }
        }
        
        result.setPointsUsed(pointsUsed);
        result.setDiscountAmount(discountAmount);
        
        long remainingForTier = totalBill - discountAmount;
        if (remainingForTier > 0 && currentCustomer != null && currentCustomer.getPhanTramChietKhau() > 0) {
            tierDiscountAmount = (long) (remainingForTier * (currentCustomer.getPhanTramChietKhau() / 100.0));
        }
        result.setTierDiscountAmount(tierDiscountAmount);
        
        if (currentCustomer != null && tienTichMotDiem > 0) {
            long paid = totalBill - discountAmount - tierDiscountAmount;
            if (paid > 0) {
                earnedPoints = (int) (paid / tienTichMotDiem);
            }
        }
        result.setEarnedPoints(earnedPoints);
        
        return result;
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

    public boolean updatePreparationStatus(int orderId, String newStatus) {
        return orderDAO.updatePreparationStatus(orderId, newStatus);
    }

    public boolean updatePaymentStatus(int orderId, String newStatus, String phuongThucThanhToan) {
        return orderDAO.updatePaymentStatus(orderId, newStatus, phuongThucThanhToan);
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
    
    // Cập nhật giá 1 món
    public boolean updateOrderDetailPrice(int detailId, int orderId, long newPrice) {
        return orderDAO.updateOrderDetailPrice(detailId, orderId, newPrice);
    }
}