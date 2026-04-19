/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author FAKK
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;

public class OtpService {

    // Thread-safe storage
    private static final Map<String, OtpDetails> otpStore = new ConcurrentHashMap<>();

    // OTP hết hạn sau 5 phút
    private static final long EXPIRATION_TIME_MS = 5 * 60 * 1000;
    
    private static final long RESEND_INTERVAL_MS = 5 * 1000;

    // Random an toàn hơn
    private static final SecureRandom random = new SecureRandom();
    
    public enum OtpType {
        REGISTER,
        RESET_PASSWORD
    }
    // Inner class lưu OTP + timestamp
    private static class OtpDetails {
        String otp;
        long timestamp;
        int attempts;

        OtpDetails(String otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
            this.attempts = 0;
        }
    }
    
    private static String buildKey(String email, OtpType type){
        return email + "_" + type.name();
    }

    // Tạo OTP
    public static String generateOTP(String email, OtpType type) {
        String key = buildKey(email, type);
        OtpDetails existing = otpStore.get(key);
        
        if(existing != null && (System.currentTimeMillis() - existing.timestamp < RESEND_INTERVAL_MS)){
            throw new RuntimeException("Vui lòng đợi 30 giây để gửi lại OTP");
        }
        
        String otp = String.valueOf(100000 + random.nextInt(900000));
        otpStore.put(key, new OtpDetails(otp, System.currentTimeMillis()));
        
        return otp;
    }

    // Xác thực OTP
    public static boolean verifyOTP(String email, String otp, OtpType type) {
        String key = buildKey(email, type);
        OtpDetails details = otpStore.get(key);

        if (details == null) {
            return false;
        }

        // Kiểm tra hết hạn
        if (System.currentTimeMillis() - details.timestamp > EXPIRATION_TIME_MS) {
            otpStore.remove(email);
            return false;
        }

        // Kiểm tra đúng OTP
        if (!details.otp.equals(otp)) {
            details.attempts++;
            if(details.attempts >= 5){
                otpStore.remove(key);
            }
            return false;
        }

        otpStore.remove(key);
        return true;
    }

    // Xóa OTP thủ công (nếu cần)
    public static void removeOTP(String email) {
        otpStore.remove(email);
    }
}