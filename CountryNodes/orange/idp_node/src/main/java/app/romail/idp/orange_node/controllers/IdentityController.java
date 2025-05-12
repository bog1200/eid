package app.romail.idp.orange_node.controllers;

import app.romail.idp.orange_node.domain.identity.Identity;
import app.romail.idp.orange_node.enviroment.NodeProperties;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import java.util.*;

@RestController(value = "identity")
@RequestMapping("/api/identity")
@CrossOrigin(origins = "*")
public class IdentityController {


    private final NodeProperties nodeProperties;

    public IdentityController(NodeProperties nodeProperties) {
        this.nodeProperties = nodeProperties;
    }


    @GetMapping("/details")
    public ResponseEntity<Optional<Identity>> getDetails() {
        return ResponseEntity.ok(Optional.empty());
    }

    @GetMapping("/exists/{did}")
    public ResponseEntity<Boolean> exists(@PathVariable String did) {
        return ResponseEntity.notFound().build();
//        RestTemplate restTemplate = new RestTemplate();
//        String url = idpProperties.getHost() + idpProperties.getDidUri() +did;
//
//        try {
//            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
//            if (response.getStatusCode().is2xxSuccessful()) {
//                return ResponseEntity.ok(true);
//            }
//        } catch (Exception e) {
//            // Handle the exception
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.notFound().build();
    }

    @PostMapping("/startLogin")
    public ResponseEntity<String> startLogin(@RequestParam String did, @RequestParam String appId) {
        RestTemplate restTemplate = new RestTemplate();
        // add X-Origin-Node header

        String url = nodeProperties.getDidQueryUri() +did;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Origin-Node", nodeProperties.getName());
            headers.set("X-Origin-AppId", appId);
            headers.set("X-Origin-URI", nodeProperties.getHost());
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            // check if identity is local

            String identityNode = response.getHeaders().getFirst("X-Identity-Node");
            if (Objects.equals(identityNode, nodeProperties.getName() )) {
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Orange IDP Not implemented");
            }
            else {
                String identityNodeUrl = response.getHeaders().getFirst("X-Identity-URI");

                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromUriString(identityNodeUrl + "/api/identity/proxyLogin")
                        .queryParam("did", did)
                        .queryParam("originAppId", appId)
                        .queryParam("originNode", nodeProperties.getName())
                        .queryParam("originUri", nodeProperties.getHost());

                URI redirectUri = builder.build().toUri();
                HttpHeaders redirectHeaders = new HttpHeaders();
                redirectHeaders.setLocation(redirectUri);
                redirectHeaders.set("Access-Control-Allow-Origin", "*");

                return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);
            }


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("DID not found");
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> loginCallback(
    ){
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Not implemented");
    }

    @GetMapping("/proxyCallback")
    public ResponseEntity<?> proxyCallback(
            @RequestParam String token,
            @RequestHeader("X-Origin-AppId") String appId
    ){
      return ResponseEntity.ok(Map.of(
              token, appId
      ));
    }
}
