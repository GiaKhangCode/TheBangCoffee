package Model;

public class ShiftModel {
    private int maCa;
    private String tenCa;
    private String gioBatDau;
    private String gioKetThuc;
    private String trangThai;

    public ShiftModel() {}

    public ShiftModel(int maCa, String tenCa, String gioBatDau, String gioKetThuc, String trangThai) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.trangThai = trangThai;
    }

    // Getters
    public int getMaCa() { return maCa; }
    public String getTenCa() { return tenCa; }
    public String getGioBatDau() { return gioBatDau; }
    public String getGioKetThuc() { return gioKetThuc; }
    public String getTrangThai() { return trangThai; }

    // Setters
    public void setMaCa(int maCa) { this.maCa = maCa; }
    public void setTenCa(String tenCa) { this.tenCa = tenCa; }
    public void setGioBatDau(String gioBatDau) { this.gioBatDau = gioBatDau; }
    public void setGioKetThuc(String gioKetThuc) { this.gioKetThuc = gioKetThuc; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}