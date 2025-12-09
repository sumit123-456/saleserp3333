package com.sales.sales.Repositories;

import com.sales.sales.Entity.ResourceAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {
    
    // Custom queries for Resource Allocation
    @Query("SELECT DISTINCT ra.itTeam FROM ResourceAllocation ra WHERE ra.itTeam IS NOT NULL")
    List<String> findDistinctTeams();
    
    @Query("SELECT COUNT(DISTINCT ra.itTeam) FROM ResourceAllocation ra WHERE ra.itTeam IS NOT NULL")
    Long countDistinctTeams();
    
    Long countByItTeam(String itTeam);
    
    List<ResourceAllocation> findByItTeam(String itTeam);
    
    List<ResourceAllocation> findByStartDateBetween(LocalDate start, LocalDate end);
    
    List<ResourceAllocation> findByItTeamAndStartDateBetween(String itTeam, LocalDate start, LocalDate end);
    
    @Query("SELECT ra.itTeam, COUNT(ra) FROM ResourceAllocation ra GROUP BY ra.itTeam")
    List<Object[]> countProjectsByTeam();
}