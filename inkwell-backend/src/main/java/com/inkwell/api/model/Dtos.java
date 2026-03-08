package com.inkwell.api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ── Waitlist DTOs ─────────────────────────────────────────────────────────────

public class Dtos {

    public record WaitlistRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email
    ) {}

    public record WaitlistResponse(
        boolean success,
        String message,
        Long position
    ) {}

    // ── AI Writing DTOs ───────────────────────────────────────────────────────

    public record WritingRequest(
        @NotBlank(message = "Prompt is required")
        @Size(min = 10, max = 500, message = "Prompt must be between 10 and 500 characters")
        String prompt,

        // Optional: "creative", "professional", "casual" — defaults to "creative"
        String tone
    ) {}

    public record WritingResponse(
        boolean success,
        String suggestion,
        String tone,
        int wordCount
    ) {}

    // ── Generic error DTO ─────────────────────────────────────────────────────

    public record ErrorResponse(
        boolean success,
        String error,
        int status
    ) {}
}
