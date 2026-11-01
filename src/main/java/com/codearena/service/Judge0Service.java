
package com.codearena.service;

import com.codearena.dto.RunRequest;
import com.codearena.dto.RunResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;

@Service
public class Judge0Service {

    private final RestClient client;
    private final int javaLanguageId;

    public Judge0Service(
        @Value("${judge0.base-url}") String baseUrl,
        @Value("${judge0.api-key:}") String apiKey,
        @Value("${judge0.api-key-header:X-Auth-Token}") String apiKeyHeader,
        @Value("${judge0.java8-language-id}") int javaLanguageId
    ) {
        this.javaLanguageId = javaLanguageId;

        RestClient.Builder builder =
            RestClient.builder().baseUrl(baseUrl);

        if (!apiKey.isBlank()) {
            builder.defaultHeader(apiKeyHeader, apiKey);
        }

        this.client = builder.build();
    }

    public RunResponse run(RunRequest request) {

        List<RunResponse.TestResult> results =
            new ArrayList<>();

        int passed = 0;
        int index = 0;

        for (RunRequest.TestCase test : request.testCases()) {
            index++;

            Map<String, Object> payload =
                new LinkedHashMap<>();

            payload.put("source_code", request.sourceCode());
            payload.put("language_id", javaLanguageId);
            payload.put("stdin", test.input());
            payload.put("expected_output", test.expectedOutput());
            payload.put("cpu_time_limit", 2.0);
            payload.put("wall_time_limit", 5.0);
            payload.put("memory_limit", 128000);

            try {
                Map response = client.post()
                    .uri("/submissions?base64_encoded=false&wait=true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

                String status = readStatus(response);

                String actual = clean(
                    response == null ? null : response.get("stdout")
                );

                String expected = clean(test.expectedOutput());

                boolean accepted =
                    "Accepted".equalsIgnoreCase(status)
                    && actual.equals(expected);

                if (accepted) {
                    passed++;
                }

                results.add(new RunResponse.TestResult(
                    index,
                    accepted ? "Accepted" : status,
                    actual,
                    expected,
                    accepted ? "Test passed" : message(response, status)
                ));

            } catch (RestClientResponseException ex) {

                results.add(new RunResponse.TestResult(
                    index,
                    "Judge Error",
                    "",
                    test.expectedOutput(),
                    "Judge service HTTP error: "
                        + ex.getStatusCode().value()
                ));

            } catch (Exception ex) {

                results.add(new RunResponse.TestResult(
                    index,
                    "Judge Error",
                    "",
                    test.expectedOutput(),
                    "Could not connect to Judge0"
                ));
            }
        }

        boolean allPassed =
            passed == request.testCases().size();

        return new RunResponse(
            allPassed ? "Accepted" : "Not Accepted",
            passed,
            request.testCases().size(),
            allPassed,
            results
        );
    }

    private String readStatus(Map response) {

        if (response == null ||
            !(response.get("status") instanceof Map status)) {
            return "Unknown";
        }

        Object description = status.get("description");

        return description == null
            ? "Unknown"
            : description.toString();
    }

    private String message(Map response, String status) {

        if (response == null) {
            return "No response from judge";
        }

        Object stderr = response.get("stderr");

        if (stderr != null && !stderr.toString().isBlank()) {
            return stderr.toString();
        }

        Object compile = response.get("compile_output");

        if (compile != null && !compile.toString().isBlank()) {
            return compile.toString();
        }

        return status;
    }

    private String clean(Object value) {

        if (value == null) {
            return "";
        }

        return value.toString()
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .strip();
    }
}
