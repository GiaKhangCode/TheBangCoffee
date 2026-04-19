/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Common;

/**
 *
 * @author FAKK
 */
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailUtil {
    private static final String FROM_EMAIL = "lamnginphat@gmail.com";
    private static final String APP_PASSWORD = "gmkt emxt fewa ivcb";

    public static void sendOTP(String toEmail, String otp, String purpose) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props,
            new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));

            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
            );
            
            String subject = "xác thực " + purpose;
            
            message.setSubject("[TheBangCF] Mã OTP " + subject);
            
            String content = "<h3>Xin chào,</h3>"
                    + "<p>Mã OTP của bạn là:</p>"
                    + "<h2 style='color:blue;'>" + otp + "</h2>"
                    + "<p>Mã này sẽ hết hạn sau <b>5 phút</b>.</p>"
                    + "<p>Không chia sẻ mã này cho bất kỳ ai.</p>"
                    + "<br><p>Trân trọng,<br>TheBangCF Team</p>";

            message.setContent(content, "text/html; charset=UTF-8");
            Transport.send(message);

            //System.out.println("Gửi email thành công tới: " + toEmail);

        } catch (MessagingException e) {
            System.out.println("Gửi email thất bại");
            e.printStackTrace();
        }
    }
}