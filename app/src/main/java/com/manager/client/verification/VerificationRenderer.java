package com.manager.client.verification;

import com.formdev.flatlaf.FlatDarkLaf;
import com.manager.client.data.MerumData;
import com.manager.client.ui.MainContentPane;
import com.manager.client.ui.components.SectionHeader;
import com.manager.client.ui.theme.Theme;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VerificationRenderer {
    private static final int WIDTH = 1440;
    private static final int HEIGHT = 900;

    private VerificationRenderer() {
    }

    public static void render(Path output) {
        assertTheme();
        try {
            SwingUtilities.invokeAndWait(() -> renderRoute("dashboard", output.toAbsolutePath().normalize()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Verification render was interrupted", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Verification render failed", exception.getCause());
        }
    }

    public static void renderAll(Path outputDirectory) {
        assertTheme();
        Path directory = outputDirectory.toAbsolutePath().normalize();
        try {
            SwingUtilities.invokeAndWait(() -> {
                smokeEveryView();
                renderRoute("dashboard", directory.resolve("dashboard.png"));
                renderRoute("agents", directory.resolve("agents.png"));
                renderRoute("arsenal", directory.resolve("arsenal.png"));
                renderRoute("reports", directory.resolve("reports.png"));
            });
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Verification render was interrupted", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Verification render failed", exception.getCause());
        }
        System.out.println("VERIFICATION_OK");
        System.out.println("views=" + MainContentPane.viewKeys().size() + "/" + MainContentPane.viewKeys().size() + " OK");
        printTheme();
        System.out.println("directory=" + directory);
    }

    private static void smokeEveryView() {
        MainContentPane shell = new MainContentPane(new MerumData());
        shell.setSize(WIDTH, HEIGHT);
        try {
            for (String key : MainContentPane.viewKeys()) {
                shell.openViewForVerification(key);
                layoutRecursively(shell);
                assertDataLayers(shell, key);
                paint(shell);
                System.out.println("view." + key + "=OK");
            }
        } finally {
            shell.shutdown();
        }
    }

    private static void renderRoute(String key, Path output) {
        MainContentPane shell = new MainContentPane(new MerumData());
        shell.setSize(WIDTH, HEIGHT);
        if (!"dashboard".equals(key)) {
            shell.openViewForVerification(key);
        }
        layoutRecursively(shell);
        prepareEvidenceState(key, shell);
        layoutRecursively(shell);
        BufferedImage image = paint(shell);
        shell.shutdown();
        write(image, output);
        System.out.println("render." + key + "=" + output);
    }

    private static void prepareEvidenceState(String key, Component component) {
        if ("agents".equals(key) && component instanceof JTable table
                && table.getName() != null && table.getName().startsWith("Agents —")
                && table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            Component renderer = table.prepareRenderer(table.getCellRenderer(0, 0), 0, 0);
            if (!Theme.SELECTION.equals(renderer.getBackground())) {
                throw new IllegalStateException("Agents row selection must be "
                        + Theme.hex(Theme.SELECTION) + ", got " + Theme.hex(renderer.getBackground()));
            }
            if (!Theme.SELECTED_TEXT.equals(renderer.getForeground())) {
                throw new IllegalStateException("Agents selected text must be "
                        + Theme.hex(Theme.SELECTED_TEXT) + ", got " + Theme.hex(renderer.getForeground()));
            }
        }
        if ("reports".equals(key) && component instanceof JTabbedPane tabs) {
            for (int index = 0; index < tabs.getTabCount(); index++) {
                if ("Issue Activity".equals(tabs.getTitleAt(index))) {
                    tabs.setSelectedIndex(index);
                }
            }
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                prepareEvidenceState(key, child);
            }
        }
    }

    private static BufferedImage paint(MainContentPane shell) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Theme.CHROME);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            shell.printAll(graphics);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static void write(BufferedImage image, Path output) {
        try {
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!ImageIO.write(image, "png", output.toFile())) {
                throw new IllegalStateException("No PNG writer is installed");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write verification image to " + output, exception);
        }
    }

    private static void assertTheme() {
        if (!(UIManager.getLookAndFeel() instanceof FlatDarkLaf)) {
            throw new IllegalStateException("Expected FlatDarkLaf, got " + UIManager.getLookAndFeel());
        }
        Color accent = UIManager.getColor("Component.accentColor");
        if (!Theme.ACCENT.equals(accent)) {
            throw new IllegalStateException("Expected FlatDarkLaf accent "
                    + Theme.hex(Theme.ACCENT) + ", got " + Theme.hex(accent));
        }
        Color chrome = UIManager.getColor("Panel.background");
        if (!Theme.CHROME.equals(chrome)) {
            throw new IllegalStateException("Expected chrome #3C3F41, got " + Theme.hex(chrome));
        }
        Color selection = UIManager.getColor("Tree.selectionBackground");
        if (!Theme.SELECTION.equals(selection)) {
            throw new IllegalStateException("Expected selection "
                    + Theme.hex(Theme.SELECTION) + ", got " + Theme.hex(selection));
        }
        assertUiColor("Component.focusColor", Theme.ACCENT);
        assertUiColor("Component.focusedBorderColor", Theme.ACCENT);
        assertUiColor("Viewport.background", Theme.DARK_CONTENT_BG);
        assertUiColor("Table.background", Theme.DARK_CONTENT_BG);
        assertUiColor("TextArea.background", Theme.DARK_CONTENT_BG);
        assertUiColor("List.background", Theme.DARK_CONTENT_BG);
        assertUiColor("Table.selectionBackground", Theme.SELECTION);
        assertUiColor("Table.selectionForeground", Theme.SELECTED_TEXT);
        assertUiColor("Tree.selectionForeground", Theme.SELECTED_TEXT);
        assertUiColor("List.selectionForeground", Theme.SELECTED_TEXT);
        assertUiColor("TabbedPane.underlineColor", Theme.ACCENT);
        assertUiColor("Table.gridColor", Theme.DIVIDER);
        assertUiColor("TableHeader.background", Theme.CHROME);
        assertUiColor("ToolBar.background", Theme.DARK_CONTENT_BG);
        assertUiColor("TabbedPane.contentAreaColor", Theme.CHROME);
    }

    private static void printTheme() {
        System.out.println("laf=" + UIManager.getLookAndFeel().getClass().getName());
        System.out.println("accent=" + Theme.hex(UIManager.getColor("Component.accentColor")));
        System.out.println("focusRing=" + Theme.hex(UIManager.getColor("Component.focusColor")));
        System.out.println("chrome=" + Theme.hex(UIManager.getColor("Panel.background")));
        System.out.println("content=" + Theme.hex(UIManager.getColor("Table.background")));
        System.out.println("tableHeader=" + Theme.hex(UIManager.getColor("TableHeader.background")));
        System.out.println("grid=" + Theme.hex(UIManager.getColor("Table.gridColor")));
        System.out.println("selection=" + Theme.hex(UIManager.getColor("Tree.selectionBackground")));
        System.out.println("selectedText=" + Theme.hex(UIManager.getColor("Tree.selectionForeground")));
        System.out.println("size=" + WIDTH + "x" + HEIGHT);
    }

    private static void assertDataLayers(Component component, String key) {
        if (component instanceof JTable table) {
            assertComponentColor(key + " table", table.getBackground(), Theme.DARK_CONTENT_BG);
            assertViewportColor(key + " table viewport", table);
            if (table.getTableHeader() != null) {
                assertComponentColor(key + " table header", table.getTableHeader().getBackground(), Theme.CHROME);
            }
        }
        if (component instanceof JTextArea area) {
            assertComponentColor(key + " text area", area.getBackground(), Theme.DARK_CONTENT_BG);
            assertViewportColor(key + " text area viewport", area);
        }
        if (component instanceof JList<?> list) {
            assertComponentColor(key + " list", list.getBackground(), Theme.DARK_CONTENT_BG);
            assertViewportColor(key + " list viewport", list);
        }
        if (component instanceof SectionHeader header) {
            assertComponentColor(key + " section header", header.getBackground(), Theme.CHROME);
        }
        if (component instanceof JToolBar toolbar) {
            assertComponentColor(key + " toolbar", toolbar.getBackground(), Theme.DARK_CONTENT_BG);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                assertDataLayers(child, key);
            }
        }
    }

    private static void assertViewportColor(String label, JComponent component) {
        if (!(component.getParent() instanceof JViewport viewport)) {
            throw new IllegalStateException(label + " is not hosted by a viewport");
        }
        assertComponentColor(label, viewport.getBackground(), Theme.DARK_CONTENT_BG);
    }

    private static void assertUiColor(String key, Color expected) {
        assertComponentColor(key, UIManager.getColor(key), expected);
    }

    private static void assertComponentColor(String label, Color actual, Color expected) {
        if (!expected.equals(actual)) {
            String actualHex = actual == null ? "null" : Theme.hex(actual);
            throw new IllegalStateException(label + " must be " + Theme.hex(expected) + ", got " + actualHex);
        }
    }

    private static void layoutRecursively(Component component) {
        if (component instanceof Container container) {
            container.doLayout();
            for (Component child : container.getComponents()) {
                layoutRecursively(child);
            }
        }
        if (component instanceof JComponent swingComponent) {
            swingComponent.revalidate();
        }
    }
}
