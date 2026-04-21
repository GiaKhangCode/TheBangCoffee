/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author FAKK
 */
public class CategoryModel {
    private int categoryID;
    private String categoryName;
    private String categoryStatus;

    public CategoryModel(int categoryID, String categoryName, String status) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
        this.categoryStatus = status;
    }

    public int getCategoryID() { 
        return categoryID; 
    }
    public void setCategoryID(int categoryID) { 
        this.categoryID = categoryID; 
    }

    public String getCategoryName() { 
        return categoryName; 
    }
    public void setCategoryName(String categoryName) { 
        this.categoryName = categoryName; 
    }

    public String getCategoryStatus() { 
        return categoryStatus; 
    }
    public void setTrangThai(String status) { 
        this.categoryStatus = status; 
    }
}
