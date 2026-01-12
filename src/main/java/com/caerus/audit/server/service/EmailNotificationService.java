package com.caerus.audit.server.service;

import com.caerus.audit.server.entity.EmailLog;
import com.caerus.audit.server.entity.ErrorLog;
import com.caerus.audit.server.entity.EventLog;
import com.caerus.audit.server.entity.ServerAppSettings;
import com.caerus.audit.server.repository.EmailLogRepository;
import com.caerus.audit.server.repository.ServerAppSettingsRepository;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final GraphServiceClient<?> graphClient;
    private final ServerAppSettingsRepository settingsRepo;
    private final EmailLogRepository emailLogRepo;

    @Value("${microsoft.graph.from-user}")
    private String fromUser;

    public void notifyAdmin(String subject, String body) {
        try {
            Optional<ServerAppSettings> configOpt = settingsRepo.findTopByOrderBySettingIdDesc();
            if (configOpt.isEmpty() || configOpt.get().getConfigAdminEmailAddr() == null) {
                log.warn("Admin email not configured. Skipping email notification.");
                return;
            }

            String adminEmail = configOpt.get().getConfigAdminEmailAddr();
            sendEmail(adminEmail, subject, body);

            emailLogRepo.save(
                    EmailLog.builder()
                            .emailSubject(subject)
                            .emailBody(body)
                            .emailSentTo(adminEmail)
                            .createdDTime(Instant.now())
                            .build()
            );

            log.info("Admin notification email sent to {}", adminEmail);

        } catch (Exception ex) {
            log.error("Failed to send admin email", ex);
        }
    }

    private void sendEmail(String to, String subject, String body) {

        Message message = new Message();
        message.subject = subject;

        ItemBody itemBody = new ItemBody();
        itemBody.contentType = BodyType.TEXT;
        itemBody.content = body;
        message.body = itemBody;

        Recipient recipient = new Recipient();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = to;
        recipient.emailAddress = emailAddress;

        message.toRecipients = List.of(recipient);

        graphClient
                .users(fromUser)
                .sendMail(
                        UserSendMailParameterSet
                                .newBuilder()
                                .withMessage(message)
                                .withSaveToSentItems(false)
                                .build()
                )
                .buildRequest()
                .post();
    }

    public void notifyAdminWithContext(
            ErrorLog error,
            EventLog event,
            String subject,
            String body) {

        try {
            Optional<ServerAppSettings> configOpt = settingsRepo.findTopByOrderBySettingIdDesc();
            if (configOpt.isEmpty()) {
                log.warn("ServerAppSettings missing. Skipping contextual email.");
                return;
            }

            String adminEmail = configOpt.get().getConfigAdminEmailAddr();
            sendEmail(adminEmail, subject, body);

            emailLogRepo.save(
                    EmailLog.builder()
                            .error(error)
                            .event(event)
                            .emailSubject(subject)
                            .emailBody(body)
                            .emailSentTo(adminEmail)
                            .createdDTime(Instant.now())
                            .build()
            );

            log.info("Contextual admin email sent to {}", adminEmail);

        } catch (Exception ex) {
            log.error("Failed to send contextual admin email", ex);
        }
    }
}


