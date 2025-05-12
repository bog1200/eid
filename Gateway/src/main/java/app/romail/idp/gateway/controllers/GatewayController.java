package app.romail.idp.gateway.controllers;

import app.romail.idp.gateway.config.BackendNodesConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/find-did")
public class GatewayController {

    private final BackendNodesConfig backendNodesConfig;
    private final WebClient webClient;

    public GatewayController(BackendNodesConfig config) {
        this.backendNodesConfig = config;
        this.webClient = WebClient.builder().build();
    }

    @GetMapping("/{did}")
    public Mono<? extends ResponseEntity<?>> dispatchToNodes(
            @PathVariable String did,
            @RequestHeader("X-Origin-Node") String originNodeName
    ) {
        List<BackendNodesConfig.Node> targetNodes = backendNodesConfig.getNodes().stream()
//                .filter(node -> !node.getName().equalsIgnoreCase(originNodeName)) // exclude origin node (self)
                .toList();
        if (targetNodes.isEmpty()) {
            return Mono.just(ResponseEntity.status(502).body("No target nodes available"));
        }
        if (targetNodes.stream().noneMatch(node -> node.getName().equalsIgnoreCase(originNodeName))) {
            return Mono.just(ResponseEntity.status(502).body("Origin node not found in target nodes"));
        }
        return Flux.fromIterable(targetNodes)
                .flatMap(node -> webClient.get()
                        .uri(node.getNodeHost() + "/api/identity/exists/" + did)
                        .header("X-Origin-Node", originNodeName)
                        .retrieve()
                        .toEntity(String.class)
                        .map(response -> Map.entry(node.getName(), response))
                        .onErrorResume(e -> Mono.empty())
                )
                .filter(entry -> entry.getValue().getStatusCode().is2xxSuccessful())
                .next()
                .map(success -> {
                    String responder = success.getKey();
                    return ResponseEntity.ok()
                            .header("X-Identity-Node", responder)
                            .header("X-Identity-URI", targetNodes.stream().filter(node -> node.getName().equalsIgnoreCase(responder)).findFirst().orElseThrow().getNodeHost())
                            .header("X-Origin-Node", originNodeName)
                            .header("X-Origin-URI", targetNodes.stream().filter(node -> node.getName().equalsIgnoreCase(originNodeName)).findFirst().orElseThrow().getNodeHost())
                            .build();
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
