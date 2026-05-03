/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DatabaseAccessObject.ToppingDAO;
import Model.ToppingModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author FAKK
 */
public class ToppingService {
    private ToppingDAO toppingDAO;

    public ToppingService() {
        toppingDAO = new ToppingDAO();
    }
    
    // Lấy toàn bộ Topping trả về ArrayList phẳng
    public ArrayList<ToppingModel> getAllToppings(){
        return toppingDAO.getAllToppings();
    }
    
    public boolean addTopping(String toppingName, long price, int ingredientID, double lossAmount, double vat) {
        return toppingDAO.insertTopping(toppingName, price, ingredientID, lossAmount, vat);
    }

    public boolean deleteTopping(int toppingId) {
        return toppingDAO.deleteTopping(toppingId);
    }

    public boolean updateTopping(int toppingId, String newName, long newPrice, int ingredientID, double lossAmount, double vat) {
        return toppingDAO.updateTopping(toppingId, newName, newPrice, ingredientID, lossAmount, vat);
    }
    
    public List<ToppingModel> getToppingsByProductID(int productID) {
        return toppingDAO.getToppingsByProductID(productID);
    }
}
