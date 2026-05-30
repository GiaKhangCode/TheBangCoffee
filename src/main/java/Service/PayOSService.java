package Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class PayOSService {

    private final String clientId = "cacf83a0-9884-4fdb-8430-1630b845e20e";
    private final String apiKey = "6af06dd8-690f-4eab-8e31-09526fb518f5";
    private final String checksumKey = "86ad621e826050b1bfc1cc529feded91568d371f86547f151e71be7957599586";

    // Lớp chứa kết quả trả về để không phụ thuộc vào SDK bị lỗi
    public static class PaymentResult {
        public String qrCode;
        public long orderCode;
    }

    public PayOSService() {}

    private String createSignature(String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Tạo link thanh toán bằng cách gọi API trực tiếp, bỏ qua SDK lỗi
     */
    public PaymentResult createPaymentLink(int orderId, int amount, String description) {
        try {
            long epochSeconds = System.currentTimeMillis() / 1000;
            long orderCode = (epochSeconds % 10000000) * 100 + orderId; 
            String cancelUrl = "http://localhost:8080/cancel";
            String returnUrl = "http://localhost:8080/success";

            // Tạo chuỗi ký theo thứ tự alphabet các trường
            String dataToSign = "amount=" + amount + "&cancelUrl=" + cancelUrl + "&description=" + description + "&orderCode=" + orderCode + "&returnUrl=" + returnUrl;
            String signature = createSignature(dataToSign);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("orderCode", orderCode);
            bodyMap.put("amount", amount);
            bodyMap.put("description", description);
            bodyMap.put("cancelUrl", cancelUrl);
            bodyMap.put("returnUrl", returnUrl);
            bodyMap.put("signature", signature);

            String requestBody = mapper.writeValueAsString(bodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api-merchant.payos.vn/v2/payment-requests"))
                    .header("Content-Type", "application/json")
                    .header("x-client-id", clientId)
                    .header("x-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode rootNode = mapper.readTree(response.body());
            if ("00".equals(rootNode.path("code").asText())) {
                JsonNode dataNode = rootNode.path("data");
                PaymentResult result = new PaymentResult();
                result.qrCode = dataNode.path("qrCode").asText();
                result.orderCode = dataNode.path("orderCode").asLong();
                return result;
            } else {
                System.out.println("PayOS API Error: " + response.body());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy trạng thái thanh toán bằng gọi API trực tiếp
     */
    public String getPaymentStatus(long orderCode) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api-merchant.payos.vn/v2/payment-requests/" + orderCode))
                    .header("x-client-id", clientId)
                    .header("x-api-key", apiKey)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.body());
            if ("00".equals(rootNode.path("code").asText())) {
                return rootNode.path("data").path("status").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "PENDING";
    }

    public BufferedImage generateQRCodeImage(String barcodeText) {
        if (barcodeText == null || barcodeText.isEmpty()) return null;
        try {
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200, hints);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
