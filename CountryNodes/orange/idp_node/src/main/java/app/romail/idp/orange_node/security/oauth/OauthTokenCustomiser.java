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
                claims.put("sub", federatedUser.getSubject()); // Always include sub

                if (scopes.contains("email") && attributes.containsKey("email")) {
                    claims.put("email", attributes.get("email"));
                }

                if (scopes.contains("profile")) {
                    // Add standard OIDC profile claims
                    if (attributes.containsKey("name")) claims.put("name", attributes.get("name"));
                    if (attributes.containsKey("phone")) claims.put("phone_number", attributes.get("phone"));
                    if (attributes.containsKey("dob")) claims.put("birthdate", attributes.get("dob"));
                    if (attributes.containsKey("age")) claims.put("age", attributes.get("age"));
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
