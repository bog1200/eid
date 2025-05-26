package app.romail.idp.orange_node.security.oauth;

import app.romail.idp.orange_node.repositories.ApplicationRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

@Component
public class OauthClientRepository implements RegisteredClientRepository {

    private final ApplicationRepository applicationRepository;
    private final ApplicationToRegisteredClientMapper mapper;

    public OauthClientRepository(ApplicationRepository applicationRepository,
                                       ApplicationToRegisteredClientMapper mapper) {
        this.applicationRepository = applicationRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
//        Application application = mapper.toApplication(registeredClient);
//        applicationRepository.save(application);

    }

    @Override
    public RegisteredClient findById(String id) {
        return applicationRepository.findById(id)
                .map(mapper::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return applicationRepository.findByClientId(clientId)
                .map(mapper::toRegisteredClient)
                .orElse(null);
    }
}
