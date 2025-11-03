package com.caerus.audit.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerAppSettingsDto {

  private Short settingId;

  @Size(max = 45)
  private String serverIpAddr;

  @Min(1)
  @Max(3600)
  private Short configIdleTimeout;

  @Min(1)
  @Max(3600)
  private Short configCaptureInterval;

  @Min(1)
  @Max(3600)
  private Short configCommIssueAutoResolveWindow;

  @Min(1)
  @Max(3600)
  private Short configHeartbeatInterval;

  @Size(max = 1024)
  private String configDestFolderPath;

  @Email
  @Size(max = 255)
  private String configAdminEmailAddr;

  @Size(max = 512)
  private String configAdminADGroups;

  @Size(max = 1024)
  private String folderStructureTemplate;

  @Min(1) @Max(100)
  private Short tempFolderFreeSpaceThreshold;

  @Min(1) @Max(100)
  private Short lockThreshold;

  private Boolean emailNotifyEnabled;
}
