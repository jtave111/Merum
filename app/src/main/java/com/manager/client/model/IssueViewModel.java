package com.manager.client.model;

/** Display-only scanner/report issue. */
public record IssueViewModel(
        Severity severity,
        String title,
        String host,
        String confidence,
        String detail
) {
    public enum Severity { HIGH, MEDIUM, LOW, INFORMATION }
}
