package Model;

public class OrderModel {
    private int orderId;
    private int accountId;
    private String orderTime;     // Thời gian đặt (Format dạng DD/MM/YYYY HH:mm:ss)
    private long subTotal;        // Tạm tính (Chưa thuế)
    private long totalVat;        // Tổng thuế
    private long finalTotal;      // Khách phải trả
    
    // [ĐÃ SỬA] Tách trạng thái thành 2 biến riêng biệt
    private String preparationStatus; // Trạng thái pha chế (Chờ tiếp nhận, Đang pha chế...)
    private String paymentStatus;     // Trạng thái thanh toán (Chưa thanh toán, Đã thanh toán...)
    
    private String orderTypeNote; // Ghi chú loại đơn ([LỄ] Mua mang đi, Dùng tại quán...)
    
    private int diemDaDung;       // [MỚI] Số điểm khách hàng đã sử dụng cho đơn hàng
    private long tienGiamGia;     // [MỚI] Số tiền được giảm giá trực tiếp từ điểm tích lũy

    public OrderModel() {
    }

    // Constructor nạp chồng cũ (tương thích ngược)
    public OrderModel(int orderId, int accountId, String orderTime, long subTotal, long totalVat, long finalTotal, String preparationStatus, String paymentStatus, String orderTypeNote) {
        this(orderId, accountId, orderTime, subTotal, totalVat, finalTotal, preparationStatus, paymentStatus, orderTypeNote, 0, 0);
    }

    // [MỚI] Constructor đầy đủ tham số bao gồm điểm đã dùng và tiền giảm giá
    public OrderModel(int orderId, int accountId, String orderTime, long subTotal, long totalVat, long finalTotal, String preparationStatus, String paymentStatus, String orderTypeNote, int diemDaDung, long tienGiamGia) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.orderTime = orderTime;
        this.subTotal = subTotal;
        this.totalVat = totalVat;
        this.finalTotal = finalTotal;
        this.preparationStatus = preparationStatus;
        this.paymentStatus = paymentStatus;
        this.orderTypeNote = orderTypeNote;
        this.diemDaDung = diemDaDung;
        this.tienGiamGia = tienGiamGia;
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

    // [THÊM MỚI] Getter & Setter cho Trạng thái Pha chế
    public String getPreparationStatus() { return preparationStatus; }
    public void setPreparationStatus(String preparationStatus) { this.preparationStatus = preparationStatus; }

    // [THÊM MỚI] Getter & Setter cho Trạng thái Thanh toán
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getOrderTypeNote() { return orderTypeNote != null ? orderTypeNote : ""; }
    public void setOrderTypeNote(String orderTypeNote) { this.orderTypeNote = orderTypeNote; }

    // [MỚI] Getter & Setter cho Điểm đã dùng và Tiền giảm giá
    public int getDiemDaDung() { return diemDaDung; }
    public void setDiemDaDung(int diemDaDung) { this.diemDaDung = diemDaDung; }

    public long getTienGiamGia() { return tienGiamGia; }
    public void setTienGiamGia(long tienGiamGia) { this.tienGiamGia = tienGiamGia; }
}