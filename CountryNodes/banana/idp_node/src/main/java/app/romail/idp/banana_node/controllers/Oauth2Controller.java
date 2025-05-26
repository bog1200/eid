package app.romail.idp.banana_node.controllers;


import app.romail.idp.banana_node.domain.identity.FederatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController()
public class Oauth2Controller {


    @GetMapping("/oauth2/userinfo")
    public ResponseEntity<Map<String, Object>> userinfo(Principal principal) {
        if (!(principal instanceof FederatedUser user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<String, Object> claims = new HashMap<>();

        claims.put("sub", user.getSubject());
        claims.put("name", user.getAttributes().get("name"));
        claims.put("email", user.getAttributes().get("email"));

        return ResponseEntity.ok(claims);
    }


}
