package Model;

public class SessionManager {
    private static String token;
    private static AccountModel account;
    
    public static void setSession(String t, AccountModel acc) {
        token = t;
        account = acc;
    }

    public static String getToken() {
        return token;
    }

    public static AccountModel getAccount() {
        return account;
    }

    public static int getAccountId() {
        return account != null ? account.getAccountID() : -1;
    }

    public static void clear() {
        token = null;
        account = null;
    }

    public static boolean isLoggedIn() {
        return token != null && account != null;
    }

    public static boolean hasToken() {
        return token != null;
    }
}