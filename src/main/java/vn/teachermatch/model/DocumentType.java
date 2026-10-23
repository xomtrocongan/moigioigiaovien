package vn.teachermatch.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Validated document types for candidate portfolio uploads.
 */
public enum DocumentType {
    CV("CV", "Curriculum Vitae / Resume", Set.of(".pdf", ".doc", ".docx"), 15 * 1024 * 1024L),
    CERTIFICATE("Certificate", "Teaching Credentials & Diplomas", Set.of(".pdf", ".doc", ".docx", ".jpg", ".jpeg", ".png"), 20 * 1024 * 1024L),
    VIDEO("Video", "Teaching demo / self-introduction video", Set.of(".mp4", ".mov", ".webm"), 50 * 1024 * 1024L);

    private final String code;
    private final String label;
    private final Set<String> allowedExtensions;
    private final long maxSizeBytes;

    DocumentType(String code, String label, Set<String> allowedExtensions, long maxSizeBytes) {
        this.code = code;
        this.label = label;
        this.allowedExtensions = Collections.unmodifiableSet(allowedExtensions);
        this.maxSizeBytes = maxSizeBytes;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public Set<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public boolean isAllowedExtension(String extension) {
        if (extension == null) return false;
        return allowedExtensions.contains(extension.toLowerCase());
    }

    public static Optional<DocumentType> fromString(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(d -> d.code.equalsIgnoreCase(text.trim()) || d.name().equalsIgnoreCase(text.trim()))
                .findFirst();
    }
}

