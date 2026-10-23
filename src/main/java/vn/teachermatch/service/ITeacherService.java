package vn.teachermatch.service;

import vn.teachermatch.dto.TeacherProfileUpdateRequest;
import vn.teachermatch.model.TeacherProfile;

import java.util.List;

public interface ITeacherService {
    List<TeacherProfile> getAllTeachers();
    TeacherProfile getTeacherById(long id);
    TeacherProfile getTeacherByUsername(String username);
    List<TeacherProfile> getVisibleTeachers(String username, String role);
    void updateProfile(String username, TeacherProfileUpdateRequest request);
    void updateStatus(long id, String status);
    void grantAccessToPartner(String partnerUsername, long teacherId);
    boolean canPartnerAccessTeacher(String partnerUsername, long teacherId);
}

