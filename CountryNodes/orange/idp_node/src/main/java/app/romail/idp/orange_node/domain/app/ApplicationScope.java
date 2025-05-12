package app.romail.idp.orange_node.domain.app;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "scopes")
@NoArgsConstructor
public class ApplicationScope {


    @Id
    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    public ApplicationScope(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
