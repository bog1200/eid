package app.romail.idp.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
@ConfigurationProperties(prefix = "backend")
public class BackendNodesConfig {

    private List<Node> nodes;

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }


    public static class Node {
        private String name;

        public String getNodeHost() {
            return nodeHost;
        }

        public void setNodeHost(String nodeHost) {
            this.nodeHost = nodeHost;
        }

        public String getIdpHost() {
            return idpHost;
        }

        public void setIdpHost(String idpHost) {
            this.idpHost = idpHost;
        }

        private String nodeHost;
        private String idpHost;



        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


    }
}