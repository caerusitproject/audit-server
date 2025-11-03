package com.caerus.audit.server.enums;

public enum EventType {
    NORMAL(0),
    NETWORK_FAILURE(1),
    SERVICE_FAILED(2),
    CLIENT_STORAGE_FULL(3),
    SERVER_SETTING_PUSHED(4),
    CLIENT_CONFIG_REQUEST(5);

    private final int code;
    EventType(int code) { this.code = code; }
    public byte getCode() { return (byte)code; }
}