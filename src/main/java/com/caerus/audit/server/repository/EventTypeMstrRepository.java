package com.caerus.audit.server.repository;

import com.caerus.audit.server.entity.EventTypeMstr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventTypeMstrRepository extends JpaRepository<EventTypeMstr, Byte> {}
