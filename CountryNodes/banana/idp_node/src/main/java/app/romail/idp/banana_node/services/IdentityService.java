package app.romail.idp.banana_node.services;

import app.romail.idp.banana_node.domain.identity.Identity;

public interface IdentityService {
   Identity getDetailedIdentity(String uuid);
}
