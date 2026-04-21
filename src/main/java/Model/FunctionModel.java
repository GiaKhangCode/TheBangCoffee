/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author SONY
 */
public class FunctionModel {
    private int functionId;
    private String functionName;

    public FunctionModel(int functionId, String functionName) {
        this.functionId = functionId;
        this.functionName = functionName;
    }
    
    public int getFunctionId() {
        return functionId;
    }

    public String getFunctionName() {
        return functionName;
    }
    
    
}
