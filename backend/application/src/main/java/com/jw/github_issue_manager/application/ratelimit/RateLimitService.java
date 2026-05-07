package com.jw.github_issue_manager.application.ratelimit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jw.github_issue_manager.core.platform.PlatformType;

@Service
public class RateLimitService {

    private final RateLimitSnapshotRepository rateLimitSnapshotRepository;

    public RateLimitService(RateLimitSnapshotRepository rateLimitSnapshotRepository) {
        this.rateLimitSnapshotRepository = rateLimitSnapshotRepository;
    }

    @Transactional
    public void record(com.jw.github_issue_manager.core.platform.RateLimitSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        rateLimitSnapshotRepository.save(new RateLimitSnapshot(
            snapshot.platform(),
            snapshot.limit(),
            snapshot.remaining(),
            snapshot.resetAt(),
            snapshot.retryAfterSeconds(),
            snapshot.resource(),
            snapshot.capturedAt()
        ));
    }

    @Transactional(readOnly = true)
    public RateLimitSnapshotResponse getLatest(PlatformType platform) {
        return rateLimitSnapshotRepository.findTopByPlatformOrderByCapturedAtDesc(platform)
            .map(this::toResponse)
            .orElse(null);
    }

    private RateLimitSnapshotResponse toResponse(RateLimitSnapshot snapshot) {
        return new RateLimitSnapshotResponse(
            snapshot.getPlatform().name(),
            snapshot.getLimitCount(),
            snapshot.getRemainingCount(),
            snapshot.getResetAt(),
            snapshot.getRetryAfterSeconds(),
            snapshot.getResource(),
            snapshot.getCapturedAt()
        );
    }
}
