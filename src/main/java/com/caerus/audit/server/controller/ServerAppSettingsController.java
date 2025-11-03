package com.caerus.audit.server.controller;

import com.caerus.audit.server.dto.ServerAppSettingsDto;
import com.caerus.audit.server.service.ServerAppSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Validated
public class ServerAppSettingsController {

  private final ServerAppSettingsService serverAppSettingsService;

  @GetMapping("/latest")
  public ResponseEntity<ServerAppSettingsDto> getLatest() {
    ServerAppSettingsDto dto = serverAppSettingsService.getLatest();
    if (dto == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(dto);
  }

  @PutMapping
  public ResponseEntity<ServerAppSettingsDto> update(
      @RequestBody @Valid ServerAppSettingsDto updateDto) {
    ServerAppSettingsDto dto = serverAppSettingsService.update(updateDto);
    return ResponseEntity.ok(dto);
  }
}
