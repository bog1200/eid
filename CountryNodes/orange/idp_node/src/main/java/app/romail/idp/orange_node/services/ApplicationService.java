package app.romail.idp.orange_node.services;

import app.romail.idp.orange_node.domain.app.Application;
import org.springframework.stereotype.Service;

@Service
public interface ApplicationService {
    Application getByApplicationId(String applicationId);
}
