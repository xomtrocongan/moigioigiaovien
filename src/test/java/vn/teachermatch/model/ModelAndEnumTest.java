package vn.teachermatch.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Enums & Entity Integrity Tests")
class ModelAndEnumTest {

    @Test
    @DisplayName("Role enum parsing and path mappings")
    void testRoleEnum() {
        assertEquals(Role.ADMIN, Role.fromString("admin").orElse(null));
        assertEquals(Role.STAFF, Role.fromString("STAFF").orElse(null));
        assertEquals(Role.PARTNER, Role.fromString("Partner").orElse(null));
        assertEquals(Role.TEACHER, Role.fromString("teacher").orElse(null));
        assertTrue(Role.fromString("INVALID").isEmpty());

        assertEquals("/admin", Role.ADMIN.getDefaultPath());
        assertEquals("/staff", Role.STAFF.getDefaultPath());
        assertEquals("/partner", Role.PARTNER.getDefaultPath());
        assertEquals("/teacher", Role.TEACHER.getDefaultPath());
    }

    @Test
    @DisplayName("MatchStatus enum parsing and labels")
    void testMatchStatusEnum() {
        assertEquals(MatchStatus.READY, MatchStatus.fromString("READY").orElse(null));
        assertEquals(MatchStatus.PENDING, MatchStatus.fromString("pending").orElse(null));
        assertEquals(MatchStatus.REJECTED, MatchStatus.fromString("Rejected").orElse(null));
        assertTrue(MatchStatus.fromString("UNKNOWN").isEmpty());
    }

    @Test
    @DisplayName("InterviewStatus and InterviewResult enums")
    void testInterviewEnums() {
        assertEquals(InterviewStatus.CONFIRMED, InterviewStatus.fromString("CONFIRMED").orElse(null));
        assertEquals(InterviewStatus.DECLINED, InterviewStatus.fromString("declined").orElse(null));

        assertEquals(InterviewResult.QUALIFIED, InterviewResult.fromLabel("Qualified").orElse(null));
        assertEquals(InterviewResult.NOT_REVIEWED, InterviewResult.fromLabel("Not reviewed").orElse(null));
        assertEquals(InterviewResult.PENDING_REVIEW, InterviewResult.fromLabel("Pending review").orElse(null));
        assertEquals(InterviewResult.REJECTED, InterviewResult.fromLabel("Rejected").orElse(null));
    }

    @Test
    @DisplayName("DocumentType extensions and file size limits")
    void testDocumentType() {
        assertTrue(DocumentType.CV.isAllowedExtension(".pdf"));
        assertTrue(DocumentType.CV.isAllowedExtension(".docx"));
        assertFalse(DocumentType.CV.isAllowedExtension(".mp4"));

        assertTrue(DocumentType.CERTIFICATE.isAllowedExtension(".jpg"));
        assertTrue(DocumentType.CERTIFICATE.isAllowedExtension(".png"));
        assertTrue(DocumentType.CERTIFICATE.isAllowedExtension(".pdf"));

        assertTrue(DocumentType.VIDEO.isAllowedExtension(".mp4"));
        assertTrue(DocumentType.VIDEO.isAllowedExtension(".webm"));
        assertFalse(DocumentType.VIDEO.isAllowedExtension(".pdf"));

        assertTrue(DocumentType.VIDEO.getMaxSizeBytes() >= 50 * 1024 * 1024L);
    }
}

