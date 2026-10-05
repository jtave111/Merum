package com.manager.client.ui.dialogs;

import com.manager.client.Branding;
import com.manager.client.data.AuthException;
import com.manager.client.data.MerumData;
import com.manager.server.model.entity.auth.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

public final class LoginDialog extends JDialog {
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel errorLabel = new JLabel(" ", SwingConstants.CENTER);

    private User authenticatedUser;

    public LoginDialog(Frame owner) {
        super(owner, Branding.DISPLAY_NAME + " — Login", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setPreferredSize(new Dimension(400, 300));
        JButton loginButton = createLoginButton();
        setContentPane(buildContent(loginButton));
        getRootPane().setDefaultButton(loginButton);
        pack();
        setLocationRelativeTo(null);
    }

    public boolean isLoginSuccessful() {
        return authenticatedUser != null;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    private JPanel buildContent(JButton loginButton) {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(BorderFactory.createEmptyBorder(28, 42, 28, 42));

        JLabel title = new JLabel("Sign in to " + Branding.NAME, SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(20f));
        content.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        fields.add(new JLabel("Username"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        fields.add(usernameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;
        fields.add(new JLabel("Password"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        fields.add(passwordField, constraints);

        content.add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(0, 10));
        errorLabel.setForeground(new Color(0xE05561));
        actions.add(errorLabel, BorderLayout.NORTH);
        actions.add(loginButton, BorderLayout.SOUTH);
        content.add(actions, BorderLayout.SOUTH);

        return content;
    }

    private JButton createLoginButton() {
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(event -> attemptLogin());
        return loginButton;
    }

    private void attemptLogin() {
        char[] passwordCharacters = passwordField.getPassword();
        try {
            authenticatedUser = MerumData.login(
                    usernameField.getText(),
                    new String(passwordCharacters)
            );
            dispose();
        } catch (AuthException exception) {
            errorLabel.setText(exception.getMessage());
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        } finally {
            Arrays.fill(passwordCharacters, '\0');
        }
    }
}
