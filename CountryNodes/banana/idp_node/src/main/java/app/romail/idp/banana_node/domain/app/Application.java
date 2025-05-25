package app.romail.idp.banana_node.domain.app;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "apps")
@NoArgsConstructor

public class Application {
    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String appId;

    @Column(nullable = false, unique = true, updatable = false)

    private String clientId;
    private String clientSecret;

    private String name;
    private boolean active = true;

//    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL)
//    private List<ApplicationToken> tokens;

    private String redirectUri;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "app_scopes",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "scope")
    )
    private Set<ApplicationScope> scopes;

    public Application(String appId, String clientId,String clientSecret, String name, boolean active, String redirectUri, Set<ApplicationScope> scopes) {
        this.appId = appId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.name = name;
        this.active = active;
        this.redirectUri = redirectUri;
        this.scopes = scopes;
    }

    @Override
    public String toString() {
        return "Application{" +
                "appId='" + appId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", name='" + name + '\'' +
                ", active=" + active +
//                ", tokens=" + tokens +
                ", redirectUri='" + redirectUri + '\'' +
                ", scopes=" + scopes +
                '}';
    }
}
