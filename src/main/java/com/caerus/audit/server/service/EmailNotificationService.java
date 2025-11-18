package com.caerus.audit.server.service;

import com.caerus.audit.server.entity.EmailLog;
import com.caerus.audit.server.entity.ErrorLog;
import com.caerus.audit.server.entity.EventLog;
import com.caerus.audit.server.entity.ServerAppSettings;
import com.caerus.audit.server.repository.EmailLogRepository;
import com.caerus.audit.server.repository.ServerAppSettingsRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

  private final JavaMailSender mailSender;
  private final ServerAppSettingsRepository settingsRepo;
  private final EmailLogRepository emailLogRepo;

  public void notifyAdmin(String subject, String body) {
    try {
      Optional<ServerAppSettings> configOpt = settingsRepo.findAll().stream().findFirst();

      if (configOpt.isEmpty() || configOpt.get().getConfigAdminEmailAddr() == null) {
        log.warn("No admin email configured in ServerAppSettings. Skipping email notification.");
        return;
      }

      String adminEmail = configOpt.get().getConfigAdminEmailAddr();
      sendEmail(adminEmail, subject, body);

      EmailLog emailLog = EmailLog.builder()
              .emailSubject(subject)
              .emailBody(body)
              .emailSentTo(adminEmail)
              .createdDTime(Instant.now())
              .build();

      emailLogRepo.save(emailLog);
      log.info("Notification email sent to admin: {}", adminEmail);
    } catch (Exception e) {
      log.error("Failed to send admin email: {}", e.getMessage(), e);
    }
  }

  private void sendEmail(String to, String subject, String body) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(body, false);
    mailSender.send(message);
  }

  @Transactional
  public void notifyAdminWithContext(ErrorLog error, EventLog event, String subject, String body) {
    try {
      Optional<ServerAppSettings> configOpt = settingsRepo.findAll().stream().findFirst();
      if (configOpt.isEmpty()) {
        log.warn("ServerAppSettings missing. Skipping contextual email notification.");
        return;
      }

      String adminEmail = configOpt.get().getConfigAdminEmailAddr();
      sendEmail(adminEmail, subject, body);

      EmailLog emailLog =
          EmailLog.builder()
              .error(error)
              .event(event)
              .emailSubject(subject)
              .emailBody(body)
              .emailSentTo(adminEmail)
              .createdDTime(Instant.now())
              .build();

      emailLogRepo.save(emailLog);
      log.info("Contextual email sent to admin: {}", adminEmail);

    } catch (Exception e) {
      log.error("Failed to send contextual admin email: {}", e.getMessage(), e);
    }
  }
}
