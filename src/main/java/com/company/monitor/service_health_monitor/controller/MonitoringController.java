package com.company.monitor.service_health_monitor.controller;

import com.company.monitor.service_health_monitor.entity.ServiceStatusHistory;
import com.company.monitor.service_health_monitor.repository.ServiceStatusHistoryRepository;
import com.company.monitor.service_health_monitor.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitor")
@CrossOrigin("*")
public class MonitoringController {

  private final ServiceStatusHistoryRepository historyRepo;
  private final HealthCheckService healthCheckService;

  public MonitoringController(ServiceStatusHistoryRepository historyRepo, HealthCheckService healthCheckService) {
    this.historyRepo = historyRepo;
    this.healthCheckService = healthCheckService;
  }

  @GetMapping("/history")
  public List<ServiceStatusHistory> getHistory() {
    return historyRepo.findAll();
  }

  @PostMapping("/refresh")
  public Map<String, String> refresh() {
    healthCheckService.runChecksImmediately();
    return Map.of("status", "Refresh Triggered");
  }
}
