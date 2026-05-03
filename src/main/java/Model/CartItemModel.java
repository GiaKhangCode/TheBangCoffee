package Model;

import java.util.List;
import java.util.ArrayList;

public class CartItemModel {
    private String cartItemId; 
    private ProductModel product;
    private VariantModel selectedVariant;
    private List<ToppingModel> selectedToppings;
    private int quantity;
    
    // [MỚI] Thêm biến lưu ghi chú (Đá, Đường, Ghi chú tự nhập)
    private String note;
    
    // Thêm trạng thái để biết nên tính tiền theo giá nào
    private boolean isTakeaway = false;
    private boolean isHoliday = false;

    // [SỬA] Thêm tham số note vào constructor
    public CartItemModel(ProductModel product, VariantModel selectedVariant, List<ToppingModel> selectedToppings, int quantity, String note) {
        this.cartItemId = java.util.UUID.randomUUID().toString(); 
        this.product = product;
        this.selectedVariant = selectedVariant;
        this.selectedToppings = selectedToppings != null ? selectedToppings : new ArrayList<>();
        this.quantity = quantity;
        this.note = note != null ? note : "";
    }
    
    // Hàm cập nhật lại trạng thái đơn hàng cho từng món
    public void setOrderType(boolean isTakeaway, boolean isHoliday) {
        this.isTakeaway = isTakeaway;
        this.isHoliday = isHoliday;
    }

    // Hàm lấy giá chính (Main Price) dựa trên trạng thái (Lễ / Mang đi / Tại quán)
    public long getMainSellingPrice() {
        if (selectedVariant != null) {
            if (isHoliday) return selectedVariant.getHolidayPrice();
            if (isTakeaway) return selectedVariant.getTakeawayPrice();
            return selectedVariant.getDineInPrice();
        } else {
            if (isHoliday) return product.getHolidayPrice();
            if (isTakeaway) return product.getTakeawayPrice();
            return product.getDineInPrice();
        }
    }

    // Tính giá của 1 ly (Giá Size + Tổng giá Topping)
    public long getUnitPrice() {
        long basePrice = getMainSellingPrice();
        long toppingsPrice = 0;
        for (ToppingModel t : selectedToppings) {
            toppingsPrice += t.getPrice();
        }
        return basePrice + toppingsPrice;
    }
    
    public double getMainVatAmount() {
        long basePrice = getMainSellingPrice();
        // Giả sử product.getVat() trả về 8.0 (tương đương 8%)
        double vatRate = product.getVat(); 
        
        // Công thức: Giá Bán - (Giá Bán / (1 + Rate))
        double priceBeforeTax = basePrice / (1.0 + (vatRate / 100.0));
        return (basePrice - priceBeforeTax) * quantity;
    }
    
    public double getToppingsVatAmount() {
        double totalToppingVat = 0;
        for (ToppingModel t : selectedToppings) {
            double vatRate = t.getVat(); 
            double priceBeforeTax = t.getPrice() / (1.0 + (vatRate / 100.0));
            totalToppingVat += (t.getPrice() - priceBeforeTax);
        }
        return totalToppingVat * quantity;
    }

    // Tính tổng tiền cho dòng này
    public long getTotalPrice() {
        return getUnitPrice() * quantity;
    }
    
    public double getTotalVatAmount() {
        return getMainVatAmount() + getToppingsVatAmount();
    }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><b>").append(product.getProductName()).append("</b><br>");
        sb.append("<small style='color:gray'>");
        if (selectedVariant != null) sb.append("Size ").append(selectedVariant.getSizeName());
        
        if (!selectedToppings.isEmpty()) {
            sb.append(", ");
            for (int i = 0; i < selectedToppings.size(); i++) {
                sb.append(selectedToppings.get(i).getToppingName());
                if (i < selectedToppings.size() - 1) sb.append(", ");
            }
        }
        sb.append("</small>");
        
        // In thêm ghi chú Đá/Đường phía dưới
        if (!note.isEmpty()) {
            // Thay thế ký tự " | Ghi chú:" thành thẻ "<br>Ghi chú:" để ép xuống dòng
            String formattedNote = note.replace(" | Ghi chú:", "<br>Ghi chú:");
            sb.append("<br><i style='color:#e67e22'>").append(formattedNote).append("</i>");
        }
        
        sb.append("</html>");
        return sb.toString();
    }

    // Getters & Setters
    public String getCartItemId() { return cartItemId; }
    public ProductModel getProduct() { return product; }
    public VariantModel getSelectedVariant() { return selectedVariant; }
    public List<ToppingModel> getSelectedToppings() { return selectedToppings; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public String getNote() { return note; }
    
    // [SỬA LẠI] Thêm tham số otherNote để so sánh
    public boolean isSameItem(ProductModel otherProduct, VariantModel otherVariant, List<ToppingModel> otherToppings, String otherNote) {
        if (this.product.getProductID() != otherProduct.getProductID()) return false;

        if (this.selectedVariant == null && otherVariant != null) return false;
        if (this.selectedVariant != null && otherVariant == null) return false;
        if (this.selectedVariant != null && otherVariant != null) {
            if (this.selectedVariant.getVariantID() != otherVariant.getVariantID()) return false;
        }

        if (!this.note.equals(otherNote != null ? otherNote : "")) return false;

        if (this.selectedToppings.size() != otherToppings.size()) return false;

        List<Integer> thisToppingIds = new ArrayList<>();
        for (ToppingModel t : this.selectedToppings) thisToppingIds.add(t.getToppingID());

        List<Integer> otherToppingIds = new ArrayList<>();
        for (ToppingModel t : otherToppings) otherToppingIds.add(t.getToppingID());

        if (!thisToppingIds.containsAll(otherToppingIds) || !otherToppingIds.containsAll(thisToppingIds)) {
            return false;
        }
        return true;
    }
}