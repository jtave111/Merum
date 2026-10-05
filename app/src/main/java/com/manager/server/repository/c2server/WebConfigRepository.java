package com.manager.server.repository.c2server;

import com.manager.server.model.entity.c2Server.WebConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebConfigRepository extends JpaRepository<WebConfig, Long> {

    Optional<WebConfig> findByServerId(Long serverId);
}
