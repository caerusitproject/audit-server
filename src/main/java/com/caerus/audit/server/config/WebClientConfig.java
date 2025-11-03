package com.caerus.audit.server.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient webClient(WebClient.Builder builder) {
    return builder
        .filter(
            (request, next) -> {
              String correlationId = MDC.get("correlationId");
              ClientRequest filtered =
                  ClientRequest.from(request)
                      .headers(
                          h -> {
                            if (correlationId != null && !correlationId.isBlank()) {
                              h.set("X-Correlation-Id", correlationId);
                            }
                          })
                      .build();
              return next.exchange(filtered);
            })
        .build();
  }
}
