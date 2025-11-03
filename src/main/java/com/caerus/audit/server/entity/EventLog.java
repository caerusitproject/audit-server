package com.caerus.audit.server.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "event_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "event_id")
  private Long eventID;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_type_id", nullable = false)
  private EventTypeMstr eventType;

  @Column(length = 500)
  private String eventDesc;

  @Column(length = 100)
  private String eventSource;

  @Column(length = 45)
  private String eventSrcIPAddr;

  private Instant eventDTime;
}
