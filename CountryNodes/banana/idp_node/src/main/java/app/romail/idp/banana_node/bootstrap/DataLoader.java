package app.romail.idp.banana_node.bootstrap;

import app.romail.idp.banana_node.builders.ApplicationBuilder;
import app.romail.idp.banana_node.domain.app.Application;
import app.romail.idp.banana_node.domain.app.ApplicationScope;
import app.romail.idp.banana_node.repositories.ApplicationRepository;
import app.romail.idp.banana_node.repositories.ApplicationScopeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    private final ApplicationRepository applicationRepository;
    private final ApplicationScopeRepository applicationScopeRepository;


    public DataLoader(ApplicationRepository applicationRepository, ApplicationScopeRepository applicationScopeRepository) {
        this.applicationRepository = applicationRepository;
        this.applicationScopeRepository = applicationScopeRepository;
    }

    @Override
    public void run(String... args){


        if (applicationRepository.count() > 0) {
            System.out.println("Data already loaded");
            return;
        }

        System.out.println("Loading data...");

        ApplicationScope openid = new ApplicationScope(
                "openid",
                "OpenID Compatible DID / Username identifier"
        );
        ApplicationScope profile = new ApplicationScope(
                "profile",
                "OpenID Compatible Profile (First Name, Last Name, Date of Birth, Gender)"
        );
        ApplicationScope email = new ApplicationScope(
                "email",
                "Email Address"
        );

        ApplicationScope did = new ApplicationScope(
                "did",
                "DID / Username identifier"
        );
        ApplicationScope pin = new ApplicationScope(
                "pin",
                "Personal Identification Number"
        );
        ApplicationScope fullName = new ApplicationScope(
                "name",
                "Full Name"
        );
        ApplicationScope firstName = new ApplicationScope(
                "first_name",
                "First Name"
        );
        ApplicationScope lastName = new ApplicationScope(
                "last_name",
                "Last Name"
        );
        ApplicationScope dob = new ApplicationScope(
                "dob",
                "Date of Birth"
        );
        ApplicationScope age = new ApplicationScope(
                "age",
                "Age (in years), without exact date of birth"
        );
        ApplicationScope gender = new ApplicationScope(
                "gender",
                "Gender"
        );

        ApplicationScope address = new ApplicationScope(
                "address",
                "Physical Address"
        );


        applicationScopeRepository.saveAll(Set.of(pin,openid,profile,did,fullName,firstName,lastName,email,dob,age,gender,address));


        Application app1 = new ApplicationBuilder()
                .appId("app1")
                .active(true)
                .name("Banana Application 1")
                .clientId("app1-client-id")
                .clientSecret("app1-client-secret")
                .redirectUris("https://moodle-plus-plus.vercel.app/api/auth/callback/bananaidp")
                .scopes(Set.of(openid,profile,email))
                .build();

        applicationRepository.save(app1);

        Application app2 = new ApplicationBuilder()
                .appId("app2-client-id")
                .active(true)
                .name("Banana Application 2")
                .clientId("app2-client-id")
                .clientSecret("app2-client-secret")
                .redirectUris("https://oauth.pstmn.io/v1/callback")
                .scopes(Set.of(pin,openid,profile,did,fullName,firstName,lastName,email,dob,age,gender,address))
                .build();

        applicationRepository.save(app2);
    }
}
