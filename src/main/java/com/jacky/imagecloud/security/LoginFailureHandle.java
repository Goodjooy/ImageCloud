package com.jacky.imagecloud.security;

import com.jacky.imagecloud.data.LoggerHandle;
import org.slf4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginFailureHandle implements AuthenticationFailureHandler {
    LoggerHandle logger;

    public LoginFailureHandle(LoggerHandle logger) {
        this.logger = logger;
    }

    /**
     * Called when an authentication attempt fails.
     *
     * @param request   the request during which the authentication attempt occurred.
     * @param response  the response.
     * @param exception the exception which was thrown to reject the authentication
     */
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        //失败情况
        logger.authenticationFailure(request.getParameter("uid"),exception);
        var writer = response.getWriter();
        writer.format(
                "{\"data\":null," +
                        "\"err\":true," +
                        "\"message\":\"%s\"}",
                String.format("user <%s> authentication failure", request.getParameter("uid"))
        );
        response.setHeader("Content-Type", "application/json");
    }
}
