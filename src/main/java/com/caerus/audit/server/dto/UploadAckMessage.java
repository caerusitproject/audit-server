package com.caerus.audit.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadAckMessage {
  private String type;
  private String uploadId;
  private String fileName;
  private boolean success;
}
