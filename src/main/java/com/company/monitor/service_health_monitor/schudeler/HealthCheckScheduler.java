package com.company.monitor.service_health_monitor.schudeler;

import com.company.monitor.service_health_monitor.service.HealthCheckService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckScheduler {

  private final HealthCheckService healthCheckService;

  public HealthCheckScheduler(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }

  @Scheduled(fixedRateString = "${monitor.scheduler-rate}")
  public void runHealthCheck() {
    healthCheckService.checkAllServices();
  }
}
