package com.caerus.audit.server.controller;

import com.caerus.audit.server.dto.FileUploadResult;
import com.caerus.audit.server.dto.UploadAckMessage;
import com.caerus.audit.server.enums.ErrorType;
import com.caerus.audit.server.service.FileStorageService;
import com.caerus.audit.server.service.LoggingService;
import com.caerus.audit.server.websocket.WebSocketSessionManager;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
      @RequestHeader(value = "Client-Id", required = false) String clientId,
      @RequestHeader(value = "X-Upload-ID", required = false) String uploadId,
      HttpServletRequest request) {

    if (clientId == null || clientId.isBlank()) {
      return ResponseEntity.badRequest()
          .body(new FileUploadResult(false, null, "Missing Client-Id header"));
    }

    if (uploadId == null || uploadId.isBlank()) {
      uploadId = UUID.randomUUID().toString();
    }

    try {
      FileUploadResult result = fileStorageService.saveAndVerifyFile(file, clientId);

      if (result.success()) {
        // Send structured JSON acknowledgment via WebSocket
        UploadAckMessage ack =
            new UploadAckMessage("UPLOAD_SUCCESS", uploadId, file.getOriginalFilename(), true);
        webSocketSessionManager.sendTextToClient(clientId, ack);

        return ResponseEntity.ok(result);

      } else {
        loggingService.logError(ErrorType.STORAGE_ERROR, "File integrity failed", clientId);

        UploadAckMessage ack =
            new UploadAckMessage("UPLOAD_FAILED", uploadId, file.getOriginalFilename(), false);
        webSocketSessionManager.sendTextToClient(clientId, ack);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
      }

    } catch (Exception e) {
      log.error("File upload failed for client {}: {}", clientId, e.getMessage(), e);
      loggingService.logError(ErrorType.STORAGE_ERROR, e.getMessage(), clientId);

      try {
        UploadAckMessage ack =
            new UploadAckMessage("UPLOAD_FAILED", uploadId, file.getOriginalFilename(), false);
        webSocketSessionManager.sendTextToClient(clientId, ack);
      } catch (Exception ignored) {
      }

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new FileUploadResult(false, null, e.getMessage()));
    }
  }
}
