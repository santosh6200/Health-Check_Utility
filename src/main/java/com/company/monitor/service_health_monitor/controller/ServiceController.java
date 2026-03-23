package com.company.monitor.service_health_monitor.controller;

import com.company.monitor.service_health_monitor.entity.MonitoredService;
import com.company.monitor.service_health_monitor.entity.ServiceStatusHistory;
import com.company.monitor.service_health_monitor.repository.MonitoredServiceRepository;
import com.company.monitor.service_health_monitor.repository.ServiceStatusHistoryRepository;
import com.company.monitor.service_health_monitor.service.KeycloakSecretService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@CrossOrigin("*")
public class ServiceController {

  private final MonitoredServiceRepository repo;
  private final ServiceStatusHistoryRepository historyRepo;
  private final KeycloakSecretService keycloakSecretService;

  public ServiceController(MonitoredServiceRepository repo, 
                           ServiceStatusHistoryRepository historyRepo,
                           KeycloakSecretService keycloakSecretService) {
    this.repo = repo;
    this.historyRepo = historyRepo;
    this.keycloakSecretService = keycloakSecretService;
  }

  @PostMapping
  public MonitoredService addService(@RequestBody java.util.Map<String, String> payload) {
    MonitoredService service = new MonitoredService();
    mapAndSaveSecrets(service, payload);
    service.setActive(true);
    return repo.save(service);
  }

  @GetMapping
  public List<MonitoredService> getAllServices() {
    return repo.findByActiveTrue();
  }

  @GetMapping("/{id}")
  public MonitoredService getServiceById(@PathVariable("id") Long id) {
    return repo.findById(id).orElseThrow(() -> new RuntimeException("Service not found"));
  }

  @PutMapping("/{id}")
  public MonitoredService updateService(@PathVariable("id") Long id, @RequestBody java.util.Map<String, String> payload) {
    MonitoredService service = repo.findById(id).orElseThrow(() -> new RuntimeException("Service not found"));
    mapAndSaveSecrets(service, payload);
    return repo.save(service);
  }

  private void mapAndSaveSecrets(MonitoredService service, java.util.Map<String, String> payload) {
    service.setServiceName(payload.get("serviceName"));
    service.setHealthUrl(payload.get("healthUrl"));
    service.setOwner(payload.get("owner"));
    service.setCriticality(payload.get("criticality"));
    service.setCategory(payload.get("category"));
    service.setAuthType(payload.get("authType"));
    service.setUsername(payload.get("username"));
    service.setKeycloakReferenceId(payload.get("keycloakReferenceId"));
    service.setTokenUrl(payload.get("tokenUrl"));
    service.setClientId(payload.get("clientId"));

    String refId = service.getKeycloakReferenceId();
    if (refId != null && !refId.isEmpty()) {
        if (payload.containsKey("password")) keycloakSecretService.storeSecret(refId, "PASSWORD", payload.get("password"));
        if (payload.containsKey("apiKey")) keycloakSecretService.storeSecret(refId, "API_KEY", payload.get("apiKey"));
        if (payload.containsKey("token")) keycloakSecretService.storeSecret(refId, "TOKEN", payload.get("token"));
        if (payload.containsKey("clientSecret")) keycloakSecretService.storeSecret(refId, "CLIENT_SECRET", payload.get("clientSecret"));
    }
  }

  @GetMapping("/{id}/history")
  public List<ServiceStatusHistory> getServiceHistory(@PathVariable("id") Long id) {
    return historyRepo.findTop1000ByServiceIdOrderByIdDesc(id);
  }

  @DeleteMapping("/{id}")
  @Transactional
  public void deleteService(@PathVariable("id") Long id) {
    historyRepo.deleteByServiceId(id);
    repo.deleteById(id);
  }
}
