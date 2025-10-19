package com.womtech.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class RememberMeUtil {
    
    private static final String SECRET_KEY = "WOMTECH_SECRET_2025";
   
    public static final int REMEMBER_ME_TTL = 30 * 24 * 60 * 60;

    public static String generateToken(String userId) {
        try {
            long now = System.currentTimeMillis();
            String data = userId + "|" + now;
            String signature = hmacSHA256(data, SECRET_KEY);
            String token = data + "|" + signature;
            return Base64.getEncoder().encodeToString(token.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Generate token failed", e);
        }
    }

    public static String verifyToken(String encodedToken) {
        try {
            String decoded = new String(Base64.getDecoder().decode(encodedToken));
            String[] parts = decoded.split("\\|");
            if (parts.length != 3) return null;

            String userId = parts[0];
            String timestamp = parts[1];
            String sig = parts[2];

            String expectedSig = hmacSHA256(userId + "|" + timestamp, SECRET_KEY);
            if (!expectedSig.equals(sig)) return null;
            long ts = Long.parseLong(timestamp);
            long now = System.currentTimeMillis();

            if ((now - ts) > REMEMBER_ME_TTL * 1000L) return null;

            return userId;
        } catch (Exception e) {
            return null;
        }
    }

    private static String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        mac.init(secret);
        byte[] hash = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}
