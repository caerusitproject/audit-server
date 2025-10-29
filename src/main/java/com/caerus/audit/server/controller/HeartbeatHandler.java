package com.caerus.audit.server.controller;

import com.caerus.audit.server.service.ClientStatusService;
import com.caerus.audit.server.service.ErrorEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatHandler extends TextWebSocketHandler {

    private final ClientStatusService clientStatusService;
    private final ErrorEventPublisher errorPublisher;

    private final ConcurrentHashMap<String, WebSocketSession> activeClients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> lastHeartbeat = new ConcurrentHashMap<>();

    private static final Duration HEARTBEAT_TIMEOUT = Duration.ofSeconds(10);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String clientId = extractClientId(session);
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        try{
            activeClients.put(clientId, session);
            lastHeartbeat.put(clientId, Instant.now());
            log.info("Client connected: {}", clientId);
            clientStatusService.markOnline(clientId);
        } catch (Exception e) {
            handleException("afterConnectionEstablished", clientId, e);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String clientId = extractClientId(session);
        try{
            String payload = message.getPayload();

            if("pong".equalsIgnoreCase(payload.trim())){
                lastHeartbeat.put(clientId, Instant.now());
            } else{
                log.info("Message from {} : {}", clientId, message);
            }
        } catch (Exception e) {
            handleException("handleTextMessage", clientId, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String clientId = extractClientId(session);
        try{
            activeClients.remove(clientId);
            lastHeartbeat.remove(clientId);
            clientStatusService.markOffline(clientId);
            log.info("Client disconnected: {} (reason: {})", clientId, status);
        } catch (Exception e) {
            handleException("afterConnectionClosed", clientId, e);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void broadcastPing(){
            activeClients.forEach((clientId, session) -> {
                try{
                    if(session.isOpen()){
                        session.sendMessage(new TextMessage("ping"));
                    }
                } catch (Exception e){
                    handleException("broadcastPing", clientId, e);
                }
            });
    }

    @Scheduled(fixedRate = 5000)
    public void cleanupStaleSessions(){
        Instant now = Instant.now();
        lastHeartbeat.forEach((clientId, lastPing) -> {
            if(Duration.between(lastPing, now).compareTo(HEARTBEAT_TIMEOUT) > 0){
                log.error("Client {} missed heartbeat > {}s, marking offline", clientId, HEARTBEAT_TIMEOUT.getSeconds());
                activeClients.remove(clientId);
                lastHeartbeat.remove(clientId);
                clientStatusService.markOffline(clientId);
                errorPublisher.publishClientTimeout(clientId, HEARTBEAT_TIMEOUT);
            }
        });
    }

    private void handleException(String operation, String clientId, Exception e) {
        log.error("Error during {} for client {}: {}", operation, clientId, e.getMessage());
        errorPublisher.publishWebSocketError(operation, clientId, e);
    }

    private String extractClientId(WebSocketSession session) {
        try{
            if(session.getUri() != null && session.getUri().getQuery() != null){
                for(String param : session.getUri().getQuery().split("&")){
                    if(param.startsWith("clientId=")){
                        return param.split("=")[1];
                    }
                }
            }
        } catch (Exception e){
            log.error("Failed to parse clientId for session {}: {}", session.getId(), e.getMessage());
        }
        return session.getId();
    }
}
