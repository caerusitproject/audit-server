package com.caerus.audit.server.util;

import java.util.concurrent.Callable;
import org.slf4j.MDC;

public final class MdcUtils {
  private MdcUtils() {}

  public static void runWith(String correlationId, Runnable r) {
    try {
      if (correlationId != null) MDC.put("correlationId", correlationId);
      r.run();
    } finally {
      MDC.remove("correlationId");
    }
  }

  public static <T> T callWith(String correlationId, Callable<T> c) throws Exception {
    try {
      if (correlationId != null) MDC.put("correlationId", correlationId);
      return c.call();
    } finally {
      MDC.remove("correlationId");
    }
  }
}
