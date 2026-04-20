/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;


/**
 *
 * @author Kiet
 */
public class IngredientModel {
    private int ID;
    private String tenNguyenLieu;
    private String donViTinh;
    private int tonKho;
    private int nguong;
    
    public IngredientModel(int ID, String tenNguyenLieu, String donViTinh, int tonKho, int nguong) {
        this.ID = ID;
        this.tenNguyenLieu = tenNguyenLieu;
        this.donViTinh = donViTinh;
        this.tonKho = tonKho;
        this.nguong = nguong;
    }

    public int getID() {
        return ID;
    }

    public String getTenNguyenLieu() {
        return tenNguyenLieu;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public int getTonKho() {
        return tonKho;
    }

    public int getNguong() {
        return nguong;
    }
    
    public String getTrangThai(){
        if (this.tonKho < this.nguong){
            return "Hết hàng";
        }
        return "Còn hàng";
    }
    
}
