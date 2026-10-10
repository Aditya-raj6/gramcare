package com.gramcare.web.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Demo-mode "authentication": the only thing checked is whether a demo role
 * has been selected on the login page and stored in the session. This is
 * enough to drive the hackathon demonstration (different nav items per role,
 * an audit trail of who changed what) but is NOT real authentication or
 * authorization - there is no password check, and a user could set the
 * session attribute directly. Production use would need a real identity
 * provider and per-endpoint authorization, not just a role label.
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object role = request.getSession().getAttribute("demoRole");
        if (role == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}
