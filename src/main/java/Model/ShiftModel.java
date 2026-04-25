package Model;

import java.time.LocalDate;

public class ShiftModel {
    private int maCa;
    private int maTaiKhoan;
    private String tenTaiKhoan; // Tên hiển thị của nhân viên (Lấy từ DB lên để gắn vào UI)
    private String buoiLamViec;
    private LocalDate ngayLam;
    private double soGioLamViec;

    // Constructor dùng khi lấy dữ liệu từ DB lên
    public ShiftModel(int maCa, int maTaiKhoan, String tenTaiKhoan, String buoiLamViec, LocalDate ngayLam, double soGioLamViec) {
        this.maCa = maCa;
        this.maTaiKhoan = maTaiKhoan;
        this.tenTaiKhoan = tenTaiKhoan;
        this.buoiLamViec = buoiLamViec;
        this.ngayLam = ngayLam;
        this.soGioLamViec = soGioLamViec;
    }

    // Constructor dùng khi tạo mới Ca làm việc (chưa có maCa)
    public ShiftModel(int maTaiKhoan, String buoiLamViec, LocalDate ngayLam, double soGioLamViec) {
        this.maTaiKhoan = maTaiKhoan;
        this.buoiLamViec = buoiLamViec;
        this.ngayLam = ngayLam;
        this.soGioLamViec = soGioLamViec;
    }

    // Getters
    public int getMaCa() { return maCa; }
    public int getMaTaiKhoan() { return maTaiKhoan; }
    public String getTenTaiKhoan() { return tenTaiKhoan; }
    public String getBuoiLamViec() { return buoiLamViec; }
    public LocalDate getNgayLam() { return ngayLam; }
    public double getSoGioLamViec() { return soGioLamViec; }

    // Setters (Phục vụ cho chức năng Sửa)
    public void setMaTaiKhoan(int maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }
    public void setBuoiLamViec(String buoiLamViec) { this.buoiLamViec = buoiLamViec; }
    public void setNgayLam(LocalDate ngayLam) { this.ngayLam = ngayLam; }
    public void setSoGioLamViec(double soGioLamViec) { this.soGioLamViec = soGioLamViec; }
}
