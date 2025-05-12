package app.romail.idp.orange_node.services;

import app.romail.idp.orange_node.domain.identity.Identity;

public interface IdentityService {
   Identity getDetailedIdentity(String uuid);
}
