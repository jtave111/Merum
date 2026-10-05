package com.manager.client.ui;

import com.manager.client.data.MerumData;
import com.manager.client.ui.shell.BottomPanel;
import com.manager.client.ui.shell.MenuBarPanel;
import com.manager.client.ui.shell.NavigatorTree;
import com.manager.client.ui.shell.StatusBar;
import com.manager.client.ui.shell.TabBar;
import com.manager.client.ui.theme.Theme;
import com.manager.client.ui.views.DashboardView;
import com.manager.client.ui.views.InnerViews;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MainContentPane extends JPanel {
    private static final Map<String, ViewDefinition> VIEWS = createViews();

    private final DashboardView dashboard;
    private final NavigatorTree navigator;
    private final TabBar tabBar;
    private final BottomPanel bottomPanel;
    private final MenuBarPanel menuBarPanel;
    private final StatusBar statusBar;
    private final Map<String, Component> viewComponents = new LinkedHashMap<>();
    private final MerumData data;

    public MainContentPane(MerumData data) {
        super(new BorderLayout());
        this.data = data;
        setBackground(Theme.CHROME);

        dashboard = new DashboardView(data);
        viewComponents.put("dashboard", dashboard);
        statusBar = new StatusBar();
        bottomPanel = new BottomPanel();
        tabBar = new TabBar(this::onTabSelected, () -> openView("dashboard"));
        navigator = new NavigatorTree(this::openView);
        menuBarPanel = new MenuBarPanel(new ShellActions());

        dashboard.setStatsListener(this::updateAgentStats);
        add(menuBarPanel, BorderLayout.NORTH);

        JPanel documents = new JPanel(new BorderLayout());
        documents.setBackground(Theme.DARK_CONTENT_BG);
        documents.add(tabBar, BorderLayout.CENTER);
        documents.add(bottomPanel, BorderLayout.SOUTH);

        JSplitPane workspace = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navigator, documents);
        workspace.setBorder(BorderFactory.createEmptyBorder());
        workspace.setDividerSize(3);
        workspace.setDividerLocation(220);
        workspace.setResizeWeight(0.0);
        workspace.setContinuousLayout(true);
        workspace.setBackground(Theme.DIVIDER);
        add(workspace, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        installKeyboardActions();
        openView("dashboard");
    }

    public void shutdown() {
        menuBarPanel.shutdown();
    }

    public void openViewForVerification(String key) {
        if (!VIEWS.containsKey(key)) {
            throw new IllegalArgumentException("Unknown Navigator view: " + key);
        }
        openView(key);
    }

    public static List<String> viewKeys() {
        return List.copyOf(VIEWS.keySet());
    }

    private void openView(String key) {
        ViewDefinition view = VIEWS.get(key);
        if (view == null) {
            return;
        }
        Component content = viewComponents.computeIfAbsent(key, viewKey -> InnerViews.create(viewKey, data));
        tabBar.open(key, view.label(), view.icon(), content);
        navigator.select(key);
    }

    private void onTabSelected(String key) {
        navigator.select(key);
    }

    private void updateAgentStats(int online, int total) {
        menuBarPanel.updateAgentStats(online, total);
        statusBar.updateAgentStats(online, total);
    }

    private void setBottomPanelVisible(boolean visible) {
        bottomPanel.setVisible(visible);
        revalidate();
        repaint();
    }

    private void showCommandPalette() {
        Object[] choices = VIEWS.values().stream().map(ViewDefinition::label).toArray();
        Object selected = JOptionPane.showInputDialog(
                this,
                "Search views, run commands…",
                "Command Palette",
                JOptionPane.PLAIN_MESSAGE,
                null,
                choices,
                "Dashboard"
        );
        if (selected != null) {
            VIEWS.entrySet().stream()
                    .filter(entry -> entry.getValue().label().equals(selected.toString()))
                    .findFirst()
                    .ifPresent(entry -> openView(entry.getKey()));
        }
    }

    private void installKeyboardActions() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK),
                "close-tab"
        );
        getActionMap().put("close-tab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                tabBar.closeSelected();
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK),
                "command-palette"
        );
        getActionMap().put("command-palette", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showCommandPalette();
            }
        });
    }

    private static Map<String, ViewDefinition> createViews() {
        LinkedHashMap<String, ViewDefinition> views = new LinkedHashMap<>();
        views.put("dashboard", view("Dashboard", "⊞"));
        views.put("agents", view("Agents", "⬡"));
        views.put("shell", view("C2 Shell", "$"));
        views.put("payloads", view("Payloads", "⊕"));
        views.put("network", view("Network Map", "⬡"));
        views.put("logs", view("Event Log", "≈"));
        views.put("settings", view("Settings", "⚙"));
        views.put("listeners", view("Listeners", "⋮"));
        views.put("credentials", view("Credentials", "⊛"));
        views.put("loot", view("Loot", "≡"));
        views.put("reports", view("Reports", "⊟"));
        views.put("users", view("Users", "⊹"));
        views.put("sweep", view("Sweep", "⌖"));
        views.put("operations", view("Op Planner", "◈"));
        views.put("mitre", view("MITRE ATT&CK", "M"));
        views.put("playbooks", view("Playbooks", "▷"));
        views.put("files", view("File Manager", "/"));
        views.put("processes", view("Processes", "%"));
        views.put("tunnels", view("Tunnels", "⇄"));
        views.put("implants", view("Implants", "⚡"));
        views.put("exploits", view("Exploits", "!"));
        views.put("arsenal", view("Arsenal Build", "⚙"));
        views.put("opsec", view("IOC Tracker", "⊘"));
        views.put("timeline", view("Timeline", "⊢"));
        views.put("clipboard", view("Clipboard", "⊡"));
        return Map.copyOf(views);
    }

    private static ViewDefinition view(String label, String icon) {
        return new ViewDefinition(label, icon);
    }

    private record ViewDefinition(String label, String icon) {
    }

    private final class ShellActions implements MenuBarPanel.Actions {
        @Override
        public void navigate(String view) {
            openView(view);
        }

        @Override
        public void refresh() {
            dashboard.refresh();
        }

        @Override
        public void action(String action) {
            switch (action) {
                case "togglebottom" -> setBottomPanelVisible(!bottomPanel.isVisible());
                case "cmdpalette" -> showCommandPalette();
                // TODO: ligar ação ao Spring empacotado (in-process, sem rede)
                case "killall" -> JOptionPane.showMessageDialog(
                        MainContentPane.this,
                        "Kill commands remain disabled until the packaged Spring binding is available.",
                        "Kill All Agents",
                        JOptionPane.WARNING_MESSAGE
                );
                case "newlistener" -> JOptionPane.showMessageDialog(
                        MainContentPane.this,
                        "Open Listeners to configure a new listener.",
                        "New Listener",
                        JOptionPane.INFORMATION_MESSAGE
                );
                case "newpayload" -> JOptionPane.showMessageDialog(
                        MainContentPane.this,
                        "Open Payloads to configure and generate an artifact.",
                        "Generate Payload",
                        JOptionPane.INFORMATION_MESSAGE
                );
                case "exit" -> {
                    Window window = SwingUtilities.getWindowAncestor(MainContentPane.this);
                    if (window != null) {
                        window.dispose();
                    }
                }
                default -> { }
            }
        }
    }
}
