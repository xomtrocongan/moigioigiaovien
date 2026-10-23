package vn.teachermatch.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Lifecycle status of an interview meeting.
 */
public enum InterviewStatus {
    PENDING("Pending", "Awaiting teacher confirmation"),
    CONFIRMED("Confirmed", "Accepted by candidate, meeting is scheduled"),
    COMPLETED("Completed", "Interview has taken place"),
    CANCELLED("Cancelled", "Cancelled by matching staff or partner"),
    DECLINED("Declined", "Declined by candidate");

    private final String label;
    private final String description;

    InterviewStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<InterviewStatus> fromString(String status) {
        if (status == null || status.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(status.trim()))
                .findFirst();
    }
}

