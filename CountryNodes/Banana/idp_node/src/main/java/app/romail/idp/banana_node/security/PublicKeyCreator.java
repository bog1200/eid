package app.romail.idp.banana_node.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PublicKeyCreator {

    public static PublicKey createPublicKey() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> pkey_response = restTemplate.getForEntity("http://localhost:2884/static/public.key", String.class);
        String pemPublicKey = pkey_response.getBody();
        assert pemPublicKey != null;
        String publicKeyPEM = pemPublicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        // Decode the Base64-encoded public key
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyPEM);

        // Generate the PublicKey object
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
