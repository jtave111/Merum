package com.manager.server.repository.c2server;

import com.manager.server.model.entity.c2Server.SecurityConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityConfigRepository extends JpaRepository<SecurityConfig, Long> {

    Optional<SecurityConfig> findByServerId(Long serverId);
}
