package com.railway.controller;

import com.railway.dto.admin.AdminTrainResponse;
import com.railway.dto.admin.TrainRunOccupancyResponse;
import com.railway.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/trains")
    public ResponseEntity<List<AdminTrainResponse>> getAllTrains() {
        return ResponseEntity.ok(adminService.getAllTrains());
    }

    @GetMapping("/train-runs/{id}/occupancy")
    public ResponseEntity<TrainRunOccupancyResponse> getTrainRunOccupancy(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getTrainRunOccupancy(id));
    }

    @PostMapping("/train-runs/{id}/cancel")
    public ResponseEntity<Void> cancelTrainRun(@PathVariable Long id) {
        adminService.cancelTrainRun(id);
        return ResponseEntity.ok().build();
    }
}
