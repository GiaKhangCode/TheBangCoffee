package Model;

public class SessionManager {
    private static String token;
    private static AccountModel account;
    
    // THÊM MỚI: Lưu trữ ID của phiên ca đang mở hiện tại
    private static int currentMaPhienCa = -1; 

    public static void setSession(String t, AccountModel acc) {
        token = t;
        account = acc;
    }

    // --- CÁC HÀM QUẢN LÝ PHIÊN CA ---
    public static void setCurrentMaPhienCa(int maPhienCa) {
        currentMaPhienCa = maPhienCa;
    }

    public static int getCurrentMaPhienCa() {
        return currentMaPhienCa;
    }

    public static boolean hasOpenShift() {
        return currentMaPhienCa != -1;
    }
    // ---------------------------------

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
        // KHÔNG reset currentMaPhienCa ở đây nếu muốn B vào thay A dùng chung két.
        // Chỉ reset currentMaPhienCa khi thực sự bấm nút "Đóng Ca".
    }

    public static boolean isLoggedIn() {
        return token != null && account != null;
    }

    public static boolean hasToken() {
        return token != null;
    }
}