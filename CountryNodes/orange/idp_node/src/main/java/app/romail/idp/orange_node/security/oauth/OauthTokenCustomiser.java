package app.romail.idp.orange_node.security.oauth;

import app.romail.idp.orange_node.domain.identity.FederatedUser;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class OauthTokenCustomiser {

    /**
     * Customizes the JWT claims for OAuth2 tokens issued by the authorization server.
     * This method adds specific claims based on the authenticated user's attributes and requested scopes.
     *
     * @return an OAuth2TokenCustomizer that modifies the JWT claims.
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            Authentication principal = context.getPrincipal();
            if (!(principal.getPrincipal() instanceof FederatedUser federatedUser)) return;

            Map<String, Object> attributes = federatedUser.getAttributes();
            Set<String> scopes = context.getAuthorizedScopes();

            context.getClaims().claims(claims -> {
                if (scopes.contains("openid") || scopes.contains("did")) {
                    claims.put("sub", federatedUser.getSubject()); // Always include sub
                }
                if (scopes.contains("profile")){
                    claims.put("first_name", attributes.get("first_name"));
                    claims.put("last_name", attributes.get("last_name"));
                    claims.put("name", attributes.get("name"));
                    claims.put("dob", attributes.get("dob"));
                    claims.put("gender", attributes.get("gender"));
                }
                else {
                    for (String scope : Set.of("first_name", "last_name", "name", "dob", "gender")) {
                        if (attributes.containsKey(scope)) {
                            claims.put(scope, attributes.get(scope));
                        }
                    }
                }

                if (scopes.contains("email") && attributes.containsKey("email")) {
                    claims.put("email", attributes.get("email"));
                }

                if (scopes.contains("pin") && attributes.containsKey("pin")) {
                    claims.put("pin", attributes.get("pin"));
                }

                claims.put("identityNode", attributes.get("identityNode"));
                claims.put("appId", attributes.get("appId"));
            });
        };
    }
}
