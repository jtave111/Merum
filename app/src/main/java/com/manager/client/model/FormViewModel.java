package com.manager.client.model;

/** A named group of static fields rendered as either a builder or compact form. */
public record FormViewModel(String tab, String title, String[][] fields) {
}
