package com.manager.client.ui.shell;

import com.manager.client.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

public final class BottomPanel extends JPanel {
    public BottomPanel() {
        super(new BorderLayout());
        setBackground(Theme.EDITOR);
        setBorder(Theme.matte(1, 0, 0, 0, Theme.BORDER));
        setPreferredSize(new Dimension(10, 188));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setFont(Theme.UI_FONT.deriveFont(11f));
        tabs.addTab("Event Log", logArea("[*] Waiting for agent events…"));
        tabs.addTab("Output", logArea("[*] No output — run a command or script to see output here."));
        add(tabs, BorderLayout.CENTER);

        JPanel command = new JPanel(new BorderLayout(6, 0));
        command.setBackground(Theme.EDITOR);
        command.setBorder(BorderFactory.createCompoundBorder(
                Theme.matte(1, 0, 0, 0, Theme.BORDER),
                Theme.padding(1, 9, 1, 9)
        ));
        command.setPreferredSize(new Dimension(10, 25));
        JLabel prompt = new JLabel("$>");
        prompt.setForeground(Theme.ACCENT);
        prompt.setFont(Theme.MONO_FONT.deriveFont(Font.BOLD, 12f));
        JTextField input = new JTextField();
        input.setBorder(BorderFactory.createEmptyBorder());
        input.setBackground(Theme.EDITOR);
        input.setForeground(Theme.CODE_TEXT);
        input.setFont(Theme.MONO_FONT.deriveFont(11f));
        input.putClientProperty("JTextField.placeholderText", "c2 command...");
        command.add(prompt, BorderLayout.WEST);
        command.add(input, BorderLayout.CENTER);
        add(command, BorderLayout.SOUTH);
    }

    private JScrollPane logArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setBackground(Theme.EDITOR);
        area.setForeground(Theme.TEXT_MUTED);
        area.setFont(Theme.MONO_FONT.deriveFont(11f));
        area.setBorder(Theme.padding(7, 10, 7, 10));
        JScrollPane scroll = new JScrollPane(area);
        scroll.getViewport().setBackground(Theme.EDITOR);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }
}
