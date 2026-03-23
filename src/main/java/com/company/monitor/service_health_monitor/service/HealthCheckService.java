package com.company.monitor.service_health_monitor.service;

import com.company.monitor.service_health_monitor.entity.MonitoredService;
import com.company.monitor.service_health_monitor.entity.ServiceStatusHistory;
import com.company.monitor.service_health_monitor.repository.MonitoredServiceRepository;
import com.company.monitor.service_health_monitor.repository.ServiceStatusHistoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.MediaType;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthCheckService {

  private final MonitoredServiceRepository serviceRepo;
  private final ServiceStatusHistoryRepository historyRepo;
  private final RestTemplate restTemplate;
  private final AlertService alertService;
  private final KeycloakSecretService keycloakSecretService;

  @PostConstruct
  public void init() {
    checkAllServices();
  }

  public void runChecksImmediately() {
    checkAllServices();
  }

  public void checkAllServices() {
    List<MonitoredService> services = serviceRepo.findByActiveTrue();
    for (MonitoredService svc : services) {
      checkService(svc);
    }
  }

  public void checkService(MonitoredService svc) {
    System.out.println("Checking service: " + svc.getServiceName() + " (" + svc.getHealthUrl() + ")");
    long start = System.currentTimeMillis();
    String status = "DOWN";

    try {
      HttpHeaders headers = new HttpHeaders();

// 🔐 Apply Authentication
      String refId = svc.getKeycloakReferenceId();
      if ("BASIC".equalsIgnoreCase(svc.getAuthType())) {
        String password = keycloakSecretService.getSecret(refId, "PASSWORD");
        headers.setBasicAuth(svc.getUsername(), password);

      } else if ("API_KEY".equalsIgnoreCase(svc.getAuthType())) {
        String apiKey = keycloakSecretService.getSecret(refId, "API_KEY");
        headers.set("x-api-key", apiKey);

      } else if ("BEARER".equalsIgnoreCase(svc.getAuthType())) {
        String token = keycloakSecretService.getSecret(refId, "TOKEN");
        headers.setBearerAuth(token);

      } else if ("OAUTH2".equalsIgnoreCase(svc.getAuthType())) {
        String token = getOAuthToken(svc);
        headers.setBearerAuth(token);
      }

      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(
        svc.getHealthUrl(),
        HttpMethod.GET,
        entity,
        String.class
      );
      if (response.getStatusCode().is2xxSuccessful()) {
        String body = response.getBody();
        if (body != null
            && (body.contains("\"status\":\"UP\"") || body.equalsIgnoreCase("UP") || body.contains("UP"))) {
          status = "UP";
        } else if (body != null && (body.contains("\"status\":\"DOWN\"") || body.equalsIgnoreCase("DOWN")
            || body.contains("DOWN") || body.contains("failing"))) {
          status = "DOWN";
        } else {
          status = "UP";
        }
      } else {
        status = "DOWN";
      }
    }  catch (HttpClientErrorException.Unauthorized e) {
    status = "UNAUTHORIZED";

  } catch (HttpClientErrorException.Forbidden e) {
    status = "FORBIDDEN";

  } catch (Exception ex) {
    status = "DOWN";
  }

    long responseTime = System.currentTimeMillis() - start;
    ServiceStatusHistory history = new ServiceStatusHistory();
    history.setServiceId(svc.getId());
    history.setStatus(status);
    history.setResponseTime(responseTime);
    history.setCheckedAt(LocalDateTime.now());
    historyRepo.save(history);

    if ("DOWN".equals(status)) {
      alertService.sendAlert(svc.getServiceName());
    }
  }
  private String getOAuthToken(MonitoredService svc) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "client_credentials");
    body.add("client_id", svc.getClientId());
    String clientSecret = keycloakSecretService.getSecret(svc.getKeycloakReferenceId(), "CLIENT_SECRET");
    body.add("client_secret", clientSecret);

    HttpEntity<MultiValueMap<String, String>> request =
      new HttpEntity<>(body, headers);

    ResponseEntity<Map> response = restTemplate.postForEntity(
      svc.getTokenUrl(),
      request,
      Map.class
    );

    return (String) response.getBody().get("access_token");
  }
}
