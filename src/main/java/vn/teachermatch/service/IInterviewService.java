package vn.teachermatch.service;

import vn.teachermatch.model.Interview;

import java.util.List;

public interface IInterviewService {
    List<Interview> getAllInterviews();
    List<Interview> getVisibleInterviews(String role, String username);
    Interview getInterviewById(long id);
    boolean scheduleInterview(long teacherId, String partnerName, String dateTime);
    void updateInterview(long id, String status, String result, String feedback);
    boolean submitPartnerFeedback(long interviewId, String partnerUsername, String result, String feedback);
    boolean updateTeacherDecision(long interviewId, long teacherId, String status);
}

