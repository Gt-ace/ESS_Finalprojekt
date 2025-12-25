package ch.unisg.studybuddy.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadCheckResult {
    
    private boolean exceedsLimit;
    private int currentMinutes;
    private int proposedMinutes;
    private int totalMinutes;
    private int dailyLimitMinutes;
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

