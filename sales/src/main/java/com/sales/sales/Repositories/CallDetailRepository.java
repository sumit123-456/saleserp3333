package com.sales.sales.Repositories;

import com.sales.sales.Entity.CallDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
@Repository
public interface CallDetailRepository extends JpaRepository<CallDetail, Long> {

    @Query(value = "SELECT emp_id, COUNT(*) as call_count FROM call_details GROUP BY emp_id ORDER BY call_count DESC LIMIT 10", nativeQuery = true)
    List<Object[]> findTop10ByCallsMade();

    @Query("SELECT c FROM CallDetail c WHERE LOWER(TRIM(c.disposition)) LIKE '%connected%'")
    List<CallDetail> findConnectedCalls();

    @Query("""
        SELECT c FROM CallDetail c
        WHERE (:team IS NULL OR LOWER(c.team) LIKE LOWER(CONCAT('%', :team, '%')))
        AND (:startDate IS NULL OR c.callDate >= :startDate)
        AND (:endDate IS NULL OR c.callDate <= :endDate)
    """)
    List<CallDetail> filterCalls(
            @Param("name") String name,
            @Param("team") String team,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT LOWER(TRIM(c.callType)), COUNT(c) FROM CallDetail c GROUP BY LOWER(TRIM(c.callType))")
    List<Object[]> countByCallType();

    @Query("SELECT LOWER(TRIM(c.disposition)), COUNT(c) FROM CallDetail c GROUP BY LOWER(TRIM(c.disposition))")
    List<Object[]> countByDisposition();

}
