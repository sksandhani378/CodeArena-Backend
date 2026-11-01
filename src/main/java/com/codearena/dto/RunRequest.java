
package com.codearena.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RunRequest(
    @NotBlank
    @Size(max = 30000)
    String sourceCode,

    @NotEmpty
    @Size(max = 20)
    List<TestCase> testCases
) {
    public record TestCase(
        @NotBlank
        @Size(max = 10000)
        String input,

        @NotBlank
        @Size(max = 10000)
        String expectedOutput
    ) {}
}
