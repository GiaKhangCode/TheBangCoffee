/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import Model.OptionGroupModel;
import DatabaseAccessObject.OptionDAO;
import Model.OptionModel;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author FAKK
 */
public class OptionService {
    private OptionDAO optionDAO;

    public OptionService() {
        optionDAO = new OptionDAO();
    }
    
    public HashMap<String, ArrayList<OptionModel>> getOption(){
        return optionDAO.getAllOption();
    }
    
    public boolean addOption(int groupID, String optionName, double extraPrice, String optionStatus) {
        return optionDAO.insertOption(groupID, optionName, extraPrice, optionStatus);
    }
    
    public boolean addOptionGroup(String groupName) {
        return optionDAO.insertOptionGroup(groupName);
    }
    
    public java.util.ArrayList<OptionGroupModel> getAllOptionGroups() {
        return optionDAO.getAllOptionGroups();
    }
    
    public int getGroupIdByName(String groupName) {
        return optionDAO.getGroupIdByName(groupName);
    }
    public boolean deleteOptionGroup(int groupID) {
        return optionDAO.deleteOptionGroup(groupID);
    }

    public boolean deleteOptionDetail(int optionId) {
        return optionDAO.deleteOptionDetail(optionId);
    }
    
}
