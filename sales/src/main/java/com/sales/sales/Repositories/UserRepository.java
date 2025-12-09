package com.sales.sales.Repositories;

import com.sales.sales.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ✔ Correct: roleName inside Role entity
    List<User> findByRole_RoleName(String roleName);

    long countByRole_RoleName(String roleName);

    // ✔ Correct version of role + team
    List<User> findByRole_RoleNameAndTeamAllocation(String roleName, String teamAllocation);

    // Count users by team allocation
    long countByTeamAllocation(String teamAllocation);

    // Distinct teams
    @Query("SELECT DISTINCT u.teamAllocation FROM User u WHERE u.teamAllocation IS NOT NULL")
    List<String> findDistinctTeamAllocations();

    // Average call target
    @Query("SELECT AVG(u.callTarget) FROM User u WHERE u.callTarget IS NOT NULL")
    Double findAverageCallTarget();

    // Average monthly target
    @Query("SELECT AVG(u.monthlyTarget) FROM User u WHERE u.monthlyTarget IS NOT NULL")
    Double findAverageMonthlyTarget();

    // Find by team allocation
    List<User> findByTeamAllocation(String teamAllocation);

    List<User> findByCallTargetGreaterThan(Integer callTarget);

    List<User> findByMonthlyTargetGreaterThan(Integer monthlyTarget);

    // Total users
    @Query("SELECT COUNT(u) FROM User u")
    long getTotalUserCount();

    // Top performers by monthly target
    @Query("SELECT u FROM User u WHERE u.monthlyTarget IS NOT NULL ORDER BY u.monthlyTarget DESC")
    List<User> findTopPerformersByMonthlyTarget();

    // Total monthly target per team
    @Query("SELECT u.teamAllocation, SUM(u.monthlyTarget) FROM User u WHERE u.monthlyTarget IS NOT NULL GROUP BY u.teamAllocation")
    List<Object[]> findTotalMonthlyTargetByTeam();

    List<User> findByTeamAllocationIsNull();

    List<User> findByTeamAllocationIsNotNull();

    // ✔ Correct search by roles (fix: match Role objects)
    @Query("SELECT u FROM User u WHERE u.role.roleName IN :roleNames")
    List<User> findByRoles(List<String> roleNames);

    List<User> findByFullNameContainingIgnoreCase(String name);

    List<User> findByCallTargetBetween(Integer minTarget, Integer maxTarget);

    List<User> findByMonthlyTargetBetween(Integer minTarget, Integer maxTarget);
}