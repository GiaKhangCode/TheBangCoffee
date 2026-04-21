/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author SONY
 */
public class RoleGroupModel {
    private int roleGroupId;
    private String roleGroupName;

    public RoleGroupModel(){}
    
    public RoleGroupModel(int roleGroupId, String roleGroupName) {
        this.roleGroupId = roleGroupId;
        this.roleGroupName = roleGroupName;
    }
    
    public int getRoleGroupId() {
        return roleGroupId;
    }

    public String getRoleGroupName() {
        return roleGroupName;
    }

    public void setRoleGroupId(int roleGroupId) {
        this.roleGroupId = roleGroupId;
    }

    public void setRoleGroupName(String roleGroupName) {
        this.roleGroupName = roleGroupName;
    }
    
    
}
