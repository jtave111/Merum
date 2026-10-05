package com.manager.client.ui.shell;

import com.manager.client.ui.theme.Theme;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class TabBar extends JPanel {
    private final JTabbedPane tabs;
    private final Map<String, Component> contentByKey = new LinkedHashMap<>();
    private final Map<String, TabHeader> headerByKey = new LinkedHashMap<>();
    private final Consumer<String> selectionListener;
    private final Runnable emptyListener;

    public TabBar(Consumer<String> selectionListener, Runnable emptyListener) {
        super(new BorderLayout());
        this.selectionListener = selectionListener;
        this.emptyListener = emptyListener;
        setBackground(Theme.DARK_CONTENT_BG);

        tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setBackground(Theme.DARK_CONTENT_BG);
        tabs.putClientProperty(
                "FlatLaf.style",
                "background: " + Theme.hex(Theme.DARK_CONTENT_BG)
                        + "; selectedBackground: " + Theme.hex(Theme.DARK_CONTENT_BG)
                        + "; contentAreaColor: " + Theme.hex(Theme.DARK_CONTENT_BG)
        );
        tabs.setBorder(null);
        tabs.setFont(Theme.UI_FONT.deriveFont(12f));
        tabs.addChangeListener(event -> {
            refreshHeaders();
            String key = selectedKey();
            if (key != null) {
                selectionListener.accept(key);
            }
        });
        add(tabs, BorderLayout.CENTER);
    }

    public void open(String key, String label, String icon, Component content) {
        Component existing = contentByKey.get(key);
        if (existing != null) {
            tabs.setSelectedComponent(existing);
            return;
        }

        contentByKey.put(key, content);
        tabs.addTab(label, content);
        int index = tabs.indexOfComponent(content);
        TabHeader header = new TabHeader(key, icon, label);
        headerByKey.put(key, header);
        tabs.setTabComponentAt(index, header);
        tabs.setSelectedIndex(index);
        refreshHeaders();
    }

    public void select(String key) {
        Component component = contentByKey.get(key);
        if (component != null) {
            tabs.setSelectedComponent(component);
        }
    }

    public String selectedKey() {
        Component selected = tabs.getSelectedComponent();
        if (selected == null) {
            return null;
        }
        return contentByKey.entrySet().stream()
                .filter(entry -> entry.getValue() == selected)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public void closeSelected() {
        String key = selectedKey();
        if (key != null) {
            close(key);
        }
    }

    private void close(String key) {
        Component component = contentByKey.remove(key);
        headerByKey.remove(key);
        if (component != null) {
            tabs.remove(component);
        }
        if (tabs.getTabCount() == 0) {
            emptyListener.run();
        }
        refreshHeaders();
    }

    private void refreshHeaders() {
        String selected = selectedKey();
        headerByKey.forEach((key, header) -> header.setSelected(key.equals(selected)));
    }

    private final class TabHeader extends JPanel {
        private final JLabel icon;
        private final JLabel title;

        private TabHeader(String key, String iconText, String titleText) {
            super(new FlowLayout(FlowLayout.LEFT, 5, 0));
            setOpaque(false);
            icon = new JLabel(iconText);
            icon.setFont(Theme.UI_FONT.deriveFont(11f));
            title = new JLabel(titleText);
            title.setFont(Theme.UI_FONT.deriveFont(12f));
            JButton close = new JButton("×");
            close.setFocusable(false);
            close.setBorderPainted(false);
            close.setContentAreaFilled(false);
            close.setForeground(Theme.TEXT_MUTED);
            close.setFont(Theme.UI_FONT.deriveFont(Font.PLAIN, 12f));
            close.setMargin(new java.awt.Insets(0, 2, 0, 2));
            close.addActionListener(event -> TabBar.this.close(key));
            add(icon);
            add(title);
            add(close);
            setSelected(false);
        }

        private void setSelected(boolean selected) {
            icon.setForeground(selected ? Theme.ACCENT : Theme.TEXT_MUTED);
            title.setForeground(selected ? Theme.TEXT_PRIMARY : Theme.SEVERITY_INFORMATION);
        }
    }
}
