package com.caerus.audit.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClientStatusService {
    public void markOnline(String clientId) {
        log.info("✅ Marking {} online", clientId);
        // TODO: update DB (status=ONLINE, lastSeen=now)
    }

    public void markOffline(String clientId) {
        log.warn("⚠️ Marking {} offline", clientId);
        // TODO: update DB (status=OFFLINE, lastSeen=now)
    }
}
