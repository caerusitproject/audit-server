package com.caerus.audit.server.controller;

import com.caerus.audit.server.dto.ErrorLogRequest;
import com.caerus.audit.server.dto.EventLogRequest;
import com.caerus.audit.server.service.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Slf4j
public class EventLogController {
    private final EventLogService eventLogService;

    @PostMapping
    public ResponseEntity<Void> eventLogEvent(@RequestBody EventLogRequest request) {
        eventLogService.saveLog(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/error")
    public ResponseEntity<Void> errorLogEvent(@RequestBody ErrorLogRequest request) {
        eventLogService.saveErrorLog(request);
        return ResponseEntity.ok().build();
    }
}
