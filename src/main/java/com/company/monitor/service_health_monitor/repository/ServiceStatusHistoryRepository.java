package com.company.monitor.service_health_monitor.repository;

import com.company.monitor.service_health_monitor.entity.ServiceStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface ServiceStatusHistoryRepository
  extends JpaRepository<ServiceStatusHistory, Long> {

  List<ServiceStatusHistory> findByServiceId(Long serviceId);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("DELETE FROM ServiceStatusHistory h WHERE h.serviceId = :serviceId")
  void deleteByServiceId(Long serviceId);
}

