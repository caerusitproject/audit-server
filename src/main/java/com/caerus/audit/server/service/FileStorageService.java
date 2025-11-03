package com.caerus.audit.server.service;

import com.caerus.audit.server.dto.FileUploadResult;
import com.caerus.audit.server.dto.ServerAppSettingsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ServerAppSettingsService serverAppSettingsService;

    public FileUploadResult saveAndVerifyFile(MultipartFile file, String clientId){
        try{
            //Load destination path from server settings
            ServerAppSettingsDto settings = serverAppSettingsService.getLatest();
            String baseDir = settings.getConfigDestFolderPath();
            if (baseDir == null || baseDir.isBlank()) {
                throw new IllegalStateException("Destination folder not configured in settings");
            }

            //Build folder structure {HOSTNAME}/{YEAR}/{MONTH}/{DAY}
            String hostname = clientId;
            LocalDateTime now = LocalDateTime.now();
            String relativePath = String.format("%s/%d/%02d/%02d",
                    hostname, now.getYear(), now.getMonthValue(), now.getDayOfMonth());

            Path destDir = Paths.get(baseDir, relativePath);
            Files.createDirectories(destDir);

            String timestamp = now.format(DateTimeFormatter.ofPattern("HHmmssSSS"));
            String ext = getFileExtension(file.getOriginalFilename());
            Path destFile = destDir.resolve(timestamp + (ext.isEmpty() ? "" : "." + ext));

            String originalChecksum = computeChecksum(file.getInputStream(), "SHA-256");

            Files.copy(file.getInputStream(), destFile, StandardCopyOption.REPLACE_EXISTING);

            String savedChecksum  = computeChecksum(destFile, "SHA-256");
            boolean integrityOk = originalChecksum.equals(savedChecksum);

            return new FileUploadResult(integrityOk, destFile.toString(),
                    integrityOk ? "File saved successfully" : "Integrity check failed");
        } catch (Exception e) {
            log.error("Error saving file for client {}: {}", clientId, e.getMessage(), e);
            return new FileUploadResult(false, null, e.getMessage());
        }
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

    private String computeChecksum(Path file, String algorithm){
        try(InputStream fis = Files.newInputStream(file)){
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buf = new byte[8192];
            int n;
            while((n = fis.read(buf))>0){
                digest.update(buf, 0, n);
            }
            byte[] hash = digest.digest();

            StringBuilder sb = new StringBuilder();
            for(byte b : hash){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Checksum calculation failed: {}", e.getMessage());
            return null;
        }
    }
}
