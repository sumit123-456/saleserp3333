package com.sales.sales.Repositories;


import com.sales.sales.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    // In your EmployeeRepository
//    @Query("SELECT e FROM Employee e ORDER BY e.achieved DESC, e.performanceScore DESC LIMIT 10")
//    List<Employee> findTop10PerformersByAchievement();

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartment(String department);

    List<Employee> findByTeam(String team);

    List<Employee> findByRole(String role);

    @Query("SELECT COUNT(e) FROM Employee e")
    Long countTotalEmployees();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.achieved >= e.monthlyTarget")
    Long countEmployeesMeetingTarget();

    @Query("SELECT SUM(e.monthlyTarget) FROM Employee e")
    Integer getTotalMonthlyTarget();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.achieved >= e.monthlyTarget")
    Long countByAchievedGreaterThanEqualMonthlyTarget();

    @Query("SELECT SUM(e.achieved) FROM Employee e")
    Integer getTotalAchieved();

    @Query("SELECT SUM(e.callsMade) FROM Employee e")
    Integer getTotalCallsMade();

    @Query("SELECT AVG(e.meetTarget) FROM Employee e")
    Double getAverageMeetTarget();

    @Query("SELECT e FROM Employee e ORDER BY e.achieved DESC LIMIT 10")
    List<Employee> findTopPerformersByAchievement();

    @Query("SELECT e FROM Employee e ORDER BY e.callsMade DESC LIMIT 10")
    List<Employee> findTopPerformersByCalls();

    @Query("SELECT e FROM Employee e WHERE e.department = :department ORDER BY e.achieved DESC")
    List<Employee> findTopPerformersByDepartment(@Param("department") String department);

    @Query("SELECT e.department, COUNT(e), SUM(e.achieved), SUM(e.monthlyTarget) FROM Employee e GROUP BY e.department")
    List<Object[]> getDepartmentWisePerformance();

    // Filter employees method
    @Query("SELECT e FROM Employee e WHERE " +
            "(:department IS NULL OR e.department = :department) AND " +
            "(:team IS NULL OR e.team = :team) AND " +
            "(:joinDateFrom IS NULL OR e.joinDate >= :joinDateFrom) AND " +
            "(:joinDateTo IS NULL OR e.joinDate <= :joinDateTo) AND " +
            "(:minAchieved IS NULL OR e.achieved >= :minAchieved)")
    List<Employee> filterEmployees(
            @Param("department") String department,
            @Param("team") String team,
            @Param("joinDateFrom") LocalDate joinDateFrom,
            @Param("joinDateTo") LocalDate joinDateTo,
            @Param("minAchieved") Integer minAchieved
    );

    // Alternative filter method with more parameters
    @Query("SELECT e FROM Employee e WHERE " +
            "(:empName IS NULL OR e.empName LIKE %:empName%) AND " +
            "(:department IS NULL OR e.department = :department) AND " +
            "(:team IS NULL OR e.team = :team) AND " +
            "(:role IS NULL OR e.role = :role) AND " +
            "(:joinDateFrom IS NULL OR e.joinDate >= :joinDateFrom) AND " +
            "(:joinDateTo IS NULL OR e.joinDate <= :joinDateTo) AND " +
            "(:minAchieved IS NULL OR e.achieved >= :minAchieved) AND " +
            "(:minCallsMade IS NULL OR e.callsMade >= :minCallsMade)")
    List<Employee> findEmployeesByFilters(
            @Param("empName") String empName,
            @Param("department") String department,
            @Param("team") String team,
            @Param("role") String role,
            @Param("joinDateFrom") LocalDate joinDateFrom,
            @Param("joinDateTo") LocalDate joinDateTo,
            @Param("minAchieved") Integer minAchieved,
            @Param("minCallsMade") Integer minCallsMade
    );

    // Find employees by achievement range
    @Query("SELECT e FROM Employee e WHERE e.achieved BETWEEN :minAchieved AND :maxAchieved")
    List<Employee> findByAchievedBetween(@Param("minAchieved") Integer minAchieved,
                                         @Param("maxAchieved") Integer maxAchieved);

    // Find employees who joined between dates
    @Query("SELECT e FROM Employee e WHERE e.joinDate BETWEEN :startDate AND :endDate")
    List<Employee> findByJoinDateBetween(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
}