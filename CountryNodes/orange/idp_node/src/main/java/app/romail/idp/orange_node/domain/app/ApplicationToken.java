package app.romail.idp.orange_node.domain.app;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tokens")
public class ApplicationToken {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;

    @Column(columnDefinition = "text")
    private String scopes;

    @Column(name = "issued_at", columnDefinition = "timestamptz")
    private Instant issuedAt;

    @Column(name = "expires_at", columnDefinition = "timestamptz")
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Application app;
}
