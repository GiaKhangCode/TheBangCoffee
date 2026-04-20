/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

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
}
