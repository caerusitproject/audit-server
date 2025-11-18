package com.caerus.audit.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class AuditServerApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(AuditServerApplication.class, args);
  }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AuditServerApplication.class);
    }
}
