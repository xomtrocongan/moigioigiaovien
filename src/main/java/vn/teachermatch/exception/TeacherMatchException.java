package vn.teachermatch.exception;

/**
 * Root unchecked application exception for TeacherMatch platform.
 */
public class TeacherMatchException extends RuntimeException {
    public TeacherMatchException(String message) {
        super(message);
    }

    public TeacherMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}

