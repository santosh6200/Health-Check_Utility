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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthCheckService {

  private final MonitoredServiceRepository serviceRepo;
  private final ServiceStatusHistoryRepository historyRepo;
  private final RestTemplate restTemplate;
  private final AlertService alertService;

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
      ResponseEntity<String> response = restTemplate.getForEntity(svc.getHealthUrl(), String.class);
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
}
