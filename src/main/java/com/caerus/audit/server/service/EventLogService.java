package com.caerus.audit.server.service;

import com.caerus.audit.server.dto.ErrorLogRequest;
import com.caerus.audit.server.dto.EventLogRequest;
import com.caerus.audit.server.entity.ErrorLog;
import com.caerus.audit.server.entity.ErrorTypeMstr;
import com.caerus.audit.server.entity.EventLog;
import com.caerus.audit.server.entity.EventTypeMstr;
import com.caerus.audit.server.repository.ErrorLogRepository;
import com.caerus.audit.server.repository.ErrorTypeMstrRepository;
import com.caerus.audit.server.repository.EventLogRepository;
import com.caerus.audit.server.repository.EventTypeMstrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventLogService {
    private final EventLogRepository eventLogRepository;
    private final ErrorLogRepository errorLogRepository;
    private final EventTypeMstrRepository eventTypeMstrRepository;
    private final ErrorTypeMstrRepository errorTypeMstrRepository;
    private final EmailNotificationService emailService;

    @Transactional
    public void saveLog(EventLogRequest dto) {
        EventTypeMstr eventTypeMstr = null;

        if (dto.getEventTypeId() != null) {
            eventTypeMstr = eventTypeMstrRepository.findById(dto.getEventTypeId()).orElse(null);
        }

        EventLog logEntity = EventLog.builder()
                .eventType(eventTypeMstr)
                .eventDesc(dto.getEventDesc())
                .eventSource(dto.getEventSource())
                .eventSrcIPAddr(dto.getEventSrcIPAddr())
                .eventDTime(dto.getEventDTime())
                .build();

        eventLogRepository.save(logEntity);
    }

    @Transactional
    public void saveErrorLog(ErrorLogRequest dto) {
        ErrorTypeMstr errorTypeMstr = null;

        if (dto.getErrorTypeId() != null) {
            errorTypeMstr = errorTypeMstrRepository.findById(dto.getErrorTypeId()).orElse(null);
        }

        ErrorLog logEntity = ErrorLog.builder()
                .errorType(errorTypeMstr)
                .errorDesc(dto.getErrorDesc())
                .errorSource(dto.getErrorSource())
                .errorSrcIPAddr(dto.getErrorSrcIPAddr())
                .errorDTime(dto.getErrorDTime())
                .build();

        errorLogRepository.save(logEntity);

        String subject = String.format("Error on client machine: %s", dto.getErrorSource());
        String body = String.format(
                "Error on client machine: %s%nIP Address: %s%n%n%s",
                dto.getErrorSource(),
                dto.getErrorSrcIPAddr(),
                dto.getErrorDesc()
        );
        emailService.notifyAdminWithContext(logEntity, null, subject, body);
    }
}
