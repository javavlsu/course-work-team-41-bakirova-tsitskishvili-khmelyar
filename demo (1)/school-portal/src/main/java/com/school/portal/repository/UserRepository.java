package com.school.portal.repository;

import com.school.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    @Query("SELECT u FROM User u WHERE " +
            "(:roleName IS NULL OR :roleName = '' OR u.role.roleName = :roleName) AND " +
            "(:classId IS NULL OR u.userId IN (SELECT sc.student.userId FROM StudentClass sc WHERE sc.schoolClass.classId = :classId)) AND " +
            "(:searchTerm IS NULL OR :searchTerm = '' OR LOWER(CONCAT(u.lastName, ' ', u.firstName, ' ', COALESCE(u.middleName, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    org.springframework.data.domain.Page<User> findFilteredUsers(
            @Param("roleName") String roleName,
            @Param("classId") Integer classId,
            @Param("searchTerm") String searchTerm,
            org.springframework.data.domain.Pageable pageable);
}