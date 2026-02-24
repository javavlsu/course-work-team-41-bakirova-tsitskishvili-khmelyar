package com.school.portal.repository;

import com.school.portal.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Метод для поиска роли по названию
    Optional<Role> findByRoleName(String roleName);
}