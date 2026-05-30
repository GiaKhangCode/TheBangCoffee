package Model;
 
import java.util.List;
import java.util.ArrayList;
 
public class CartItemModel {
    private String cartItemId; 
    private ProductModel product;
    private VariantModel selectedVariant;
    private List<ToppingModel> selectedToppings;
    private int quantity;
    private String note;
    private boolean isTakeaway = false;
    private boolean isHoliday = false;
 
    public CartItemModel(ProductModel product, VariantModel selectedVariant, List<ToppingModel> selectedToppings, int quantity, String note) {
        this.cartItemId = java.util.UUID.randomUUID().toString(); 
        this.product = product;
        this.selectedVariant = selectedVariant;
        this.selectedToppings = selectedToppings != null ? selectedToppings : new ArrayList<>();
        this.quantity = quantity;
        this.note = note != null ? note : "";
    }
    public void setOrderType(boolean isTakeaway, boolean isHoliday) {
        this.isTakeaway = isTakeaway;
        this.isHoliday = isHoliday;
    }
 
    public long getMainSellingPrice() {
        if (selectedVariant != null) {
            if (isHoliday) return selectedVariant.getHolidayPrice();
            if (isTakeaway) return selectedVariant.getTakeawayPrice();
            return selectedVariant.getDineInPrice();
        } else {
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                VariantModel fallback = product.getVariants().get(0);
                if (isHoliday) return fallback.getHolidayPrice();
                if (isTakeaway) return fallback.getTakeawayPrice();
                return fallback.getDineInPrice();
            }
            return 0;
        }
    }
 
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
        double vatRate = product.getVat(); 
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
        if (!note.isEmpty()) {
            String formattedNote = note.replace(" | Ghi chú:", "<br>Ghi chú:");
            sb.append("<br><i style='color:#e67e22'>").append(formattedNote).append("</i>");
        }
        sb.append("</html>");
        return sb.toString();
    }
 
    public String getCartItemId() { return cartItemId; }
    public ProductModel getProduct() { return product; }
    public VariantModel getSelectedVariant() { return selectedVariant; }
    public List<ToppingModel> getSelectedToppings() { return selectedToppings; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getNote() { return note; }
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