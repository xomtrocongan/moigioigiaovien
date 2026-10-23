
package vn.teachermatch.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import vn.teachermatch.model.AppUser;
import vn.teachermatch.model.TeacherProfile;
import vn.teachermatch.service.PortalStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Controller
public class PortalController {

    private final PortalStore store;

    private static final Set<String> ALLOWED_ROLES =
            Set.of("ADMIN", "STAFF", "PARTNER", "TEACHER");

    private static final Path UPLOAD_DIR =
            Paths.get("uploads").toAbsolutePath().normalize();

    public PortalController(PortalStore store) {
        this.store = store;
    }

    // =========================
    // SESSION AND ROLE HELPERS
    // =========================

    private AppUser current(HttpSession session) {
        Object value = session.getAttribute("user");

        if (value instanceof AppUser user) {
            return user;
        }

        return null;
    }

    private String role(HttpSession session) {
        AppUser user = current(session);

        if (user == null || user.getRole() == null) {
            return "";
        }

        return user.getRole().trim().toUpperCase();
    }

    private String username(HttpSession session) {
        AppUser user = current(session);

        if (user == null || user.getUsername() == null) {
            return "";
        }

        return user.getUsername().trim();
    }

    private boolean hasRole(HttpSession session, String... allowedRoles) {
        String currentRole = role(session);

        for (String allowedRole : allowedRoles) {
            if (allowedRole.equalsIgnoreCase(currentRole)) {
                return true;
            }
        }

        return false;
    }

    private String dashboardFor(String userRole) {
        if (userRole == null) {
            return "redirect:/login";
        }

        return switch (userRole.trim().toUpperCase()) {
            case "ADMIN" -> "redirect:/admin";
            case "STAFF" -> "redirect:/staff";
            case "PARTNER" -> "redirect:/partner";
            case "TEACHER" -> "redirect:/teacher";
            default -> "redirect:/login";
        };
    }

    // =========================
    // HOME AND LOGIN
    // =========================

    @GetMapping("/")
    public String root(HttpSession session) {
        AppUser user = current(session);

        if (user == null) {
            return "redirect:/login";
        }

        return dashboardFor(user.getRole());
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        // If already logged in, go to the matching dashboard.
        AppUser user = current(session);

        if (user != null) {
            return dashboardFor(user.getRole());
        }

        return "login";
    }

    @PostMapping("/login")
    public String doLogin(
            @RequestParam String username,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        String normalizedUsername = username == null
                ? ""
                : username.trim();

        if (normalizedUsername.isBlank()
                || password == null
                || password.isBlank()) {

            model.addAttribute(
                    "error",
                    "Please enter your username and password."
            );

            return "login";
        }

        AppUser user = store.authenticate(normalizedUsername, password);

        if (user == null) {
            model.addAttribute(
                    "error",
                    "Invalid username or password."
            );

            return "login";
        }

        String userRole = user.getRole();

        if (userRole == null
                || !ALLOWED_ROLES.contains(userRole.trim().toUpperCase())) {

            model.addAttribute(
                    "error",
                    "This account does not have a valid role."
            );

            return "login";
        }

        // Save the authenticated user in the session.
        session.setAttribute("user", user);

        store.log(user.getUsername(), "Logged in");

        return dashboardFor(userRole);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        AppUser user = current(session);

        if (user != null) {
            store.log(user.getUsername(), "Logged out");
        }

        session.invalidate();

        return "redirect:/login";
    }

    @ModelAttribute
    public void common(Model model, HttpSession session) {
        model.addAttribute("user", current(session));
        model.addAttribute("role", role(session));
    }

    // =========================
    // ADMIN
    // =========================

    @GetMapping("/admin")
    public String admin(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "ALL") String level,
            @RequestParam(defaultValue = "") String date,
            Model model,
            HttpSession session) {
        if (!hasRole(session, "ADMIN")) {
            return "redirect:/login";
        }

        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedLevel = level.trim().toUpperCase(Locale.ROOT);
        var logs = store.logs().stream()
                .filter(entry -> normalizedLevel.equals("ALL")
                        || entry.getLevel().equalsIgnoreCase(normalizedLevel))
                .filter(entry -> date.isBlank() || entry.getDate().equals(date))
                .filter(entry -> normalizedKeyword.isBlank()
                        || entry.getTime().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || entry.getLevel().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || entry.getUsername().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || entry.getAction().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .toList();

        model.addAttribute("users", store.users());
        model.addAttribute("logs", logs);
        model.addAttribute("totalLogs", store.logs().size());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedLevel", normalizedLevel);
        model.addAttribute("selectedDate", date);
        model.addAttribute("teachers", store.teachers());

        return "admin";
    }

    @PostMapping("/admin/users")
    public String addUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            HttpSession session) {

        if (!hasRole(session, "ADMIN")) {
            return "redirect:/login";
        }

        String newUsername = username == null ? "" : username.trim();
        String newRole = role == null
                ? ""
                : role.trim().toUpperCase();

        if (newUsername.isBlank()
                || password == null
                || password.isBlank()
                || !ALLOWED_ROLES.contains(newRole)) {

            return "redirect:/admin";
        }

        if (!store.userExists(newUsername)) {
            store.addUser(newUsername, password, newRole);
        }

        return "redirect:/admin";
    }

    // =========================
    // STAFF
    // =========================

    @GetMapping("/staff")
    public String staff(Model model, HttpSession session) {
        if (!hasRole(session, "STAFF", "ADMIN")) {
            return "redirect:/login";
        }

        model.addAttribute("teachers", store.teachers());
        model.addAttribute("interviews", store.interviews());

        return "staff";
    }

    @PostMapping("/staff/status/{id}")
    public String status(
            @PathVariable long id,
            @RequestParam String status,
            HttpSession session) {

        if (!hasRole(session, "STAFF", "ADMIN")) {
            return "redirect:/login";
        }

        store.status(id, status);

        return "redirect:/staff";
    }

    @PostMapping("/staff/grant/{id}")
    public String grant(
            @PathVariable long id,
            @RequestParam(defaultValue = "partner") String partner,
            HttpSession session) {

        if (!hasRole(session, "STAFF", "ADMIN")) {
            return "redirect:/login";
        }

        store.grant(partner.trim(), id);

        return "redirect:/staff";
    }

    @PostMapping("/staff/interview/{id}")
    public String staffInterview(
            @PathVariable long id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String feedback,
            HttpSession session) {

        if (!hasRole(session, "STAFF", "ADMIN")) {
            return "redirect:/login";
        }

        store.updateInterview(id, status, result, feedback);

        return "redirect:/staff#interviews";
    }

    // =========================
    // PARTNER
    // =========================

    @GetMapping("/partner")
    public String partner(Model model, HttpSession session) {
        if (!hasRole(session, "PARTNER")) {
            return "redirect:/login";
        }

        model.addAttribute(
                "teachers",
                store.visibleTeachers(username(session), role(session))
        );

        model.addAttribute(
                "interviews",
                store.visibleInterviews(role(session), username(session))
        );

        return "partner";
    }

    @PostMapping("/partner/interview/{id}")
    public String requestInterview(
            @PathVariable long id,
            @RequestParam String dateTime,
            HttpSession session) {

        if (!hasRole(session, "PARTNER")) {
            return "redirect:/login";
        }

        if (dateTime == null || dateTime.isBlank()) {
            return "redirect:/partner#interviews";
        }

        // Preserve the current demo partner mapping.
        // A multi-partner system should use the authenticated
        // partner's actual organization instead of a fixed name.
        store.requestInterview(
                id,
                "Bright English Center",
                dateTime
        );

        return "redirect:/partner#interviews";
    }

    @PostMapping("/partner/feedback/{id}")
    public String feedback(
            @PathVariable long id,
            @RequestParam String result,
            @RequestParam String feedback,
            HttpSession session) {

        if (!hasRole(session, "PARTNER")) {
            return "redirect:/login";
        }

        store.updateInterview(id, null, result, feedback);

        return "redirect:/partner#interviews";
    }

    // =========================
    // TEACHER
    // =========================

    @GetMapping("/teacher")
    public String teacher(Model model, HttpSession session) {
        if (!hasRole(session, "TEACHER")) {
            return "redirect:/login";
        }

        model.addAttribute("teacher", store.teacher(1));

        model.addAttribute(
                "interviews",
                store.visibleInterviews(role(session), username(session))
        );

        return "teacher";
    }

    @PostMapping("/teacher/profile")
    public String profile(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String nationality,
            @RequestParam String location,
            @RequestParam String experience,
            @RequestParam String qualification,
            HttpSession session) {

        if (!hasRole(session, "TEACHER")) {
            return "redirect:/login";
        }

        TeacherProfile teacher = store.teacher(1);

        if (teacher == null) {
            return "redirect:/teacher";
        }

        teacher.setName(name);
        teacher.setEmail(email);
        teacher.setNationality(nationality);
        teacher.setLocation(location);
        teacher.setExperience(experience);
        teacher.setQualification(qualification);

        store.log(username(session), "Updated teacher profile");

        return "redirect:/teacher";
    }

    @PostMapping("/teacher/upload")
    public String upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam String documentType,
            HttpSession session) throws IOException {

        if (!hasRole(session, "TEACHER")) {
            return "redirect:/login";
        }

        if (file == null || file.isEmpty()) {
            return "redirect:/teacher";
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            return "redirect:/teacher";
        }

        Files.createDirectories(UPLOAD_DIR);

        String safeFilename = UUID.randomUUID()
                + "_"
                + Paths.get(originalFilename).getFileName();

        Path destination = UPLOAD_DIR.resolve(safeFilename).normalize();

        if (!destination.startsWith(UPLOAD_DIR)) {
            return "redirect:/teacher";
        }

        Files.copy(
                file.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        TeacherProfile teacher = store.teacher(1);

        if (teacher == null) {
            Files.deleteIfExists(destination);
            return "redirect:/teacher";
        }

        if ("Video".equalsIgnoreCase(documentType)) {
            teacher.setVideoUrl("/uploads/" + safeFilename);
        } else {
            teacher.setCvUrl("/uploads/" + safeFilename);
        }

        store.log(username(session), "Uploaded " + documentType);

        return "redirect:/teacher";
    }

    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> uploadedFile(
            @PathVariable String filename) throws IOException {

        Path filePath = UPLOAD_DIR.resolve(filename).normalize();

        // Do not allow a request to read files outside uploads/.
        if (!filePath.startsWith(UPLOAD_DIR)
                || !Files.exists(filePath)
                || !Files.isRegularFile(filePath)) {

            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/teacher/interview/{id}")
    public String teacherInterview(
            @PathVariable long id,
            @RequestParam String status,
            HttpSession session) {

        if (!hasRole(session, "TEACHER")) {
            return "redirect:/login";
        }

        store.updateInterview(id, status, null, null);

        return "redirect:/teacher";
    }
}
