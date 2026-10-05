package com.manager.server.repository.localNetwork;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manager.server.model.entity.localNetwork.Port;

public interface PortRepository extends JpaRepository<Port, Long> {

    Optional<Integer> findByNumber(Integer portNumber);
}
