package com.issuetracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> welcome() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("message", "Welcome to the Issue Tracker API");
        body.put("version", "1.0.0");
        return ResponseEntity.ok(body);
    }
}
