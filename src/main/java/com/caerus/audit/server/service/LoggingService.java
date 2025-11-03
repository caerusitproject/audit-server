package com.caerus.audit.server.service;

import com.caerus.audit.server.entity.ErrorLog;
import com.caerus.audit.server.entity.ErrorTypeMstr;
import com.caerus.audit.server.entity.EventLog;
import com.caerus.audit.server.entity.EventTypeMstr;
import com.caerus.audit.server.enums.ErrorType;
import com.caerus.audit.server.enums.EventType;
import com.caerus.audit.server.repository.*;
import java.net.InetAddress;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoggingService {

  private final ErrorLogRepository errorRepo;
  private final EventLogRepository eventRepo;
  private final EmailLogRepository emailRepo;
  private final ErrorTypeMstrRepository errorTypeRepo;
  private final EventTypeMstrRepository eventTypeRepo;
  private final EmailNotificationService emailService;

  public void logError(ErrorType errorType, String desc, String source) {
    try {
      ErrorTypeMstr type =
          errorTypeRepo
              .findById(errorType.getCode())
              .orElseThrow(() -> new IllegalStateException("Invalid error type: " + errorType.getCode()));

      String ip = InetAddress.getLocalHost().getHostAddress();
      ErrorLog logEntry =
          ErrorLog.builder()
              .errorType(type)
              .errorDesc(desc)
              .errorSource(source)
              .errorSrcIPAddr(ip)
              .errorDTime(Instant.now())
              .build();
      errorRepo.save(logEntry);
      emailService.notifyAdmin("Error: " + type.getErrorTypeMne(), desc);
      log.error("[{}] {}", source, desc);
    } catch (Exception e) {
      log.error("Failed to log error: {}", e.getMessage(), e);
    }
  }

  public void logEvent(EventType eventType, String desc, String source) {
    try {
      EventTypeMstr type =
          eventTypeRepo
              .findById(eventType.getCode())
              .orElseThrow(() -> new IllegalStateException("Invalid event type: " + eventType.getCode()));

      String ip = InetAddress.getLocalHost().getHostAddress();
      EventLog event =
          EventLog.builder()
              .eventType(type)
              .eventDesc(desc)
              .eventSource(source)
              .eventSrcIPAddr(ip)
              .eventDTime(Instant.now())
              .build();
      eventRepo.save(event);
      log.info("[{}] {}", source, desc);
    } catch (Exception e) {
      log.error("Failed to log event: {}", e.getMessage(), e);
    }
  }
}
