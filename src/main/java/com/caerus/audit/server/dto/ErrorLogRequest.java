package com.caerus.audit.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLogRequest {
    private Byte errorTypeId;
    private String errorDesc;
    private String errorSource;
    private String errorSrcIPAddr;
    private Instant errorDTime;
}
