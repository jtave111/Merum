package com.manager.server.repository.c2server;

import com.manager.server.model.entity.c2Server.NetworkConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NetworkConfigRepository extends JpaRepository<NetworkConfig, Long> {

    List<NetworkConfig> findByServerId(Long serverId);
}
