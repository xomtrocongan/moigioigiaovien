package vn.teachermatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.teachermatch.dto.TeacherProfileUpdateRequest;
import vn.teachermatch.dto.UserCreateRequest;
import vn.teachermatch.exception.ResourceNotFoundException;
import vn.teachermatch.exception.ValidationException;
import vn.teachermatch.model.TeacherProfile;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Service Layer Business Logic Tests")
class ServiceLayerTest {

    private PortalStore store;
    private UserService userService;
    private TeacherService teacherService;
    private InterviewService interviewService;
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        store = new PortalStore();
        userService = new UserService(store);
        teacherService = new TeacherService(store);
        interviewService = new InterviewService(store);
        auditLogService = new AuditLogService(store);
    }

    @Test
    @DisplayName("UserService registration validations")
    void testUserServiceRegistration() {
        UserCreateRequest validReq = new UserCreateRequest("staff_john", "pass123", "STAFF", "John Doe");
        userService.registerUser(validReq);
        assertTrue(userService.userExists("staff_john"));

        // Duplicate username should throw ValidationException
        assertThrows(ValidationException.class, () -> userService.registerUser(validReq));

        // Invalid role should throw ValidationException
        UserCreateRequest invalidRoleReq = new UserCreateRequest("hacker", "pass123", "SUPERUSER", "Hacker");
        assertThrows(ValidationException.class, () -> userService.registerUser(invalidRoleReq));
    }

    @Test
    @DisplayName("TeacherService profile update and email conflict handling")
    void testTeacherServiceUpdate() {
        TeacherProfile profile = teacherService.getTeacherById(1L);
        assertEquals("Emily Martin", profile.getName());

        TeacherProfileUpdateRequest updateReq = new TeacherProfileUpdateRequest(
                "Emily Martin PhD",
                "emily.updated@example.com",
                "American",
                "Saigon",
                "8 years",
                "PhD, TEFL"
        );
        teacherService.updateProfile("teacher", updateReq);

        TeacherProfile updated = teacherService.getTeacherById(1L);
        assertEquals("Emily Martin PhD", updated.getName());
        assertEquals("emily.updated@example.com", updated.getEmail());

        // Attempting to take teacher 2's email should fail
        TeacherProfileUpdateRequest conflictReq = new TeacherProfileUpdateRequest(
                "Emily Martin",
                "daniel.walker@example.com",
                "American", "Saigon", "8 years", "TEFL"
        );
        assertThrows(ValidationException.class, () -> teacherService.updateProfile("teacher", conflictReq));
    }

    @Test
    @DisplayName("TeacherService should throw ResourceNotFoundException for unknown ID")
    void testTeacherNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> teacherService.getTeacherById(9999L));
        assertThrows(ResourceNotFoundException.class, () -> teacherService.getTeacherByUsername("unknown_user"));
    }

    @Test
    @DisplayName("InterviewService interview feedback and teacher response lifecycle")
    void testInterviewLifecycle() {
        var interview = interviewService.getInterviewById(1L);
        assertNotNull(interview);

        // Partner feedback submission
        boolean feedbackSubmitted = interviewService.submitPartnerFeedback(
                1L, "partner", "Qualified", "Excellent communication skills and pedagogy."
        );
        assertTrue(feedbackSubmitted);
        assertEquals("Qualified", store.findInterview(1L).getResult());
        assertEquals("Excellent communication skills and pedagogy.", store.findInterview(1L).getFeedback());

        // Teacher status update
        boolean accepted = interviewService.updateTeacherDecision(1L, 1L, "CONFIRMED");
        assertTrue(accepted);
        assertEquals("CONFIRMED", store.findInterview(1L).getStatus());
    }

    @Test
    @DisplayName("AuditLogService log searching and filtering")
    void testAuditLogSearch() {
        auditLogService.log("AUDIT_TEST", "Verified admin panel filters");
        var results = auditLogService.searchLogs("Verified", "ALL", "");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(l -> l.getAction().contains("Verified admin panel filters")));
    }
}

