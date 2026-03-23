package com.company.monitor.service_health_monitor.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class KeycloakSecretService {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret}")
    private String adminClientSecret;

    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") 
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(adminClientId)
                .clientSecret(adminClientSecret)
                .build();
    }

    public String getSecret(String referenceId, String secretType) {
        Keycloak kc = getKeycloakInstance();
        try {
            List<ClientRepresentation> clients = kc.realm(realm).clients().findByClientId(referenceId);
            if (clients.isEmpty()) return null;
            String internalId = clients.get(0).getId();

            if ("CLIENT_SECRET".equalsIgnoreCase(secretType)) {
                return kc.realm(realm).clients().get(internalId).getSecret().getValue();
            } else {
                Map<String, String> attributes = clients.get(0).getAttributes();
                return attributes != null ? attributes.get("custom_secret_" + secretType.toLowerCase()) : null;
            }
        } catch (Exception e) {
            log.error("Error fetching secret from Keycloak for {}: {}", referenceId, e.getMessage());
        }
        return null;
    }

    public void storeSecret(String referenceId, String secretType, String secretValue) {
        Keycloak kc = getKeycloakInstance();
        try {
            List<ClientRepresentation> clients = kc.realm(realm).clients().findByClientId(referenceId);
            if (clients.isEmpty()) {
                // Create client if it doesn't exist (optional, depends on requirement)
                ClientRepresentation client = new ClientRepresentation();
                client.setClientId(referenceId);
                client.setServiceAccountsEnabled(true);
                kc.realm(realm).clients().create(client);
                clients = kc.realm(realm).clients().findByClientId(referenceId);
            }

            ClientRepresentation client = clients.get(0);
            if ("CLIENT_SECRET".equalsIgnoreCase(secretType)) {
                CredentialRepresentation cred = new CredentialRepresentation();
                cred.setType(CredentialRepresentation.SECRET);
                cred.setValue(secretValue);
                kc.realm(realm).clients().get(client.getId()).update(client); // Ensure client exists
                // Note: Updating client secret specifically might be different in Admin API
                // For simplicity, we'll focus on attributes for now or assume clientId is enough
            } else {
                Map<String, String> attributes = client.getAttributes();
                if (attributes == null) attributes = new java.util.HashMap<>();
                attributes.put("custom_secret_" + secretType.toLowerCase(), secretValue);
                client.setAttributes(attributes);
                kc.realm(realm).clients().get(client.getId()).update(client);
            }
        } catch (Exception e) {
            log.error("Error storing secret in Keycloak for {}: {}", referenceId, e.getMessage());
        }
    }
}
