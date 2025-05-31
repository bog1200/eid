package app.romail.idp.banana_node.controllers;

import app.romail.idp.banana_node.domain.app.Application;
import app.romail.idp.banana_node.domain.app.ApplicationScope;
import app.romail.idp.banana_node.domain.identity.FederatedUser;
import app.romail.idp.banana_node.domain.identity.Identity;
import app.romail.idp.banana_node.enviroment.IdpProperties;
import app.romail.idp.banana_node.enviroment.NodeProperties;
import app.romail.idp.banana_node.repositories.ApplicationRepository;
import app.romail.idp.banana_node.security.IdpStateUtil;
import app.romail.idp.banana_node.security.PublicKeyCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.SecretKey;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

import java.util.*;

@RestController(value = "identity")
@RequestMapping("/api/identity")
@CrossOrigin(origins = "*")
public class IdentityController {

    private final PublicKey idp_publicKey;
    private final IdpProperties idpProperties;
    private final NodeProperties nodeProperties;
    private final ApplicationRepository applicationRepository;

    public IdentityController(PublicKeyCreator publicKeyCreator, IdpProperties idpProperties, NodeProperties nodeProperties, ApplicationRepository applicationRepository) throws Exception {
        this.idp_publicKey = publicKeyCreator.createPublicKey();
        this.idpProperties = idpProperties;
        this.nodeProperties = nodeProperties;
        this.applicationRepository = applicationRepository;
    }


    @GetMapping("/details")
    public ResponseEntity<Optional<Identity>> getDetails() {
        return ResponseEntity.ok(Optional.empty());
    }

    @GetMapping("/exists/{did}")
    public ResponseEntity<Boolean> exists(@PathVariable String did) {
        RestTemplate restTemplate = new RestTemplate();
        String url = idpProperties.getHost() + idpProperties.getDidUri() +did;

        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(true);
            }
        } catch (Exception e) {
            // Handle the exception
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/startLogin")
    public ResponseEntity<String> startLogin(@RequestParam String did, @RequestParam String appId, @RequestParam(required = false)  String scopes) {
        RestTemplate restTemplate = new RestTemplate();
        // add X-Origin-Node header

        String url = nodeProperties.getDidQueryUri() +did;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Origin-Node", nodeProperties.getName());
            headers.set("X-Origin-AppId", appId);
            headers.set("X-Origin-Scopes", String.valueOf(scopes));
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                // check if identity is local
                String identityNode = response.getHeaders().getFirst("X-Identity-Node");
                if (Objects.equals(identityNode, nodeProperties.getName() )) {
                   // redirect to local login
                    String state = IdpStateUtil.generateState(nodeProperties.getName(),nodeProperties.getHost(), appId);
                    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(idpProperties.getHost() + idpProperties.getAuthorizationUri() +
                            "client_id="+idpProperties.getClientId()+"&" +
                            "redirect_uri="+nodeProperties.getHost()+"/api/identity/callback&" +
                            "response_type=code&" +
                            "state="+state +"&" +
                            "login_hint="+did
                    )).build();
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).location(URI.create("/login.html?error=identity_not_found")).build();
        }
    }

    @GetMapping("/proxyLogin")
    public ResponseEntity<String> proxyLogin(@RequestParam String did, @RequestParam String originNode, @RequestParam String originAppId, @RequestParam String originUri) throws Exception {
        String state = IdpStateUtil.generateState(originNode,originUri, originAppId);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(idpProperties.getHost() + idpProperties.getAuthorizationUri() +
                "client_id="+idpProperties.getClientId()+"&" +
                "redirect_uri="+nodeProperties.getHost()+"/api/identity/callback&" +
                "response_type=code&" +
                "state="+state +"&" +
                "login_hint="+did
        )).build();

    }


    @GetMapping("/callback")
    public ResponseEntity<?> loginCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest originalRequest,
            HttpServletResponse originalResponse
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
            String originUri = stateMap.get("originUri");

            // Check if login is local
            Claims jwt =  Jwts.parser().verifyWith(idp_publicKey).build().parseSignedClaims(accessToken).getPayload();
            if (originNode.equals(nodeProperties.getName())) {
                return authorizeCallback(appId, jwt, originalRequest, originalResponse);
            } else {
                /*Foreign login callback*/
                //TODO: Change this to scoped token sent
                JwtBuilder jws = Jwts.builder();
                jws.issuer(nodeProperties.getName());
                jws.issuedAt(new Date(System.currentTimeMillis()));
                jws.expiration(new Date(System.currentTimeMillis() + 1000 * 60));
                jws.subject(jwt.getSubject());

                jws.claim("given_name", jwt.get("given_name")); // OAuth2 to OIDC mapping
                jws.claim("family_name", jwt.get("family_name")); // OAuth2 to OIDC mapping
                jws.claim("name", jwt.get("name"));
                jws.claim("birthdate", jwt.get("birthdate"));
                jws.claim("gender", jwt.get("gender"));
                jws.claim("email", jwt.get("email"));
                jws.claim("pin", jwt.get("pin"));
                jws.claim("age", jwt.get("age"));
                jws.claim("address", jwt.get("address"));

                jws.claim("identityNode", nodeProperties.getName());
                jws.claim("appId", appId);
                jws.claim("applicationNode", originNode);
                SecretKey key = Keys.hmacShaKeyFor("secretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkey".getBytes(StandardCharsets.UTF_8));
                String token = jws.signWith(key).compact();
                URI uri = URI.create(originUri + "/api/identity/proxyCallback");
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(uri+"?token="+token)).build();

                // return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Foreign idp login not supported yet");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/proxyCallback")
    public ResponseEntity<?> proxyCallback(
            @RequestParam String token,
            HttpServletRequest originalRequest,
            HttpServletResponse originalResponse
    ){
        SecretKey key = Keys.hmacShaKeyFor("secretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkey".getBytes(StandardCharsets.UTF_8));
        Claims jwt = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        // Extract the DID from the JWT token
        Optional<Application> app = applicationRepository.findByClientId(jwt.get("appId").toString());
        if (app.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // Check if the app is allowed to access the identity
        Set<ApplicationScope> appScopes = app.get().getScopes();
        if (appScopes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("App not allowed to access identity");
        }
        return authorizeCallback(jwt.get("appId").toString(), jwt, originalRequest, originalResponse);
        }


    private ResponseEntity<?> authorizeCallback(String appId, Claims jwt, HttpServletRequest originalRequest, HttpServletResponse originalResponse) {
        Map<String, Object> userAttributes = new HashMap<>();
        for (String claim: Set.of("given_name", "family_name", "name", "birthdate", "gender", "email", "pin", "age", "address")) {
            if (jwt.containsKey(claim)) {
                userAttributes.put(claim, jwt.get(claim));
            }
        }

        userAttributes.put("identityNode",  jwt.containsKey("identityNode") ? jwt.get("identityNode").toString() : nodeProperties.getName());
        userAttributes.put("appId", appId);
        FederatedUser principal = new FederatedUser(jwt.getSubject(), userAttributes);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(auth);
        new HttpSessionSecurityContextRepository()
                .saveContext(SecurityContextHolder.getContext(), originalRequest, originalResponse);
        // Resume /authorize (saved by Spring earlier)
        SavedRequest saved = new HttpSessionRequestCache().getRequest(originalRequest, originalResponse);
        if (saved != null) {
            return ResponseEntity.status(302).location(URI.create(saved.getRedirectUrl())).build();
        } else {
            return ResponseEntity.badRequest().body("No original request found");
        }
    }
}
