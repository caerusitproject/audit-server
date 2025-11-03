package com.caerus.audit.server.enums;

public enum ErrorType {
    NORMAL(0),
    CONNECTION_ERROR(1),
    STORAGE_ERROR(2);

    private final int code;
    ErrorType(int code) { this.code = code; }
    public byte getCode() { return (byte) code; }
}
