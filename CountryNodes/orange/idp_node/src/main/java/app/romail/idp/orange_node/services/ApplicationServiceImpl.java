package app.romail.idp.orange_node.services;

import app.romail.idp.orange_node.domain.app.Application;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class ApplicationServiceImpl implements ApplicationService {
    @PersistenceContext
    private EntityManager em;


    @Override
    public Application getByApplicationId(String applicationId) {
       return null;
    }
}
