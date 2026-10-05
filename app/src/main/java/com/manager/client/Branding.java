package com.manager.client;

public final class Branding {
    private static final String DEVELOPMENT_VERSION = "0.1.0";

    public static final String NAME = "Merum";
    public static final String VERSION = resolveVersion();
    public static final String DISPLAY_NAME = NAME + " v" + VERSION;

    private Branding() {
    }

    private static String resolveVersion() {
        String implementationVersion = Branding.class.getPackage().getImplementationVersion();
        return implementationVersion == null || implementationVersion.isBlank()
                ? DEVELOPMENT_VERSION
                : implementationVersion;
    }
}
