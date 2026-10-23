package vn.teachermatch.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogEntry {

    private final LocalDateTime timestamp;
    private final String level;
    private final String username;
    private final String action;

    public LogEntry(
            LocalDateTime timestamp,
            String level,
            String username,
            String action
    ) {
        this.timestamp = timestamp;
        this.level = level;
        this.username = username;
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getTime() {
        return timestamp.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        );
    }

    public String getDate() {
        return timestamp.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );
    }

    public String getLevel() {
        return level;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }
}
