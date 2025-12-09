package com.sales.sales.Controller;

import com.sales.sales.Entity.CallDetail;
import com.sales.sales.Services.CallDetailService;
import com.sales.sales.validation.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/calls")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CallDetailController {

    private final CallDetailService service;

    // Get all calls
    @GetMapping
    public List<CallDetail> getAllCalls() {
        return service.getAllCalls();
    }

    // Get call by ID
    @GetMapping("/{id}")
    public Optional<CallDetail> getCallById(@PathVariable Long id) {
        return service.getCallById(id);
    }

    // Add new call
    @PostMapping
    public CallDetail addCall(@RequestBody CallDetail call) {
        return service.addCall(call);
    }

    // Update existing call
    @PutMapping("/{id}")
    public CallDetail updateCall(@PathVariable Long id, @RequestBody CallDetail call) {
        return service.updateCall(id, call);
    }

    // Delete call
    @DeleteMapping("/{id}")
    public void deleteCall(@PathVariable Long id) {
        service.deleteCall(id);
    }

    // Filter calls
    @GetMapping("/filter")
    public ResponseEntity<List<CallDetail>> filterCalls(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String range
    ) {
        return ResponseEntity.ok(service.filterCalls(name, team, range));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getCallSummary() {
        try {
            Map<String, Object> summary = service.getCallSummary();
            return CommonUtil.createBuildResponse(summary, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting call summary: {}", e.getMessage());
            return CommonUtil.createErrorResponse("Failed to fetch call summary", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/type-summary")
    public ResponseEntity<?> getCallTypeSummary() {
        try {
            Map<String, Long> typeSummary = service.getCallTypeSummary();
            return CommonUtil.createBuildResponse(typeSummary, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting call type summary: {}", e.getMessage());
            return CommonUtil.createErrorResponse("Failed to fetch call type summary", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/disposition-summary")
    public ResponseEntity<?> getDispositionSummary() {
        try {
            Map<String, Long> dispositionSummary = service.getDispositionSummary();
            return CommonUtil.createBuildResponse(dispositionSummary, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting disposition summary: {}", e.getMessage());
            return CommonUtil.createErrorResponse("Failed to fetch disposition summary", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}



