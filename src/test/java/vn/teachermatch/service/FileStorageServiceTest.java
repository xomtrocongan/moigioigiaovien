package vn.teachermatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import vn.teachermatch.exception.StorageException;
import vn.teachermatch.model.DocumentType;
import vn.teachermatch.support.TestMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileStorageService Upload & Security Tests")
class FileStorageServiceTest {

    private FileStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new FileStorageService();
    }

    @Test
    @DisplayName("Should successfully store valid PDF CV")
    void testStoreValidCv() {
        TestMultipartFile file = new TestMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "Dummy PDF content".getBytes(StandardCharsets.UTF_8)
        );

        String fileUrl = storageService.storeFile(file, DocumentType.CV);
        assertNotNull(fileUrl);
        assertTrue(fileUrl.startsWith("/uploads/"));
        assertTrue(fileUrl.endsWith("_resume.pdf"));

        String filename = fileUrl.substring("/uploads/".length());
        Resource loaded = storageService.loadAsResource(filename);
        assertNotNull(loaded);
        assertTrue(loaded.exists());
        assertTrue(loaded.isReadable());

        // Cleanup
        storageService.deletePreviousFile(fileUrl);
        assertNull(storageService.loadAsResource(filename));
    }

    @Test
    @DisplayName("Should reject disallowed file extensions (e.g., .exe, .sh, .html)")
    void testRejectDisallowedExtensions() {
        TestMultipartFile malicious = new TestMultipartFile(
                "file",
                "exploit.exe",
                "application/octet-stream",
                "malicious payload".getBytes()
        );

        assertThrows(StorageException.class, () -> storageService.storeFile(malicious, DocumentType.CV));
        assertThrows(StorageException.class, () -> storageService.storeFile(malicious, DocumentType.CERTIFICATE));
        assertThrows(StorageException.class, () -> storageService.storeFile(malicious, DocumentType.VIDEO));
    }

    @Test
    @DisplayName("Should reject empty or missing file uploads")
    void testRejectEmptyFile() {
        TestMultipartFile emptyFile = new TestMultipartFile("file", "test.pdf", "application/pdf", new byte[0]);
        assertThrows(StorageException.class, () -> storageService.storeFile(emptyFile, DocumentType.CV));
        assertThrows(StorageException.class, () -> storageService.storeFile(null, DocumentType.CV));
    }

    @Test
    @DisplayName("Should prevent directory traversal attacks on filename lookup")
    void testPathTraversalProtection() {
        assertNull(storageService.loadAsResource("../../../Windows/System32/drivers/etc/hosts"));
        assertNull(storageService.loadAsResource("..\\..\\pom.xml"));
        assertNull(storageService.loadAsResource("non_existent_file_xyz123.pdf"));
    }
}

