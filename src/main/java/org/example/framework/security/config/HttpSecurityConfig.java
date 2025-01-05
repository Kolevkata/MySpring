package org.example.framework.security.config;

import org.example.framework.core.annotations.Component;
import org.example.framework.core.annotations.Inject;
import org.example.framework.security.session.HttpSessionService;
import org.example.framework.security.session.SessionService;

@Component
public class HttpSecurityConfig {
    private int sessionTimeout;

    public HttpSecurityConfig() {
        this.sessionTimeout = 60 * 60 * 24 * 7;//1 week
    }

    public HttpSecurityConfig(SessionService sessionService, int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public HttpSecurityConfig setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    @Override
    public String toString() {
        return "HttpSecurityConfig{" +
                ", sessionTimeout=" + sessionTimeout +
                '}';
    }
}