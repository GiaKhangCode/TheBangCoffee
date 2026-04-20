/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author FAKK
 */
public class OptionModel {
    private int maTuyChon;
    private String tenTuyChon, trangThai;
    private double giaPhuThu;

    public OptionModel(int maTuyChon, String tenTuyChon, double giaPhuThu, String trangThai) {
        this.maTuyChon = maTuyChon;
        this.tenTuyChon = tenTuyChon;
        this.giaPhuThu = giaPhuThu;
        this.trangThai = trangThai;
    }



    public int getMaTuyChon() {
        return maTuyChon;
    }

    public void setMaTuyChon(int maTuyChon) {
        this.maTuyChon = maTuyChon;
    }

    public String getTenTuyChon() {
        return tenTuyChon;
    }

    public void setTenTuyChon(String tenTuyChon) {
        this.tenTuyChon = tenTuyChon;
    }

    public double getGiaPhuThu() {
        return giaPhuThu;
    }

    public void setGiaPhuThu(double giaPhuThu) {
        this.giaPhuThu = giaPhuThu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    public String getLabel() {
        if (giaPhuThu > 0) return tenTuyChon + " (+" + String.format("%,d", (long)giaPhuThu).replace(',', '.') + "đ)";
        return tenTuyChon;
    }
}
