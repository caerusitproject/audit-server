package com.caerus.audit.server.util;

import org.slf4j.MDC;

public final class CorrelationIdHolder {
  private static final String KEY = "correlationId";

  private CorrelationIdHolder() {}

  public static String get() {
    return MDC.get(KEY);
  }

  public static void set(String id) {
    MDC.put(KEY, id);
  }

  public static void clear() {
    MDC.remove(KEY);
  }
}
