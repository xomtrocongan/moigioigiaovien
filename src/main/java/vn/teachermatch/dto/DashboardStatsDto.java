package vn.teachermatch.dto;

public class DashboardStatsDto {
    private final long totalTeachers;
    private final long readyTeachers;
    private final long pendingTeachers;
    private final long totalInterviews;
    private final long confirmedInterviews;
    private final long totalUsers;

    public DashboardStatsDto(long totalTeachers, long readyTeachers, long pendingTeachers,
                             long totalInterviews, long confirmedInterviews, long totalUsers) {
        this.totalTeachers = totalTeachers;
        this.readyTeachers = readyTeachers;
        this.pendingTeachers = pendingTeachers;
        this.totalInterviews = totalInterviews;
        this.confirmedInterviews = confirmedInterviews;
        this.totalUsers = totalUsers;
    }

    public long getTotalTeachers() {
        return totalTeachers;
    }

    public long getReadyTeachers() {
        return readyTeachers;
    }

    public long getPendingTeachers() {
        return pendingTeachers;
    }

    public long getTotalInterviews() {
        return totalInterviews;
    }

    public long getConfirmedInterviews() {
        return confirmedInterviews;
    }

    public long getTotalUsers() {
        return totalUsers;
    }
}

