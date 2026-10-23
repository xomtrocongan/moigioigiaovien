package vn.teachermatch.service;

import org.springframework.stereotype.Service;
import vn.teachermatch.model.LogEntry;

import java.util.List;
import java.util.Locale;

@Service
public class AuditLogService implements IAuditLogService {

    private final PortalStore store;

    public AuditLogService(PortalStore store) {
        this.store = store;
    }

    @Override
    public List<LogEntry> getAllLogs() {
        return store.logs();
    }

    @Override
    public List<LogEntry> searchLogs(String keyword, String level, String date) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedLevel = level == null ? "ALL" : level.trim().toUpperCase(Locale.ROOT);
        String normalizedDate = date == null ? "" : date.trim();

        return store.logs().stream()
                .filter(entry -> normalizedLevel.equals("ALL") || entry.getLevel().equalsIgnoreCase(normalizedLevel))
                .filter(entry -> normalizedDate.isBlank() || entry.getDate().equals(normalizedDate))
                .filter(entry -> normalizedKeyword.isBlank()
                        || entry.getTime().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || entry.getLevel().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || entry.getUsername().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || entry.getAction().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .toList();
    }

    @Override
    public void log(String actor, String action) {
        store.log(actor, action);
    }
}

