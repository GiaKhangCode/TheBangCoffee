/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author Kiet
 */
public class IngredientTypeModel {
    private int typeID;
    private String typeName;

    public IngredientTypeModel(int typeID, String typeName) {
        this.typeID = typeID;
        this.typeName = typeName;
    }
    public int getTypeID() { 
        return typeID; 
    }
    
    public String getTypeName() { 
        return typeName; 
    }
    
    @Override
    public String toString() {
        return typeName; 
    }
}
