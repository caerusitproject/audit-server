package com.caerus.audit.server.dto;

public record FileUploadResult(boolean success, String filePath, String message) {
}
