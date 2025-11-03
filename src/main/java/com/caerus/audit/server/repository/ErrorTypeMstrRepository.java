package com.caerus.audit.server.repository;

import com.caerus.audit.server.entity.ErrorTypeMstr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorTypeMstrRepository extends JpaRepository<ErrorTypeMstr, Byte> {}
