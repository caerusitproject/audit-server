package com.caerus.audit.server.service;

import com.caerus.audit.server.dto.ServerAppSettingsDto;
import com.caerus.audit.server.entity.ServerAppSettings;
import com.caerus.audit.server.enums.ErrorType;
import com.caerus.audit.server.enums.EventType;
import com.caerus.audit.server.exception.ServerSettingsException;
import com.caerus.audit.server.repository.ServerAppSettingsRepository;
import com.caerus.audit.server.util.PatternValidator;
import com.caerus.audit.server.util.ServerAppSettingsMapper;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
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
      try {
          cache.set(getLatest());
          log.info("Preloaded ServerAppSettings into cache");
      } catch (Exception e) {
          log.warn("Failed to preload ServerAppSettings: {}", e.getMessage());
          loggingService.logError(ErrorType.NORMAL, "Failed to preload settings", e.getClass().getName());
      }
  }

  public ServerAppSettingsDto getLatest() {
    ServerAppSettingsDto cached = cache.get();
    if (cached != null) return cached;

    Optional<ServerAppSettings> settingRes = serverAppSettingsRepository.findTopByOrderBySettingIdDesc();
    ServerAppSettingsDto dto = settingRes.map(ServerAppSettingsMapper::toDto).orElse(null);
    cache.set(dto);
    return dto;
  }

  @Transactional
  public ServerAppSettingsDto update(ServerAppSettingsDto updateDto) {
    try {
        PatternValidator.validate(updateDto.getFolderStructureTemplate());
        ServerAppSettings entity = ServerAppSettingsMapper.toEntity(updateDto);

      entity.setSettingId(null);
      ServerAppSettings saved = serverAppSettingsRepository.save(entity);
      ServerAppSettingsDto savedDto = ServerAppSettingsMapper.toDto(saved);
      cache.set(savedDto);

      eventPublisher.publishEvent(new SettingsChangedEvent(this, savedDto));
      loggingService.logEvent(
          EventType.SERVER_SETTING_PUSHED,
          "Server settings updated and pushed to all clients",
          "System");
      log.info("Server settings updated and cached (id={})", savedDto.getSettingId());
      return savedDto;

    }catch (DataAccessException dae) {
        loggingService.logError(ErrorType.NORMAL, "Error updating server settings due to: " + dae.getMessage(), dae.getClass().getName());
        throw new ServerSettingsException("Database error while updating server settings", dae);
    }
    catch (Exception e) {
        loggingService.logError(ErrorType.NORMAL, "Error updating server settings due to: " + e.getMessage(), e.getClass().getName());
        throw new ServerSettingsException("Unexpected error while updating server settings", e);
    }
  }
}
