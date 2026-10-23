package vn.teachermatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.teachermatch.model.AppUser;
import vn.teachermatch.model.Interview;
import vn.teachermatch.model.TeacherProfile;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PortalStore Domain & State Management Tests")
class PortalStoreTest {

    private PortalStore store;

    @BeforeEach
    void setUp() {
        store = new PortalStore();
    }

    @Test
    @DisplayName("Should successfully authenticate valid credentials by username")
    void testAuthenticateByUsername() {
        AppUser admin = store.authenticate("admin", "admin123");
        assertNotNull(admin);
        assertEquals("ADMIN", admin.getRole());

        AppUser staff = store.authenticate("staff", "staff123");
        assertNotNull(staff);
        assertEquals("STAFF", staff.getRole());
    }

    @Test
    @DisplayName("Should successfully authenticate teacher using their profile email")
    void testAuthenticateByTeacherEmail() {
        AppUser teacher = store.authenticate("emily.martin@example.com", "teacher123");
        assertNotNull(teacher);
        assertEquals("teacher", teacher.getUsername());
        assertEquals("TEACHER", teacher.getRole());
    }

    @Test
    @DisplayName("Should reject authentication with invalid password or missing user")
    void testAuthenticateInvalid() {
        assertNull(store.authenticate("admin", "wrong_pass"));
        assertNull(store.authenticate("non_existent_user", "password"));
        assertNull(store.authenticate(null, "pass"));
        assertNull(store.authenticate("admin", null));
    }

    @Test
    @DisplayName("Should create user and auto-initialize TeacherProfile when role is TEACHER")
    void testAddUser() {
        assertFalse(store.userExists("newteacher"));
        store.addUser("newteacher", "secret123", "TEACHER");

        assertTrue(store.userExists("newteacher"));
        AppUser created = store.authenticate("newteacher", "secret123");
        assertNotNull(created);
        assertEquals("TEACHER", created.getRole());

        Long teacherId = store.teacherIdForUser("newteacher");
        assertNotNull(teacherId);

        TeacherProfile profile = store.teacher(teacherId);
        assertNotNull(profile);
        assertEquals("newteacher", profile.getName());
        assertEquals("PENDING", profile.getStatus());
    }

    @Test
    @DisplayName("Should enforce email uniqueness check for teacher profiles")
    void testTeacherEmailInUse() {
        assertTrue(store.teacherEmailInUse("emily.martin@example.com", 999L));
        // Same teacher updating their own email should not conflict with themselves
        assertFalse(store.teacherEmailInUse("emily.martin@example.com", 1L));
        // New email should not be in use
        assertFalse(store.teacherEmailInUse("brand_new_email@example.com", 1L));
    }

    @Test
    @DisplayName("Should filter visible teachers strictly based on partner access grants")
    void testPartnerAccessControl() {
        // Initially partner has access to teachers 1 and 2, but not 3
        List<TeacherProfile> visible = store.visibleTeachers("partner", "PARTNER");
        assertEquals(2, visible.size());
        assertTrue(store.canPartnerAccessTeacher("partner", 1L));
        assertTrue(store.canPartnerAccessTeacher("partner", 2L));
        assertFalse(store.canPartnerAccessTeacher("partner", 3L));

        // Grant access to teacher 3
        store.grant("partner", 3L);
        assertTrue(store.canPartnerAccessTeacher("partner", 3L));
        assertEquals(3, store.visibleTeachers("partner", "PARTNER").size());

        // Staff and Admin see all teachers
        assertEquals(store.teachers().size(), store.visibleTeachers("staff", "STAFF").size());
        assertEquals(store.teachers().size(), store.visibleTeachers("admin", "ADMIN").size());
    }

    @Test
    @DisplayName("Should validate teacher status updates and ignore invalid transitions")
    void testStatusUpdate() {
        TeacherProfile teacher = store.teacher(1L);
        assertEquals("READY", teacher.getStatus());

        store.status(1L, "REJECTED");
        assertEquals("REJECTED", teacher.getStatus());

        store.status(1L, "PENDING");
        assertEquals("PENDING", teacher.getStatus());

        // Invalid status should be ignored
        store.status(1L, "HACKED_STATUS");
        assertEquals("PENDING", teacher.getStatus());
    }

    @Test
    @DisplayName("Should synchronize teacher name in interviews when profile is updated")
    void testUpdateTeacherNameInInterviews() {
        Interview interview = store.findInterview(1L);
        assertNotNull(interview);
        assertEquals("Emily Martin", interview.getTeacherName());

        store.updateTeacherNameInInterviews(1L, "Emily Martin-Johnson");

        Interview updated = store.findInterview(1L);
        assertNotNull(updated);
        assertEquals("Emily Martin-Johnson", updated.getTeacherName());
    }

    @Test
    @DisplayName("Should allow partner to request interview with valid future schedule")
    void testRequestInterview() {
        String meetingTime = LocalDateTime.now().plusDays(5).toString();
        boolean created = store.requestInterview(1L, "Bright English Center", meetingTime);
        assertTrue(created);

        List<Interview> partnerInterviews = store.visibleInterviews("PARTNER", "partner");
        assertTrue(partnerInterviews.stream().anyMatch(i -> i.getDateTime().equals(meetingTime)));
    }

    @Test
    @DisplayName("Should allow teacher to confirm or decline proposed interview")
    void testTeacherInterviewStatusUpdate() {
        boolean confirmed = store.updateTeacherInterview(1L, 1L, "CONFIRMED");
        assertTrue(confirmed);
        assertEquals("CONFIRMED", store.findInterview(1L).getStatus());

        boolean declined = store.updateTeacherInterview(1L, 1L, "DECLINED");
        assertTrue(declined);
        assertEquals("DECLINED", store.findInterview(1L).getStatus());

        // Different teacher cannot update another teacher's interview
        boolean unauthorized = store.updateTeacherInterview(1L, 999L, "CONFIRMED");
        assertFalse(unauthorized);
    }

    @Test
    @DisplayName("Should record audit log entries with timestamp and actor")
    void testAuditLogs() {
        int initialCount = store.logs().size();
        store.log("TEST_ACTOR", "Executed unit test action");

        assertEquals(initialCount + 1, store.logs().size());
        assertEquals("TEST_ACTOR", store.logs().get(0).getUsername());
        assertEquals("Executed unit test action", store.logs().get(0).getAction());
    }
}

