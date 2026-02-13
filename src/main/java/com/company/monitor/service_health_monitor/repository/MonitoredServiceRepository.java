package com.company.monitor.service_health_monitor.repository;

import com.company.monitor.service_health_monitor.entity.MonitoredService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitoredServiceRepository
  extends JpaRepository<MonitoredService, Long> {

  List<MonitoredService> findByActiveTrue();
}
