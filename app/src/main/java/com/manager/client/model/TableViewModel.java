package com.manager.client.model;

/** Columns and rows for a dense Swing table; no serialization metadata or transport semantics. */
public record TableViewModel(String[] columns, Object[][] rows) {
}
