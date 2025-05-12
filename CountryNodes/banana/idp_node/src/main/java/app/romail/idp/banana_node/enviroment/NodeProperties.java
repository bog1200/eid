package app.romail.idp.banana_node.enviroment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "node")
@Getter
@Setter
public class NodeProperties {
    private String name;
    private String id;
    private String host;
    private String didQueryUri;



}


