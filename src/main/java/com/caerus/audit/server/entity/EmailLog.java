package com.caerus.audit.server.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "email_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long emailID;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "error_id")
  private ErrorLog error;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id")
  private EventLog event;

  @Column(length = 255)
  private String emailSubject;

  @Column(length = 4000)
  private String emailBody;

  @Column(length = 255)
  private String emailSentTo;

  private Instant createdDTime;
}
