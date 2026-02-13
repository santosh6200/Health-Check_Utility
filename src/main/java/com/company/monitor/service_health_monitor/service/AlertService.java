package com.company.monitor.service_health_monitor.service;

import org.springframework.stereotype.Service;

@Service
public class AlertService {

  public void sendAlert(String serviceName) {

    System.out.println("ALERT: Service DOWN -> " + serviceName);
  }
}

