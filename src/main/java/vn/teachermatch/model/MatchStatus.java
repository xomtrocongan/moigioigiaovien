package vn.teachermatch.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Lifecycle status of a candidate teacher profile in matching pool.
 */
public enum MatchStatus {
    PENDING("Pending", "Profile under review by matching staff"),
    READY("Ready", "Approved and eligible to share with hiring partners"),
    REJECTED("Rejected", "Does not meet current program requirements");

    private final String label;
    private final String description;

    MatchStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<MatchStatus> fromString(String status) {
        if (status == null || status.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(status.trim()))
                .findFirst();
    }
}

