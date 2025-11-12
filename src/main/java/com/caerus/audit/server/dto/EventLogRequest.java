package com.caerus.audit.server.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventLogRequest {
  private Byte eventTypeId;
  private String eventDesc;
  private String eventSource;
  private String eventSrcIPAddr;
  private Instant eventDTime;
}
