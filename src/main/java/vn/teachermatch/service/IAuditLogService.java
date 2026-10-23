package vn.teachermatch.service;

import vn.teachermatch.model.LogEntry;

import java.util.List;

public interface IAuditLogService {
    List<LogEntry> getAllLogs();
    List<LogEntry> searchLogs(String keyword, String level, String date);
    void log(String actor, String action);
}

