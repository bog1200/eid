package app.romail.idp.orange_node.controllers;

import app.romail.idp.orange_node.domain.app.Application;
import app.romail.idp.orange_node.domain.app.ApplicationScope;
import app.romail.idp.orange_node.domain.identity.Identity;
import app.romail.idp.orange_node.enviroment.IdpProperties;
import app.romail.idp.orange_node.enviroment.NodeProperties;
import app.romail.idp.orange_node.repositories.ApplicationRepository;
import app.romail.idp.orange_node.repositories.ApplicationScopeRepository;
import app.romail.idp.orange_node.security.IdpStateUtil;
import app.romail.idp.orange_node.security.PublicKeyCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.PublicKey;

import java.util.*;

@RestController(value = "identity")
@RequestMapping("/api/identity")
@CrossOrigin(origins = "*")
public class IdentityController {

    private final PublicKey idp_publicKey = PublicKeyCreator.createPublicKey();
    private final ApplicationScopeRepository applicationScopeRepository;
    private final ApplicationRepository applicationRepository;
    private final IdpProperties idpProperties;
    private final NodeProperties nodeProperties;

    public IdentityController(ApplicationScopeRepository applicationScopeRepository, ApplicationRepository applicationRepository, IdpProperties idpProperties, NodeProperties nodeProperties) throws Exception {
        this.applicationScopeRepository = applicationScopeRepository;
        this.applicationRepository = applicationRepository;
        this.idpProperties = idpProperties;
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
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                // check if identity is local
                String identityNode = response.getHeaders().getFirst("X-Identity-Node");
                if (Objects.equals(identityNode, nodeProperties.getName() )) {
                   // redirect to local login
                    String state = IdpStateUtil.generateState(nodeProperties.getName(), appId);
                    return ResponseEntity.status(HttpStatus.FOUND).location(java.net.URI.create(idpProperties.getHost() + idpProperties.getAuthorizationUri() +
                            "client_id=idp.banana&" +
                            "redirect_uri=http://localhost:8080/api/identity/callback&" +
                            "response_type=code&" +
                            "state="+state +"&" +
                            "login_hint="+did
                    )).build();
                }
                else {
                    // redirect to identity node
                    //return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Foreign idp login not supported yet");
                    String identityNodeUrl = String.valueOf(response.getHeaders().getOrDefault("X-Identity-URI", null));
                    if (identityNodeUrl == null) {
                        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Identity node not found");
                    }
                    // headers
                    // include X-Origin-Node and X-Origin-AppId headers

                    return ResponseEntity.status(302).headers(headers).location(java.net.URI.create(identityNodeUrl+"/identity/proxyLogin")).build();
                }


        } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("DID not found");
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> loginCallback(
            @RequestParam String code,
            @RequestParam String state
    ){

        RestTemplate restTemplate = new RestTemplate();
        String url = idpProperties.getHost() + idpProperties.getTokenUri();

        // body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("code", code);
        requestBody.put("client_id", idpProperties.getClientId());
        requestBody.put("client_secret", idpProperties.getClientSecret());
        requestBody.put("redirect_uri", nodeProperties.getHost()+"/api/identity/callback");
        requestBody.put("grant_type", "authorization_code");


        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            // Parse the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();

            // Decode the state
            if (!IdpStateUtil.verifyState(state)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid state");
            }
            Map<String, String> stateMap = IdpStateUtil.getState(state);
            String originNode = stateMap.get("originNode");
            String appId = stateMap.get("appId");

            // Check if login is local

            if (originNode.equals(nodeProperties.getName())) {


                // Decode the JWT token
                Claims jwt =  Jwts.parser().verifyWith(idp_publicKey).build().parseSignedClaims(accessToken).getPayload();

                // Extract the DID from the JWT token
               Optional<Application> app = applicationRepository.findById(appId);

                if (app.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }

                // Check if the app is allowed to access the identity
                Set<ApplicationScope> appScopes = app.get().getScopes();

                if (appScopes.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("App not allowed to access identity");
                }


                JwtBuilder jws = Jwts.builder();

                jws.issuer(originNode);
                jws.issuedAt(new Date(System.currentTimeMillis()));
                jws.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24));

                if (appScopes.stream().anyMatch(scope -> scope.getName().equals("pin"))) {
                    jws.subject(jwt.get("pin").toString());
                }

                if (appScopes.stream().anyMatch(scope -> scope.getName().equals("name"))) {
                    jws.claim("name", jwt.get("name"));
                }

                if (appScopes.stream().anyMatch(scope -> scope.getName().equals("email"))) {
                    jws.claim("email", jwt.get("email"));
                }

                if (appScopes.stream().anyMatch(scope -> scope.getName().equals("phone"))) {
                    jws.claim("phone", jwt.get("phone"));
                }

                if (appScopes.stream().anyMatch(scope -> scope.getName().equals("dob"))) {
                    jws.claim("dob", jwt.get("dob"));
                }

                if (appScopes.stream().anyMatch(scope -> scope.getName().equals("age"))) {
                    jws.claim("age", jwt.get("age"));
                }

                String token = jws.compact();



                // Return the response
                return ResponseEntity.ok(Map.of(
                        "access_token", token,
                        "appId", appId
                ));
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Foreign idp login not supported yet");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}
