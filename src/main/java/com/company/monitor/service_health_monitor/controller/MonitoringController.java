package com.company.monitor.service_health_monitor.controller;

import com.company.monitor.service_health_monitor.entity.ServiceStatusHistory;
import com.company.monitor.service_health_monitor.repository.ServiceStatusHistoryRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/monitor")
@CrossOrigin("*")
public class MonitoringController {

  private final ServiceStatusHistoryRepository historyRepo;

  public MonitoringController(ServiceStatusHistoryRepository historyRepo) {
    this.historyRepo = historyRepo;
  }

  @GetMapping("/history")
  public List<ServiceStatusHistory> getHistory() {
    return historyRepo.findAll();
  }
}
