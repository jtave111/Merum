package com.manager.client.ui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public final class Theme {
    public static final Color CHROME = color(0x3C3F41);
    public static final Color DARK_CONTENT_BG = color(0x2B2B2B);
    public static final Color EDITOR = DARK_CONTENT_BG;
    public static final Color GUTTER = color(0x313335);
    public static final Color COMPONENT = color(0x45494A);
    public static final Color TEXT_PRIMARY = color(0xBBBBBB);
    public static final Color CODE_TEXT = color(0xA9B7C6);
    public static final Color TEXT_MUTED = color(0x606366);
    public static final Color ACCENT = color(0xCC4747);
    public static final Color SELECTION = color(0xB23A3A);
    public static final Color SELECTED_TEXT = color(0xF5F5F5);
    public static final Color DIVIDER = color(0x323232);
    public static final Color BORDER = color(0x515151);
    public static final Color SEVERITY_HIGH = color(0xE05561);
    public static final Color SEVERITY_MEDIUM = color(0xE0A44E);
    public static final Color SEVERITY_LOW = color(0x5A9BD4);
    public static final Color SEVERITY_INFORMATION = color(0x8A8D90);

    public static final Font UI_FONT = new Font(findFont("Inter", Font.SANS_SERIF), Font.PLAIN, 12);
    public static final Font MONO_FONT = new Font(findFont("JetBrains Mono", Font.MONOSPACED), Font.PLAIN, 12);

    private Theme() {
    }

    public static void setup() {
        Properties palette = loadPalette();
        requirePaletteValue(palette, "chrome", "#3C3F41");
        requirePaletteValue(palette, "darkContentBg", "#2B2B2B");
        requirePaletteValue(palette, "editor", "#2B2B2B");
        requirePaletteValue(palette, "accent", "#CC4747");
        requirePaletteValue(palette, "selection", "#B23A3A");
        requirePaletteValue(palette, "selectedText", "#F5F5F5");
        requirePaletteValue(palette, "@accentColor", "#CC4747");
        requirePaletteValue(palette, "@accentBaseColor", "#CC4747");
        requirePaletteValue(palette, "Component.accentColor", "#CC4747");

        FlatLaf.setGlobalExtraDefaults(Map.of(
                "@accentColor", hex(ACCENT),
                "@accentBaseColor", hex(ACCENT),
                "@accentSelectionBackground", hex(SELECTION),
                "@selectionBackground", hex(SELECTION),
                "@selectionInactiveBackground", hex(SELECTION),
                "@selectionForeground", hex(SELECTED_TEXT),
                "@selectionInactiveForeground", hex(SELECTED_TEXT),
                "Component.accentColor", hex(ACCENT)
        ));

        if (!FlatDarkLaf.setup()) {
            throw new IllegalStateException("FlatDarkLaf could not be installed");
        }

        UIManager.put("defaultFont", new FontUIResource(UI_FONT));
        UIManager.put("Component.accentColor", ACCENT);
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("Component.focusedBorderColor", ACCENT);
        UIManager.put("Panel.background", CHROME);
        UIManager.put("Viewport.background", DARK_CONTENT_BG);
        UIManager.put("ToolBar.background", DARK_CONTENT_BG);
        UIManager.put("MenuBar.background", DARK_CONTENT_BG);
        UIManager.put("MenuBar.foreground", TEXT_PRIMARY);
        UIManager.put("Menu.foreground", TEXT_PRIMARY);
        UIManager.put("MenuItem.foreground", TEXT_PRIMARY);
        UIManager.put("MenuItem.selectionBackground", SELECTION);
        UIManager.put("MenuItem.selectionForeground", SELECTED_TEXT);
        UIManager.put("TextField.background", DARK_CONTENT_BG);
        UIManager.put("TextField.foreground", CODE_TEXT);
        UIManager.put("TextField.selectionBackground", SELECTION);
        UIManager.put("TextField.selectionForeground", SELECTED_TEXT);
        UIManager.put("TextArea.background", DARK_CONTENT_BG);
        UIManager.put("TextArea.foreground", CODE_TEXT);
        UIManager.put("TextArea.selectionBackground", SELECTION);
        UIManager.put("TextArea.selectionForeground", SELECTED_TEXT);
        UIManager.put("Table.background", DARK_CONTENT_BG);
        UIManager.put("Table.foreground", CODE_TEXT);
        UIManager.put("Table.selectionBackground", SELECTION);
        UIManager.put("Table.selectionInactiveBackground", SELECTION);
        UIManager.put("Table.selectionForeground", SELECTED_TEXT);
        UIManager.put("Table.selectionInactiveForeground", SELECTED_TEXT);
        UIManager.put("Table.gridColor", DIVIDER);
        UIManager.put("TableHeader.background", CHROME);
        UIManager.put("TableHeader.foreground", TEXT_MUTED);
        UIManager.put("Tree.background", DARK_CONTENT_BG);
        UIManager.put("Tree.foreground", TEXT_PRIMARY);
        UIManager.put("Tree.selectionBackground", SELECTION);
        UIManager.put("Tree.selectionInactiveBackground", SELECTION);
        UIManager.put("Tree.selectionForeground", SELECTED_TEXT);
        UIManager.put("Tree.selectionInactiveForeground", SELECTED_TEXT);
        UIManager.put("List.background", DARK_CONTENT_BG);
        UIManager.put("List.foreground", CODE_TEXT);
        UIManager.put("List.selectionBackground", SELECTION);
        UIManager.put("List.selectionInactiveBackground", SELECTION);
        UIManager.put("List.selectionForeground", SELECTED_TEXT);
        UIManager.put("List.selectionInactiveForeground", SELECTED_TEXT);
        UIManager.put("TabbedPane.background", CHROME);
        UIManager.put("TabbedPane.contentAreaColor", CHROME);
        UIManager.put("TabbedPane.selectedBackground", CHROME);
        UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("TabbedPane.inactiveUnderlineColor", TEXT_MUTED);
        UIManager.put("TabbedPane.tabHeight", 28);
        UIManager.put("ScrollBar.track", DARK_CONTENT_BG);
        UIManager.put("ScrollBar.thumb", BORDER);
        UIManager.put("Separator.foreground", DIVIDER);
        UIManager.put("SplitPaneDivider.draggingColor", ACCENT);
    }

    public static Border matte(int top, int left, int bottom, int right, Color color) {
        return BorderFactory.createMatteBorder(top, left, bottom, right, color);
    }

    public static Border padding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static String hex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static Color color(int rgb) {
        return new Color(rgb);
    }

    private static String findFont(String preferred, String fallback) {
        if (GraphicsEnvironment.isHeadless()) {
            return fallback;
        }
        return Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
                .anyMatch(preferred::equalsIgnoreCase) ? preferred : fallback;
    }

    private static Properties loadPalette() {
        Properties properties = new Properties();
        try (InputStream input = Theme.class.getResourceAsStream("/themes/merum-flatlaf.properties")) {
            if (input == null) {
                throw new IllegalStateException("Missing /themes/merum-flatlaf.properties");
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load the Merum FlatLaf palette", exception);
        }
    }

    private static void requirePaletteValue(Properties properties, String key, String expected) {
        String actual = properties.getProperty(key);
        if (!expected.equalsIgnoreCase(actual)) {
            throw new IllegalStateException("Palette " + key + " must equal " + expected + ", got " + actual);
        }
    }
}
