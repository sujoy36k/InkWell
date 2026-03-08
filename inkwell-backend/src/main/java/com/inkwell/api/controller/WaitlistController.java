package com.inkwell.api.controller;

import com.inkwell.api.model.Dtos.*;
import com.inkwell.api.service.WaitlistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * POST /api/waitlist        — Join the Inkwell waitlist
 * GET  /api/waitlist/count  — Get total signup count (admin/public stat)
 */
@Slf4j
@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;

    /**
     * Join the waitlist.
     *
     * Request body:
     * {
     *   "email": "user@example.com"
     * }
     *
     * Response 200:
     * {
     *   "success": true,
     *   "message": "You're on the list! 🎉 We'll reach out when your spot is ready.",
     *   "position": 1042
     * }
     */
    @PostMapping
    public ResponseEntity<WaitlistResponse> joinWaitlist(
            @Valid @RequestBody WaitlistRequest request,
            HttpServletRequest httpRequest) {

        String ip = resolveClientIp(httpRequest);
        WaitlistResponse response = waitlistService.join(request, ip);
        return ResponseEntity.ok(response);
    }

    /**
     * Get total waitlist count — useful for displaying social proof on the frontend.
     *
     * Response 200:
     * {
     *   "success": true,
     *   "message": "Total signups retrieved",
     *   "position": 1042
     * }
     */
    @GetMapping("/count")
    public ResponseEntity<WaitlistResponse> getCount() {
        long count = waitlistService.getTotalSignups();
        return ResponseEntity.ok(new WaitlistResponse(true, "Total signups retrieved", count));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
