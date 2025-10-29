package com.caerus.audit.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ErrorEventPublisher {

    public void publishWebSocketError(String context, String clientId, Exception e) {
        // Persist error details in DB
        log.error("[WebSocketError] context={}, clientId={}, cause={}", context, clientId, e.toString());
        // TODO: insert into DB table 'error_logs'
        // TODO: send email to admin (async)
    }

    public void publishClientTimeout(String clientId, java.time.Duration duration) {
        log.error("[HeartbeatTimeout] clientId={}, timeout={}s", clientId, duration.getSeconds());
        // TODO: insert into DB + send email alert
    }
}
