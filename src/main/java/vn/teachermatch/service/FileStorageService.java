package vn.teachermatch.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.teachermatch.exception.StorageException;
import vn.teachermatch.model.DocumentType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootDir = Paths.get("uploads").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new StorageException("Could not initialize upload storage location", e);
        }
    }

    public String storeFile(MultipartFile file, DocumentType type) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Cannot store empty or null file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new StorageException("Original filename is missing");
        }

        String fileName = Paths.get(originalFilename).getFileName().toString();
        int extIndex = fileName.lastIndexOf('.');
        String extension = extIndex < 0 ? "" : fileName.substring(extIndex).toLowerCase(Locale.ROOT);

        if (!type.isAllowedExtension(extension)) {
            throw new StorageException("Extension '" + extension + "' is not permitted for document type: " + type.getLabel());
        }

        if (file.getSize() > type.getMaxSizeBytes()) {
            throw new StorageException("File size exceeds maximum limit of " + (type.getMaxSizeBytes() / (1024 * 1024)) + " MB");
        }

        String safeFileName = UUID.randomUUID() + "_" + fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path destination = rootDir.resolve(safeFileName).normalize();

        if (!destination.startsWith(rootDir)) {
            throw new StorageException("Security error: destination path is outside upload directory");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + safeFileName;
        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + safeFileName, e);
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = rootDir.resolve(filename).normalize();
            if (!file.startsWith(rootDir) || !Files.exists(file) || !Files.isRegularFile(file)) {
                return null;
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void deletePreviousFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || !fileUrl.startsWith("/uploads/")) {
            return;
        }
        try {
            String fileName = fileUrl.substring("/uploads/".length());
            Path target = rootDir.resolve(fileName).normalize();
            if (target.startsWith(rootDir)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ignored) {
            // Best effort cleanup
        }
    }
}

