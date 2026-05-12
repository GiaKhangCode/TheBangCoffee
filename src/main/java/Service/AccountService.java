/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import Common.HashUtil;
import DatabaseAccessObject.AccountDAO;
import Model.AccountModel;
import Model.RoleGroupModel;
import Model.RoleModel;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author FAKK
 */
public class AccountService {
    private AccountDAO dao = new AccountDAO();
    
    public AccountModel login(String username, String password){
        String hashedPassword = HashUtil.hashPassword(password);
        return dao.loginProcess(username, hashedPassword);
    }
    
    public String signUp(String fullName, String username, String password, String phoneNumber, String email){
        String hashedPassword = HashUtil.hashPassword(password);
        return dao.signUpProcess(fullName, username, hashedPassword, phoneNumber, email);
    }
    
    public boolean resetPassword(String email, String newPassword){
        String hashedPassword = HashUtil.hashPassword(newPassword);
        return dao.updatePassword(email, hashedPassword);
    }
    
    public String loginAndCreateToken(AccountModel acc){
        if(acc == null) return null;

        return dao.createToken(acc.getAccountID());
    }
    
    public boolean logout(String token){
        return dao.revokeToken(token);
    }
    
    public boolean revokeAllTokens(String email) {
        return dao.revokeAllTokensByEmail(email);
    }
    
    public boolean isEmailExists(String email){
        return dao.isEmailExists(email);
    }
    
    public List<AccountModel> getAccountList(){
        return dao.getAccountList();
    }
    
    public boolean assignRoleGroupToAccount(int accountId, int roleGroupId){
        return dao.assignRoleGroupToAccount(accountId, roleGroupId);
    }
    
    public boolean deleteRoleGroupFromAccount(int accountId, int roleGroupId){
        return dao.deleteRoleGroupFromAccount(accountId, roleGroupId);
    }
    
    public boolean assignRoleToAccount(int accountId, int roleId){
        return dao.assignRoleToAccount(accountId, roleId);
    }
    
    public boolean deleteRoleFromAccount(int accountId, int roleId){
        return dao.deleteRoleFromAccount(accountId, roleId);
    }
    
    public List<RoleGroupModel> getAssignedRoleGroupsByAccountId(int accountId) throws SQLException{
        return dao.getAssignedRoleGroupsByAccountId(accountId);
    }
    
    public List<RoleGroupModel> getUnassignedRoleGroupsByAccountId(int accountId) throws SQLException{
        return dao.getUnassignedRoleGroupsByAccountId(accountId);
    }
    
    public List<RoleModel> getUnassignedRolesByAccountId(int accountId) throws SQLException{
        return dao.getUnassignedRolesByAccountId(accountId);
    }
    
    public List<RoleModel> getAssignedRolesByAccountId(int accountId) throws SQLException{
        return dao.getAssignedRolesByAccountId(accountId);
    }
    
    public AccountModel getAccountFromToken(){
        return dao.getAccountFromToken();
    }
    
    public String getUnrevokedToken(){
        return dao.getUnrevokedToken();
    }
    
}
