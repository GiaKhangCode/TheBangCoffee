/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

public class ToppingModel {
    private int toppingID;
    private String toppingName;
    private long price; // Đổi thành long vì tiền Việt Nam hay dùng số lớn
    private int ingredientID; // Mã nguyên liệu bị trừ khi bán topping này
    private double lossAmount; // Định lượng bị trừ (Ví dụ: 50 gram trân châu)
    private double vat; // Thuế GTGT của Topping

    public ToppingModel(int toppingID, String toppingName, long price, int ingredientID, double lossAmount, double vat) {
        this.toppingID = toppingID;
        this.toppingName = toppingName;
        this.price = price;
        this.ingredientID = ingredientID;
        this.lossAmount = lossAmount;
        this.vat = vat;
    }

    public int getToppingID() { return toppingID; }
    public void setToppingID(int toppingID) { this.toppingID = toppingID; }

    public String getToppingName() { return toppingName; }
    public void setToppingName(String toppingName) { this.toppingName = toppingName; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public int getIngredientID() { return ingredientID; }
    public void setIngredientID(int ingredientID) { this.ingredientID = ingredientID; }

    public double getLossAmount() { return lossAmount; }
    public void setLossAmount(double lossAmount) { this.lossAmount = lossAmount; }

    public double getVat() { return vat; }
    public void setVat(double vat) { this.vat = vat; }

    public String getLabel() {
        if (price > 0) return toppingName + " (+" + String.format("%,d", price).replace(',', '.') + "đ)";
        return toppingName;
    }
}
