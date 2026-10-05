package com.manager.server.repository.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manager.server.model.entity.auth.User;

public interface UserRepository extends JpaRepository<User, Long > {
    
   Optional<User> findByUsername(String username);
}
