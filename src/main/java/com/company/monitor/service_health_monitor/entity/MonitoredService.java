package com.company.monitor.service_health_monitor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class MonitoredService {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String serviceName;
  private String healthUrl;
  private boolean active = true;
  private String owner;
  private String criticality;
  private String category;
  private String authType;
  private String username;
  private String keycloakReferenceId; // Reference to Keycloak Client or User
  private String tokenUrl;
  private String clientId;
}
