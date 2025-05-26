package app.romail.idp.orange_node.repositories;

import app.romail.idp.orange_node.domain.app.Application;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ApplicationRepository extends CrudRepository<Application, String > {
    Optional<Application> findByClientId(String clientId);
}
