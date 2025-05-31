package app.romail.idp.banana_node.controllers;

import app.romail.idp.banana_node.domain.app.Application;
import app.romail.idp.banana_node.domain.app.ApplicationScope;
import app.romail.idp.banana_node.repositories.ApplicationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController(value = "apps")
@RequestMapping("/api/apps")
@CrossOrigin(origins = "*")
public class AppController {

    private final ApplicationRepository applicationRepository;

    public AppController(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> findById(@PathVariable("id") String appId) {
        Application app = applicationRepository.findByClientId(appId).orElse(null);
        if (app == null) {
            return ResponseEntity.notFound().build();
        }
        app.setClientSecret(null); // Do not expose client secret
        return ResponseEntity.ok(app);
    }

    @GetMapping("/{id}/scopes")
    public ResponseEntity<Set<ApplicationScope>> findScopesById(@PathVariable("id") String appId) {
        Application app = applicationRepository.findByClientId(appId).orElse(null);
        if (app == null) {
            return ResponseEntity.notFound().build();
        }
        Set<ApplicationScope> scopes = app.getScopes();
        if (scopes == null || scopes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(scopes);

    }
}
