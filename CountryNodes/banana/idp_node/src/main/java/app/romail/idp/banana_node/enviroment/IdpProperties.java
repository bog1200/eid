package app.romail.idp.banana_node.enviroment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "idp")
@Getter
@Setter
public class IdpProperties {
    private String host;
    private String didUri;
    private String clientId;
    private String clientSecret;
    private String authorizationUri;
    private String tokenUri;



}


