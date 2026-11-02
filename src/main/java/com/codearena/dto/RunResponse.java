

package com.codearena.dto;

import java.util.List;

public record RunResponse(
    String verdict,
    int passed,
    int total,
    boolean accepted,
    List<TestResult> results
) {
    public record TestResult(
        int testCase,
        String status,
        String actualOutput,
        String expectedOutput,
        String message,
        double executionTimeSeconds,
        long memoryKb
    ) {}
}