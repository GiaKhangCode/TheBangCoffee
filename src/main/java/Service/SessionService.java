/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import DatabaseAccessObject.SessionDAO;
import Model.AccountModel;

/**
 *
 * @author FAKK
 */
public class SessionService {
    private SessionDAO dao = new SessionDAO();

    public boolean isValid(String token) {
        return dao.isTokenValid(token);
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
}
