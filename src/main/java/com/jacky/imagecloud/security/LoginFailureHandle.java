package com.jacky.imagecloud.security;

import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginFailureHandle implements AuthenticationFailureHandler {
    Logger logger;

    public LoginFailureHandle(Logger logger) {
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
            AuthenticationException exception) throws IOException, ServletException {
        //失败情况
        //重定向403响应器
        logger.info(String.format("User[%s] authentication failure", request.getParameter("uid")), exception);
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
