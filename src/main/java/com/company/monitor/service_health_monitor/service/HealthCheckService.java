package com.company.monitor.service_health_monitor.service;

import com.company.monitor.service_health_monitor.entity.MonitoredService;
import com.company.monitor.service_health_monitor.entity.ServiceStatusHistory;
import com.company.monitor.service_health_monitor.repository.MonitoredServiceRepository;
import com.company.monitor.service_health_monitor.repository.ServiceStatusHistoryRepository;
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

  public void checkAllServices() {

    List<MonitoredService> services =
      serviceRepo.findByActiveTrue();

    for (MonitoredService svc : services) {

      long start = System.currentTimeMillis();
      String status = "DOWN";

      try {
        ResponseEntity<String> response =
          restTemplate.getForEntity(svc.getHealthUrl(), String.class);

        if (response.getStatusCode().is2xxSuccessful()
          && response.getBody().contains("UP")) {
          status = "UP";
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
}

