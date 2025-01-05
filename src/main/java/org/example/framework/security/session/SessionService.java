package org.example.framework.security.session;

import jakarta.servlet.http.HttpServletRequest;
import org.example.framework.core.annotations.Component;
import org.example.framework.core.annotations.Inject;
import org.example.framework.security.config.HttpSecurityConfig;
import org.example.framework.security.user.UserDetails;

public interface SessionService {
    void createSession(HttpServletRequest request, UserDetails userDetails);
    UserDetails getUserDetailsFromSession(HttpServletRequest request);
    void invalidateSession(HttpServletRequest request);
}
