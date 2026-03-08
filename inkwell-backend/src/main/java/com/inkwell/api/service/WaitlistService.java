package com.inkwell.api.service;

import com.inkwell.api.model.Dtos.*;
import com.inkwell.api.model.WaitlistEntry;
import com.inkwell.api.model.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;

    @Transactional
    public WaitlistResponse join(WaitlistRequest request, String ipAddress) {
        String email = request.email().trim().toLowerCase();

        // Duplicate check
        if (waitlistRepository.existsByEmail(email)) {
            log.info("Duplicate waitlist signup attempted for: {}", email);
            long position = waitlistRepository.count();
            return new WaitlistResponse(
                true,
                "You're already on the list! We'll be in touch soon.",
                position
            );
        }

        WaitlistEntry entry = new WaitlistEntry();
        entry.setEmail(email);
        entry.setIpAddress(ipAddress);
        waitlistRepository.save(entry);

        long position = waitlistRepository.count();
        log.info("New waitlist signup: {} (position #{})", email, position);

        return new WaitlistResponse(
            true,
            "You're on the list! 🎉 We'll reach out when your spot is ready.",
            position
        );
    }

    public long getTotalSignups() {
        return waitlistRepository.count();
    }
}
