package com.caerus.audit.server.service;

import com.caerus.audit.server.dto.ServerAppSettingsDto;
import com.caerus.audit.server.entity.ServerAppSettings;
import com.caerus.audit.server.enums.ErrorType;
import com.caerus.audit.server.enums.EventType;
import com.caerus.audit.server.repository.ServerAppSettingsRepository;
import com.caerus.audit.server.util.ServerAppSettingsMapper;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerAppSettingsService {

  private final ServerAppSettingsRepository serverAppSettingsRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final LoggingService loggingService;

  private final AtomicReference<ServerAppSettingsDto> cache = new AtomicReference<>();

  @PostConstruct
  public void preloadCache() {
    cache.set(getLatest());
    log.info("Preloaded ServerAppSettings into cache");
  }

  public ServerAppSettingsDto getLatest() {
    ServerAppSettingsDto cached = cache.get();
    if (cached != null) return cached;

    List<ServerAppSettings> all = serverAppSettingsRepository.findAllOrderByIdDesc();
    ServerAppSettingsDto dto = all.isEmpty() ? null : ServerAppSettingsMapper.toDto(all.get(0));
    cache.set(dto);
    return dto;
  }

  @Transactional
  public ServerAppSettingsDto update(ServerAppSettingsDto updateDto) {
    try {
      ServerAppSettings entity = ServerAppSettingsMapper.toEntity(updateDto);

      entity.setSettingId(null);
      ServerAppSettings saved = serverAppSettingsRepository.save(entity);
      ServerAppSettingsDto savedDto = ServerAppSettingsMapper.toDto(saved);
      cache.set(savedDto);

      // publish event so websocket notifier can push to clients
      eventPublisher.publishEvent(new SettingsChangedEvent(this, savedDto));
      loggingService.logEvent(
          EventType.SERVER_SETTING_PUSHED,
          "Server settings updated and pushed to all clients",
          "System");
      log.info("Server settings updated and cached (id={})", savedDto.getSettingId());
      return savedDto;
    } catch (Exception e) {
      loggingService.logError(ErrorType.NORMAL, e.getMessage(), e.getClass().getName());
      throw e;
    }
  }
}
