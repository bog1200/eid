package app.romail.idp.banana_node.builders;

import app.romail.idp.banana_node.domain.app.Application;
import app.romail.idp.banana_node.domain.app.ApplicationScope;

import java.util.Set;

public class ApplicationBuilder {
    private String appId;
    private String clientId;
    private String name;
    private boolean active = true;
    private String clientSecret;
    private String redirectUris;
    private Set<ApplicationScope> scopes;

    public ApplicationBuilder appId(String appId) {
        this.appId = appId;
        return this;
    }

    public ApplicationBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public ApplicationBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ApplicationBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    public ApplicationBuilder redirectUris(String redirectUris) {
        this.redirectUris = redirectUris;
        return this;
    }

    public ApplicationBuilder scopes(Set<ApplicationScope> scopes) {
        this.scopes = scopes;
        return this;
    }

    public ApplicationBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public Application build() {
        if (appId == null || appId.isEmpty()) {
            throw new IllegalArgumentException("App ID cannot be null or empty");
        }
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalArgumentException("Client ID cannot be null or empty");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (redirectUris == null || redirectUris.isEmpty()) {
            throw new IllegalArgumentException("Redirect URIs cannot be null or empty");
        }
        if (scopes == null || scopes.isEmpty()) {
            throw new IllegalArgumentException("Scopes cannot be null or empty");
        }
        if (clientSecret == null || clientSecret.isEmpty()) {
            throw new IllegalArgumentException("Client Secret cannot be null or empty");
        }
        return new Application(appId, clientId, clientSecret, name, active, redirectUris, scopes);
    }
}