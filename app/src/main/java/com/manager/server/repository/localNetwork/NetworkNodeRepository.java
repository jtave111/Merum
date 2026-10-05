package com.manager.server.repository.localNetwork;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manager.server.model.entity.localNetwork.NetworkNode;

public interface NetworkNodeRepository extends JpaRepository <NetworkNode, Long> {
    Optional<NetworkNode> findByMacAddress(String macAddress);
}
