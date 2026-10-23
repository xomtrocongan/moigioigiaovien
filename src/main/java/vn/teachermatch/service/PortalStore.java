package vn.teachermatch.service;

import org.springframework.stereotype.Service;

import vn.teachermatch.model.AppUser;
import vn.teachermatch.model.Interview;
import vn.teachermatch.model.LogEntry;
import vn.teachermatch.model.TeacherProfile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PortalStore {

    private final Map<String, AppUser> users = new LinkedHashMap<>();
    private final Map<String, Long> teacherAccounts = new LinkedHashMap<>();
    private final List<TeacherProfile> teachers = new ArrayList<>();
    private final List<Interview> interviews = new ArrayList<>();
    private final List<LogEntry> logs = new ArrayList<>();

    // Stores access grants in the form username:teacherId.
    private final Set<String> granted = new HashSet<>();

    private final AtomicLong interviewIds = new AtomicLong(3);
    private final AtomicLong teacherIds = new AtomicLong(3);

    public PortalStore() {
        LocalDateTime firstInterviewTime = LocalDateTime.now().plusDays(2)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime secondInterviewTime = LocalDateTime.now().plusDays(3)
                .withHour(14).withMinute(30).withSecond(0).withNano(0);
        // Demo accounts
        users.put(
                "admin",
                new AppUser(
                        "admin",
                        "admin123",
                        "ADMIN",
                        "System Administrator"
                )
        );

        users.put(
                "staff",
                new AppUser(
                        "staff",
                        "staff123",
                        "STAFF",
                        "Matching Staff"
                )
        );

        users.put(
                "partner",
                new AppUser(
                        "partner",
                        "partner123",
                        "PARTNER",
                        "Bright English Center"
                )
        );

        users.put(
                "teacher",
                new AppUser(
                        "teacher",
                        "teacher123",
                        "TEACHER",
                        "Emily Martin"
                )
        );

        // Demo teacher profiles
        teachers.add(
                new TeacherProfile(
                        1,
                        "Emily Martin",
                        "emily.martin@example.com",
                        "American",
                        "Ho Chi Minh City",
                        "5 years",
                        "TEFL, Bachelor's degree",
                        "READY",
                        "",
                        ""
                )
        );

        teachers.add(
                new TeacherProfile(
                        2,
                        "Daniel Walker",
                        "daniel.walker@example.com",
                        "British",
                        "Hanoi",
                        "3 years",
                        "CELTA",
                        "READY",
                        "",
                        ""
                )
        );

        teachers.add(
                new TeacherProfile(
                        3,
                        "Sarah Thompson",
                        "sarah.thompson@example.com",
                        "Australian",
                        "Da Nang",
                        "7 years",
                        "TESOL, Master's degree",
                        "PENDING",
                        "",
                        ""
                )
        );

        teacherAccounts.put("teacher", 1L);

        // Initially, the demo Partner can view teachers 1 and 2.
        granted.add("partner:1");
        granted.add("partner:2");

        // Demo interviews
        interviews.add(
                new Interview(
                        1,
                        1,
                        "Emily Martin",
                        "Bright English Center",
                        firstInterviewTime.toString(),
                        "PENDING",
                        "Not reviewed",
                        "",
                        ""
                )
        );

        interviews.add(
                new Interview(
                        2,
                        2,
                        "Daniel Walker",
                        "Bright English Center",
                        secondInterviewTime.toString(),
                        "CONFIRMED",
                        "Not reviewed",
                        "",
                        ""
                )
        );

        log("SYSTEM", "Seed data loaded");
    }

    // =========================
    // USERS
    // =========================

    public Collection<AppUser> users() {
        return users.values();
    }

    public boolean userExists(String username) {
        return username != null && users.containsKey(username.trim());
    }

    public List<AppUser> partners() {
        return users.values().stream()
                .filter(user -> "PARTNER".equalsIgnoreCase(user.getRole()))
                .toList();
    }

    public String userRole(String username) {
        AppUser user = users.get(username);
        return user == null ? null : user.getRole();
    }

    public AppUser authenticate(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) {
            return null;
        }

        String identifier = usernameOrEmail.trim();
        AppUser user = users.get(identifier);

        if (user == null) {
            TeacherProfile matchedTeacher = teachers.stream()
                    .filter(teacher -> teacher.getEmail() != null
                            && teacher.getEmail().equalsIgnoreCase(identifier))
                    .findFirst()
                    .orElse(null);
            if (matchedTeacher != null) {
                String teacherUsername = teacherAccounts.entrySet().stream()
                        .filter(entry -> entry.getValue() == matchedTeacher.getId())
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                user = teacherUsername == null ? null : users.get(teacherUsername);
            }
        }

        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }

        return user;
    }


    public void addUser(String username, String password, String role) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || role == null || role.isBlank()) {
            return;
        }

        if (users.containsKey(username)) {
            return;
        }

        users.put(
                username,
                new AppUser(
                        username,
                        password,
                        role,
                        username
                )
        );

        if ("TEACHER".equalsIgnoreCase(role.trim())) {
            long teacherId = teacherIds.incrementAndGet();
            teachers.add(new TeacherProfile(
                    teacherId, username.trim(), "", "", "", "", "", "PENDING", "", ""));
            teacherAccounts.put(username.trim(), teacherId);
        }

        log("ADMIN", "Created account " + username);
    }

    // =========================
    // TEACHER PROFILES
    // =========================

    public List<TeacherProfile> teachers() {
        return teachers;
    }

    public TeacherProfile teacher(long id) {
        return teachers.stream()
                .filter(teacher -> teacher.getId() == id)
                .findFirst()
                .orElse(null);
    }
    public Long teacherIdForUser(String username) {
        return username == null ? null : teacherAccounts.get(username.trim());
    }

    public boolean teacherEmailInUse(String email, long exceptTeacherId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return teachers.stream()
                .anyMatch(teacher -> teacher.getId() != exceptTeacherId
                        && teacher.getEmail() != null
                        && teacher.getEmail().equalsIgnoreCase(email.trim()));
    }
    public TeacherProfile teacherForUser(String username) {
        Long teacherId = teacherIdForUser(username);
        return teacherId == null ? null : teacher(teacherId);
    }

    public List<TeacherProfile> visibleTeachers(
            String username,
            String role) {

        if ("STAFF".equals(role) || "ADMIN".equals(role)) {
            return teachers;
        }

        if ("PARTNER".equals(role)) {
            return teachers.stream()
                    .filter(teacher ->
                            granted.contains(
                                    username + ":" + teacher.getId()
                            )
                    )
                    .toList();
        }

        // Other roles must not see Partner-shared profiles.
        return List.of();
    }

    public boolean canPartnerAccessTeacher(
            String username,
            long teacherId) {

        if (username == null || !userExists(username)) {
            return false;
        }

        if (!"PARTNER".equals(userRole(username))) {
            return false;
        }

        if (teacher(teacherId) == null) {
            return false;
        }

        return granted.contains(username + ":" + teacherId);
    }

    public void grant(String username, long teacherId) {
        if (username == null || !userExists(username)) {
            return;
        }

        if (!"PARTNER".equals(userRole(username))) {
            return;
        }

        if (teacher(teacherId) == null) {
            return;
        }

        granted.add(username + ":" + teacherId);

        log(
                "STAFF",
                "Granted teacher " + teacherId + " to " + username
        );
    }

    public void status(long id, String status) {
        TeacherProfile teacher = teacher(id);

        if (teacher == null || status == null || status.isBlank()) {
            return;
        }

        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("PENDING", "READY", "REJECTED").contains(normalizedStatus)) {
            return;
        }

        teacher.setStatus(normalizedStatus);

        log(
                "STAFF",
                "Updated " + teacher.getName()
                        + " status to " + status
        );
    }

    // =========================
    // INTERVIEWS
    // =========================

    public List<Interview> interviews() {
        return interviews;
    }

    public List<Interview> visibleInterviews(
            String role,
            String username) {

        if ("STAFF".equals(role) || "ADMIN".equals(role)) {
            return interviews;
        }

        if ("TEACHER".equals(role)) {
            Long teacherId = teacherIdForUser(username);
            if (teacherId == null) {
                return List.of();
            }
            return interviews.stream()
                    .filter(interview -> interview.getTeacherId() == teacherId)
                    .toList();
        }

        if ("PARTNER".equals(role)) {
            AppUser user = users.get(username);

            if (user == null) {
                return List.of();
            }

            String partnerName = user.getFullName();

            if (partnerName == null || partnerName.isBlank()) {
                partnerName = user.getUsername();
            }

            final String currentPartner = partnerName;

            return interviews.stream()
                    .filter(interview ->
                            interview.getPartner() != null
                                    && interview.getPartner()
                                    .equalsIgnoreCase(currentPartner)
                    )
                    .toList();
        }

        return List.of();
    }

    public boolean requestInterview(
            long teacherId,
            String partnerName,
            String dateTime) {

        TeacherProfile teacher = teacher(teacherId);

        if (teacher == null
                || partnerName == null
                || partnerName.isBlank()
                || dateTime == null
                || dateTime.isBlank()) {
            return false;
        }

        long newId = interviewIds.incrementAndGet();

        Interview interview = new Interview(
                newId,
                teacherId,
                teacher.getName(),
                partnerName,
                dateTime,
                "PENDING",
                "Not reviewed",
                "",
                ""
        );

        interviews.add(interview);

        log(
                partnerName,
                "Interview requested for " + teacher.getName()
                        + " at " + dateTime
        );

        return true;
    }

    public void updateInterview(
            long id,
            String status,
            String result,
            String feedback) {

        Interview interview = findInterview(id);
        if (interview == null) {
            return;
        }

        String normalizedStatus = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        String normalizedResult = result == null ? "" : result.trim();
        if ((!normalizedStatus.isBlank()
                        && !Set.of("PENDING", "CONFIRMED", "COMPLETED", "CANCELLED", "DECLINED")
                                .contains(normalizedStatus))
                || (!normalizedResult.isBlank()
                        && !Set.of("Not reviewed", "Pending review", "Qualified", "Rejected")
                                .contains(normalizedResult))) {
            return;
        }

        if (!normalizedStatus.isBlank()) {
            interview.setStatus(normalizedStatus);
        }

        if (!normalizedResult.isBlank()) {
            interview.setResult(normalizedResult);
        }

        if (feedback != null) {
            interview.setFeedback(feedback);
        }

        // This is a demo placeholder, not a generated meeting.
        // A real meeting URL must be supplied by the scheduling workflow.
        log("INTERVIEW", "Updated interview #" + id);
    }

    public boolean updatePartnerFeedback(
            long interviewId,
            String username,
            String result,
            String feedback) {

        if (username == null || result == null || result.isBlank()
                || feedback == null || feedback.isBlank()
                || !Set.of("Pending review", "Qualified", "Rejected").contains(result)) {
            return false;
        }

        AppUser user = users.get(username);

        if (user == null || !"PARTNER".equals(user.getRole())) {
            return false;
        }

        String partnerName = user.getFullName();

        if (partnerName == null || partnerName.isBlank()) {
            partnerName = user.getUsername();
        }

        Interview interview = findInterview(interviewId);

        if (interview == null || interview.getPartner() == null) {
            return false;
        }

        // A Partner can only update an interview assigned to that Partner.
        if (!interview.getPartner().equalsIgnoreCase(partnerName)) {
            return false;
        }

        interview.setResult(result);
        interview.setFeedback(feedback);

        log(
                username,
                "Submitted feedback for interview #" + interviewId
                        + ": " + result
        );

        return true;
    }

    public boolean updateTeacherInterview(
            long interviewId,
            long teacherId,
            String status) {

        if (status == null
                || !(status.equalsIgnoreCase("CONFIRMED")
                        || status.equalsIgnoreCase("DECLINED"))) {
            return false;
        }

        Interview interview = findInterview(interviewId);
        if (interview == null || interview.getTeacherId() != teacherId) {
            return false;
        }

        updateInterview(interviewId, status, null, null);
        return true;
    }

    private Interview findInterview(long id) {
        return interviews.stream()
                .filter(interview -> interview.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // =========================
    // SYSTEM LOGS
    // =========================

    public List<LogEntry> logs() {
        return logs;
    }

    public void log(String actor, String action) {
        logs.add(0, new LogEntry(LocalDateTime.now(), "INFO", actor, action));
    }
}
