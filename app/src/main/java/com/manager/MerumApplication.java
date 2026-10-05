package com.manager;

import com.manager.client.ui.MainFrame;
import com.manager.client.ui.dialogs.LoginDialog;
import com.manager.client.ui.theme.Theme;
import com.manager.client.verification.VerificationRenderer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class MerumApplication {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        loadDotEnv();

        boolean verificationMode = args.length > 0
                && ("--verify".equals(args[0]) || "--verify-all".equals(args[0]));

        if (verificationMode) {
            System.setProperty("java.awt.headless", "true");
        }

        Theme.setup();

        if (verificationMode) {
            runVerification(args);
            return;
        }

        SpringApplication.run(MerumApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("No graphical display is available.");
        }

        ConfigurableApplicationContext context = event.getApplicationContext();
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);

            if (loginDialog.isLoginSuccessful()) {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                return;
            }

            context.close();
            System.exit(0);
        });
    }

    private static void runVerification(String[] args) {
        if ("--verify-all".equals(args[0])) {
            Path output = args.length > 1
                    ? Path.of(args[1])
                    : Path.of("target", "verification");
            VerificationRenderer.renderAll(output);
        } else {
            Path output = args.length > 1
                    ? Path.of(args[1])
                    : Path.of("target", "verification", "dashboard.png");
            VerificationRenderer.render(output);
        }
    }

    private static void loadDotEnv() {
        Path env = findEnvFile();
        if (env == null) {
            return;
        }

        try (var lines = Files.lines(env)) {
            lines.forEach(line -> {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    return;
                }

                int separator = trimmedLine.indexOf('=');
                if (separator < 1) {
                    return;
                }

                String key = trimmedLine.substring(0, separator).trim();
                String value = trimmedLine.substring(separator + 1).trim();
                if (System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            });
            System.out.println("Loaded .env from: " + env.toAbsolutePath());
        } catch (IOException exception) {
            System.err.println("Warning: could not load .env: " + exception.getMessage());
        }
    }

    private static Path findEnvFile() {
        Path currentDirectoryEnv = Path.of(".env");
        if (Files.exists(currentDirectoryEnv)) {
            return currentDirectoryEnv;
        }

        try {
            Path classesDirectory = Path.of(
                    MerumApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            );
            Path moduleEnv = classesDirectory.getParent().getParent().resolve(".env");
            if (Files.exists(moduleEnv)) {
                return moduleEnv;
            }
        } catch (Exception ignored) {
            // The working-directory lookup above remains the fallback.
        }

        return null;
    }
}
