package vn.teachermatch.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Outcome/Evaluation result submitted by hiring partner or matching staff.
 */
public enum InterviewResult {
    NOT_REVIEWED("Not reviewed"),
    PENDING_REVIEW("Pending review"),
    QUALIFIED("Qualified"),
    REJECTED("Rejected");

    private final String label;

    InterviewResult(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Optional<InterviewResult> fromLabel(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(r -> r.label.equalsIgnoreCase(text.trim()) || r.name().equalsIgnoreCase(text.trim()))
                .findFirst();
    }
}

