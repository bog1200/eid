package app.romail.idp.banana_node.services;

import app.romail.idp.banana_node.domain.app.Application;
import org.springframework.stereotype.Service;

@Service
public interface ApplicationService {
    Application getByApplicationId(String applicationId);
}
