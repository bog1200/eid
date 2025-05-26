package app.romail.idp.orange_node.services;

import app.romail.idp.orange_node.domain.app.Application;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Application> findByClientId(String clientId) {
        return em.createQuery("SELECT a FROM Application a WHERE a.clientId = :clientId", Application.class)
                .setParameter("clientId", clientId)
                .getResultStream()
                .findFirst();
    }
}
