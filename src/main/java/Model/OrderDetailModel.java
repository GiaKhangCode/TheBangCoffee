package Model;

public class OrderDetailModel {
    private int detailId;
    private int orderId;
    private String productName; // Tên món nước
    private String sizeName;    // Tên Size
    private String toppings;    // Chuỗi nối các Topping (VD: "Trân châu đen, Thạch đào")
    private int quantity;       // Số lượng
    private long totalRowPrice; // Thành tiền (Bao gồm cả Giá nước + Giá Topping)

    public OrderDetailModel() {
    }

    public OrderDetailModel(int detailId, int orderId, String productName, String sizeName, String toppings, int quantity, long totalRowPrice) {
        this.detailId = detailId;
        this.orderId = orderId;
        this.productName = productName;
        this.sizeName = sizeName;
        this.toppings = toppings;
        this.quantity = quantity;
        this.totalRowPrice = totalRowPrice;
    }

    // Hàm này hỗ trợ hiển thị đẹp trên UI của OrderPanel (Cột "Tên Món")
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><b>").append(productName).append("</b><br>");
        sb.append("<small style='color:gray'>Size ").append(sizeName);
        
        if (toppings != null && !toppings.trim().isEmpty()) {
            sb.append(", ").append(toppings);
        }
        sb.append("</small></html>");
        return sb.toString();
    }

    public int getDetailId() { return detailId; }
    public void setDetailId(int detailId) { this.detailId = detailId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }

    public String getToppings() { return toppings != null ? toppings : ""; }
    public void setToppings(String toppings) { this.toppings = toppings; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public long getTotalRowPrice() { return totalRowPrice; }
    public void setTotalRowPrice(long totalRowPrice) { this.totalRowPrice = totalRowPrice; }
}