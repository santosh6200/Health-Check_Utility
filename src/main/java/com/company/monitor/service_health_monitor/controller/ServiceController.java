package com.company.monitor.service_health_monitor.controller;

import com.company.monitor.service_health_monitor.entity.MonitoredService;
import com.company.monitor.service_health_monitor.repository.MonitoredServiceRepository;
import com.company.monitor.service_health_monitor.repository.ServiceStatusHistoryRepository;
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

  public ServiceController(MonitoredServiceRepository repo, ServiceStatusHistoryRepository historyRepo) {
    this.repo = repo;
    this.historyRepo = historyRepo;
  }

  @PostMapping
  public MonitoredService addService(@RequestBody MonitoredService service) {
    service.setActive(true);
    return repo.save(service);
  }

  @GetMapping
  public List<MonitoredService> getAllServices() {
    return repo.findByActiveTrue();
  }

  @PutMapping("/{id}")
  public MonitoredService updateService(@PathVariable Long id, @RequestBody MonitoredService serviceDetails) {
    MonitoredService service = repo.findById(id).orElseThrow(() -> new RuntimeException("Service not found"));
    service.setServiceName(serviceDetails.getServiceName());
    service.setHealthUrl(serviceDetails.getHealthUrl());
    service.setOwner(serviceDetails.getOwner());
    service.setCriticality(serviceDetails.getCriticality());
    service.setCategory(serviceDetails.getCategory());
    return repo.save(service);
  }

  @DeleteMapping("/{id}")
  @Transactional
  public void deleteService(@PathVariable Long id) {
    historyRepo.deleteByServiceId(id);
    repo.deleteById(id);
  }
}
