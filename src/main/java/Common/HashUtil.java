/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author FAKK
 */
public class HashUtil {
    public static String hashPassword(String password) {
        try {
            // Tạo instance SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
 
            // Hash password -> byte[]
            byte[] hashBytes = digest.digest(password.getBytes());
 
            // Convert byte[] -> hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
 
            // Trả về kết quả
            return hexString.toString();
 
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi SHA-256", e);
        }
    }
}
