package com.manager.client.ui.shell;

import com.manager.client.Branding;
import com.manager.client.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MenuBarPanel extends JPanel {
    private static final DateTimeFormatter UTC_TIME = DateTimeFormatter.ofPattern("HH:mm:ss 'UTC'");
    private static final Map<String, String> VIEW_MAP = Map.ofEntries(
            Map.entry("Dashboard", "dashboard"),
            Map.entry("Agents", "agents"),
            Map.entry("Network Map", "network"),
            Map.entry("List Agents", "agents"),
            Map.entry("Generate Payload", "payloads"),
            Map.entry("Manage Listeners", "listeners")
    );
    private static final Map<String, String> ACTION_MAP = Map.of(
            "Toggle Bottom Panel", "togglebottom",
            "Kill All Agents", "killall",
            "New Listener", "newlistener"
    );

    private final Actions actions;
    private final JLabel clockLabel;
    private final JLabel agentCountLabel;
    private final JLabel agentLed;
    private final Timer clockTimer;

    public MenuBarPanel(Actions actions) {
        super(new BorderLayout());
        this.actions = actions;
        setBackground(Theme.DARK_CONTENT_BG);

        JMenuBar menuBar = createMenuBar();
        add(menuBar, BorderLayout.NORTH);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(Theme.matte(0, 0, 1, 0, Theme.BORDER));
        toolbar.setBackground(Theme.DARK_CONTENT_BG);
        toolbar.setPreferredSize(new Dimension(10, 28));

        toolbar.add(toolButton("⟳  Refresh", "Refresh agent data", event -> actions.refresh()));
        toolbar.addSeparator(new Dimension(9, 16));
        toolbar.add(toolButton("⌨  Cmd  ⌘K", "Command palette (Ctrl+K)", event -> actions.action("cmdpalette")));
        toolbar.addSeparator(new Dimension(9, 16));

        JButton newButton = toolButton("⊕  New  ▾", "New resource", null);
        JPopupMenu newMenu = new JPopupMenu();
        JMenuItem listener = new JMenuItem("⋮  New Listener");
        listener.addActionListener(event -> actions.action("newlistener"));
        newMenu.add(listener);
        JMenuItem payload = new JMenuItem("⊕  Gen Payload");
        payload.addActionListener(event -> actions.action("newpayload"));
        newMenu.add(payload);
        newButton.addActionListener(event -> newMenu.show(newButton, 0, newButton.getHeight()));
        toolbar.add(newButton);

        JButton killButton = toolButton("⊘  Kill All", "Kill all agents", event -> actions.action("killall"));
        killButton.setForeground(Theme.TEXT_PRIMARY);
        toolbar.add(killButton);
        toolbar.addSeparator(new Dimension(9, 16));

        JPanel agents = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        agents.setOpaque(false);
        agentLed = new JLabel("●");
        agentLed.setForeground(Theme.TEXT_MUTED);
        agentLed.setFont(Theme.UI_FONT.deriveFont(8f));
        agentCountLabel = new JLabel("0/0");
        agentCountLabel.setForeground(Theme.TEXT_MUTED);
        agentCountLabel.setFont(Theme.MONO_FONT.deriveFont(Font.BOLD, 11f));
        JLabel agentWord = new JLabel("agents");
        agentWord.setForeground(Theme.TEXT_MUTED);
        agentWord.setFont(Theme.UI_FONT.deriveFont(9f));
        agents.add(agentLed);
        agents.add(agentCountLabel);
        agents.add(agentWord);
        toolbar.add(agents);

        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(toolButton("⊟  Panel", "Toggle bottom panel", event -> actions.action("togglebottom")));
        toolbar.addSeparator(new Dimension(9, 16));
        JTextField search = new JTextField("Search…  ⌘K");
        search.setEditable(false);
        search.setForeground(Theme.TEXT_MUTED);
        search.setFont(Theme.UI_FONT.deriveFont(11f));
        search.setPreferredSize(new Dimension(150, 20));
        search.setMaximumSize(new Dimension(150, 20));
        search.setBorder(BorderFactory.createCompoundBorder(
                Theme.matte(1, 1, 1, 1, Theme.BORDER),
                Theme.padding(1, 7, 1, 7)
        ));
        search.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent event) {
                actions.action("cmdpalette");
            }
        });
        toolbar.add(search);
        toolbar.add(Box.createHorizontalStrut(6));
        add(toolbar, BorderLayout.SOUTH);

        clockLabel = findClockLabel(menuBar);
        clockTimer = new Timer(1_000, event -> updateClock());
        clockTimer.setInitialDelay(0);
        clockTimer.start();
        updateClock();
    }

    public void updateAgentStats(int online, int total) {
        agentCountLabel.setText(online + "/" + total);
        ColorPair colors = online > 0
                ? new ColorPair(Theme.ACCENT, Theme.ACCENT)
                : new ColorPair(Theme.TEXT_MUTED, Theme.TEXT_MUTED);
        agentLed.setForeground(colors.led());
        agentCountLabel.setForeground(colors.text());
    }

    public void shutdown() {
        clockTimer.stop();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Theme.DARK_CONTENT_BG);
        menuBar.setBorder(Theme.matte(0, 0, 1, 0, Theme.BORDER));
        menuBar.setPreferredSize(new Dimension(10, 25));

        JLabel brand = new JLabel(Branding.NAME);
        brand.setForeground(Theme.ACCENT);
        brand.setFont(Theme.MONO_FONT.deriveFont(Font.BOLD, 12f));
        brand.setBorder(Theme.padding(0, 8, 0, 10));
        menuBar.add(brand);
        JSeparator brandSeparator = new JSeparator(JSeparator.VERTICAL);
        brandSeparator.setPreferredSize(new Dimension(1, 16));
        brandSeparator.setMaximumSize(new Dimension(1, 16));
        menuBar.add(brandSeparator);

        LinkedHashMap<String, List<String>> menus = new LinkedHashMap<>();
        menus.put("File", List.of("New Session", "Import Config", "---", "Export Logs", "Export Loot", "---", "Exit"));
        menus.put("View", List.of("Dashboard", "Agents", "Network Map", "---", "Toggle Bottom Panel", "Toggle Navigator"));
        menus.put("Agents", List.of("List Agents", "Kill All Agents", "---", "New Listener", "Generate Payload", "---", "Export Agent List"));
        menus.put("Attack", List.of("Launch Network Scan", "Port Scan Single Target", "---", "Credential Dump", "Lateral Movement", "---", "Run Script"));
        menus.put("Payloads", List.of("Generate Payload", "Manage Listeners", "---", "Stage Server", "One-liner Generator"));
        menus.put("Scripts", List.of("Run Aggressor Script", "Script Manager", "---", "Load .merum File", "Script Console"));
        menus.put("Help", List.of("Documentation", "Keyboard Shortcuts", "---", "About " + Branding.DISPLAY_NAME));

        menus.forEach((label, entries) -> {
            JMenu menu = new JMenu(label);
            menu.setFont(Theme.UI_FONT.deriveFont(12f));
            for (String entry : entries) {
                if ("---".equals(entry)) {
                    menu.addSeparator();
                } else {
                    JMenuItem item = new JMenuItem(entry);
                    item.setFont(Theme.UI_FONT.deriveFont(12f));
                    item.addActionListener(event -> handleMenuItem(entry));
                    menu.add(item);
                }
            }
            menuBar.add(menu);
        });

        menuBar.add(Box.createHorizontalGlue());
        JLabel c2 = new JLabel("●  C2");
        c2.setForeground(Theme.ACCENT);
        c2.setFont(Theme.UI_FONT.deriveFont(11f));
        c2.setBorder(Theme.padding(0, 8, 0, 8));
        menuBar.add(c2);

        JLabel clock = new JLabel("--:--:-- UTC");
        clock.setName("utc-clock");
        clock.setForeground(Theme.SEVERITY_INFORMATION);
        clock.setFont(Theme.MONO_FONT.deriveFont(11f));
        clock.setBorder(Theme.padding(0, 8, 0, 8));
        menuBar.add(clock);

        JLabel operator = new JLabel("ROOT_ADMIN");
        operator.setForeground(Theme.TEXT_PRIMARY);
        operator.setFont(Theme.UI_FONT.deriveFont(Font.BOLD, 11f));
        operator.setBorder(Theme.padding(0, 8, 0, 8));
        menuBar.add(operator);
        return menuBar;
    }

    private void handleMenuItem(String item) {
        String view = VIEW_MAP.get(item);
        if (view != null) {
            actions.navigate(view);
            return;
        }
        String action = ACTION_MAP.get(item);
        if (action != null) {
            actions.action(action);
            return;
        }
        if ("Exit".equals(item)) {
            actions.action("exit");
        }
    }

    private JButton toolButton(String text, String tooltip, ActionListener listener) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setFont(Theme.UI_FONT.deriveFont(11f));
        button.setForeground(Theme.TEXT_PRIMARY);
        button.setMargin(new java.awt.Insets(1, 6, 1, 6));
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        if (listener != null) {
            button.addActionListener(listener);
        }
        return button;
    }

    private JLabel findClockLabel(JMenuBar menuBar) {
        for (java.awt.Component component : menuBar.getComponents()) {
            if (component instanceof JLabel label && "utc-clock".equals(label.getName())) {
                return label;
            }
        }
        throw new IllegalStateException("UTC clock label not found");
    }

    private void updateClock() {
        clockLabel.setText(UTC_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
    }

    public interface Actions {
        void navigate(String view);

        void refresh();

        void action(String action);
    }

    private record ColorPair(java.awt.Color led, java.awt.Color text) {
    }
}
