package com.caerus.audit.server.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class WebSocketSessionManagerImpl implements WebSocketSessionManager {

  private final Map<String, WebSocketSession> activeClients = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Register a client session (usually called from HeartbeatHandler after connection established)
   */
  @Override
  public void registerClient(String clientId, WebSocketSession session) {
    activeClients.put(clientId, session);
    log.info("Client connected: {} (total active: {})", clientId, activeClients.size());
  }

  /** Remove a disconnected client */
  @Override
  public void removeClient(String clientId) {
    activeClients.remove(clientId);
    log.info("Client disconnected: {} (total active: {})", clientId, activeClients.size());
  }

  /** Broadcast a message to all connected clients */
  @Override
  public void broadcastText(String payload) {
    activeClients.forEach(
        (clientId, session) -> {
          try {
            if (session.isOpen()) {
              session.sendMessage(new TextMessage(payload));
            }
          } catch (IOException e) {
            log.error("Failed to send broadcast to {}: {}", clientId, e.getMessage());
          }
        });
    log.debug("Broadcasted message to {} clients", activeClients.size());
  }

  /** Send message to a specific client */
  @Override
  public void sendTextToClient(String clientId, Object payload) throws IOException {
    WebSocketSession session = activeClients.get(clientId);
    if (session != null && session.isOpen()) {
        String json = mapper.writeValueAsString(payload);
      session.sendMessage(new TextMessage(json));
      log.info("Sent message to client {}", clientId);
    } else {
      log.warn("Client {} not found or closed", clientId);
    }
  }

  /** For diagnostics */
  public int getActiveClientCount() {
    return activeClients.size();
  }
}
