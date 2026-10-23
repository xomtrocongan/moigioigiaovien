package vn.teachermatch.service;

import vn.teachermatch.dto.UserCreateRequest;
import vn.teachermatch.model.AppUser;

import java.util.Collection;
import java.util.List;

public interface IUserService {
    AppUser authenticate(String usernameOrEmail, String password);
    boolean userExists(String username);
    void registerUser(UserCreateRequest request);
    Collection<AppUser> getAllUsers();
    List<AppUser> getPartners();
    String getUserRole(String username);
}

