/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author FAKK
 */
public class OptionGroupModel {
    private int optionGroupID;
    private String optionGroupName;

    public OptionGroupModel(int optionGroupID, String optionGroupName) {
        this.optionGroupID = optionGroupID;
        this.optionGroupName = optionGroupName;
    }

    public int getOptionGroupID() {
        return optionGroupID;
    }

    public void setOptionGroupID(int optionGroupID) {
        this.optionGroupID = optionGroupID;
    }

    public String getOptionGroupName() {
        return optionGroupName;
    }

    public void setOptionGroupName(String optionGroupName) {
        this.optionGroupName = optionGroupName;
    }


}
