package com.blog_app_apis.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to the Blog Application Backend API!");
        response.put("status", "Running");
        response.put("documentation", "/swagger-ui/index.html");
        return ResponseEntity.ok(response);
    }
}
