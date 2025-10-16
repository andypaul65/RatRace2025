package com.finmodel;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Basic REST controller for testing the RatRace application.
 */
@RestController
@RequestMapping("/api")
public class RatRaceController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("RatRace2025 backend is running!");
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("{\"status\":\"ready\",\"service\":\"ratrace2025\"}");
    }
}