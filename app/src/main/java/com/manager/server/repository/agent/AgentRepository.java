package com.manager.server.repository.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.manager.server.model.entity.agent.Agent;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface    AgentRepository extends JpaRepository<Agent, UUID> {

    Optional<Agent> findByIpv4(String ipv4);

    Optional<Agent> findByMacAddress(String macAddress);
    
}
