package Model;

public class CustomerModel {
    private int MaKhachHang;
    private String soDienThoai;
    private String HoTen;
    private int diemHienTai; // [MỚI] Điểm để dùng trừ tiền
    private int diemTichLuy; // [MỚI] Điểm dồn trọn đời để xét thăng hạng
    private String hangThanhVien;
    private double phanTramChietKhau; // [MỚI] Phần trăm chiết khấu của hạng

    public CustomerModel() {}

    public CustomerModel(int maKH, String soDienThoai, String tenKH, int diemHienTai, int diemTichLuy, String hangThanhVien, double phanTramChietKhau) {
        this.MaKhachHang = maKH;
        this.soDienThoai = soDienThoai;
        this.HoTen = tenKH;
        this.diemHienTai = diemHienTai;
        this.diemTichLuy = diemTichLuy;
        this.hangThanhVien = hangThanhVien;
        this.phanTramChietKhau = phanTramChietKhau;
    }
    
    // Tương thích ngược
    public CustomerModel(int maKH, String soDienThoai, String tenKH, int diemHienTai, int diemTichLuy, String hangThanhVien) {
        this(maKH, soDienThoai, tenKH, diemHienTai, diemTichLuy, hangThanhVien, 0.0);
    }

    public int getMaKH() { return MaKhachHang; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getTenKH() { return HoTen; }
    
    public int getDiemHienTai() { return diemHienTai; } // [MỚI]
    public int getDiemTichLuy() { return diemTichLuy; }
    public String getHangThanhVien() { return hangThanhVien; }
    public double getPhanTramChietKhau() { return phanTramChietKhau; }

    public void setMaKH(int MaKhachHang) { this.MaKhachHang = MaKhachHang; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public void setTenKH(String HoTen) { this.HoTen = HoTen; }
    
    public void setDiemHienTai(int diemHienTai) { this.diemHienTai = diemHienTai; } // [MỚI]
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }
    public void setHangThanhVien(String hangThanhVien) { this.hangThanhVien = hangThanhVien; } 
    public void setPhanTramChietKhau(double phanTramChietKhau) { this.phanTramChietKhau = phanTramChietKhau; }

    // Các hàm trùng lặp do bạn đặt tên
    public int getMaKhachHang() { return MaKhachHang; }
    public String getHoTen() { return HoTen; }
    public void setMaKhachHang(int MaKhachHang) { this.MaKhachHang = MaKhachHang; }
    public void setHoTen(String HoTen) { this.HoTen = HoTen; }
}