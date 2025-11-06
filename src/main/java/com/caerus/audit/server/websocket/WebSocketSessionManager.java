package com.caerus.audit.server.websocket;

import java.io.IOException;
import org.springframework.web.socket.WebSocketSession;

public interface WebSocketSessionManager {
  void broadcastText(String payload);

  void sendTextToClient(String clientId, Object  payload) throws IOException;

  void registerClient(String clientId, WebSocketSession session);

  void removeClient(String clientId);
}
