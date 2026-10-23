package vn.teachermatch.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class InterviewScheduleRequest {
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotBlank(message = "Date and time is required")
    private String dateTime;

    public InterviewScheduleRequest() {}

    public InterviewScheduleRequest(Long teacherId, String dateTime) {
        this.teacherId = teacherId;
        this.dateTime = dateTime;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}

