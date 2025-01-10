package org.example.app.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.framework.core.annotations.Inject;
import org.example.framework.core.annotations.Service;
import org.example.framework.security.session.SessionService;
import org.example.framework.security.user.Authority;
import org.example.framework.security.user.UserDetails;

@Service
public class UserService {
    @Inject
    private SessionService sessionService;

    public boolean login(HttpServletRequest request, String username, String password) {
        if (!password.equals("123") && !password.equals("admin")) {
            return false;
        }
        UserDetails userDetails = new UserDetails();
        userDetails.setName(username);
        userDetails.getAuthorities().add(new Authority("ROLE_USER"));
        if (password.equals("admin")) {
            userDetails.getAuthorities().add(new Authority("ROLE_ADMIN"));
        }
        sessionService.createSession(request, userDetails);
        return true;
    }
}
