package ch.unisg.studybuddy.service.dto;

import ch.unisg.studybuddy.model.StudySession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClashCheckResult {
    
    private boolean hasClash;
    
    @Builder.Default
    private List<StudySession> clashingSessions = new ArrayList<>();
    
    private String warningMessage;
    
    public static ClashCheckResult noClash() {
        return ClashCheckResult.builder()
                .hasClash(false)
                .clashingSessions(new ArrayList<>())
                .warningMessage(null)
                .build();
    }
    
    public static ClashCheckResult withClashes(List<StudySession> clashingSessions) {
        StringBuilder message = new StringBuilder("Session conflicts with ");
        message.append(clashingSessions.size());
        message.append(" existing session(s): ");
        
        for (int i = 0; i < clashingSessions.size(); i++) {
            StudySession s = clashingSessions.get(i);
            if (i > 0) message.append(", ");
            message.append(String.format("%s (%s - %s)",
                    s.getCourse() != null ? s.getCourse().getTitle() : "Unknown",
                    s.getStartTime().toLocalTime(),
                    s.getEndTime().toLocalTime()));
        }
        
        return ClashCheckResult.builder()
                .hasClash(true)
                .clashingSessions(clashingSessions)
                .warningMessage(message.toString())
                .build();
    }
}

