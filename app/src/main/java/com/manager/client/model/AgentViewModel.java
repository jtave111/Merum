package com.manager.client.model;

/** Display-only agent state used by Swing views. */
public record AgentViewModel(
        String id,
        String status,
        String ipAddress,
        String macAddress,
        String hostname,
        String user,
        String privilege,
        String operatingSystem,
        String process,
        int processId,
        String architecture,
        String lastSeen,
        String actions
) {
    public boolean isOnline() {
        return "ONLINE".equals(status);
    }

    public boolean isIdle() {
        return "IDLE".equals(status);
    }

    public boolean isLost() {
        return "LOST".equals(status);
    }

    public boolean isRoot() {
        return "root".equalsIgnoreCase(privilege) || "SYSTEM".equals(privilege);
    }

    public Object[] toRow() {
        return new Object[]{
                id, status, ipAddress, macAddress, hostname, user, privilege,
                operatingSystem, process, processId, architecture, lastSeen, actions
        };
    }
}
