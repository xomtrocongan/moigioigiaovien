package vn.teachermatch.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSize(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        log.warn("Uploaded file exceeds server limit: {}", exc.getMessage());
        redirectAttributes.addFlashAttribute("error", "The uploaded file is too large. Please upload a smaller file.");
        return "redirect:/teacher";
    }

    @ExceptionHandler(StorageException.class)
    public String handleStorageException(StorageException exc, RedirectAttributes redirectAttributes) {
        log.warn("Storage operation failed: {}", exc.getMessage());
        redirectAttributes.addFlashAttribute("error", exc.getMessage());
        return "redirect:/teacher";
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorized(UnauthorizedException exc) {
        log.warn("Access denied: {}", exc.getMessage());
        return "redirect:/login";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception exc, Model model) {
        log.error("Unhandled application exception", exc);
        model.addAttribute("error", "An unexpected error occurred. Please try again.");
        return "login";
    }
}

