package ch.unisg.studybuddy.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a daily workload check for a study session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadCheckResult {
    
    /**
     * Whether the proposed session would exceed the daily workload limit.
     */
    private boolean exceedsLimit;
    
    /**
     * Current total minutes already scheduled for the day.
     */
    private int currentMinutes;
    
    /**
     * Proposed additional minutes.
     */
    private int proposedMinutes;
    
    /**
     * Total minutes if the proposed session is added.
     */
    private int totalMinutes;
    
    /**
     * The preferred daily limit in minutes.
     */
    private int dailyLimitMinutes;
    
    /**
     * Warning message if limit would be exceeded.
     */
    private String warningMessage;
    
    public static LoadCheckResult ok(int currentMinutes, int proposedMinutes, int dailyLimit) {
        return LoadCheckResult.builder()
                .exceedsLimit(false)
                .currentMinutes(currentMinutes)
                .proposedMinutes(proposedMinutes)
                .totalMinutes(currentMinutes + proposedMinutes)
                .dailyLimitMinutes(dailyLimit)
                .warningMessage(null)
                .build();
    }
    
    public static LoadCheckResult warning(int currentMinutes, int proposedMinutes, int dailyLimit) {
        int total = currentMinutes + proposedMinutes;
        int overBy = total - dailyLimit;
        return LoadCheckResult.builder()
                .exceedsLimit(true)
                .currentMinutes(currentMinutes)
                .proposedMinutes(proposedMinutes)
                .totalMinutes(total)
                .dailyLimitMinutes(dailyLimit)
                .warningMessage(String.format(
                        "Adding this session would exceed your daily limit by %d minutes. " +
                        "Current: %d min, Proposed: %d min, Limit: %d min.",
                        overBy, currentMinutes, proposedMinutes, dailyLimit))
                .build();
    }
}

