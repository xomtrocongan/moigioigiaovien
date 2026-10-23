package vn.teachermatch.service;

import org.springframework.stereotype.Service;
import vn.teachermatch.exception.ResourceNotFoundException;
import vn.teachermatch.model.Interview;

import java.util.List;

@Service
public class InterviewService implements IInterviewService {

    private final PortalStore store;

    public InterviewService(PortalStore store) {
        this.store = store;
    }

    @Override
    public List<Interview> getAllInterviews() {
        return store.interviews();
    }

    @Override
    public List<Interview> getVisibleInterviews(String role, String username) {
        return store.visibleInterviews(role, username);
    }

    @Override
    public Interview getInterviewById(long id) {
        Interview interview = store.findInterview(id);
        if (interview == null) {
            throw new ResourceNotFoundException("Interview not found with ID: " + id);
        }
        return interview;
    }

    @Override
    public boolean scheduleInterview(long teacherId, String partnerName, String dateTime) {
        return store.requestInterview(teacherId, partnerName, dateTime);
    }

    @Override
    public void updateInterview(long id, String status, String result, String feedback) {
        getInterviewById(id);
        store.updateInterview(id, status, result, feedback);
    }

    @Override
    public boolean submitPartnerFeedback(long interviewId, String partnerUsername, String result, String feedback) {
        getInterviewById(interviewId);
        return store.updatePartnerFeedback(interviewId, partnerUsername, result, feedback);
    }

    @Override
    public boolean updateTeacherDecision(long interviewId, long teacherId, String status) {
        getInterviewById(interviewId);
        return store.updateTeacherInterview(interviewId, teacherId, status);
    }
}

