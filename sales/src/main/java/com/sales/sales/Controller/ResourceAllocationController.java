package com.sales.sales.Controller;

import com.sales.sales.Entity.ResourceAllocation;
import com.sales.sales.Services.ResourceAllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/allocations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResourceAllocationController {

    private final ResourceAllocationService service;

    // Get all allocations
    @GetMapping
    public ResponseEntity<List<ResourceAllocation>> getAllAllocations() {
        log.info("Getting all resource allocations");
        List<ResourceAllocation> allocations = service.getAllAllocations();
        return ResponseEntity.ok(allocations);
    }

    // Get allocation by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResourceAllocation> getAllocationById(@PathVariable Long id) {
        log.info("Getting allocation by ID: {}", id);
        Optional<ResourceAllocation> allocation = service.getAllocationById(id);
        return allocation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create new allocation
    @PostMapping
    public ResponseEntity<?> createAllocation(@RequestBody ResourceAllocation allocation) {
        log.info("Creating new resource allocation: {}", allocation);
        try {
            ResourceAllocation savedAllocation = service.addAllocation(allocation);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAllocation);
        } catch (Exception e) {
            log.error("Error creating allocation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to create allocation",
                "message", e.getMessage()
            ));
        }
    }

    // Update allocation
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAllocation(@PathVariable Long id, @RequestBody ResourceAllocation allocation) {
        log.info("Updating allocation with ID: {}", id);
        try {
            ResourceAllocation updatedAllocation = service.updateAllocation(id, allocation);
            if (updatedAllocation != null) {
                return ResponseEntity.ok(updatedAllocation);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating allocation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to update allocation",
                "message", e.getMessage()
            ));
        }
    }

    // Delete allocation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAllocation(@PathVariable Long id) {
        log.info("Deleting allocation with ID: {}", id);
        if (service.getAllocationById(id).isPresent()) {
            service.deleteAllocation(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get stats (KPIs)
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAllocationStats() {
        log.info("Getting allocation statistics");
        
        List<ResourceAllocation> allocations = service.getAllAllocations();
        
        // Count distinct teams
        long totalTeams = allocations.stream()
                .map(ResourceAllocation::getItTeam)
                .distinct()
                .count();
        
        // Count active allocations
        long activeAllocations = allocations.stream()
                .filter(a -> "Active".equalsIgnoreCase(a.getStatus()))
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTeams", totalTeams);
        stats.put("totalAllocations", allocations.size());
        stats.put("activeProjects", activeAllocations);
        stats.put("inactiveProjects", allocations.size() - activeAllocations);
        
        return ResponseEntity.ok(stats);
    }

    // Get distinct teams for filter
    @GetMapping("/teams")
    public ResponseEntity<List<String>> getDistinctTeams() {
        log.info("Getting distinct IT teams");
        List<ResourceAllocation> allocations = service.getAllAllocations();
        List<String> teams = allocations.stream()
                .map(ResourceAllocation::getItTeam)
                .distinct()
                .sorted()
                .toList();
        return ResponseEntity.ok(teams);
    }

    // Get projects by team (for charts)
    @GetMapping("/projects-by-team")
    public ResponseEntity<Map<String, Long>> getProjectsByTeam() {
        log.info("Getting projects count by team");
        List<ResourceAllocation> allocations = service.getAllAllocations();
        
        Map<String, Long> teamCounts = new HashMap<>();
        for (ResourceAllocation allocation : allocations) {
            String team = allocation.getItTeam();
            teamCounts.put(team, teamCounts.getOrDefault(team, 0L) + 1);
        }
        
        return ResponseEntity.ok(teamCounts);
    }

    // Filter allocations
    @GetMapping("/filter")
    public ResponseEntity<List<ResourceAllocation>> filterAllocations(
            @RequestParam(required = false) String team,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Filtering allocations - Team: {}, Start: {}, End: {}", team, startDate, endDate);
        
        List<ResourceAllocation> allocations = service.getAllAllocations();
        
        // Apply filters
        List<ResourceAllocation> filtered = allocations.stream()
                .filter(a -> team == null || team.equals("All Teams") || team.equals(a.getItTeam()))
                .filter(a -> startDate == null || 
                        (a.getStartDate() != null && !a.getStartDate().isBefore(startDate)))
                .filter(a -> endDate == null || 
                        (a.getEndDate() != null && !a.getEndDate().isAfter(endDate)))
                .toList();
        
        return ResponseEntity.ok(filtered);
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Resource Allocation Service is healthy!");
    }
}