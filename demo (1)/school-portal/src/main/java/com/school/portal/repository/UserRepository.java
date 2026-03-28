package com.school.portal.repository;

import com.school.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByLogin(String login);

    @Query("SELECT u FROM User u WHERE u.role.roleName = :roleName")
    List<User> findByRole_RoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE u.role.roleName = :roleName AND u NOT IN " +
            "(SELECT sp.student FROM StudentParent sp)")
    List<User> findAvailableParents(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.lastName, ' ', u.firstName, ' ', u.middleName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByFullName(@Param("searchTerm") String searchTerm);
}