package com.caerus.audit.server.service;

import com.caerus.audit.server.dto.ServerAppSettingsDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SettingsChangedEvent extends ApplicationEvent {
  private final ServerAppSettingsDto settings;

  public SettingsChangedEvent(Object source, ServerAppSettingsDto settings) {
    super(source);
    this.settings = settings;
  }
}
