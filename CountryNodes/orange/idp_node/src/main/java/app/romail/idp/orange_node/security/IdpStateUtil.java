package app.romail.idp.orange_node.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class IdpStateUtil {

    private static final String SECRET_KEY = "your-secret-key";

    public static String generateState(String originNode,String originUri, String appId) throws Exception {
        // Combine the values
        String data = originNode + "::" + originUri +"::" + appId;

        // Encode as Base64
        String base64Encoded = Base64.getEncoder().encodeToString(data.getBytes());

        // Sign the data
        String signature = sign(base64Encoded);

        // Return the state (Base64 + Signature)
        return base64Encoded + "." + signature;
    }

    public static boolean verifyState(String state) throws Exception {
        // Split the state into Base64 and signature
        String[] parts = state.split("\\.");
        if (parts.length != 2) {
            return false;
        }

        String base64Encoded = parts[0];
        String signature = parts[1];

        // Verify the signature
        // Decode the Base64 part
        String computedSignature = sign(base64Encoded);
        return signature.equals(computedSignature);
    }

    public static Map<String, String> getState(String state) throws Exception {
        String[] parts = state.split("\\.");
        if (parts.length != 2) {
            throw new Exception("Invalid state format");
        }
        String base64Encoded = parts[0];
        String decoded = new String(Base64.getDecoder().decode(base64Encoded));
        String[] keyValuePairs = decoded.split("::");
        Map<String, String> stateMap = new HashMap<>();
            if (keyValuePairs.length == 3) {
                stateMap.put("originNode", keyValuePairs[0]);
                stateMap.put("originUri", keyValuePairs[1]);
                stateMap.put("appId", keyValuePairs[2]);
            }
        return stateMap;
    }

    private static String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes());
        return Base64.getUrlEncoder().encodeToString(hmacBytes);
    }
}