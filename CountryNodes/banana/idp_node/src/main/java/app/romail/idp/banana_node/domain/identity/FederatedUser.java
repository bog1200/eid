package app.romail.idp.banana_node.domain.identity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class FederatedUser implements OAuth2AuthenticatedPrincipal {
    private final String subject;
    private final Map<String, Object> attributes;

    public FederatedUser(String subject, Map<String, Object> attributes) {
        this.subject = subject;
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return subject;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }




}
