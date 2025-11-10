package com.caerus.audit.server.controller;

import com.caerus.audit.server.dto.ServerAppSettingsDto;
import com.caerus.audit.server.enums.ErrorType;
import com.caerus.audit.server.enums.EventType;
import com.caerus.audit.server.service.LoggingService;
import com.caerus.audit.server.service.ServerAppSettingsService;
import com.caerus.audit.server.util.MdcUtils;
import com.caerus.audit.server.websocket.WebSocketSessionManager;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatHandler extends TextWebSocketHandler {

  private final WebSocketSessionManager webSocketSessionManager;
  private final ServerAppSettingsService settingsService;
  private final LoggingService loggingService;

  private final ConcurrentHashMap<String, WebSocketSession> activeClients =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Instant> lastHeartbeat = new ConcurrentHashMap<>();
  private final Map<String, String> sessionIdToClient = new ConcurrentHashMap<>();

  private Duration getHeartbeatTimeout() {
    ServerAppSettingsDto settings = settingsService.getLatest();
    short timeoutSec =
        settings != null && settings.getConfigCommIssueAutoResolveWindow() != null
            ? settings.getConfigCommIssueAutoResolveWindow()
            : 120;
    return Duration.ofSeconds(timeoutSec);
  }

  private Duration getHeartbeatInterval() {
    ServerAppSettingsDto settings = settingsService.getLatest();
    short intervalSec =
        settings != null && settings.getConfigHeartbeatInterval() != null
            ? settings.getConfigHeartbeatInterval()
            : 60;
    return Duration.ofSeconds(intervalSec);
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    String clientId = extractClientId(session);
    String sessionId = session.getId();
    MdcUtils.runWith(
        clientId,
        () -> {
          try {
            activeClients.put(clientId, session);
            lastHeartbeat.put(clientId, Instant.now());
            sessionIdToClient.put(sessionId, clientId);
            webSocketSessionManager.registerClient(clientId, session);

            loggingService.logEvent(
                EventType.NORMAL, "Client connected: " + clientId, "HeartbeatHandler");
            log.info("Client connected: {}", clientId);
          } catch (Exception e) {
            handleException("afterConnectionEstablished", clientId, e);
          }
        });
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) {
    String clientId = extractClientId(session);
    MdcUtils.runWith(
        clientId,
        () -> {
          try {
            String payload = message.getPayload().trim();

            if ("pong".equalsIgnoreCase(payload.trim())) {
              log.info("Received pong from {}", clientId);
              lastHeartbeat.put(clientId, Instant.now());
            } else {
              log.info("Message from {} : {}", clientId, message);
            }
          } catch (Exception e) {
            handleException("handleTextMessage", clientId, e);
          }
        });
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    String clientId = sessionIdToClient.getOrDefault(session.getId(), extractClientId(session));
    MdcUtils.runWith(
        clientId,
        () -> {
          try {
            activeClients.remove(clientId);
            lastHeartbeat.remove(clientId);
            sessionIdToClient.remove(session.getId());
            webSocketSessionManager.removeClient(clientId);

            loggingService.logEvent(
                EventType.SERVICE_FAILED,
                "Client disconnected: " + clientId + " (status=" + status + ")",
                "HeartbeatHandler");

            log.warn("Client disconnected: {} (status={})", clientId, status);
          } catch (Exception e) {
            handleException("afterConnectionClosed", clientId, e);
          }
        });
  }

  @Scheduled(
      fixedRateString =
          "#{@serverAppSettingsService.getLatest()?.configHeartbeatInterval != null ? @serverAppSettingsService.getLatest().configHeartbeatInterval * 1000 : 60000}")
  public void broadcastPing() {
    activeClients.forEach(
        (clientId, session) ->
            MdcUtils.runWith(
                clientId,
                () -> {
                  try {
                    if (session.isOpen()) {
                      log.info("Sending ping to {}", clientId);
                      session.sendMessage(new TextMessage("ping"));
                    }
                  } catch (Exception e) {
                    handleException("broadcastPing", clientId, e);
                  }
                }));
  }

  /** Check for unresponsive clients every 2 minutes. */
  @Scheduled(fixedRate = 120000)
  public void cleanupStaleSessions() {
    Instant now = Instant.now();
    Duration timeout = getHeartbeatTimeout();

    lastHeartbeat.forEach(
        (clientId, lastPing) -> {
          if (Duration.between(lastPing, now).compareTo(timeout) > 0) {
            MdcUtils.runWith(
                clientId,
                () -> {
                  log.warn(
                      "Client {} missed heartbeat > {}s — marked offline",
                      clientId,
                      timeout.getSeconds());
                  activeClients.remove(clientId);
                  lastHeartbeat.remove(clientId);
                  webSocketSessionManager.removeClient(clientId);
                  loggingService.logError(
                      ErrorType.CONNECTION_ERROR,
                      "Client "
                          + clientId
                          + " missed heartbeat for "
                          + timeout.getSeconds()
                          + "s, marked offline",
                      "HeartbeatHandler");
                });
          }
        });
  }

  private void handleException(String context, String clientId, Exception e) {
    log.error("Error {} for client {}: {}", context, clientId, e.getMessage(), e);
    try {
      loggingService.logError(
          ErrorType.CONNECTION_ERROR,
          String.format("Error in %s for client %s: %s", context, clientId, e.getMessage()),
          "HeartbeatHandler");
    } catch (Exception ex) {
      log.error("Failed to publish error event: {}", ex.getMessage(), ex);
    }
  }

  private String extractClientId(WebSocketSession session) {
    try {
      if (session.getUri() != null && session.getUri().getQuery() != null) {
        for (String param : session.getUri().getQuery().split("&")) {
          if (param.startsWith("clientId=")) {
            String val = param.split("=")[1];
            if (!val.isBlank()) return val;
          }
        }
      }
    } catch (Exception e) {
      log.debug("Failed to parse clientId: {}", e.getMessage());
    }
    return session.getId();
  }
}
