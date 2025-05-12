package app.romail.idp.banana_node.controllers;

import app.romail.idp.banana_node.domain.app.Application;
import app.romail.idp.banana_node.repositories.ApplicationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        Application app = applicationRepository.findById(appId).orElse(null);
        return ResponseEntity.ok(app);
    }
}
