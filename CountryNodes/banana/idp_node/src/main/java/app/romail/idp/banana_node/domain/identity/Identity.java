package app.romail.idp.banana_node.domain.identity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Identity {
    @Id
    private UUID uuid;
    private final String origin = "banana";
    private String pin;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dob;
    private String phone;
    private String address;
    private boolean verified;
}
