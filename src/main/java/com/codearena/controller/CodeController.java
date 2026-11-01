
package com.codearena.controller;

import com.codearena.dto.RunRequest;
import com.codearena.dto.RunResponse;
import com.codearena.service.Judge0Service;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeController {

    private final Judge0Service judge0Service;

    public CodeController(Judge0Service judge0Service) {
        this.judge0Service = judge0Service;
    }

    @PostMapping("/run")
    public RunResponse run(
        @Valid @RequestBody RunRequest request
    ) {
        return judge0Service.run(request);
    }

    @GetMapping("/health")
    public String health() {
        return "CodeArena backend is running";
    }
}
