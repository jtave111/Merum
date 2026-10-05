package com.manager.client.ui.components;

import com.manager.client.model.IssueViewModel;
import com.manager.client.model.IssueViewModel.Severity;
import com.manager.client.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

/** Burp-like issue activity list. Severity colors are restricted to the six-pixel markers. */
public final class IssueActivityPanel extends JPanel {
    public IssueActivityPanel(List<IssueViewModel> issues) {
        super(new BorderLayout());
        setBackground(Theme.DARK_CONTENT_BG);

        JList<IssueViewModel> list = new JList<>(issues.toArray(IssueViewModel[]::new));
        list.setName("Issue activity severity list");
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(24);
        list.setBackground(Theme.DARK_CONTENT_BG);
        list.setForeground(Theme.CODE_TEXT);
        list.setSelectionBackground(Theme.SELECTION);
        list.setSelectionForeground(Theme.SELECTED_TEXT);
        list.setCellRenderer(new IssueRenderer());

        JTextArea detail = ViewSupport.console("");
        detail.setName("Issue detail");
        detail.setBackground(Theme.DARK_CONTENT_BG);
        list.addListSelectionListener(event -> {
            IssueViewModel selected = list.getSelectedValue();
            if (!event.getValueIsAdjusting() && selected != null) {
                detail.setText("Issue:       " + selected.title() + "\n"
                        + "Severity:    " + selected.severity() + "\n"
                        + "Host:        " + selected.host() + "\n"
                        + "Confidence:  " + selected.confidence() + "\n\n"
                        + selected.detail());
                detail.setCaretPosition(0);
            }
        });
        if (!issues.isEmpty()) {
            list.setSelectedIndex(0);
        }

        JScrollPane listScroll = new JScrollPane(list);
        listScroll.getViewport().setBackground(Theme.DARK_CONTENT_BG);
        listScroll.setBorder(BorderFactory.createTitledBorder(
                Theme.matte(0, 0, 0, 0, Theme.BORDER), "Issue activity",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                Theme.UI_FONT.deriveFont(10f), Theme.TEXT_MUTED));
        JScrollPane detailScroll = new JScrollPane(detail);
        detailScroll.getViewport().setBackground(Theme.DARK_CONTENT_BG);
        detailScroll.setBorder(BorderFactory.createTitledBorder(
                Theme.matte(0, 0, 0, 0, Theme.BORDER), "Advisory / evidence",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                Theme.UI_FONT.deriveFont(10f), Theme.TEXT_MUTED));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listScroll, detailScroll);
        split.setDividerSize(4);
        split.setResizeWeight(0.58);
        split.setBorder(BorderFactory.createEmptyBorder());
        add(split, BorderLayout.CENTER);
    }

    private static Color color(Severity severity) {
        return switch (severity) {
            case HIGH -> Theme.SEVERITY_HIGH;
            case MEDIUM -> Theme.SEVERITY_MEDIUM;
            case LOW -> Theme.SEVERITY_LOW;
            case INFORMATION -> Theme.SEVERITY_INFORMATION;
        };
    }

    private static final class IssueRenderer extends JPanel implements ListCellRenderer<IssueViewModel> {
        private Severity severity = Severity.INFORMATION;
        private String text = "";
        private Color textColor = Theme.CODE_TEXT;

        private IssueRenderer() {
            setOpaque(true);
            setFont(Theme.MONO_FONT.deriveFont(10f));
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends IssueViewModel> list,
                IssueViewModel issue,
                int index,
                boolean selected,
                boolean focused
        ) {
            severity = issue.severity();
            text = issue.severity() + "   " + issue.title() + "   [" + issue.host() + "]";
            textColor = selected ? Theme.SELECTED_TEXT : Theme.CODE_TEXT;
            setBackground(selected ? Theme.SELECTION : Theme.DARK_CONTENT_BG);
            setPreferredSize(new Dimension(100, 24));
            return this;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            graphics.setColor(color(severity));
            graphics.fillOval(8, 9, 6, 6);
            graphics.setColor(textColor);
            graphics.setFont(getFont());
            graphics.drawString(text, 24, 16);
        }
    }
}
