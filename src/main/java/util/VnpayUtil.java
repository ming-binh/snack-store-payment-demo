package util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * VNPay utility class. ROOT CAUSE OF ORIGINAL BUGS: 1. Params must be SORTED by
 * key (a-z) before building hash data 2. Hash data uses RAW values (not
 * URL-encoded) 3. Query string uses URL-encoded values 4. Amount must be
 * multiplied by 100 (VNPay expects no decimals) 5. vnp_SecureHash must NOT be
 * included in hash computation
 */
public class VnpayUtil {

    private static final DateTimeFormatter FMT_DT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Build complete signed query string. Params sorted a→z, HMAC-SHA512
     * appended as vnp_SecureHash.
     */
    public static String buildQueryString(Map<String, String> params, String secretKey) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> it = fieldNames.iterator();
        while (it.hasNext()) {
            String field = it.next();
            String value = params.get(field);
            if (value != null && !value.isEmpty()) {
                // hash uses URL-encoded values (US_ASCII) — same as VNPay reference impl
                hashData.append(field).append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                // query uses URL-encoded values
                query.append(URLEncoder.encode(field, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (it.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String secureHash = hmacSHA512(secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);
        return query.toString();
    }

    /**
     * Verify HMAC-SHA512 on VNPay return callback. All vnp_ params EXCEPT
     * vnp_SecureHash and vnp_SecureHashType are used.
     */
    public static boolean verifyChecksum(Map<String, String[]> requestParams, String secretKey) {
        String receivedHash = "";
        Map<String, String> fields = new LinkedHashMap<>();

        for (Map.Entry<String, String[]> e : requestParams.entrySet()) {
            String key   = e.getKey();
            String value = e.getValue()[0];
            if (key.equals("vnp_SecureHash")) {
                receivedHash = value;
            } else if (!key.equals("vnp_SecureHashType")) {
                // URL-encode cả key và value bằng US_ASCII — giống VnpayReturn trong VN_PAY
                String encodedKey   = URLEncoder.encode(key,   StandardCharsets.US_ASCII);
                String encodedValue = URLEncoder.encode(value, StandardCharsets.US_ASCII);
                fields.put(encodedKey, encodedValue);
            }
        }

        List<String> keys = new ArrayList<>(fields.keySet());
        Collections.sort(keys);

        StringBuilder data = new StringBuilder();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String k = it.next();
            data.append(k).append('=').append(fields.get(k));
            if (it.hasNext()) {
                data.append('&');
            }
        }

        String computed = hmacSHA512(secretKey, data.toString());
        return computed.equalsIgnoreCase(receivedHash);
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("HMAC-SHA512 failed", ex);
        }
    }

    /**
     * Unique transaction reference: yyyyMMddHHmmss + 4-digit random
     */
    public static String generateTxnRef() {
        String ts = LocalDateTime.now().format(FMT_DT);
        int rand = new Random().nextInt(9000) + 1000;
        return ts + rand;
    }

    public static String getClientIp(jakarta.servlet.http.HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = req.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = req.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return (ip == null || ip.isBlank()) ? "127.0.0.1" : ip;
    }

    public static String now() {
        return LocalDateTime.now().format(FMT_DT);
    }

    public static String expireIn(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes).format(FMT_DT);
    }
}