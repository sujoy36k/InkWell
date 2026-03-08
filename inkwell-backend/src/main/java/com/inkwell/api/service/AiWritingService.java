package com.inkwell.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.inkwell.api.model.Dtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiWritingService {

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    @Value("${anthropic.api.model:claude-sonnet-4-20250514}")
    private String model;

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";
    private static final MediaType JSON_MEDIA = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WritingResponse generateSuggestion(WritingRequest request) {
        String tone = normaliseTone(request.tone());
        String systemPrompt = buildSystemPrompt(tone);
        String userPrompt = buildUserPrompt(request.prompt(), tone);

        String rawSuggestion;

        if (anthropicApiKey == null || anthropicApiKey.isBlank()) {
            // Fallback demo response when no API key is configured
            log.warn("No Anthropic API key configured — returning demo suggestion");
            rawSuggestion = generateDemoSuggestion(request.prompt(), tone);
        } else {
            rawSuggestion = callAnthropicApi(systemPrompt, userPrompt);
        }

        int wordCount = countWords(rawSuggestion);
        return new WritingResponse(true, rawSuggestion, tone, wordCount);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String callAnthropicApi(String systemPrompt, String userPrompt) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", 300);
            body.put("system", systemPrompt);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("role", "user");
            msg.put("content", userPrompt);
            messages.add(msg);
            body.set("messages", messages);

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body), JSON_MEDIA);

            Request httpRequest = new Request.Builder()
                    .url(ANTHROPIC_URL)
                    .post(requestBody)
                    .addHeader("x-api-key", anthropicApiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "{}";

                if (!response.isSuccessful()) {
                    log.error("Anthropic API error {}: {}", response.code(), responseBody);
                    throw new RuntimeException("AI service returned an error: " + response.code());
                }

                JsonNode json = objectMapper.readTree(responseBody);
                return json.path("content").get(0).path("text").asText();
            }
        } catch (IOException e) {
            log.error("Failed to call Anthropic API", e);
            throw new RuntimeException("Failed to reach AI service. Please try again.");
        }
    }

    private String buildSystemPrompt(String tone) {
        return """
                You are Inkwell, an AI writing assistant embedded in a creative writing app.
                Your role is to provide beautiful, evocative writing continuations and suggestions.
                Keep suggestions concise (2-4 sentences), polished, and true to the requested tone.
                Never add explanations or meta-commentary — output only the suggested text.
                """;
    }

    private String buildUserPrompt(String userPrompt, String tone) {
        return String.format(
            "Continue or improve the following writing in a %s tone:\n\n\"%s\"",
            tone, userPrompt
        );
    }

    private String normaliseTone(String tone) {
        if (tone == null || tone.isBlank()) return "creative";
        return switch (tone.toLowerCase().trim()) {
            case "professional" -> "professional";
            case "casual"       -> "casual";
            case "poetic"       -> "poetic";
            default             -> "creative";
        };
    }

    private String generateDemoSuggestion(String prompt, String tone) {
        // Lightweight demo fallback — no API key needed
        return switch (tone) {
            case "professional" ->
                "Building on this foundation, the approach offers a structured pathway toward measurable outcomes. " +
                "Each step reinforces the next, creating a compounding effect that drives sustainable progress.";
            case "casual" ->
                "Honestly, it just works — and that's the best part. " +
                "You don't need to overthink it; the pieces fall into place naturally once you get started.";
            case "poetic" ->
                "Between the lines, a quiet understanding blooms — wordless and certain as morning light. " +
                "It asks nothing, yet offers everything the searching heart could hope to find.";
            default ->
                "The story unfolded in ways nobody had anticipated, each twist revealing another layer of the truth beneath. " +
                "She paused at the threshold, knowing that whatever came next would change everything.";
        };
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
}
