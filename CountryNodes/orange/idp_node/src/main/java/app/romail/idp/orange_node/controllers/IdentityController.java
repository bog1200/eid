package app.romail.idp.orange_node.controllers;

import app.romail.idp.orange_node.domain.app.Application;
import app.romail.idp.orange_node.domain.app.ApplicationScope;
import app.romail.idp.orange_node.domain.identity.FederatedUser;
import app.romail.idp.orange_node.domain.identity.Identity;
import app.romail.idp.orange_node.enviroment.IdpProperties;
import app.romail.idp.orange_node.enviroment.NodeProperties;
import app.romail.idp.orange_node.repositories.ApplicationRepository;
import app.romail.idp.orange_node.security.IdpStateUtil;
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
import java.util.*;

@RestController(value = "identity")
@RequestMapping("/api/identity")
@CrossOrigin(origins = "*")
public class IdentityController {


    private final NodeProperties nodeProperties;
    private final ApplicationRepository applicationRepository;
    private final IdpProperties idpProperties;

    public IdentityController(NodeProperties nodeProperties, ApplicationRepository applicationRepository, IdpProperties idpProperties) {
        this.nodeProperties = nodeProperties;
        this.applicationRepository = applicationRepository;
        this.idpProperties = idpProperties;
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

                String state = IdpStateUtil.generateState(nodeProperties.getName(), nodeProperties.getHost(), appId);
                URI redirectUri = UriComponentsBuilder
                        .fromUriString(idpProperties.getHost() + idpProperties.getAuthorizationUri())
                        .queryParam("state", state)
                        .build().toUri();

                HttpHeaders redirectHeaders = new HttpHeaders();
                redirectHeaders.setLocation(redirectUri);
                redirectHeaders.set("Access-Control-Allow-Origin", "*");
                return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);
//                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Orange IDP Not implemented");
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
                "?state="+state +"&" +
                "login_hint="+did
        )).build();

    }

    @GetMapping("/callback")
    public ResponseEntity<?> loginCallback(
            @RequestParam String state,
            @RequestParam String token,
            HttpServletRequest originalRequest,
            HttpServletResponse originalResponse
    ) {
        try {
            Map<String, String> stateMap = IdpStateUtil.getState(state);
            if (stateMap.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid state");
            }

            String originNode = stateMap.get("originNode");
            String originUri = stateMap.get("originUri");
            String appId = stateMap.get("appId");
            SecretKey idp_publicKey = Keys.hmacShaKeyFor(idpProperties.getClientSecret().getBytes(StandardCharsets.UTF_8));
            // Check if the origin node is the current node
            Claims jwt =  Jwts.parser().verifyWith(idp_publicKey).build().parseSignedClaims(token).getPayload();
            if (originNode.equals(nodeProperties.getName())) {
               return authorizeCallback(appId, jwt, originalRequest, originalResponse);
            } else {
                // Redirect to the origin node's callback
                JwtBuilder jws = Jwts.builder();
                jws.issuer(nodeProperties.getName());
                jws.issuedAt(new Date(System.currentTimeMillis()));
                jws.expiration(new Date(System.currentTimeMillis() + 1000 * 60));
                jws.subject(jwt.getSubject());
                jws.claim("name", jwt.get("name"));
                jws.claim("email", jwt.get("email"));
                jws.claim("phone", jwt.get("phone"));
                jws.claim("dob", jwt.get("dob"));
                jws.claim("age", jwt.get("age"));
                jws.claim("identityNode", nodeProperties.getName());
                jws.claim("appId", appId);
                jws.claim("applicationNode", originNode);
                SecretKey key = Keys.hmacShaKeyFor("secretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkey".getBytes(StandardCharsets.UTF_8));
                String out_token = jws.signWith(key).compact();
                URI uri = URI.create(originUri + "/api/identity/proxyCallback");
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(uri+"?token="+ out_token)).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid state format");
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
//        JwtBuilder jws = Jwts.builder();
//
//        jws.issuer(nodeProperties.getName());
//        jws.issuedAt(new Date(System.currentTimeMillis()));
//        jws.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24));
//
//        if (appScopes.stream().anyMatch(scope -> scope.getName().equals("pin"))) {
//            jws.subject(jwt.get("pin").toString());
//        }
//
//        if (appScopes.stream().anyMatch(scope -> scope.getName().equals("name"))) {
//            jws.claim("name", jwt.get("name"));
//        }
//
//        if (appScopes.stream().anyMatch(scope -> scope.getName().equals("email"))) {
//            jws.claim("email", jwt.get("email"));
//        }
//
//        if (appScopes.stream().anyMatch(scope -> scope.getName().equals("phone"))) {
//            jws.claim("phone", jwt.get("phone"));
//        }
//
//        if (appScopes.stream().anyMatch(scope -> scope.getName().equals("dob"))) {
//            jws.claim("dob", jwt.get("dob"));
//        }
//
//        if (appScopes.stream().anyMatch(scope -> scope.getName().equals("age"))) {
//            jws.claim("age", jwt.get("age"));
//        }
//
//        jws.claim("identityNode", jwt.get("identityNode"));
//        jws.claim("appId", jwt.get("appId"));
//        jws.claim("applicationNode", nodeProperties.getName());
//
//
//        String out_token = jws.compact();
//        // Return the response
//        Map<String, String> response = new HashMap<>();
//        response.put("token", out_token);
//        response.put("identityNode", jwt.get("identityNode").toString());
//        response.put("appId", jwt.get("appId").toString());
//        response.put("applicationNode", nodeProperties.getName());
//
//        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> authorizeCallback(String appId, Claims jwt, HttpServletRequest originalRequest, HttpServletResponse originalResponse) {
        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put("email", jwt.get("email"));
        userAttributes.put("name", jwt.get("name"));
        userAttributes.put("identityNode", nodeProperties.getName());
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
