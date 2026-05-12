package Model;

public class CustomerModel {
    private int MaKhachHang;
    private String soDienThoai;
    private String HoTen;
    private int diemTichLuy;
    private String hangThanhVien;

    public CustomerModel() {}

    public CustomerModel(int MaKhachHang, String soDienThoai, String HoTen, int diemTichLuy, String hangThanhVien) {
        this.MaKhachHang = MaKhachHang;
        this.soDienThoai = soDienThoai;
        this.HoTen = HoTen;
        this.diemTichLuy = diemTichLuy;
        this.hangThanhVien = hangThanhVien;
    }

    public int getMaKH() {
        return MaKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getTenKH() {
        return HoTen;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public String getHangThanhVien() {
        return hangThanhVien;
    }

    public void setMaKH(int MaKhachHang) {
        this.MaKhachHang = MaKhachHang;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public void setTenKH(String HoTen) {
        this.HoTen = HoTen;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    public void setHangThanhVien(String hangThanhVien) {
        this.hangThanhVien = hangThanhVien;
    } 
    
}