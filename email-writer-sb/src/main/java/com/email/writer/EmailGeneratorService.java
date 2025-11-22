package com.email.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.function.Supplier;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;
    private final String apiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder,
                                 @Value("${gemini.api.url}") String baseUrl,
                                 @Value("${gemini.api.key}") String geminiApiKey) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = geminiApiKey;
    }


    private String callWithRetry(Supplier<String> apiCall) {
        int retries = 3;
        long delay = 1500;

        while (true) {
            try {
                return apiCall.get();
            } catch (Exception ex) {

                boolean rateLimit =
                        ex instanceof WebClientResponseException &&
                                ((WebClientResponseException) ex).getStatusCode().value() == 429;

                if (!rateLimit || retries <= 0) {
                    throw new RuntimeException("API request failed", ex);
                }

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", e);
                }

                delay *= 2; // exponential backoff
                retries--;
            }
        }
    }

    // MAIN FUNCTION
    public String generateEmailReply(EmailRequest emailRequest) {

        String prompt = buildPrompt(emailRequest);
        ObjectMapper mapper = new ObjectMapper();


        String requestBody;
        try {
            ObjectNode root = mapper.createObjectNode();
            ArrayNode contents = root.putArray("contents");

            ObjectNode contentNode = mapper.createObjectNode();
            contentNode.put("role", "user");

            ArrayNode parts = contentNode.putArray("parts");
            parts.add(mapper.createObjectNode().put("text", prompt));

            contents.add(contentNode);

            requestBody = mapper.writeValueAsString(root);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build JSON request body", e);
        }


        String response = callWithRetry(() ->
                webClient.post()
                        .uri("/v1beta/models/gemini-2.0-flash:generateContent")
                        .header("x-goog-api-key", apiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
        );

        return extractResponseContent(response);
    }

    // Extract text response
    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }


    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional reply for the following email: ");

        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone. ");
        }

        prompt.append("\nOriginal Email:\n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }
}
