package com.manager.client.model;

/** Display-only C2 health summary. */
public record C2InfoViewModel(
        String threads,
        String uptime,
        String memory,
        String cpuLoad,
        String diskFree
) {
}
