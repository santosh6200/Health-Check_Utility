package com.company.monitor.service_health_monitor.controller;

import com.company.monitor.service_health_monitor.entity.MonitoredService;
import com.company.monitor.service_health_monitor.repository.MonitoredServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@CrossOrigin("*")
public class ServiceController {

  private final MonitoredServiceRepository repo;

  public ServiceController(MonitoredServiceRepository repo) {
    this.repo = repo;
  }

  @PostMapping
  public MonitoredService addService(@RequestBody MonitoredService service) {
    return repo.save(service);
  }

  @GetMapping
  public List<MonitoredService> getAllServices() {
    return repo.findAll();
  }
}
