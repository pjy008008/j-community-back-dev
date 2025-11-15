package com.pjy008008.j_community.repository;

import com.pjy008008.j_community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
}