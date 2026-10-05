package com.manager.client.ui.shell;

import com.manager.client.Branding;
import com.manager.client.ui.theme.Theme;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import java.awt.Dimension;
import java.awt.Font;

public final class StatusBar extends JPanel {
    private final JLabel agentLabel;

    public StatusBar() {
        super();
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
        setBackground(Theme.GUTTER);
        setBorder(Theme.matte(1, 0, 0, 0, Theme.ACCENT));
        setPreferredSize(new Dimension(10, 21));

        add(segment("0.0.0.0:4444", Theme.TEXT_PRIMARY));
        add(separator());
        add(segment("DB: CONNECTED", Theme.TEXT_PRIMARY));
        add(separator());
        agentLabel = segment("Agents: 0 / 0", Theme.TEXT_MUTED);
        add(agentLabel);
        add(Box.createHorizontalGlue());
        add(segment("⌘K — Command Palette · Ctrl+W — Close Tab", Theme.TEXT_MUTED));
        add(separator());
        add(segment(Branding.DISPLAY_NAME, Theme.TEXT_MUTED));
    }

    public void updateAgentStats(int online, int total) {
        agentLabel.setText("Agents: " + online + " / " + total);
        agentLabel.setForeground(online > 0 ? Theme.ACCENT : Theme.TEXT_MUTED);
    }

    private JLabel segment(String text, java.awt.Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(Theme.MONO_FONT.deriveFont(Font.PLAIN, 10f));
        label.setBorder(Theme.padding(0, 8, 0, 8));
        label.setAlignmentY(CENTER_ALIGNMENT);
        return label;
    }

    private JSeparator separator() {
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setForeground(Theme.BORDER);
        separator.setMaximumSize(new Dimension(1, 15));
        return separator;
    }
}
