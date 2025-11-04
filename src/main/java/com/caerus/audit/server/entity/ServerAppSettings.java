package com.caerus.audit.server.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "server_app_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerAppSettings {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Short settingId;

  @Column(length = 45)
  private String serverIpAddr;

  private Short configIdleTimeout;
  private Short configCaptureInterval;
  private Short configCommIssueAutoResolveWindow;
  private Short configHeartbeatInterval;

  @Column(length = 1024)
  private String configDestFolderPath;

  @Column(length = 255)
  private String configAdminEmailAddr;

  @Column(length = 512)
  private String configAdminADGroups;

  @Column(length = 1024)
  private String folderStructureTemplate;

  private Short tempFolderFreeSpaceThreshold;
  private Short lockThreshold;
  private Boolean emailNotifyEnabled;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant createdAt;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant updatedAt;

  @PrePersist
  public void prePersist() {
    createdAt = Instant.now();
    updatedAt = createdAt;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = Instant.now();
  }
}
