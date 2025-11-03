package com.caerus.audit.server.repository;

import com.caerus.audit.server.entity.ServerAppSettings;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerAppSettingsRepository extends JpaRepository<ServerAppSettings, Short> {

  @Query(value = "SELECT s FROM ServerAppSettings s ORDER BY s.settingId DESC")
  List<ServerAppSettings> findAllOrderByIdDesc();
}
