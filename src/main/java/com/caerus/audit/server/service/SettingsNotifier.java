package com.caerus.audit.server.service;

import com.caerus.audit.server.dto.ServerAppSettingsDto;
import com.caerus.audit.server.websocket.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettingsNotifier {

  private final WebSocketSessionManager sessionManager;
  private final ObjectMapper objectMapper;

  @EventListener
  public void onSettingsChanged(SettingsChangedEvent event) {
    ServerAppSettingsDto dto = event.getSettings();

    try {
      Map<String, Object> meta = new HashMap<>();
      meta.put("type", "SETTINGS_UPDATE");
      meta.put("ts", Instant.now().toString());

      Map<String, Object> envelope = new HashMap<>();
      envelope.put("meta", meta);
      envelope.put("body", dto);

      String payload = objectMapper.writeValueAsString(envelope);
      sessionManager.broadcastText(payload);
      log.info("Pushed settings update to clients (id={})", dto.getSettingId());
    } catch (Exception e) {
      log.error("Failed to notify clients about settings change: {}", e.getMessage(), e);
    }
  }
}
