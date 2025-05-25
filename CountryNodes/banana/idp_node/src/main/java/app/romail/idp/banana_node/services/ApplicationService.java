package app.romail.idp.banana_node.services;

import app.romail.idp.banana_node.domain.app.Application;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ApplicationService {
    Optional<Application> findByClientId(String clientId);
}
