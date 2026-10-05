package com.manager.client.data;

import com.manager.client.model.AgentViewModel;
import com.manager.client.model.C2InfoViewModel;
import com.manager.client.model.FormViewModel;
import com.manager.client.model.IssueViewModel;
import com.manager.client.model.IssueViewModel.Severity;
import com.manager.client.model.RoleViewModel;
import com.manager.client.model.TableViewModel;
import com.manager.client.model.UserViewModel;
import com.manager.server.dtos.auth.LoginDtos;
import com.manager.server.model.entity.auth.User;
import com.manager.server.service.auth.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The sole data seam for the Swing client. It deliberately contains only faithful static
 * fallback data until the packaged Spring application supplies in-process collaborators.
 */
@Component
public final class MerumData implements ApplicationContextAware {
    private static volatile ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static User login(String username, String password) throws AuthException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthException("Invalid username or password.");
        }

        ApplicationContext context = applicationContext;
        if (context == null) {
            throw new AuthException("Authentication service is unavailable.");
        }

        LoginDtos credentials = new LoginDtos();
        credentials.setUsername(username);
        credentials.setPassword(password);

        try {
            AuthService authService = context.getBean(AuthService.class);
            authService.authenticateUser(credentials, createLoginRequest());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User user) {
                return user;
            }

            SecurityContextHolder.clearContext();
            throw new AuthException("Authentication did not return a user.");
        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            throw new AuthException("Invalid username or password.", exception);
        } catch (BeansException exception) {
            SecurityContextHolder.clearContext();
            throw new AuthException("Authentication service is unavailable.", exception);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            throw new AuthException("Unable to authenticate.", exception);
        }
    }

    private static HttpServletRequest createLoginRequest() {
        Map<String, Object> sessionAttributes = new ConcurrentHashMap<>();

        HttpSession session = (HttpSession) Proxy.newProxyInstance(
                MerumData.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "setAttribute" -> {
                        sessionAttributes.put((String) arguments[0], arguments[1]);
                        yield null;
                    }
                    case "getAttribute" -> sessionAttributes.get((String) arguments[0]);
                    case "removeAttribute" -> {
                        sessionAttributes.remove((String) arguments[0]);
                        yield null;
                    }
                    case "getId" -> "swing-login";
                    case "toString" -> "SwingLoginSession";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    default -> defaultValue(method.getReturnType());
                }
        );

        return (HttpServletRequest) Proxy.newProxyInstance(
                MerumData.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getSession" -> session;
                    case "toString" -> "SwingLoginRequest";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0f;
        }
        return 0d;
    }

    public List<AgentViewModel> listAgents() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return List.of(
                new AgentViewModel("MERUM-001", "ONLINE", "10.10.20.11", "00:15:5D:01:20:11", "WIN-DC01", "svc_backup", "SYSTEM", "Windows Server 2022", "svchost.exe", 784, "x64", "2s", "Shell · Kill"),
                new AgentViewModel("MERUM-002", "ONLINE", "10.10.20.24", "00:15:5D:01:20:24", "UBUNTU-WEB", "www-data", "root", "Ubuntu 24.04", "systemd", 1, "x64", "4s", "Shell · Kill"),
                new AgentViewModel("MERUM-003", "IDLE", "10.10.30.53", "00:15:5D:01:30:53", "WIN-WS03", "analyst", "User", "Windows 11", "explorer.exe", 4120, "x64", "1m 12s", "Shell · Kill"),
                new AgentViewModel("MERUM-004", "LOST", "10.10.40.18", "00:15:5D:01:40:18", "CENTOS-DB", "postgres", "root", "CentOS Stream", "postgres", 1198, "x64", "18m", "Remove"),
                new AgentViewModel("MERUM-005", "ONLINE", "10.10.20.15", "00:15:5D:01:20:15", "WIN-EXCH01", "SYSTEM", "SYSTEM", "Windows Server 2019", "w3wp.exe", 2356, "x64", "7s", "Shell · Kill"),
                new AgentViewModel("MERUM-006", "ONLINE", "10.10.50.9", "00:15:5D:01:50:09", "DEBIAN-PROXY", "proxy", "User", "Debian 12", "nginx", 940, "x64", "9s", "Shell · Kill")
        );
    }

    public C2InfoViewModel c2Info() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return new C2InfoViewModel("14", "—", "—", "—", "—");
    }

    public TableViewModel dashboardHealth() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"SERVICE", "VALUE", "STATUS"},
                new Object[][]{
                        {"C2 Listener", "0.0.0.0:4444", "ONLINE"},
                        {"HTTPS Listener", "0.0.0.0:8443", "ONLINE"},
                        {"Database", "localhost:3306", "CONNECTED"},
                        {"Spring Boot", "packaged process", "RUNNING"},
                        {"Beacon", "10s", ""},
                        {"Jitter", "23%", ""},
                        {"Threads", "14", ""},
                        {"Uptime", "—", ""},
                        {"Memory", "—", ""},
                        {"CPU Load", "—", ""},
                        {"Disk Free", "—", ""}
                }
        );
    }

    public ShellViewModel shell() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return new ShellViewModel(
                "MERUM C2 — Interactive Operator Shell\n"
                        + "Authenticated as ROOT_ADMIN\n"
                        + "Type 'help' for available commands.\n\n"
                        + "merum > agents\n"
                        + "[*] 6 registered agents; 4 online, 1 idle, 1 lost\n"
                        + "merum > c2 status\n"
                        + "[*] Listener 0.0.0.0:4444 ONLINE\n"
                        + "merum > _",
                table(
                        new String[]{"Time", "Level", "Source", "Message"},
                        new Object[][]{
                                {"03:28:41", "INFO", "C2", "MERUM-005 checked in"},
                                {"03:28:39", "CMD", "ROOT_ADMIN", "agents"},
                                {"03:28:35", "INFO", "Listener", "HTTPS listener healthy"}
                        }
                )
        );
    }

    public TableViewModel listeners() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"ID", "Name", "Protocol", "Host", "Port", "Status", "Agents", "Actions"},
                new Object[][]{
                        {"LST-001", "HTTPS Primary", "HTTPS", "0.0.0.0", 8443, "RUNNING", 4, "Stop · Edit"},
                        {"LST-002", "TCP Beacon", "TCP", "0.0.0.0", 4444, "RUNNING", 2, "Stop · Edit"},
                        {"LST-003", "DNS Fallback", "DNS", "10.10.20.5", 53, "STAGED", 0, "Start · Edit"}
                }
        );
    }

    public List<FormViewModel> payloads() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return List.of(
                builder("Basic", "Basic Payload Configuration", new String[][]{
                        {"Operating System", "Windows | Linux | macOS"}, {"Architecture", "x64 | x86 | arm64"},
                        {"Output Format", "exe | dll | elf | raw | shellcode"}, {"Listener", "HTTPS Primary | TCP Beacon"},
                        {"Callback Host", "10.10.20.5"}, {"Comms Protocol", "HTTPS | TCP | DNS"},
                        {"Sleep / Jitter", "10s / 23%"}, {"Max Retry", "10"}, {"Kill Date", "2026-12-31"},
                        {"Stageless", "enabled", "check"}
                }),
                builder("Evasion", "Evasion", new String[][]{
                        {"Payload Encoding", "xor | base64 | aes"}, {"Code Obfuscation", "standard | aggressive"},
                        {"Prepend null bytes", "enabled", "check"}, {"Injection Technique", "CreateRemoteThread | APC | Early Bird"},
                        {"Spawn-To", "C:\\Windows\\System32\\rundll32.exe"}, {"EDR / AV Bypass", "Syscalls | AMSI Patch | ETW Patch"},
                        {"Sandbox Check", "enabled", "check"}, {"Persistence", "none | registry | service | scheduled task"}
                }),
                builder("Network", "Network Profile", new String[][]{
                        {"User-Agent", "Mozilla/5.0"}, {"Referer", "portal.local"}, {"Payload Proxy", "none"},
                        {"Check-in Path", "/api/v1/status"}, {"Verify certificate", "enabled", "check"},
                        {"Generated Header", "Accept: */*; Cache-Control: no-cache"}
                }),
                builder("Advanced", "Advanced / Builder Command", new String[][]{
                        {"Raw Builder Command", "merum-builder --os windows --arch x64 --listener LST-001"},
                        {"Builder Binding", "Pending in-process wiring"}, {"Build Output", "target/payloads/"}
                })
        );
    }

    public TableViewModel sweep() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"Selected", "Agent", "Host", "OS", "Privilege", "Status", "Result", "Exit", "Latency"},
                new Object[][]{
                        {true, "MERUM-001", "WIN-DC01", "Windows", "SYSTEM", "ONLINE", "NT AUTHORITY\\SYSTEM", 0, "184ms"},
                        {true, "MERUM-002", "UBUNTU-WEB", "Linux", "root", "ONLINE", "uid=0(root)", 0, "92ms"},
                        {true, "MERUM-003", "WIN-WS03", "Windows", "User", "ONLINE", "WIN-WS03\\analyst", 0, "231ms"},
                        {false, "MERUM-004", "CENTOS-DB", "Linux", "root", "OFFLINE", "not executed", "—", "—"},
                        {true, "MERUM-005", "WIN-EXCH01", "Windows", "SYSTEM", "ONLINE", "NT AUTHORITY\\SYSTEM", 0, "318ms"},
                        {true, "MERUM-006", "DEBIAN-PROXY", "Linux", "www-data", "ONLINE", "www-data", 0, "109ms"}
                }
        );
    }

    public TableViewModel timeline() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"Time", "Type", "Agent", "Host", "Event", "Status", "Operator"},
                new Object[][]{
                        {"03:28:41", "AGENT", "MERUM-005", "WIN-EXCH01", "Beacon check-in", "OK", "system"},
                        {"03:28:09", "COMMAND", "MERUM-005", "WIN-EXCH01", "lsass minidump requested", "OK", "ROOT_ADMIN"},
                        {"03:16:54", "LOOT", "MERUM-003", "WIN-WS03", "SAM hive copy captured", "WARN", "ROOT_ADMIN"},
                        {"03:12:17", "LATERAL", "MERUM-003", "WIN-EXCH01", "SMB authentication", "OK", "operator"},
                        {"02:58:02", "PROCESS", "MERUM-003", "WIN-WS03", "Injection attempt detected", "ERR", "operator"},
                        {"02:44:12", "SYSTEM", "MERUM-002", "UBUNTU-WEB", "SOCKS5 tunnel opened", "SYS", "ROOT_ADMIN"}
                }
        );
    }

    public NetworkViewModel network() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        TableViewModel hosts = table(
                new String[]{"Node", "IP", "MAC", "OS", "Role", "Agent", "Last Scan", "State"},
                new Object[][]{
                        {"MERUM-C2", "10.10.20.5", "00:15:5D:00:20:05", "Linux", "C2", "local", "03:29", "UP"},
                        {"WIN-DC01", "10.10.20.11", "00:15:5D:01:20:11", "Windows", "Domain Controller", "MERUM-001", "03:28", "OWNED"},
                        {"WIN-EXCH01", "10.10.20.15", "00:15:5D:01:20:15", "Windows", "Mail", "MERUM-005", "03:28", "OWNED"},
                        {"UBUNTU-WEB", "10.10.20.24", "00:15:5D:01:20:24", "Linux", "Web", "MERUM-002", "03:27", "UP"}
                }
        );
        return new NetworkViewModel(
                hosts,
                table(
                        new String[]{"IP", "Hostname", "OS", "TTL", "Open Ports", "Vulns"},
                        new Object[][]{
                                {"10.10.20.5", "MERUM-C2", "Linux", 64, "22, 8080, 8443", 1},
                                {"10.10.20.11", "WIN-DC01", "Windows Server 2022", 128, "53, 88, 135, 389, 445", 2},
                                {"10.10.20.15", "WIN-EXCH01", "Windows Server 2019", 128, "80, 443, 445, 5985", 2},
                                {"10.10.20.24", "UBUNTU-WEB", "Ubuntu 24.04", 64, "22, 80, 443", 1}
                        }
                ),
                builder("Standard Scanner", "Standard Scanner", new String[][]{
                        {"Subnet (CIDR)", "10.10.20.0/24"}, {"Ports", "Common | Full | Web | Databases | Risk Ports | Custom"},
                        {"Threads", "50"}, {"Scan Types", "TCP SYN, OS Detect, Svc Probe, Vuln Scan, ARP Sweep, ICMP Ping"},
                        {"Aggression", "STEALTH | STD | AGGR"}, {"Auto Loop", "disabled", "check"}
                }),
                builder("Custom / Nmap", "Custom Nmap", new String[][]{
                        {"Binary", "/usr/bin/nmap"}, {"Arguments", "-sS -sV -O --script vuln"}, {"Target", "10.10.20.0/24"}
                }),
                table(
                        new String[]{"Host", "Port", "Protocol", "Service", "Version", "State"},
                        new Object[][]{
                                {"WIN-DC01", 88, "tcp", "kerberos", "Microsoft Windows Kerberos", "open"},
                                {"WIN-DC01", 445, "tcp", "microsoft-ds", "Windows Server 2022", "open"},
                                {"WIN-EXCH01", 443, "tcp", "https", "Microsoft IIS 10.0", "open"},
                                {"UBUNTU-WEB", 22, "tcp", "ssh", "OpenSSH 9.6", "open"}
                        }
                ),
                new String[]{"Sessions", "3", "Nodes", "18", "Open Ports", "47", "Vulnerabilities", "6"},
                issues()
        );
    }

    public TableViewModel loot() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"Type", "Filename", "Path", "Size", "Host", "Agent", "Captured", "Tags", "Actions"},
                new Object[][]{
                        {"HASH", "ntds.dit", "C:\\Windows\\NTDS", "31.4 MB", "WIN-DC01", "MERUM-001", "02:31", "critical,ad", "View · Export"},
                        {"SCREENSHOT", "desktop-0328.png", "C:\\ProgramData\\Merum", "1.8 MB", "WIN-WS03", "MERUM-003", "03:18", "screen", "Preview · Export"},
                        {"KEY", "id_rsa", "/home/deploy/.ssh", "3.2 KB", "UBUNTU-WEB", "MERUM-002", "02:47", "ssh,key", "View · Export"},
                        {"FILE", "Q4-financials.xlsx", "D:\\Finance", "14.9 MB", "WIN-EXCH01", "MERUM-005", "03:26", "finance", "Download"}
                }
        );
    }

    public TableViewModel credentials() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"Type", "Host", "Port", "Username", "Secret", "Source", "Agent", "Captured", "Verified", "Actions"},
                new Object[][]{
                        {"NTLM", "WIN-DC01", 445, "svc_backup", "aad3b435…:31d6cfe0…", "LSASS", "MERUM-001", "02:31", true, "Copy · Test"},
                        {"PASSWORD", "WIN-EXCH01", 5985, "Administrator", "••••••••••", "Winlogon", "MERUM-005", "03:29", true, "Reveal · Test"},
                        {"SSH KEY", "UBUNTU-WEB", 22, "deploy", "-----BEGIN OPENSSH…", "/home/deploy/.ssh", "MERUM-002", "02:47", true, "Copy · Export"},
                        {"TOKEN", "portal.local", 443, "analyst", "eyJhbGciOi…", "Browser Data", "MERUM-003", "03:16", false, "Copy · Test"}
                }
        );
    }

    public ClipboardViewModel clipboard() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return new ClipboardViewModel(
                table(
                        new String[]{"ID", "Tag", "Label", "Content", "Created", "Operator", "Actions"},
                        new Object[][]{
                                {"CB-001", "CRED", "Domain admin", "CORP\\administrator : ••••••••", "03:30", "ROOT_ADMIN", "Copy · Delete"},
                                {"CB-002", "IP-HOST", "Exchange", "10.10.20.15 WIN-EXCH01", "03:22", "operator", "Copy · Delete"},
                                {"CB-003", "COMMAND", "LSASS dump", "rundll32.exe C:\\Windows\\System32\\comsvcs.dll, MiniDump", "03:13", "ROOT_ADMIN", "Copy · Delete"},
                                {"CB-004", "NOTE", "Pivot path", "MERUM-C2 → UBUNTU-WEB → CENTOS-DB", "02:49", "operator", "Copy · Delete"}
                        }
                ),
                builder("Add Entry", "Add Entry", new String[][]{
                        {"Label", ""}, {"Tag", "CRED | HASH | IP-HOST | COMMAND | NOTE | OTHER"}, {"Content", ""}
                })
        );
    }

    public ReportsViewModel reports() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return new ReportsViewModel(
                table(
                        new String[]{"Type", "Title", "Status", "Created", "Size", "Agents", "Vulns", "Creds", "Actions"},
                        new Object[][]{
                                {"ENGAGEMENT", "Merum Internal Assessment", "EXPORTED", "2026-08-27 03:31", "2.8 MB", 6, 6, 9, "Open · Export"},
                                {"TECHNICAL", "Network Scanner Findings", "GENERATED", "2026-08-27 03:25", "914 KB", 4, 6, 0, "Open · Export"},
                                {"EXECUTIVE", "Operation Summary", "DRAFT", "2026-08-27 03:12", "—", 6, 3, 5, "Edit · Generate"}
                        }
                ),
                issues(),
                builder("Report Builder", "Report Builder", new String[][]{
                        {"Title", "Merum Security Assessment"}, {"Report Type", "Engagement | Technical | Executive | Evidence"},
                        {"Output", "PDF | HTML | MD | JSON"}, {"Sections", "Executive Summary, Scope, Hosts, Issues, Agents, Credentials, Loot, Timeline"}
                })
        );
    }

    public TableViewModel arsenalBuilds() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"Domain", "Tool", "Target", "Language", "Status", "Last Build", "Artifact"},
                new Object[][]{
                        {"NET", "LocalFingerPrint", "linux-x64", "C", "READY", "03:11", "localfingerprint"},
                        {"LIBS", "libping", "linux-x64", "C", "READY", "02:58", "libping.a"},
                        {"LIBS", "libnet_utils", "linux-x64", "C++", "DIRTY", "02:44", "libnet_utils.a"},
                        {"NET", "D_DOS", "linux-x64", "C", "READY", "01:31", "d_dos"},
                        {"AGT", "beacon-linux", "linux-x64", "Rust", "READY", "03:24", "beacon"},
                        {"AGT", "beacon-windows", "windows-x64", "Rust", "BUILDING", "03:29", "beacon.exe"},
                        {"AGT", "shellcode-x64", "windows-x64", "ASM", "READY", "03:20", "stage.bin"}
                }
        );
    }

    public TableViewModel exploits() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"ID", "Name", "CVE", "Platform", "Category", "Rank", "Target"},
                new Object[][]{
                        {"EXP-001", "EternalBlue SMB RCE", "CVE-2017-0144", "Windows", "Remote", "excellent", "445/tcp"},
                        {"EXP-002", "PrintNightmare", "CVE-2021-34527", "Windows", "PrivEsc", "great", "spooler"},
                        {"EXP-003", "ProxyShell", "CVE-2021-34473", "Windows", "Web", "great", "Exchange"},
                        {"EXP-004", "Dirty Pipe", "CVE-2022-0847", "Linux", "Local", "excellent", "kernel"},
                        {"EXP-005", "PwnKit pkexec", "CVE-2021-4034", "Linux", "PrivEsc", "excellent", "pkexec"},
                        {"EXP-006", "Log4Shell", "CVE-2021-44228", "Multi", "Remote", "excellent", "Java/JNDI"},
                        {"EXP-007", "Spring4Shell", "CVE-2022-22965", "Multi", "Web", "great", "Spring MVC"},
                        {"EXP-008", "sudo Baron Samedit", "CVE-2021-3156", "Linux", "Local", "great", "sudo"}
                }
        );
    }

    public ImplantsViewModel implants() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return new ImplantsViewModel(
                List.of(
                        builder("Identity", "Implant / Beacon Builder", new String[][]{
                                {"Name", "merum-beacon"}, {"Operating System", "Windows | Linux | macOS"}, {"Architecture", "x64 | x86 | arm64"}, {"Output", "exe | dll | shellcode | elf"}
                        }),
                        builder("Callback / C2", "Callback / C2", new String[][]{
                                {"Listener", "HTTPS Primary | TCP Beacon | DNS Fallback"}, {"Sleep", "10s"}, {"Jitter", "23%"}, {"Retry", "10"}
                        }),
                        builder("Encoding / Obfuscation", "Encoding / Obfuscation", new String[][]{
                                {"Encoding", "AES | XOR | Base64"}, {"Obfuscation", "standard | aggressive"}, {"String Encryption", "enabled", "check"}
                        }),
                        builder("Process Injection", "Process Injection", new String[][]{
                                {"Technique", "CreateRemoteThread | APC | Early Bird"}, {"Spawn-To", "rundll32.exe"}, {"Syscalls", "enabled", "check"}
                        }),
                        builder("Persistence", "Persistence", new String[][]{{"Method", "none | registry | service | scheduled task | cron"}}),
                        builder("Evasion / Anti-Analysis", "Evasion / Anti-Analysis", new String[][]{
                                {"AMSI Patch", "enabled", "check"}, {"ETW Patch", "enabled", "check"}, {"Sandbox Checks", "enabled", "check"}
                        }),
                        builder("Lifetime", "Lifetime", new String[][]{{"Kill Date", "2026-12-31"}, {"Max Runtime", "7d"}, {"Working Hours", "08:00-20:00"}})
                ),
                new String[]{"Target", "windows-x64", "Listener", "LST-001 HTTPS", "Output", "merum-beacon.exe"}
        );
    }

    public TableViewModel playbooks() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"Playbook", "Phase", "Step", "Action", "Target Agent", "State"},
                new Object[][]{
                        {"Quick Recon", "Discovery", 1, "System info and identity", "MERUM-001", "READY"},
                        {"Quick Recon", "Discovery", 2, "Network interfaces and routes", "MERUM-001", "READY"},
                        {"Cred Harvest", "Credential Access", 1, "Enumerate credential stores", "MERUM-005", "READY"},
                        {"Cred Harvest", "Credential Access", 2, "Dump LSASS", "MERUM-005", "APPROVAL"},
                        {"Establish Persistence", "Persistence", 1, "Create scheduled task", "MERUM-001", "READY"},
                        {"OPSEC Cleanup", "Defense Evasion", 1, "Remove dropped files", "MERUM-003", "READY"}
                }
        );
    }

    public TableViewModel files() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"Type", "Name", "Size", "Permissions", "Owner", "Modified"},
                new Object[][]{
                        {"DIR", "Windows", "—", "drwxr-xr-x", "SYSTEM", "2026-08-24 11:20"},
                        {"DIR", "Users", "—", "drwxr-xr-x", "SYSTEM", "2026-08-22 08:14"},
                        {"DIR", "ProgramData", "—", "drwxr-xr-x", "SYSTEM", "2026-08-27 02:11"},
                        {"FILE", "pagefile.sys", "8.0 GB", "-rw-------", "SYSTEM", "2026-08-27 03:29"},
                        {"FILE", "bootmgr", "398 KB", "-rwxr-xr-x", "SYSTEM", "2025-11-02 09:00"}
                }
        );
    }

    public TableViewModel processes() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"PID", "PPID", "Name", "User", "CPU%", "MEM", "Cmdline"},
                new Object[][]{
                        {4, 0, "System", "SYSTEM", 0.4, "2.1 MB", "System"},
                        {612, 4, "smss.exe", "SYSTEM", 0.0, "1.2 MB", "\\SystemRoot\\System32\\smss.exe"},
                        {784, 704, "svchost.exe", "SYSTEM", 1.2, "38.4 MB", "svchost.exe -k netsvcs"},
                        {2356, 704, "w3wp.exe", "IIS APPPOOL", 3.1, "188 MB", "w3wp.exe -ap Exchange"},
                        {4120, 1180, "explorer.exe", "analyst", 0.9, "142 MB", "C:\\Windows\\explorer.exe"},
                        {5944, 784, "rundll32.exe", "SYSTEM", 7.4, "12.3 MB", "rundll32.exe comsvcs.dll MiniDump"}
                }
        );
    }

    public TableViewModel tunnels() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"ID", "Type", "Status", "Agent", "Binding", "Remote", "Bytes", "Conns", "Actions"},
                new Object[][]{
                        {"TUN-001", "SOCKS5", "ACTIVE", "MERUM-002", "127.0.0.1:1080", "dynamic", "84.2 MB", 12, "Stop · Copy"},
                        {"TUN-002", "TCP FORWARD", "ACTIVE", "MERUM-001", "127.0.0.1:13389", "10.10.30.53:3389", "18.7 MB", 1, "Stop · Copy"},
                        {"TUN-003", "REVERSE", "PAUSED", "MERUM-005", "0.0.0.0:1445", "10.10.40.18:445", "2.1 MB", 0, "Start · Delete"}
                }
        );
    }

    public TableViewModel operations() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"ID", "Title", "Phase", "Agent", "Tags", "Priority", "Time"},
                new Object[][]{
                        {"OP-001", "Domain Recon — AD Enum", "COMPLETED", "MERUM-001", "recon, AD", "high", "02:14"},
                        {"OP-002", "Kerberoasting — SPN Accounts", "COMPLETED", "MERUM-001", "cred, kerberos", "critical", "02:31"},
                        {"OP-003", "Lateral — WIN-EXCH01", "EXECUTING", "MERUM-003", "lateral, SMB", "high", "03:12"},
                        {"OP-004", "Dump LSASS — MERUM-005", "EXECUTING", "MERUM-005", "cred, LSASS", "critical", "03:28"},
                        {"OP-005", "Establish Persistence — WMI", "PLANNED", "MERUM-001", "persist, WMI", "high", "—"},
                        {"OP-006", "SOCKS Tunnel — CENTOS-DB", "PLANNED", "MERUM-002", "pivot, SOCKS5", "med", "—"},
                        {"OP-007", "Exfil — Financial Reports", "PLANNED", "MERUM-005", "exfil, HTTPS", "critical", "—"},
                        {"OP-008", "UAC Bypass — WIN-WS03", "BLOCKED", "MERUM-003", "privesc, UAC", "high", "02:58"}
                }
        );
    }

    public TableViewModel mitreTechniques() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"Tactic ID", "Tactic", "Technique ID", "Technique", "Status"},
                new Object[][]{
                        {"TA0043", "Reconnaissance", "T1595", "Active Scanning", "DONE"},
                        {"TA0001", "Initial Access", "T1190", "Exploit Public App", "DONE"},
                        {"TA0002", "Execution", "T1059", "Command and Script Interpreter", "ACTIVE"},
                        {"TA0003", "Persistence", "T1543", "Create or Modify System Process", "PLANNED"},
                        {"TA0004", "Privilege Escalation", "T1055", "Process Injection", "ACTIVE"},
                        {"TA0005", "Defense Evasion", "T1070", "Indicator Removal", "PLANNED"},
                        {"TA0006", "Credential Access", "T1003", "OS Credential Dumping", "DONE"},
                        {"TA0007", "Discovery", "T1046", "Network Service Discovery", "DONE"},
                        {"TA0008", "Lateral Movement", "T1550", "Pass the Hash", "ACTIVE"},
                        {"TA0009", "Collection", "T1113", "Screen Capture", "DONE"},
                        {"TA0011", "Command and Control", "T1071", "Application Layer Protocol", "ACTIVE"},
                        {"TA0010", "Exfiltration", "T1041", "Exfiltration Over C2", "PLANNED"},
                        {"TA0040", "Impact", "T1489", "Service Stop", "NONE"}
                }
        );
    }

    public TableViewModel opsecIndicators() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"ID", "Type", "Host", "Agent", "Path", "Detail", "Risk", "Status", "Action"},
                new Object[][]{
                        {"IOC-001", "file", "WIN-DC01", "MERUM-001", "C:\\Windows\\Temp\\svchosts.exe", "Dropped beacon binary", "critical", "DIRTY", "Clean"},
                        {"IOC-002", "registry", "WIN-DC01", "MERUM-001", "HKCU\\...\\Run\\Update", "Persistence key added", "critical", "DIRTY", "Clean"},
                        {"IOC-003", "log", "WIN-DC01", "MERUM-001", "Windows Security Log", "4625 failed logon x47", "high", "DIRTY", "Clean"},
                        {"IOC-004", "file", "UBUNTU-WEB", "MERUM-002", "/tmp/.update", "Cron persistence script", "high", "CLEAN", "Verify"},
                        {"IOC-005", "file", "UBUNTU-WEB", "MERUM-002", "/tmp/linpeas.sh", "PrivEsc enum script", "med", "VERIFIED", "Done"},
                        {"IOC-006", "network", "UBUNTU-WEB", "MERUM-002", "0.0.0.0:1080", "SOCKS5 proxy listener", "high", "DIRTY", "Clean"},
                        {"IOC-007", "process", "WIN-WS03", "MERUM-003", "lsass.exe (accessed)", "Minidump via comsvcs.dll", "critical", "DIRTY", "Clean"},
                        {"IOC-008", "file", "WIN-WS03", "MERUM-003", "C:\\Users\\user\\AppData\\sam.bak", "SAM hive copy", "critical", "CLEANING", "Verify"},
                        {"IOC-009", "service", "WIN-EXCH01", "MERUM-005", "WinUpdate (svc)", "Malicious service installed", "critical", "DIRTY", "Clean"}
                }
        );
    }

    public TableViewModel eventLog() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return table(
                new String[]{"Time", "Level", "Module", "Agent", "Operator", "Message"},
                new Object[][]{
                        {"03:31:04", "INFO", "Reports", "—", "ROOT_ADMIN", "Engagement report exported"},
                        {"03:29:51", "COMMAND", "Shell", "MERUM-005", "ROOT_ADMIN", "rundll32 comsvcs MiniDump"},
                        {"03:29:42", "AGENT", "C2", "MERUM-005", "system", "Beacon check-in from 10.10.20.15"},
                        {"03:28:10", "WARNING", "OPSEC", "MERUM-003", "operator", "SAM hive artifact remains"},
                        {"03:27:04", "SYSTEM", "Listener", "—", "system", "HTTPS listener health check passed"}
                }
        );
    }

    public List<UserViewModel> listUsers() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return List.of(
                new UserViewModel(1, "Root Administrator", "root", "ADMIN", "Role · Pwd · Del"),
                new UserViewModel(2, "Red Team Operator", "operator", "OPERATOR", "Role · Pwd · Del"),
                new UserViewModel(3, "Assessment Viewer", "viewer", "VIEWER", "Role · Pwd · Del")
        );
    }

    public List<RoleViewModel> listRoles() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return List.of(
                new RoleViewModel(1, "ADMIN"),
                new RoleViewModel(2, "OPERATOR"),
                new RoleViewModel(3, "VIEWER")
        );
    }

    public List<FormViewModel> settings() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return List.of(
                form("General", new String[]{"Operator", "ROOT_ADMIN", "Theme", "FlatDarkLaf", "Language", "English", "Auto refresh", "10 seconds"}),
                form("C2 Server", new String[]{"Host", "0.0.0.0", "Port", "4444", "Beacon interval", "10s", "Jitter", "23%"}),
                form("Database", new String[]{"Provider", "Packaged Spring managed", "Connection", "CONNECTED", "Pool size", "10", "Timeout", "30s"}),
                form("Integration", new String[]{"Mode", "In-process", "Host server", "Packaged Spring", "Transport", "None", "Binding", "Pending"}),
                form("Security", new String[]{"Session timeout", "30 min", "MFA", "required", "Audit events", "enabled", "TLS validation", "host managed"}),
                form("Logging", new String[]{"Level", "INFO", "Retention", "30 days", "Console", "enabled", "File", "logs/merum-client.log"}),
                form("About", new String[]{"Product", "Merum Java Client", "Version", "3.0.1", "Runtime", "Java 21", "UI", "Swing · FlatLaf 3.7.2"}),
                form("Danger Zone", new String[]{"Disconnect all agents", "Confirmation required", "Clear scan history", "Confirmation required", "Restart C2 server", "Confirmation required", "Factory reset", "Disabled in desktop client"})
        );
    }

    private List<IssueViewModel> issues() {
        // TODO: ligar ao Spring empacotado (in-process, sem rede)
        return List.of(
                new IssueViewModel(Severity.HIGH, "SMB signing not required", "10.10.20.11:445", "Firm", "The server permits unsigned SMB sessions. Enforce SMB signing and validate relay exposure."),
                new IssueViewModel(Severity.MEDIUM, "TLS certificate hostname mismatch", "10.10.20.15:443", "Firm", "The presented certificate does not contain the scanned hostname."),
                new IssueViewModel(Severity.MEDIUM, "Outdated OpenSSH service", "10.10.20.24:22", "Tentative", "Version fingerprint indicates a package behind the current security baseline."),
                new IssueViewModel(Severity.LOW, "HTTP TRACE method enabled", "10.10.20.24:80", "Firm", "TRACE responded with 200 OK. Disable unnecessary methods."),
                new IssueViewModel(Severity.INFORMATION, "Kerberos service exposed", "10.10.20.11:88", "Certain", "Service was identified during TCP SYN and version probing."),
                new IssueViewModel(Severity.INFORMATION, "Server header disclosed", "10.10.20.15:443", "Certain", "Response headers identify Microsoft-IIS/10.0.")
        );
    }

    private static TableViewModel table(String[] columns, Object[][] rows) {
        return new TableViewModel(columns, rows);
    }

    private static FormViewModel builder(String tab, String title, String[][] fields) {
        return new FormViewModel(tab, title, fields);
    }

    private static FormViewModel form(String tab, String[] fields) {
        String[][] pairs = new String[fields.length / 2][];
        for (int index = 0; index < fields.length; index += 2) {
            pairs[index / 2] = new String[]{fields[index], fields[index + 1]};
        }
        return new FormViewModel(tab, tab, pairs);
    }

    public record ShellViewModel(String transcript, TableViewModel events) {
    }

    public record NetworkViewModel(
            TableViewModel hosts,
            TableViewModel scannerHosts,
            FormViewModel standardScanner,
            FormViewModel customScanner,
            TableViewModel ports,
            String[] stats,
            List<IssueViewModel> issues
    ) {
    }

    public record ClipboardViewModel(TableViewModel entries, FormViewModel addEntry) {
    }

    public record ReportsViewModel(
            TableViewModel library,
            List<IssueViewModel> issues,
            FormViewModel builder
    ) {
    }

    public record ImplantsViewModel(List<FormViewModel> sections, String[] summary) {
    }
}
