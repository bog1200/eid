package app.romail.idp.banana_node.security;

import app.romail.idp.banana_node.enviroment.IdpProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class PublicKeyCreator {

    private final IdpProperties idpProperties;

    public PublicKeyCreator(IdpProperties idpProperties) {
        this.idpProperties = idpProperties;
    }


    public PublicKey createPublicKey() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = URI.create(idpProperties.getHost()+"/static/public.key");
        ResponseEntity<String> pkey_response = restTemplate.getForEntity(uri, String.class);
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
