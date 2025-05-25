package app.romail.idp.banana_node.repositories;

import app.romail.idp.banana_node.domain.app.Application;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends CrudRepository<Application, String > {
    Optional<Application> findByClientId(String clientId);
    // Optional: Define custom query methods if needed
    // For example:
    // Optional<Application> findByClientId(String clientId);
    // Optional<Application> findByName(String name);

    // Uncomment and implement if you need to find by clientId or other fields
//    Application findBy
}
