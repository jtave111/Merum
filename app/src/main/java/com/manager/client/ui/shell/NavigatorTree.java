package com.manager.client.ui.shell;

import com.manager.client.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public final class NavigatorTree extends JPanel {
    private static final List<NavSection> SECTIONS = List.of(
            section("c2", "C2 Operations",
                    item("dashboard", "⊞", "Dashboard"),
                    item("agents", "⬡", "Agents", "6", false),
                    item("shell", "$", "C2 Shell"),
                    item("listeners", "⋮", "Listeners", "2", false),
                    item("payloads", "⊕", "Payloads"),
                    item("sweep", "⌖", "Sweep", "!", true),
                    item("timeline", "⊢", "Timeline")),
            section("net", "Network & Recon", item("network", "⬡", "Network Map")),
            section("intel", "Intelligence",
                    item("loot", "≡", "Loot"),
                    item("credentials", "⊛", "Credentials"),
                    item("clipboard", "⊡", "Clipboard"),
                    item("reports", "⊟", "Reports")),
            section("arsenal", "Arsenal",
                    item("implants", "⚡", "Implants"),
                    item("exploits", "!", "Exploits"),
                    item("arsenal", "⚙", "Build Manager")),
            section("ops", "Operations",
                    item("operations", "◈", "Op Planner"),
                    item("mitre", "M", "MITRE ATT&CK"),
                    item("playbooks", "▷", "Playbooks")),
            section("post", "Post-Exploitation",
                    item("files", "/", "File Manager"),
                    item("processes", "%", "Processes"),
                    item("tunnels", "⇄", "Tunnels")),
            section("opsec", "OPSEC",
                    item("opsec", "⊘", "IOC Tracker"),
                    item("logs", "≈", "Event Log")),
            section("sys", "System",
                    item("users", "⊹", "Users"),
                    item("settings", "⚙", "Settings"))
    );

    private final Consumer<String> navigation;
    private final JTree tree;
    private final JTextField filter;

    public NavigatorTree(Consumer<String> navigation) {
        super(new BorderLayout());
        this.navigation = navigation;
        setBackground(Theme.DARK_CONTENT_BG);
        setBorder(Theme.matte(0, 0, 0, 1, Theme.BORDER));
        setMinimumSize(new Dimension(160, 200));
        setPreferredSize(new Dimension(220, 600));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.DARK_CONTENT_BG);
        header.setBorder(Theme.matte(0, 0, 1, 0, Theme.BORDER));
        header.setPreferredSize(new Dimension(10, 24));
        JLabel title = new JLabel("⬡  Navigator");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.UI_FONT.deriveFont(11f));
        title.setBorder(Theme.matte(2, 0, 0, 0, Theme.ACCENT));
        title.setPreferredSize(new Dimension(100, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, BorderLayout.WEST);

        JPanel treeActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        treeActions.setOpaque(false);
        JButton collapse = headerButton("⊟", "Collapse all");
        collapse.addActionListener(event -> collapseAll());
        JButton expand = headerButton("⊞", "Expand all");
        expand.addActionListener(event -> expandAll());
        treeActions.add(collapse);
        treeActions.add(expand);
        header.add(treeActions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        filter = new JTextField();
        filter.setFont(Theme.UI_FONT.deriveFont(11f));
        filter.setBackground(Theme.DARK_CONTENT_BG);
        filter.setForeground(Theme.CODE_TEXT);
        filter.setBorder(BorderFactory.createCompoundBorder(
                Theme.matte(1, 1, 1, 1, Theme.BORDER),
                Theme.padding(2, 6, 2, 6)
        ));
        filter.putClientProperty("JTextField.placeholderText", "Filter views...");
        filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                rebuildTree();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                rebuildTree();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                rebuildTree();
            }
        });
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Theme.DARK_CONTENT_BG);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                Theme.matte(0, 0, 1, 0, Theme.BORDER),
                Theme.padding(4, 6, 4, 6)
        ));
        searchPanel.add(filter, BorderLayout.CENTER);

        tree = new JTree();
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(23);
        tree.setFont(Theme.UI_FONT.deriveFont(12f));
        tree.setBackground(Theme.DARK_CONTENT_BG);
        tree.setForeground(Theme.TEXT_PRIMARY);
        tree.setCellRenderer(new NavRenderer());
        tree.putClientProperty("JTree.wideSelection", true);
        tree.addTreeSelectionListener(event -> {
            Object selected = tree.getLastSelectedPathComponent();
            if (selected instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof NavItem item) {
                navigation.accept(item.key());
            }
        });

        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setBorder(BorderFactory.createEmptyBorder());
        treeScroll.getViewport().setBackground(Theme.DARK_CONTENT_BG);
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Theme.DARK_CONTENT_BG);
        body.add(searchPanel, BorderLayout.NORTH);
        body.add(treeScroll, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        rebuildTree();
    }

    public void select(String key) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        var nodes = root.breadthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
            if (node.getUserObject() instanceof NavItem item && item.key().equals(key)) {
                TreePath path = new TreePath(node.getPath());
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
                return;
            }
        }
    }

    private void rebuildTree() {
        String needle = filter == null ? "" : filter.getText().strip().toLowerCase(Locale.ROOT);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT");
        for (NavSection section : SECTIONS) {
            List<NavItem> visible = new ArrayList<>();
            for (NavItem item : section.items()) {
                if (needle.isEmpty() || item.label().toLowerCase(Locale.ROOT).contains(needle)) {
                    visible.add(item);
                }
            }
            if (!visible.isEmpty()) {
                DefaultMutableTreeNode sectionNode = new DefaultMutableTreeNode(
                        new NavSection(section.key(), section.label(), visible)
                );
                visible.forEach(item -> sectionNode.add(new DefaultMutableTreeNode(item)));
                root.add(sectionNode);
            }
        }
        tree.setModel(new DefaultTreeModel(root));
        if (!needle.isEmpty()) {
            expandAll();
        } else {
            expandSections("c2", "net", "intel");
        }
    }

    private void expandSections(String... keys) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        for (int index = 0; index < root.getChildCount(); index++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(index);
            if (node.getUserObject() instanceof NavSection section) {
                for (String key : keys) {
                    if (section.key().equals(key)) {
                        tree.expandPath(new TreePath(node.getPath()));
                    }
                }
            }
        }
    }

    private void expandAll() {
        for (int row = 0; row < tree.getRowCount(); row++) {
            tree.expandRow(row);
        }
    }

    private void collapseAll() {
        for (int row = tree.getRowCount() - 1; row >= 0; row--) {
            tree.collapseRow(row);
        }
    }

    private JButton headerButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setForeground(Theme.TEXT_MUTED);
        button.setFont(Theme.UI_FONT.deriveFont(11f));
        button.setPreferredSize(new Dimension(20, 19));
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        return button;
    }

    private static NavSection section(String key, String label, NavItem... items) {
        return new NavSection(key, label, List.of(items));
    }

    private static NavItem item(String key, String icon, String label) {
        return item(key, icon, label, "", false);
    }

    private static NavItem item(String key, String icon, String label, String badge, boolean alert) {
        return new NavItem(key, icon, label, badge, alert);
    }

    private record NavSection(String key, String label, List<NavItem> items) {
    }

    private record NavItem(String key, String icon, String label, String badge, boolean alert) {
    }

    private static final class NavRenderer extends DefaultTreeCellRenderer {
        private NavRenderer() {
            setOpenIcon(null);
            setClosedIcon(null);
            setLeafIcon(null);
            setBorderSelectionColor(null);
            setBackgroundNonSelectionColor(Theme.DARK_CONTENT_BG);
            setBackgroundSelectionColor(Theme.SELECTION);
            setTextNonSelectionColor(Theme.TEXT_PRIMARY);
            setTextSelectionColor(Theme.SELECTED_TEXT);
        }

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean focused
        ) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, focused
            );
            Object user = ((DefaultMutableTreeNode) value).getUserObject();
            if (user instanceof NavSection section) {
                label.setText(section.label().toUpperCase() + "    " + section.items().size());
                label.setForeground(selected ? Theme.SELECTED_TEXT : Theme.SEVERITY_INFORMATION);
                label.setFont(Theme.UI_FONT.deriveFont(Font.BOLD, 10f));
            } else if (user instanceof NavItem item) {
                String badge = item.badge().isBlank() ? "" : "    [" + item.badge() + "]";
                label.setText(item.icon() + "   " + item.label() + badge);
                label.setForeground(selected ? Theme.SELECTED_TEXT : Theme.TEXT_PRIMARY);
                label.setFont(Theme.UI_FONT.deriveFont(12f));
            }
            label.setBorder(Theme.padding(0, 3, 0, 6));
            return label;
        }
    }
}
