package com.manager.server.configuration;

import com.manager.server.model.entity.auth.Role;
import com.manager.server.model.entity.auth.User;
import com.manager.server.repository.auth.RoleRepository;
import com.manager.server.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer — executa uma única vez ao subir a aplicação.
 *
 * Responsabilidades:
 *  1. Garantir que as roles base (ADMIN, OPERATOR) existam no banco.
 *  2. Criar o usuário administrador inicial se ainda não existir.
 *
 * Todas as operações são idempotentes: reiniciar a aplicação não duplica dados.
 *
 * Credenciais do admin são lidas do arquivo .env (nunca hardcoded aqui).
 * Variáveis necessárias:
 *   ADMIN_USERNAME — login do administrador
 *   ADMIN_PASSWORD — senha em texto plano (será armazenada com BCrypt)
 *   ADMIN_NAME     — nome de exibição do administrador
 *
 * Se qualquer uma das três variáveis estiver ausente, a criação do admin é
 * ignorada e um aviso é exibido no log — as roles ainda são criadas normalmente.
 */
@Component
public class DataInitializer implements ApplicationRunner {


    @Value("${ADMIN_USERNAME:}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${ADMIN_NAME:}")
    private String adminName;

    private boolean resetAdminPassword = false;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        Role adminRole = ensureRole("ADMIN");
        ensureRole("OPERATOR");

        // Definir as variaveis na env
        if (adminUsername.isBlank() || adminPassword.isBlank() || adminName.isBlank()) {

            System.err.println("Please fill both both admin and username and password");
            return;
        }

        // Só cria se ainda não existir um usuário com esse username.
        userRepository.findByUsername(adminUsername).ifPresentOrElse(
                admin -> {
                    if(resetAdminPassword) {
                        admin.setPassword(passwordEncoder.encode(adminPassword));
                        admin.setRole(adminRole);
                        admin.setUsername(adminUsername);

                        userRepository.save(admin);
                    }
                },
                () -> {
                    User admin = new User(

                    );

                    admin.setUsername(adminUsername);
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    admin.setRole(adminRole);
                    admin.setName(adminName);

                    userRepository.save(admin);

                }
        );
    }
    private Role ensureRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    return roleRepository.save(role);
                });
    }




}



