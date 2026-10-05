package com.manager.client.ui.views;

import com.manager.client.data.MerumData;
import com.manager.client.model.AgentViewModel;
import com.manager.client.model.C2InfoViewModel;
import com.manager.client.model.TableViewModel;
import com.manager.client.ui.components.SectionHeader;
import com.manager.client.ui.components.StatCard;
import com.manager.client.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.BiConsumer;

public final class DashboardView extends JPanel {
    private static final int ROW_UPTIME = 7;
    private static final int ROW_MEMORY = 8;
    private static final int ROW_CPU_LOAD = 9;
    private static final int ROW_DISK_FREE = 10;
    private static final int ROW_THREADS = 6;

    private final MerumData data;
    private final StatCard activeAgents;
    private final StatCard lostAgents;
    private final StatCard totalAgents;
    private final StatCard rootSessions;
    private final DefaultTableModel healthModel;
    private final JLabel onlineSummary;
    private final JLabel idleSummary;
    private final JLabel lostSummary;
    private final JLabel rootSummary;
    private BiConsumer<Integer, Integer> statsListener = (online, total) -> { };

    public DashboardView(MerumData data) {
        super(new BorderLayout());
        this.data = data;
        setBackground(Theme.CHROME);

        JPanel cards = new JPanel(new GridLayout(1, 6, 0, 0));
        cards.setBackground(Theme.CHROME);
        cards.setBorder(Theme.matte(0, 0, 1, 0, Theme.BORDER));
        activeAgents = new StatCard("ACTIVE AGENTS", "0", "0 idle · 0 lost", Theme.ACCENT);
        lostAgents = new StatCard("LOST AGENTS", "0", "timeout / no response", Theme.TEXT_MUTED);
        totalAgents = new StatCard("TOTAL AGENTS", "0", "registered", Theme.TEXT_PRIMARY);
        rootSessions = new StatCard("ROOT SESSIONS", "0", "of 0 agents", Theme.TEXT_MUTED);
        cards.add(activeAgents);
        cards.add(lostAgents);
        cards.add(totalAgents);
        cards.add(rootSessions);
        cards.add(new StatCard("C2 SERVERS", "1 / 1", "all operational", Theme.TEXT_PRIMARY));
        cards.add(new StatCard("SYSTEM LOAD", "—", "monitoring", Theme.TEXT_MUTED));
        add(cards, BorderLayout.NORTH);

        TableViewModel health = data.dashboardHealth();
        healthModel = new DefaultTableModel(health.rows(), health.columns()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable healthTable = createHealthTable();
        JScrollPane healthScroll = new JScrollPane(healthTable);
        healthScroll.setBorder(BorderFactory.createEmptyBorder());
        healthScroll.getViewport().setBackground(Theme.EDITOR);

        JPanel healthPanel = new JPanel(new BorderLayout());
        healthPanel.setBackground(Theme.EDITOR);
        healthPanel.add(new SectionHeader("System Health"), BorderLayout.NORTH);
        healthPanel.add(healthScroll, BorderLayout.CENTER);

        JPanel summary = new JPanel(new BorderLayout());
        summary.setBackground(Theme.EDITOR);
        summary.setBorder(Theme.matte(1, 0, 0, 0, Theme.BORDER));
        summary.add(new SectionHeader("Agent Summary"), BorderLayout.NORTH);

        JPanel summaryValues = new JPanel(new GridLayout(1, 4, 0, 0));
        summaryValues.setBackground(Theme.EDITOR);
        onlineSummary = createSummaryCell(summaryValues, "0", "Online", Theme.ACCENT, true);
        idleSummary = createSummaryCell(summaryValues, "0", "Idle", Theme.TEXT_PRIMARY, true);
        lostSummary = createSummaryCell(summaryValues, "0", "Lost", Theme.TEXT_MUTED, true);
        rootSummary = createSummaryCell(summaryValues, "0", "Root", Theme.TEXT_MUTED, false);
        summaryValues.setPreferredSize(new Dimension(100, 66));
        summary.add(summaryValues, BorderLayout.CENTER);
        healthPanel.add(summary, BorderLayout.SOUTH);
        add(healthPanel, BorderLayout.CENTER);

        refresh();
    }

    public void setStatsListener(BiConsumer<Integer, Integer> listener) {
        statsListener = listener == null ? (online, total) -> { } : listener;
    }

    public void refresh() {
        applyAgents(data.listAgents());
        applyC2Info(data.c2Info());
    }

    private JTable createHealthTable() {
        JTable table = new JTable(healthModel);
        table.setTableHeader(null);
        table.setRowHeight(29);
        table.setShowGrid(true);
        table.setGridColor(Theme.DIVIDER);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setBackground(Theme.EDITOR);
        table.setForeground(Theme.CODE_TEXT);
        table.setSelectionBackground(Theme.SELECTION);
        table.setSelectionForeground(Theme.SELECTED_TEXT);
        table.setFont(Theme.MONO_FONT.deriveFont(11f));
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer labelRenderer = new DefaultTableCellRenderer();
        labelRenderer.setBackground(Theme.EDITOR);
        labelRenderer.setForeground(Theme.SEVERITY_INFORMATION);
        labelRenderer.setBorder(Theme.padding(0, 10, 0, 8));
        table.getColumnModel().getColumn(0).setCellRenderer(labelRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);

        DefaultTableCellRenderer valueRenderer = new DefaultTableCellRenderer();
        valueRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        valueRenderer.setBackground(Theme.EDITOR);
        valueRenderer.setForeground(Theme.CODE_TEXT);
        valueRenderer.setBorder(Theme.padding(0, 8, 0, 8));
        table.getColumnModel().getColumn(1).setCellRenderer(valueRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(240);

        table.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setMaxWidth(130);
        return table;
    }

    private JLabel createSummaryCell(
            JPanel parent,
            String value,
            String label,
            Color color,
            boolean rightBorder
    ) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(Theme.EDITOR);
        if (rightBorder) {
            cell.setBorder(Theme.matte(0, 0, 0, 1, Theme.BORDER));
        }

        JLabel number = new JLabel(value, SwingConstants.CENTER);
        number.setForeground(color);
        number.setFont(Theme.UI_FONT.deriveFont(Font.BOLD, 18f));
        number.setBorder(Theme.padding(7, 4, 0, 4));
        cell.add(number, BorderLayout.CENTER);

        JLabel caption = new JLabel(label.toUpperCase(), SwingConstants.CENTER);
        caption.setForeground(Theme.TEXT_MUTED);
        caption.setFont(Theme.UI_FONT.deriveFont(9f));
        caption.setBorder(Theme.padding(0, 4, 7, 4));
        cell.add(caption, BorderLayout.SOUTH);
        parent.add(cell);
        return number;
    }

    private void applyAgents(List<AgentViewModel> agents) {
        int online = (int) agents.stream().filter(AgentViewModel::isOnline).count();
        int idle = (int) agents.stream().filter(AgentViewModel::isIdle).count();
        int lost = (int) agents.stream().filter(AgentViewModel::isLost).count();
        int roots = (int) agents.stream().filter(AgentViewModel::isRoot).count();

        activeAgents.setValue(Integer.toString(online), Theme.ACCENT);
        activeAgents.setMeta(idle + " idle · " + lost + " lost");
        lostAgents.setValue(Integer.toString(lost), lost > 0 ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED);
        totalAgents.setValue(Integer.toString(agents.size()), Theme.TEXT_PRIMARY);
        rootSessions.setValue(Integer.toString(roots), roots > 0 ? Theme.ACCENT : Theme.TEXT_MUTED);
        rootSessions.setMeta("of " + agents.size() + " agents");

        onlineSummary.setText(Integer.toString(online));
        idleSummary.setText(Integer.toString(idle));
        lostSummary.setText(Integer.toString(lost));
        rootSummary.setText(Integer.toString(roots));
        rootSummary.setForeground(roots > 0 ? Theme.ACCENT : Theme.TEXT_MUTED);
        statsListener.accept(online, agents.size());
    }

    private void applyC2Info(C2InfoViewModel info) {
        if (info == null) {
            return;
        }
        setHealthValue(ROW_THREADS, info.threads());
        setHealthValue(ROW_UPTIME, info.uptime());
        setHealthValue(ROW_MEMORY, info.memory());
        setHealthValue(ROW_CPU_LOAD, info.cpuLoad());
        setHealthValue(ROW_DISK_FREE, info.diskFree());
    }

    private void setHealthValue(int row, String value) {
        if (value != null && !value.isBlank()) {
            healthModel.setValueAt(value, row, 1);
        }
    }

    private static final class StatusCellRenderer extends DefaultTableCellRenderer {
        private StatusCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean selected,
                boolean focused,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            String text = value == null ? "" : value.toString();
            label.setText(text);
            label.setFont(Theme.MONO_FONT.deriveFont(Font.PLAIN, 9f));
            label.setForeground(selected ? Theme.SELECTED_TEXT : Theme.ACCENT);
            label.setBackground(selected ? Theme.SELECTION : Theme.EDITOR);
            label.setBorder(text.isBlank()
                    ? Theme.padding(0, 6, 0, 10)
                    : BorderFactory.createCompoundBorder(
                            Theme.matte(1, 1, 1, 1, Theme.ACCENT),
                            Theme.padding(1, 5, 1, 5)
                    ));
            return label;
        }
    }
}
