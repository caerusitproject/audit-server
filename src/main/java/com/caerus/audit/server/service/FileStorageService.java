package com.caerus.audit.server.service;

import com.caerus.audit.server.dto.FileUploadResult;
import com.caerus.audit.server.dto.ServerAppSettingsDto;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.caerus.audit.server.util.PatternValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

  private final ServerAppSettingsService serverAppSettingsService;

  public FileUploadResult saveAndVerifyFile(MultipartFile file, String clientId) {
    try {
      // Load destination path from server settings
      ServerAppSettingsDto settings = serverAppSettingsService.getLatest();
      String baseDir = settings.getConfigDestFolderPath();
      String pattern = settings.getFolderStructureTemplate();
      if (baseDir == null || baseDir.isBlank()) {
        throw new IllegalStateException("Destination folder not configured in settings");
      }

     if(pattern == null || pattern.isBlank()) {
        throw new IllegalStateException("Folder structure template not configured in settings");
      }

     LocalDateTime now = LocalDateTime.now();
     String ext = getFileExtension(file.getOriginalFilename());
     String timestamp = String.valueOf(System.currentTimeMillis());

        Map<String, String> tokens = Map.of(
                "HOSTNAME", clientId,
                "YEAR", String.valueOf(now.getYear()),
                "MONTH", String.format("%02d", now.getMonthValue()),
                "DAY", String.format("%02d", now.getDayOfMonth()),
                "HOUR", String.format("%02d", now.getHour()),
                "MIN", String.format("%02d", now.getMinute()),
                "SECOND", String.format("%02d", now.getSecond()),
                "DATE", now.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                "TIMESTAMP", timestamp,
                "EXT", ext.isEmpty() ? "" : ext
        );

     PatternValidator.validate(pattern);
      String resolved = resolvePattern(pattern, tokens);
      Path outputPath = Paths.get(baseDir, resolved);
      Path dir = outputPath.getParent();
      if(dir != null) Files.createDirectories(dir);

      Path destFile = outputPath;

      String originalChecksum = computeChecksum(file.getInputStream(), "SHA-256");

      Files.copy(file.getInputStream(), destFile, StandardCopyOption.REPLACE_EXISTING);

      String savedChecksum = computeChecksum(destFile, "SHA-256");
      boolean integrityOk = originalChecksum.equals(savedChecksum);

      return new FileUploadResult(
          integrityOk,
          destFile.toString(),
          integrityOk ? "File saved successfully" : "Integrity check failed");
    } catch (Exception e) {
      log.error("Error saving file for client {}: {}", clientId, e.getMessage(), e);
      return new FileUploadResult(false, null, e.getMessage());
    }
  }

  private String resolvePattern(String pattern, Map<String, String> tokens) {
    String result = pattern;
    for (Map.Entry<String, String> entry : tokens.entrySet()) {
      result = result.replaceAll(
              "(?i)\\{" + entry.getKey() + "}",
              entry.getValue());
    }
    return result;
  }

  private String getFileExtension(String filename) {
    if (filename == null) return "";
    int idx = filename.lastIndexOf('.');
    return (idx > 0 && idx < filename.length() - 1) ? filename.substring(idx + 1) : "";
  }

  private String computeChecksum(InputStream inputStream, String algorithm) throws Exception {
    MessageDigest digest = MessageDigest.getInstance(algorithm);
    try (DigestInputStream dis = new DigestInputStream(inputStream, digest)) {
      byte[] buffer = new byte[8192];
      while (dis.read(buffer) != -1) {}
    }
    return toHex(digest.digest());
  }

  private String toHex(byte[] hashBytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : hashBytes) sb.append(String.format("%02x", b));
    return sb.toString();
  }

  private String computeChecksum(Path file, String algorithm) {
    try (InputStream fis = Files.newInputStream(file)) {
      MessageDigest digest = MessageDigest.getInstance(algorithm);
      byte[] buf = new byte[8192];
      int n;
      while ((n = fis.read(buf)) > 0) {
        digest.update(buf, 0, n);
      }
      byte[] hash = digest.digest();

      StringBuilder sb = new StringBuilder();
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      log.error("Checksum calculation failed: {}", e.getMessage());
      return null;
    }
  }
}
