package com.manager.server.repository.c2server;

import com.manager.server.model.entity.c2Server.ServerLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServerLocationRepository extends JpaRepository<ServerLocation, Long> {

    List<ServerLocation> findByServerId(Long serverId);
}
