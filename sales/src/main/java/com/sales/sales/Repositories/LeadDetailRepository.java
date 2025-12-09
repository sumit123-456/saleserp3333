package com.sales.sales.Repositories;

import com.sales.sales.Entity.Employee;
import com.sales.sales.Entity.LeadDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeadDetailRepository extends JpaRepository<LeadDetail, Long> {
    List<LeadDetail> findByEmpId(String empId);

    List<LeadDetail> findByCreatedDateBetween(LocalDate startDate, LocalDate endDate);
    List<LeadDetail> findByEmpIdInAndCreatedDateBetween(List<String> empIds, LocalDate startDate, LocalDate endDate);

//    @Query("SELECT l FROM LeadDetail l JOIN Employee e ON l.empId = e.empId WHERE e.team = :team AND l.createdDate BETWEEN :start AND :end")
//    List<LeadDetail> findLeadsByTeamAndDate(@Param("team") String team,
//                                            @Param("start") LocalDate start,
//                                            @Param("end") LocalDate end);

//    List<Employee> findByTeam(String team);

@Query("""
    SELECT l.empId, 
           (SUM(CASE WHEN l.convertedToDeal = 'Yes' THEN 1 ELSE 0 END) * 100) / COUNT(*)
    FROM LeadDetail l
    GROUP BY l.empId
""")
List<Object[]> getTeamConversionRates();


}
