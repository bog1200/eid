package app.romail.idp.banana_node.repositories;

import app.romail.idp.banana_node.domain.app.ApplicationScope;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationScopeRepository extends CrudRepository<ApplicationScope, String> {
}
