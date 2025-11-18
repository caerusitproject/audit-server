package com.caerus.audit.server.repository;

import com.caerus.audit.server.entity.ServerAppSettings;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerAppSettingsRepository extends JpaRepository<ServerAppSettings, Short> {

    Optional<ServerAppSettings> findTopByOrderBySettingIdDesc();
}
