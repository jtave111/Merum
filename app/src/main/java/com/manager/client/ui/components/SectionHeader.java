package com.manager.client.ui.components;

import com.manager.client.ui.theme.Theme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

public final class SectionHeader extends JPanel {
    public SectionHeader(String title) {
        super(new BorderLayout());
        setBackground(Theme.CHROME);
        setBorder(Theme.matte(0, 0, 1, 0, Theme.BORDER));
        setPreferredSize(new Dimension(10, 25));

        JLabel label = new JLabel(title.toUpperCase());
        label.setForeground(Theme.TEXT_MUTED);
        label.setFont(Theme.UI_FONT.deriveFont(Font.BOLD, 11f));
        label.setBorder(Theme.padding(0, 10, 0, 10));
        add(label, BorderLayout.CENTER);
    }
}
