package org.example.framework.security.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.framework.core.annotations.Component;
import org.example.framework.core.annotations.Inject;
import org.example.framework.core.annotations.OnInit;
import org.example.framework.security.config.HttpSecurityConfig;
import org.example.framework.security.user.UserDetails;

@Component
public class HttpSessionService implements SessionService{
    @Inject
    HttpSecurityConfig httpSecurityConfig;
    @Override
    public void createSession(HttpServletRequest request, UserDetails userDetails) {
        HttpSession session = request.getSession(true);  // Create a new session or get the existing one
        session.setAttribute("user", userDetails);  // Store UserDetails in session
    }

    @Override
    public UserDetails getUserDetailsFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);  // Create a new session or get the existing one
        if (session == null) {
            return null;
        }
        Object userDetails =  session.getAttribute("user");
        if (userDetails != null) {
            return (UserDetails) userDetails;
        }
        return null;
    }

    @Override
    public void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);  // Create a new session or get the existing one
        if (session != null) {
            session.invalidate();
        }
    }
}
