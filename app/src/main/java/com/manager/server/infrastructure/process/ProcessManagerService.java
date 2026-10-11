package com.manager.server.infrastructure.process;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


@Service
public class ProcessManagerService {

    private final ConcurrentMap<UUID, ManagedProcess> activeProcesses =
            new ConcurrentHashMap<>();

    public UUID register(String name, Process p){
        UUID id = UUID.randomUUID();

        ManagedProcess managedProcess = new ManagedProcess(
            id,
            name,
            p,
            Instant.now()
        );

        activeProcesses.put(id, managedProcess);
        return id;
    }

    public boolean isRunning(UUID id){
        if(id == null) return false;

        ManagedProcess managedProcess = activeProcesses.get(id);

        return managedProcess != null && managedProcess.isAlive();
    }

    public boolean unregister(UUID id, ManagedProcess managedProcess){
        if(id == null) return false;

        managedProcess = activeProcesses.get(id);

        if(managedProcess == null ) return false;

        return  activeProcesses.remove(id, managedProcess);


    }

    public boolean stop(UUID id){

        if (id == null) return false;

        ManagedProcess managedProcess = activeProcesses.get(id);

        if(managedProcess !=  null && !managedProcess.isAlive() ){
            this.unregister(id, managedProcess);
            return false;
        }

        Process process = managedProcess.process();

        try {
            process.destroy();

            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();

                if (!process.waitFor(2, TimeUnit.SECONDS)) {
                    return false;
                }
            }

            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            return false;

        } finally {
            if(!process.isAlive()){
                this.unregister(id, managedProcess) ;
            }
        }

    }


}
