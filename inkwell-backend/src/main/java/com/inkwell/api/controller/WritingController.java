package com.inkwell.api.controller;

import com.inkwell.api.model.Dtos.*;
import com.inkwell.api.service.AiWritingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * POST /api/writing/suggest — Generate an AI writing suggestion
 */
@Slf4j
@RestController
@RequestMapping("/api/writing")
@RequiredArgsConstructor
public class WritingController {

    private final AiWritingService aiWritingService;

    /**
     * Generate an AI writing suggestion based on a user prompt.
     *
     * Request body:
     * {
     *   "prompt": "The fog held the harbour like a secret it wasn't ready to share.",
     *   "tone": "creative"        // optional: creative | professional | casual | poetic
     * }
     *
     * Response 200:
     * {
     *   "success": true,
     *   "suggestion": "The fishing boats rocked gently, their hulls whispering old stories...",
     *   "tone": "creative",
     *   "wordCount": 24
     * }
     */
    @PostMapping("/suggest")
    public ResponseEntity<WritingResponse> suggest(
            @Valid @RequestBody WritingRequest request) {

        log.info("AI writing suggestion requested | tone={} | promptLength={}",
                request.tone(), request.prompt().length());

        WritingResponse response = aiWritingService.generateSuggestion(request);
        return ResponseEntity.ok(response);
    }
}
