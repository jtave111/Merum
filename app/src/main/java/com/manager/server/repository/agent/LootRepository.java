package com.manager.server.repository.agent;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manager.server.model.entity.agent.Loot;

import java.util.UUID;

public interface LootRepository extends JpaRepository<Loot, UUID> {

}
