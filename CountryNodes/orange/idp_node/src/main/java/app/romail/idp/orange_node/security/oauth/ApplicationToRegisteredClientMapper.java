package app.romail.idp.orange_node.security.oauth;

import app.romail.idp.orange_node.builders.ApplicationBuilder;
import app.romail.idp.orange_node.domain.app.Application;
import app.romail.idp.orange_node.domain.app.ApplicationScope;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ApplicationToRegisteredClientMapper {

    public RegisteredClient toRegisteredClient(Application app) {
        RegisteredClient.Builder builder = RegisteredClient.withId(app.getClientId())
                .clientName(app.getName())
                .clientId(app.getClientId())
                .clientSecret("{noop}"+app.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(app.getRedirectUri())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false) // Assuming no consent is required
                        .requireProofKey(false) // Assuming PKCE is not required
                        .build())
                .tokenSettings(TokenSettings.builder().build());

        app.getScopes().forEach(scope -> builder.scope(scope.getName()));

        return builder.build();
    }

    public Application toApplication(RegisteredClient registeredClient) {
        ApplicationBuilder builder = new ApplicationBuilder()
                .appId(registeredClient.getId())
                .clientId(registeredClient.getClientId())
                .name(registeredClient.getClientName())
                .active(true) // Assuming active is true for registered clients
                .clientSecret(registeredClient.getClientSecret())
                .redirectUris(String.join(",", registeredClient.getRedirectUris()))
                .scopes(registeredClient.getScopes().stream()
                        .map(scope -> new ApplicationScope(scope, null)) // Assuming ApplicationScope has a constructor that takes name and id
                        .collect(Collectors.toSet()));

        return builder.build();
    }
}
