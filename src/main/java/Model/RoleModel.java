/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author SONY
 */
public class RoleModel {
    private int roleId;
    private String roleName;
    private int functionId;
    private int add;
    private int edit;
    private int delete;
    private int view;
    private int exportFile;

    public RoleModel(){}
    
    public RoleModel(int roleId, String roleName, int functionId, int add, int edit, int delete, int view, int exportFile) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.functionId = functionId;
        this.add = add;
        this.edit = edit;
        this.delete = delete;
        this.view = view;
        this.exportFile = exportFile;
    }
    
    public RoleModel(RoleModel newRole){
        this.roleId = newRole.getRoleId();
        this.roleName = newRole.getRoleName();
        this.functionId = newRole.getFunctionId();
        this.add = newRole.getAdd();
        this.edit = newRole.getEdit();
        this.delete = newRole.getDelete();
        this.view = newRole.getView();
        this.exportFile = newRole.getExportFile();
    }

    public int getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public int getFunctionId() {
        return functionId;
    }

    public int getAdd() {
        return add;
    }

    public int getEdit() {
        return edit;
    }

    public int getDelete() {
        return delete;
    }

    public int getView() {
        return view;
    }

    public int getExportFile() {
        return exportFile;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setFunctionId(int functionId) {
        this.functionId = functionId;
    }

    public void setAdd(int add) {
        this.add = add;
    }

    public void setEdit(int edit) {
        this.edit = edit;
    }

    public void setDelete(int delete) {
        this.delete = delete;
    }

    public void setView(int view) {
        this.view = view;
    }

    public void setExportFile(int exportFile) {
        this.exportFile = exportFile;
    }

    
    
    
}
