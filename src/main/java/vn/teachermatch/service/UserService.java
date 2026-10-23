package vn.teachermatch.service;

import org.springframework.stereotype.Service;
import vn.teachermatch.dto.UserCreateRequest;
import vn.teachermatch.exception.ValidationException;
import vn.teachermatch.model.AppUser;
import vn.teachermatch.model.Role;

import java.util.Collection;
import java.util.List;

@Service
public class UserService implements IUserService {

    private final PortalStore store;

    public UserService(PortalStore store) {
        this.store = store;
    }

    @Override
    public AppUser authenticate(String usernameOrEmail, String password) {
        return store.authenticate(usernameOrEmail, password);
    }

    @Override
    public boolean userExists(String username) {
        return store.userExists(username);
    }

    @Override
    public void registerUser(UserCreateRequest request) {
        if (request == null) {
            throw new ValidationException("User registration request cannot be null");
        }

        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username is required");
        }

        if (store.userExists(username)) {
            throw new ValidationException("User already exists with username: " + username);
        }

        String role = request.getRole();
        if (!Role.isValid(role)) {
            throw new ValidationException("Invalid role specified: " + role);
        }

        store.addUser(username, request.getPassword(), role);
    }

    @Override
    public Collection<AppUser> getAllUsers() {
        return store.users();
    }

    @Override
    public List<AppUser> getPartners() {
        return store.partners();
    }

    @Override
    public String getUserRole(String username) {
        return store.userRole(username);
    }
}

