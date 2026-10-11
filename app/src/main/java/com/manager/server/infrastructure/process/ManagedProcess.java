package com.manager.server.infrastructure.process;

import java.time.Instant;
import java.util.UUID;

public record ManagedProcess(UUID id, String name, Process process, Instant startedAt) {

    public boolean isAlive(){
        return process.isAlive();
    }

    public long pid (){
        return process.pid();
    }
}
