package com.manager.client.ui.components;

import com.manager.client.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class ViewSupport {
    private ViewSupport() { }

    public static JPanel page(String title, String subtitle, Component content) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Theme.CHROME);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CHROME);
        header.setBorder(BorderFactory.createCompoundBorder(
                Theme.matte(0, 0, 1, 0, Theme.BORDER), Theme.padding(6, 10, 6, 10)));
        JLabel name = new JLabel(title.toUpperCase());
        name.setForeground(Theme.TEXT_PRIMARY);
        name.setFont(Theme.UI_FONT.deriveFont(Font.BOLD, 11f));
        header.add(name, BorderLayout.WEST);
        JLabel hint = new JLabel(subtitle);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setFont(Theme.MONO_FONT.deriveFont(9f));
        header.add(hint, BorderLayout.EAST);
        page.add(header, BorderLayout.NORTH);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    public static DenseTablePanel table(String title, String[] columns, Object[][] rows, String detailTitle) {
        return new DenseTablePanel(title, columns, rows, detailTitle, null);
    }

    public static JTabbedPane tabs(Object... nameAndComponents) {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Theme.DARK_CONTENT_BG);
        tabs.putClientProperty(
                "FlatLaf.style",
                "background: " + Theme.hex(Theme.DARK_CONTENT_BG)
                        + "; selectedBackground: " + Theme.hex(Theme.DARK_CONTENT_BG)
                        + "; contentAreaColor: " + Theme.hex(Theme.DARK_CONTENT_BG)
        );
        tabs.setForeground(Theme.TEXT_PRIMARY);
        for (int index = 0; index < nameAndComponents.length; index += 2) {
            tabs.addTab(nameAndComponents[index].toString(), (Component) nameAndComponents[index + 1]);
        }
        return tabs;
    }

    public static JPanel form(String... fields) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.CHROME);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CHROME);
        form.setBorder(Theme.padding(12, 14, 12, 14));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        for (int index = 0; index < fields.length; index += 2) {
            c.gridx = 0;
            c.gridy = index / 2;
            c.weightx = 0;
            JLabel label = new JLabel(fields[index]);
            label.setForeground(Theme.TEXT_MUTED);
            label.setFont(Theme.UI_FONT.deriveFont(10f));
            form.add(label, c);
            c.gridx = 1;
            c.weightx = 1;
            JTextField value = new JTextField(fields[index + 1]);
            value.setFont(Theme.MONO_FONT.deriveFont(10f));
            form.add(value, c);
        }
        c.gridx = 1;
        c.gridy = fields.length / 2;
        c.weightx = 1;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
        actions.setOpaque(false);
        // TODO: ligar ações ao Spring empacotado (in-process, sem rede)
        actions.add(new JButton("Apply"));
        actions.add(new JButton("Reset"));
        form.add(actions, c);
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    public static JPanel builder(String title, String[][] groups) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.CHROME);
        content.setBorder(Theme.padding(10, 12, 10, 12));
        JLabel header = new JLabel(title.toUpperCase());
        header.setForeground(Theme.TEXT_PRIMARY);
        header.setFont(Theme.UI_FONT.deriveFont(Font.BOLD, 11f));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(header);
        content.add(Box.createVerticalStrut(8));
        for (String[] group : groups) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setBackground(Theme.EDITOR);
            row.setBorder(BorderFactory.createCompoundBorder(
                    Theme.matte(0, 0, 1, 0, Theme.DIVIDER), Theme.padding(4, 8, 4, 8)));
            JLabel label = new JLabel(group[0]);
            label.setForeground(Theme.TEXT_MUTED);
            label.setFont(Theme.UI_FONT.deriveFont(10f));
            row.add(label, BorderLayout.WEST);
            JComponent value;
            if (group.length > 2 && "check".equals(group[2])) {
                value = new JCheckBox(group[1], true);
            } else if (group[1].contains(" | ")) {
                value = new JComboBox<>(group[1].split(" \\| "));
            } else {
                value = new JTextField(group[1]);
            }
            value.setFont(Theme.MONO_FONT.deriveFont(10f));
            row.add(value, BorderLayout.CENTER);
            content.add(row);
        }
        content.add(Box.createVerticalStrut(8));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        // TODO: ligar ações ao Spring empacotado (in-process, sem rede)
        JButton run = new JButton("Generate / Run");
        run.putClientProperty("JButton.buttonType", "default");
        buttons.add(run);
        buttons.add(new JButton("Save Configuration"));
        content.add(buttons);
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.CHROME);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    public static JTextArea console(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(Theme.MONO_FONT.deriveFont(11f));
        area.setBackground(Theme.EDITOR);
        area.setForeground(Theme.CODE_TEXT);
        area.setBorder(Theme.padding(8, 10, 8, 10));
        return area;
    }
}
