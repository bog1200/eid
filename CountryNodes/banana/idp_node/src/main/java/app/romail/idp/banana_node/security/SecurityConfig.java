package app.romail.idp.banana_node.security;

import app.romail.idp.banana_node.enviroment.NodeProperties;
import app.romail.idp.banana_node.security.oauth.RedirectLoginEntryPoint;
import org.apache.catalina.connector.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, RedirectLoginEntryPoint entryPoint) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())

                .with(authorizationServerConfigurer, (authorizationServer) ->
                        authorizationServer
                                .oidc(Customizer.withDefaults())	// Enable OpenID Connect 1.0
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers("/api/**","/.well-known/**").permitAll()
                                .anyRequest().authenticated()
                )
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                );
//                .formLogin(form -> form
//                        .loginPage("/login.html")
//                        .permitAll()
 //              );

        return http.build();

    }



    @Bean
    public AuthorizationServerSettings authorizationServerSettings(NodeProperties nodeProperties) {

        return AuthorizationServerSettings.builder()
                .issuer(nodeProperties.getHost())
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // or use allowedOriginPatterns(List.of("*")) if wildcard is rejected
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false); // true only if you're NOT using '*' for origins

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/oauth2/**", configuration);
        return source;
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        System.out.println("AUTH FAIL: " + event.getException().getMessage());
    }

}
