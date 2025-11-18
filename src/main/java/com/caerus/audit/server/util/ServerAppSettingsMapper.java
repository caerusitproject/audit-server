package com.caerus.audit.server.util;

import com.caerus.audit.server.dto.ServerAppSettingsDto;
import com.caerus.audit.server.entity.ServerAppSettings;

public final class ServerAppSettingsMapper {

  private ServerAppSettingsMapper() {}

  public static ServerAppSettingsDto toDto(ServerAppSettings e) {
    if (e == null) return null;
    return ServerAppSettingsDto.builder()
        .settingId(e.getSettingId())
        .serverIpAddr(e.getServerIpAddr())
        .configIdleTimeout(e.getConfigIdleTimeout())
        .configCaptureInterval(e.getConfigCaptureInterval())
        .configCommIssueAutoResolveWindow(e.getConfigCommIssueAutoResolveWindow())
        .configHeartbeatInterval(e.getConfigHeartbeatInterval())
        .configDestFolderPath(e.getConfigDestFolderPath())
        .configAdminEmailAddr(e.getConfigAdminEmailAddr())
        .configAdminADGroups(e.getConfigAdminADGroups())
        .folderStructureTemplate(e.getFolderStructureTemplate())
        .build();
  }

  public static ServerAppSettings toEntity(ServerAppSettingsDto dto) {
    if (dto == null) return null;
    return ServerAppSettings.builder()
        .settingId(dto.getSettingId())
        .serverIpAddr(dto.getServerIpAddr())
        .configIdleTimeout(dto.getConfigIdleTimeout())
        .configCaptureInterval(dto.getConfigCaptureInterval())
        .configCommIssueAutoResolveWindow(dto.getConfigCommIssueAutoResolveWindow())
        .configHeartbeatInterval(dto.getConfigHeartbeatInterval())
        .configDestFolderPath(dto.getConfigDestFolderPath())
        .configAdminEmailAddr(dto.getConfigAdminEmailAddr())
        .configAdminADGroups(dto.getConfigAdminADGroups())
        .folderStructureTemplate(dto.getFolderStructureTemplate())
        .build();
  }
}
