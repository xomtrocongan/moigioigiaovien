package vn.teachermatch.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Defines system roles and their presentation in TeacherMatch platform.
 */
public enum Role {
    ADMIN("Administrator", "System Administrator", "/admin"),
    STAFF("Matching Staff", "Matching Workspace", "/staff"),
    PARTNER("Hiring Partner", "Partner Portal", "/partner"),
    TEACHER("Teacher", "Career Workspace", "/teacher");

    private final String displayName;
    private final String workspaceLabel;
    private final String defaultPath;

    Role(String displayName, String workspaceLabel, String defaultPath) {
        this.displayName = displayName;
        this.workspaceLabel = workspaceLabel;
        this.defaultPath = defaultPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getWorkspaceLabel() {
        return workspaceLabel;
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public static Optional<Role> fromString(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(r -> r.name().equalsIgnoreCase(roleName.trim()))
                .findFirst();
    }

    public static boolean isValid(String roleName) {
        return fromString(roleName).isPresent();
    }
}

