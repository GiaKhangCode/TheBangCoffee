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
    private int optionID;
    private String optionName, optionStatus;
    private double extraPrice;

    public OptionModel(int optionID, String optionName, double extraPrice, String optionStatus) {
        this.optionID = optionID;
        this.optionName = optionName;
        this.optionStatus = optionStatus;
        this.extraPrice = extraPrice;
    }

    public int getOptionID() {
        return optionID;
    }

    public void setOptionID(int optionID) {
        this.optionID = optionID;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public double getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(double extraPrice) {
        this.extraPrice = extraPrice;
    }

    public String getOptionStatus() {
        return optionStatus;
    }

    public void setOptionStatus(String optionStatus) {
        this.optionStatus = optionStatus;
    }
    public String getLabel() {
        if (extraPrice > 0) return optionName + " (+" + String.format("%,d", (long)extraPrice).replace(',', '.') + "đ)";
        return optionName;
    }
}
