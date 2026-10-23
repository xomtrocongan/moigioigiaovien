package vn.teachermatch.service;

import org.springframework.stereotype.Service;
import vn.teachermatch.dto.TeacherProfileUpdateRequest;
import vn.teachermatch.exception.ResourceNotFoundException;
import vn.teachermatch.exception.ValidationException;
import vn.teachermatch.model.TeacherProfile;

import java.util.List;

@Service
public class TeacherService implements ITeacherService {

    private final PortalStore store;

    public TeacherService(PortalStore store) {
        this.store = store;
    }

    @Override
    public List<TeacherProfile> getAllTeachers() {
        return store.teachers();
    }

    @Override
    public TeacherProfile getTeacherById(long id) {
        TeacherProfile teacher = store.teacher(id);
        if (teacher == null) {
            throw new ResourceNotFoundException("Teacher profile not found for ID: " + id);
        }
        return teacher;
    }

    @Override
    public TeacherProfile getTeacherByUsername(String username) {
        TeacherProfile teacher = store.teacherForUser(username);
        if (teacher == null) {
            throw new ResourceNotFoundException("Teacher profile not found for user: " + username);
        }
        return teacher;
    }

    @Override
    public List<TeacherProfile> getVisibleTeachers(String username, String role) {
        return store.visibleTeachers(username, role);
    }

    @Override
    public void updateProfile(String username, TeacherProfileUpdateRequest request) {
        if (request == null) {
            throw new ValidationException("Profile update request cannot be null");
        }

        TeacherProfile teacher = getTeacherByUsername(username);

        String email = request.getEmail();
        if (store.teacherEmailInUse(email, teacher.getId())) {
            throw new ValidationException("Email address is already in use by another teacher: " + email);
        }

        teacher.setName(request.getName());
        teacher.setEmail(email);
        teacher.setNationality(request.getNationality());
        teacher.setLocation(request.getLocation());
        teacher.setExperience(request.getExperience());
        teacher.setQualification(request.getQualification());

        store.updateTeacherNameInInterviews(teacher.getId(), request.getName());
        store.log(username, "Updated teacher profile via TeacherService");
    }

    @Override
    public void updateStatus(long id, String status) {
        getTeacherById(id);
        store.status(id, status);
    }

    @Override
    public void grantAccessToPartner(String partnerUsername, long teacherId) {
        getTeacherById(teacherId);
        store.grant(partnerUsername, teacherId);
    }

    @Override
    public boolean canPartnerAccessTeacher(String partnerUsername, long teacherId) {
        return store.canPartnerAccessTeacher(partnerUsername, teacherId);
    }
}

