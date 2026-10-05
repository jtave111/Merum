package com.manager.client.ui.components;

import com.manager.client.ui.theme.Theme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class StatCard extends JPanel {
    private final JLabel valueLabel;
    private final JLabel metaLabel;

    public StatCard(String label, String value, String meta, Color valueColor) {
        super(new GridBagLayout());
        setBackground(Theme.CHROME);
        setBorder(Theme.matte(0, 0, 0, 1, Theme.BORDER));
        setPreferredSize(new Dimension(150, 84));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 14, 0, 12);

        JLabel titleLabel = new JLabel(label);
        titleLabel.setForeground(Theme.TEXT_MUTED);
        titleLabel.setFont(Theme.UI_FONT.deriveFont(Font.PLAIN, 9f));
        constraints.gridy = 0;
        constraints.insets = new Insets(7, 14, 3, 12);
        add(titleLabel, constraints);

        valueLabel = new JLabel(value);
        valueLabel.setForeground(valueColor);
        valueLabel.setFont(Theme.UI_FONT.deriveFont(Font.BOLD, 22f));
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 14, 1, 12);
        add(valueLabel, constraints);

        metaLabel = new JLabel(meta);
        metaLabel.setForeground(Theme.SEVERITY_INFORMATION);
        metaLabel.setFont(Theme.UI_FONT.deriveFont(Font.PLAIN, 10f));
        constraints.gridy = 2;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 14, 5, 12);
        add(metaLabel, constraints);
    }

    public void setValue(String value, Color color) {
        valueLabel.setText(value);
        valueLabel.setForeground(color);
    }

    public void setMeta(String meta) {
        metaLabel.setText(meta);
    }
}
