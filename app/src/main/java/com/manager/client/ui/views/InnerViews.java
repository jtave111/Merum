package com.manager.client.ui.views;

import com.manager.client.data.MerumData;
import com.manager.client.data.MerumData.ClipboardViewModel;
import com.manager.client.data.MerumData.ImplantsViewModel;
import com.manager.client.data.MerumData.NetworkViewModel;
import com.manager.client.data.MerumData.ReportsViewModel;
import com.manager.client.data.MerumData.ShellViewModel;
import com.manager.client.model.AgentViewModel;
import com.manager.client.model.FormViewModel;
import com.manager.client.model.RoleViewModel;
import com.manager.client.model.TableViewModel;
import com.manager.client.model.UserViewModel;
import com.manager.client.ui.components.DenseTablePanel;
import com.manager.client.ui.components.IssueActivityPanel;
import com.manager.client.ui.components.ViewSupport;
import com.manager.client.ui.theme.Theme;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.List;

/** Swing ports of every React route listed by App.tsx and Sidebar.tsx. */
public final class InnerViews {
    private InnerViews() { }

    public static JComponent create(String key, MerumData data) {
        return switch (key) {
            case "agents" -> agents(data);
            case "shell" -> shell(data);
            case "listeners" -> listeners(data);
            case "payloads" -> payloads(data);
            case "sweep" -> sweep(data);
            case "timeline" -> timeline(data);
            case "network" -> network(data);
            case "loot" -> loot(data);
            case "credentials" -> credentials(data);
            case "clipboard" -> clipboard(data);
            case "reports" -> reports(data);
            case "implants" -> implants(data);
            case "exploits" -> exploits(data);
            case "arsenal" -> arsenal(data);
            case "operations" -> operations(data);
            case "mitre" -> mitre(data);
            case "playbooks" -> playbooks(data);
            case "files" -> fileManager(data);
            case "processes" -> processes(data);
            case "tunnels" -> tunnels(data);
            case "opsec" -> opsec(data);
            case "logs" -> eventLog(data);
            case "users" -> users(data);
            case "settings" -> settings(data);
            default -> ViewSupport.page("Unknown view", key, new JPanel());
        };
    }

    private static JComponent agents(MerumData data) {
        List<AgentViewModel> agents = data.listAgents();
        Object[][] rows = agents.stream().map(AgentViewModel::toRow).toArray(Object[][]::new);
        String[] columns = {
                "ID", "Status", "IP Address", "MAC", "Hostname", "User", "Priv",
                "OS", "Process", "PID", "Arch", "Last Seen", "Actions"
        };
        DenseTablePanel table = new DenseTablePanel(
                "Agents — ALL / ONLINE / IDLE / LOST",
                columns,
                rows,
                "Agent detail / interactive modules",
                row -> "Agent:        " + rows[row][0] + "\n"
                        + "Session:      " + rows[row][1] + " on " + rows[row][4] + " (" + rows[row][2] + ")\n"
                        + "Identity:     " + rows[row][5] + " · " + rows[row][6] + "\n"
                        + "Runtime:      " + rows[row][7] + " / " + rows[row][10] + "\n"
                        + "Process:      " + rows[row][8] + " (PID " + rows[row][9] + ")\n\n"
                        + "Modules: Shell | Process List | File Manager | Port Forward | Sysinfo"
        );
        long online = agents.stream().filter(AgentViewModel::isOnline).count();
        long idle = agents.stream().filter(AgentViewModel::isIdle).count();
        long lost = agents.stream().filter(AgentViewModel::isLost).count();
        return ViewSupport.page("Agents", "Refresh · Kill All · " + online + " online / " + idle + " idle / " + lost + " lost", table);
    }

    private static JComponent shell(MerumData data) {
        ShellViewModel view = data.shell();
        JTextArea terminal = ViewSupport.console(view.transcript());
        JPanel terminalPanel = commandConsole(terminal, "command...", "Execute");
        DenseTablePanel log = table("Event Log", view.events(), "Event detail");
        JTabbedPane tabs = ViewSupport.tabs("Terminal", terminalPanel, "Event Log", log);
        return ViewSupport.page("C2 Shell", "Agents · Sessions · C2 Status · Listeners · Credentials · Loot · DB Status", tabs);
    }

    private static JComponent listeners(MerumData data) {
        return ViewSupport.page(
                "Listeners",
                "+ New Listener · Refresh",
                table("Listeners", data.listeners(), "Listener configuration")
        );
    }

    private static JComponent payloads(MerumData data) {
        return ViewSupport.page("Payload Generator", "Build Output · Generate Payload", builderTabs(data.payloads()));
    }

    private static JComponent sweep(MerumData data) {
        DenseTablePanel targets = table(
                "Target Filter — ALL / ONLINE / WINDOWS / LINUX / ROOT",
                data.sweep(),
                "Sweep output"
        );
        JPanel command = new JPanel(new BorderLayout(6, 0));
        command.setBackground(Theme.CHROME);
        command.setBorder(Theme.padding(5, 8, 5, 8));
        JTextField field = new JTextField("whoami /all");
        command.add(field, BorderLayout.CENTER);
        // TODO: ligar ação ao Spring empacotado (in-process, sem rede)
        command.add(new JButton("Execute Sweep"), BorderLayout.EAST);
        JPanel body = new JPanel(new BorderLayout());
        body.add(command, BorderLayout.NORTH);
        body.add(targets, BorderLayout.CENTER);
        return ViewSupport.page(
                "Sweep",
                "Presets: whoami · net users · ps list · netstat · env dump · arp table · sudo check",
                body
        );
    }

    private static JComponent timeline(MerumData data) {
        return ViewSupport.page(
                "Timeline",
                "Search events · status counts",
                table("Timeline — ALL / OK / ERR / WARN / SYS", data.timeline(), "Timeline event")
        );
    }

    private static JComponent network(MerumData data) {
        NetworkViewModel view = data.network();
        JTabbedPane tabs = ViewSupport.tabs(
                "Topology", networkHosts(view),
                "Scanner", scanner(view),
                "Issue activity", issueActivity(view),
                "Admin", ViewSupport.tabs(
                        "Nodes", networkHosts(view),
                        "Vulnerabilities", issueActivity(view),
                        "Ports", portTable(view),
                        "Stats", ViewSupport.form(view.stats())
                )
        );
        return ViewSupport.page("Network Map / Scanner", "Topology · Scanner · Nodes · Vulns · Ports · Stats", tabs);
    }

    private static JComponent scanner(NetworkViewModel view) {
        DenseTablePanel hosts = table("Network Scanner — 10.10.20.0/24", view.scannerHosts(), "Host detail / scan output");
        return ViewSupport.tabs(
                "Results", hosts,
                view.standardScanner().tab(), builder(view.standardScanner()),
                view.customScanner().tab(), builder(view.customScanner())
        );
    }

    private static JComponent networkHosts(NetworkViewModel view) {
        return table("Network Sessions / Hosts", view.hosts(), "Node / relationship detail");
    }

    private static JComponent portTable(NetworkViewModel view) {
        return table("Discovered Ports", view.ports(), "Service evidence");
    }

    private static JComponent issueActivity(NetworkViewModel view) {
        return new IssueActivityPanel(view.issues());
    }

    private static JComponent loot(MerumData data) {
        return ViewSupport.page(
                "Loot",
                "Total Files 12 · Total Size 48.7 MB · Critical 3 · Screenshots 4 · Keys/Certs 2 · Hash Files 3 · Agents 4",
                table("Collected Loot", data.loot(), "Loot metadata / preview")
        );
    }

    private static JComponent credentials(MerumData data) {
        return ViewSupport.page(
                "Credentials",
                "Total Credentials 9 · Verified 5 · Unverified 4 · SSH Keys 2 · Passwords 3 · Hashes 3 · Tokens/Keys 1",
                table("Credential Store", data.credentials(), "Credential detail / validation")
        );
    }

    private static JComponent clipboard(MerumData data) {
        ClipboardViewModel view = data.clipboard();
        DenseTablePanel entries = table("Clipboard Entries", view.entries(), "Clipboard entry");
        JTabbedPane tabs = ViewSupport.tabs(
                "Entries", entries,
                view.addEntry().tab(), builder(view.addEntry())
        );
        return ViewSupport.page("Clipboard", "CRED · HASH · IP-HOST · COMMAND · NOTE · OTHER", tabs);
    }

    private static JComponent reports(MerumData data) {
        ReportsViewModel view = data.reports();
        JTabbedPane tabs = ViewSupport.tabs(
                "Report Library", table("Report Library", view.library(), "Report metadata / contents"),
                "Issue Activity", new IssueActivityPanel(view.issues()),
                view.builder().tab(), builder(view.builder())
        );
        return ViewSupport.page("Reports", "Total Reports 3 · Exported 1 · Generated 1 · Drafts 1", tabs);
    }

    private static JComponent arsenal(MerumData data) {
        return ViewSupport.page(
                "Arsenal",
                "Exploits · Implants · Playbooks · Tunnels · File Manager",
                ViewSupport.tabs(
                        "Build Manager", buildManager(data),
                        "Exploits", exploits(data),
                        "Implants", implants(data),
                        "Playbooks", playbooks(data),
                        "Tunnels", tunnels(data),
                        "File Manager", fileManager(data)
                )
        );
    }

    private static JComponent buildManager(MerumData data) {
        return table("Build Manager — ALL / NET / AGT / LIBS", data.arsenalBuilds(), "Build configuration / log");
    }

    private static JComponent exploits(MerumData data) {
        return ViewSupport.page(
                "Exploit Arsenal",
                "Platform · Category · Search",
                table("Exploits", data.exploits(), "Exploit options / target / output")
        );
    }

    private static JComponent implants(MerumData data) {
        ImplantsViewModel view = data.implants();
        Object[] tabs = new Object[(view.sections().size() + 1) * 2];
        int index = 0;
        for (FormViewModel section : view.sections()) {
            tabs[index++] = section.tab();
            tabs[index++] = builder(section);
        }
        tabs[index++] = "Summary";
        tabs[index] = ViewSupport.form(view.summary());
        return ViewSupport.page("Implants", "Generate Implant", ViewSupport.tabs(tabs));
    }

    private static JComponent playbooks(MerumData data) {
        return ViewSupport.page(
                "Playbooks",
                "Quick Recon · Cred Harvest · Establish Persistence · OPSEC Cleanup · Run Playbook",
                table("Playbooks", data.playbooks(), "Selected playbook steps / run log")
        );
    }

    private static JComponent fileManager(MerumData data) {
        return ViewSupport.page(
                "File Manager",
                "Agent · Path C:\\ · Quick Paths · Upload · Refresh",
                table("Remote File Manager — MERUM-001 · WIN-DC01", data.files(), "File metadata / Download · Execute · Delete")
        );
    }

    private static JComponent processes(MerumData data) {
        return ViewSupport.page(
                "Process List",
                "Agent MERUM-001 · WIN-DC01 · Filter name / PID · Suspicious only · Refresh",
                table("Processes", data.processes(), "Process detail / Kill · Migrate · Inject")
        );
    }

    private static JComponent tunnels(MerumData data) {
        return ViewSupport.page(
                "Pivot & Tunnel Manager",
                "+ New Tunnel · Active chain",
                table("Tunnels", data.tunnels(), "Tunnel chain / traffic statistics")
        );
    }

    private static JComponent operations(MerumData data) {
        return ViewSupport.page(
                "Operation Planner",
                "PLANNED 3 · EXECUTING 2 · COMPLETED 2 · BLOCKED 1",
                table("Operation Board", data.operations(), "Task description / move phase")
        );
    }

    private static JComponent mitre(MerumData data) {
        return ViewSupport.page(
                "MITRE ATT&CK — Enterprise",
                "ALL · PLANNED · ACTIVE · DONE · RESET · click technique to cycle status",
                table("Technique Matrix", data.mitreTechniques(), "Technique status / operation evidence")
        );
    }

    private static JComponent opsec(MerumData data) {
        return ViewSupport.page(
                "OPSEC / IOC Tracker",
                "OPSEC Score 40% · DIRTY 6 · CLEANING 1 · CLEAN 1 · VERIFIED 2 · Clean Visible",
                table(
                        "IOC Tracker — all / file / registry / process / service / network / log",
                        data.opsecIndicators(),
                        "IOC evidence / cleanup history"
                )
        );
    }

    private static JComponent eventLog(MerumData data) {
        return ViewSupport.page(
                "Event Log",
                "ALL · INFO · COMMAND · AGENT · WARNING · SYSTEM",
                table("Events", data.eventLog(), "Raw event / local envelope")
        );
    }

    private static JComponent users(MerumData data) {
        List<UserViewModel> users = data.listUsers();
        List<RoleViewModel> roles = data.listRoles();
        Object[][] rows = users.stream().map(UserViewModel::toRow).toArray(Object[][]::new);
        long admins = users.stream().filter(user -> "ADMIN".equals(user.role())).count();
        long operators = users.stream().filter(user -> "OPERATOR".equals(user.role())).count();
        return ViewSupport.page(
                "Users",
                "Total Operators " + users.size() + " · Admins " + admins + " · Operators " + operators
                        + " · Roles " + roles.size() + " · Refresh · + New Operator",
                ViewSupport.table(
                        "Operators",
                        new String[]{"ID", "Name", "Username", "Role", "Actions"},
                        rows,
                        "Operator / role permissions\nStatic fallback · in-process wiring pending"
                )
        );
    }

    private static JComponent settings(MerumData data) {
        List<FormViewModel> sections = data.settings();
        Object[] tabs = new Object[sections.size() * 2];
        int index = 0;
        for (FormViewModel section : sections) {
            tabs[index++] = section.tab();
            tabs[index++] = ViewSupport.form(flatten(section.fields()));
        }
        return ViewSupport.page(
                "Settings",
                "General · C2 Server · Database · Integration · Security · Logging · About · Danger Zone",
                ViewSupport.tabs(tabs)
        );
    }

    private static DenseTablePanel table(String title, TableViewModel view, String detailTitle) {
        return ViewSupport.table(title, view.columns(), view.rows(), detailTitle);
    }

    private static JComponent builder(FormViewModel view) {
        return ViewSupport.builder(view.title(), view.fields());
    }

    private static JTabbedPane builderTabs(List<FormViewModel> sections) {
        Object[] tabs = new Object[sections.size() * 2];
        int index = 0;
        for (FormViewModel section : sections) {
            tabs[index++] = section.tab();
            tabs[index++] = builder(section);
        }
        return ViewSupport.tabs(tabs);
    }

    private static String[] flatten(String[][] fields) {
        String[] flat = new String[fields.length * 2];
        for (int index = 0; index < fields.length; index++) {
            flat[index * 2] = fields[index][0];
            flat[index * 2 + 1] = fields[index][1];
        }
        return flat;
    }

    private static JPanel commandConsole(JTextArea console, String placeholder, String buttonText) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.CHROME);
        JScrollPane consoleScroll = new JScrollPane(console);
        consoleScroll.getViewport().setBackground(Theme.EDITOR);
        panel.add(consoleScroll, BorderLayout.CENTER);
        JPanel command = new JPanel(new BorderLayout(6, 0));
        command.setBackground(Theme.CHROME);
        command.setBorder(Theme.padding(5, 7, 5, 7));
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", placeholder);
        command.add(field, BorderLayout.CENTER);
        // TODO: ligar ação ao Spring empacotado (in-process, sem rede)
        command.add(new JButton(buttonText), BorderLayout.EAST);
        panel.add(command, BorderLayout.SOUTH);
        return panel;
    }
}
