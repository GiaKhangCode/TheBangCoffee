/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DatabaseAccessObject.RoleDAO;
import Model.FunctionModel;
import Model.RoleGroupModel;
import Model.RoleModel;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author SONY
 */
public class RoleService {
    private RoleDAO roleDAO;

    public RoleService() {
        this.roleDAO = new RoleDAO();
    }
    
    public List<FunctionModel> getFunctionList() throws SQLException{
        return roleDAO.getFunctionList();
    }
    
    public boolean createRole(RoleModel role) throws SQLException{
        return roleDAO.createRole(role);
    }
    
    public boolean updateRole(RoleModel role) throws SQLException{
        return roleDAO.updateRole(role);
    }
    
    public boolean deleteRole(int roleId) throws SQLException{
        return roleDAO.deleteRole(roleId);
    }
    
    public boolean createRoleGroup(RoleGroupModel roleGroup) throws SQLException{
        return roleDAO.createRoleGroup(roleGroup);
    }
    
    public boolean updateRoleGroup(RoleGroupModel roleGroup) throws SQLException{
        return roleDAO.updateRoleGroup(roleGroup);
    }
    
    public boolean deleteRoleGroup(int roleGroupId) throws SQLException{
        return roleDAO.deleteRoleGroup(roleGroupId);
    }
    
    
    public boolean assignRoleToRoleGroup(int roleGroupId, int roleId) throws SQLException{
        return roleDAO.assignRoleToRoleGroup(roleGroupId, roleId);
    }
    
    public boolean deleteRoleFromRoleGroup(int roleGroupId, int roleId) throws SQLException{
        return roleDAO.deleteRoleFromRoleGroup(roleGroupId, roleId);
    }
    
    
    public List<RoleModel> getRoleList() throws SQLException{
        return roleDAO.getRoleList();
    }
    
    public List<RoleModel> getUnconfiguredRolesByGroupId(int groupId) throws SQLException{
        return roleDAO.getUnconfiguredRolesByGroupId(groupId);
    }

    public List<RoleModel> getConfiguredRolesByGroupId(int groupId) throws SQLException{
        return roleDAO.getConfiguredRolesByGroupId(groupId);
    }
    
    public List<RoleGroupModel> getRoleGroupList(){
        return roleDAO.getRoleGroupList();
    }
    
    public boolean isPermissed(String operationName, int accountId, int functionId) throws SQLException{
        return roleDAO.isPermissed(operationName, accountId, functionId) > 0;
    }
    
    public int getFunctionIdByName(String functionName) throws SQLException {
        List<FunctionModel> list = getFunctionList();
        for(FunctionModel f : list) {
            if(f.getFunctionName().trim().equalsIgnoreCase(functionName.trim())) {
                return f.getFunctionId();
            }
        }
        return -1; // Trả về -1 nếu không tìm thấy
    }
    
    
    
    
    
}
