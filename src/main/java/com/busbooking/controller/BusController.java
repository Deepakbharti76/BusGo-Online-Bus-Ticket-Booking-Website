package com.busbooking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.busbooking.model.BusDTO;
import com.busbooking.service.BusService;

@RestController
@RequestMapping("/api/buses")
@CrossOrigin(origins = "*")
public class BusController {

    @Autowired
    private BusService busService;

    // GET /api/buses - all buses
    @GetMapping
    public ResponseEntity<List<BusDTO>> getAllBuses() {
        return ResponseEntity.ok(busService.getAllBuses());
    }

    // GET /api/buses/search?from=Delhi&to=Patna - search by route
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchBuses(
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(busService.searchBuses(from, to));
    }

    // GET /api/buses/{busId}/seats - get seat types & availability
    @GetMapping("/{busId}/seats")
    public ResponseEntity<List<Map<String, Object>>> getSeatTypes(@PathVariable int busId) {
        return ResponseEntity.ok(busService.getSeatTypes(busId));
    }
}