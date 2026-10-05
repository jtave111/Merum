package com.manager.client.ui.components;

import com.manager.client.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.IntFunction;

/** Burp-style filterable history table with a request/response-like detail pane. */
public final class DenseTablePanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JLabel count;
    private final JTextArea detail;
    private final IntFunction<String> detailText;

    public DenseTablePanel(
            String title,
            String[] columns,
            Object[][] rows,
            String detailTitle,
            IntFunction<String> detailText
    ) {
        super(new BorderLayout());
        this.detailText = detailText;
        setBackground(Theme.DARK_CONTENT_BG);

        model = new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                for (int row = 0; row < getRowCount(); row++) {
                    Object value = getValueAt(row, column);
                    if (value != null) {
                        return value.getClass();
                    }
                }
                return Object.class;
            }
        };
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer cellRenderer, int row, int column) {
                Component component = super.prepareRenderer(cellRenderer, row, column);
                component.setBackground(isRowSelected(row) ? Theme.SELECTION : Theme.DARK_CONTENT_BG);
                component.setForeground(isRowSelected(row) ? Theme.SELECTED_TEXT : Theme.CODE_TEXT);
                return component;
            }
        };
        table.setName(title + " table");
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(20);
        table.setShowGrid(true);
        table.setGridColor(Theme.DIVIDER);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(Theme.DARK_CONTENT_BG);
        table.setForeground(Theme.CODE_TEXT);
        table.setSelectionBackground(Theme.SELECTION);
        table.setSelectionForeground(Theme.SELECTED_TEXT);
        table.setFont(Theme.MONO_FONT.deriveFont(10f));
        table.getTableHeader().setFont(Theme.UI_FONT.deriveFont(Font.BOLD, 9f));
        table.getTableHeader().setBackground(Theme.CHROME);
        table.getTableHeader().setForeground(Theme.TEXT_PRIMARY);
        table.getTableHeader().setPreferredSize(new Dimension(10, 22));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(Theme.padding(0, 5, 0, 5));
        table.setDefaultRenderer(Object.class, renderer);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(Theme.CHROME);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                Theme.matte(0, 0, 1, 0, Theme.BORDER),
                Theme.padding(4, 8, 4, 8)
        ));
        JLabel heading = new JLabel(title.toUpperCase());
        heading.setFont(Theme.UI_FONT.deriveFont(Font.BOLD, 10f));
        heading.setForeground(Theme.TEXT_PRIMARY);
        toolbar.add(heading, BorderLayout.WEST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        filters.setOpaque(false);
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setForeground(Theme.TEXT_MUTED);
        filterLabel.setFont(Theme.UI_FONT.deriveFont(10f));
        filters.add(filterLabel);
        JTextField filter = new JTextField(24);
        filter.setName(title + " filter");
        filter.putClientProperty("JTextField.placeholderText", "Search all columns...");
        filter.setFont(Theme.MONO_FONT.deriveFont(10f));
        filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { applyFilter(filter.getText()); }
            @Override public void removeUpdate(DocumentEvent event) { applyFilter(filter.getText()); }
            @Override public void changedUpdate(DocumentEvent event) { applyFilter(filter.getText()); }
        });
        filters.add(filter);
        JButton clear = new JButton("Clear");
        clear.setFont(Theme.UI_FONT.deriveFont(9f));
        clear.addActionListener(event -> filter.setText(""));
        filters.add(clear);
        count = new JLabel();
        count.setForeground(Theme.TEXT_MUTED);
        count.setFont(Theme.MONO_FONT.deriveFont(9f));
        filters.add(count);
        toolbar.add(filters, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(Theme.DARK_CONTENT_BG);

        detail = new JTextArea();
        detail.setName(detailTitle + " detail");
        detail.setEditable(false);
        detail.setLineWrap(false);
        detail.setFont(Theme.MONO_FONT.deriveFont(10f));
        detail.setBackground(Theme.DARK_CONTENT_BG);
        detail.setForeground(Theme.CODE_TEXT);
        detail.setBorder(Theme.padding(8, 10, 8, 10));
        JScrollPane detailScroll = new JScrollPane(detail);
        detailScroll.getViewport().setBackground(Theme.DARK_CONTENT_BG);
        detailScroll.setBorder(BorderFactory.createCompoundBorder(
                Theme.matte(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createTitledBorder(
                        Theme.matte(0, 0, 0, 0, Theme.BORDER), detailTitle,
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        Theme.UI_FONT.deriveFont(Font.BOLD, 9f), Theme.TEXT_MUTED
                )
        ));
        detailScroll.setPreferredSize(new Dimension(100, 150));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailScroll);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setDividerSize(4);
        split.setResizeWeight(0.68);
        split.setContinuousLayout(true);
        add(split, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateDetail();
            }
        });
        updateCount();
        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    public JTable table() {
        return table;
    }

    private void applyFilter(String query) {
        String text = query == null ? "" : query.strip();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
        updateCount();
    }

    private void updateCount() {
        count.setText(table.getRowCount() + " / " + model.getRowCount());
    }

    private void updateDetail() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            detail.setText("No row selected.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        if (detailText != null) {
            detail.setText(detailText.apply(modelRow));
        } else {
            StringBuilder text = new StringBuilder();
            for (int column = 0; column < model.getColumnCount(); column++) {
                text.append(String.format("%-18s %s%n", model.getColumnName(column) + ":", model.getValueAt(modelRow, column)));
            }
            detail.setText(text.toString());
        }
        detail.setCaretPosition(0);
    }
}
