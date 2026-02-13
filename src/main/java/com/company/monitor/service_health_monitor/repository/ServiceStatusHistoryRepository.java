package com.company.monitor.service_health_monitor.repository;

import com.company.monitor.service_health_monitor.entity.ServiceStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ServiceStatusHistoryRepository
  extends JpaRepository<ServiceStatusHistory, Long> {

  List<ServiceStatusHistory> findByServiceId(Long serviceId);
}

