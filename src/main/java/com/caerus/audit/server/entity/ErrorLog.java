package com.caerus.audit.server.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "error_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long errorId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "error_type_id", nullable = false)
  private ErrorTypeMstr errorType;

  private String errorDesc;
  private String errorSource;
  private String errorSrcIPAddr;
  private Instant errorDTime = Instant.now();
}
