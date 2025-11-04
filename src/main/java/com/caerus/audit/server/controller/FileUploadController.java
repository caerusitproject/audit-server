package com.caerus.audit.server.controller;

import com.caerus.audit.server.dto.FileUploadResult;
import com.caerus.audit.server.enums.ErrorType;
import com.caerus.audit.server.enums.EventType;
import com.caerus.audit.server.service.FileStorageService;
import com.caerus.audit.server.service.LoggingService;
import com.caerus.audit.server.websocket.WebSocketSessionManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
public class FileUploadController {

  private final FileStorageService fileStorageService;
  private final LoggingService loggingService;
  private final WebSocketSessionManager webSocketSessionManager;

  @PostMapping
  public ResponseEntity<FileUploadResult> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("clientId") String clientId,
      HttpServletRequest request) {
    if (clientId == null || clientId.isBlank()) {
      return ResponseEntity.badRequest()
          .body(new FileUploadResult(false, null, "Missing clientId"));
    }

    try {
      FileUploadResult result = fileStorageService.saveAndVerifyFile(file, clientId);
      if (result.success()) {
        loggingService.logEvent(EventType.NORMAL, "File uploaded successfully", clientId);
        webSocketSessionManager.sendTextToClient(clientId, "UPLOAD_SUCCESS:" + result.filePath());
        return ResponseEntity.ok(result);
      } else {
        loggingService.logError(ErrorType.STORAGE_ERROR, "File integrity failed", clientId);
        webSocketSessionManager.sendTextToClient(clientId, "UPLOAD_FAILED:" + result.message());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
      }
    } catch (Exception e) {
      log.error("File upload failed for client {}: {}", clientId, e.getMessage(), e);
      loggingService.logError(ErrorType.STORAGE_ERROR, e.getMessage(), clientId);
      try {
        webSocketSessionManager.sendTextToClient(clientId, "UPLOAD_FAILED:" + e.getMessage());
      } catch (Exception ignored) {
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new FileUploadResult(false, null, e.getMessage()));
    }
  }
}
