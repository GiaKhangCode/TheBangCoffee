package Model;

public class OrderModel {
    private int orderId;
    private int accountId;
    private String orderTime;     // Thời gian đặt (Format dạng DD/MM/YYYY HH:mm:ss)
    private long subTotal;        // Tạm tính (Chưa thuế)
    private long totalVat;        // Tổng thuế
    private long finalTotal;      // Khách phải trả
    private String status;        // Trạng thái (Chờ tiếp nhận, Hoàn thành...)
    private String orderTypeNote; // Ghi chú loại đơn ([LỄ] Mua mang đi, Dùng tại quán...)

    public OrderModel() {
    }

    public OrderModel(int orderId, int accountId, String orderTime, long subTotal, long totalVat, long finalTotal, String status, String orderTypeNote) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.orderTime = orderTime;
        this.subTotal = subTotal;
        this.totalVat = totalVat;
        this.finalTotal = finalTotal;
        this.status = status;
        this.orderTypeNote = orderTypeNote;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }

    public long getSubTotal() { return subTotal; }
    public void setSubTotal(long subTotal) { this.subTotal = subTotal; }

    public long getTotalVat() { return totalVat; }
    public void setTotalVat(long totalVat) { this.totalVat = totalVat; }

    public long getFinalTotal() { return finalTotal; }
    public void setFinalTotal(long finalTotal) { this.finalTotal = finalTotal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderTypeNote() { return orderTypeNote != null ? orderTypeNote : ""; }
    public void setOrderTypeNote(String orderTypeNote) { this.orderTypeNote = orderTypeNote; }
}