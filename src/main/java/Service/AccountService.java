/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import Common.HashUtil;
import DatabaseAccessObject.AccountDAO;
import Model.AccountModel;

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
    
    public boolean isEmailExists(String email){
        return dao.isEmailExists(email);
    }
}
