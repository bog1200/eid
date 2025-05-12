package app.romail.idp.banana_node.repositories;

import app.romail.idp.banana_node.domain.app.Application;
import org.springframework.data.repository.CrudRepository;

public interface ApplicationRepository extends CrudRepository<Application, String > {
//    Application findBy
}
