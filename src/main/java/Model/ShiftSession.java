package Model;

import java.sql.Timestamp;

public class ShiftSession {
    private int maPhienCa;
    private Integer maLich; // Dùng Integer để có thể chứa null
    private int maTaiKhoanMo;
    private Integer maTaiKhoanNhan;
    private Timestamp thoiGianMo;
    private Timestamp thoiGianDong;
    private String trangThai;
    private String ghiChu;

    public ShiftSession() {
    }

    public ShiftSession(int maTaiKhoanMo, Integer maLich) {
        this.maTaiKhoanMo = maTaiKhoanMo;
        this.maLich = maLich;
        this.trangThai = "Đang mở";
    }

    // Getters and Setters
    public int getMaPhienCa() { return maPhienCa; }
    public void setMaPhienCa(int maPhienCa) { this.maPhienCa = maPhienCa; }

    public Integer getMaLich() { return maLich; }
    public void setMaLich(Integer maLich) { this.maLich = maLich; }

    public int getMaTaiKhoanMo() { return maTaiKhoanMo; }
    public void setMaTaiKhoanMo(int maTaiKhoanMo) { this.maTaiKhoanMo = maTaiKhoanMo; }

    public Integer getMaTaiKhoanNhan() { return maTaiKhoanNhan; }
    public void setMaTaiKhoanNhan(Integer maTaiKhoanNhan) { this.maTaiKhoanNhan = maTaiKhoanNhan; }

    public Timestamp getThoiGianMo() { return thoiGianMo; }
    public void setThoiGianMo(Timestamp thoiGianMo) { this.thoiGianMo = thoiGianMo; }

    public Timestamp getThoiGianDong() { return thoiGianDong; }
    public void setThoiGianDong(Timestamp thoiGianDong) { this.thoiGianDong = thoiGianDong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}