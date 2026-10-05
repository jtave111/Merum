package com.manager.server.repository.c2server;

import com.manager.server.model.entity.c2Server.PortConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortConfigRepository extends JpaRepository<PortConfig, Long> {

    List<PortConfig> findByNetworkId(Long networkId);
}
