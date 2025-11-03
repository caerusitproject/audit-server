package com.caerus.audit.server.controller;

import com.caerus.audit.server.service.ErrorEventPublisher;
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
  private final ErrorEventPublisher errorPublisher;

  private final ConcurrentHashMap<String, WebSocketSession> activeClients =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Instant> lastHeartbeat = new ConcurrentHashMap<>();
  private final Map<String, String> sessionIdToClient = new ConcurrentHashMap<>();

  private static final Duration HEARTBEAT_TIMEOUT = Duration.ofSeconds(50);

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
            log.info("Client connected: {}", clientId);
            webSocketSessionManager.registerClient(clientId, session);
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
            String payload = message.getPayload();

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
            log.warn("Client disconnected: {} (status={})", clientId, status);
          } catch (Exception e) {
            handleException("afterConnectionClosed", clientId, e);
          }
        });
  }

  @Scheduled(fixedRate = 1000)
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

  @Scheduled(fixedRate = 5000)
  public void cleanupStaleSessions() {
    Instant now = Instant.now();
    lastHeartbeat.forEach(
        (clientId, lastPing) -> {
          if (Duration.between(lastPing, now).compareTo(HEARTBEAT_TIMEOUT) > 0) {
            MdcUtils.runWith(
                clientId,
                () -> {
                  log.error(
                      "Client {} missed heartbeat > {}s — marking offline",
                      clientId,
                      HEARTBEAT_TIMEOUT.getSeconds());
                  activeClients.remove(clientId);
                  lastHeartbeat.remove(clientId);
                  webSocketSessionManager.removeClient(clientId);
                  errorPublisher.publishClientTimeout(clientId, HEARTBEAT_TIMEOUT);
                });
          }
        });
  }

  private void handleException(String context, String clientId, Exception e) {
    log.error("Error {} for client {}: {}", context, clientId, e.getMessage(), e);
    try {
      errorPublisher.publishWebSocketError(context, clientId, e);
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
