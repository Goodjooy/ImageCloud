package com.jacky.imagecloud.security;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacky.imagecloud.data.LoggerHandle;
import com.jacky.imagecloud.data.Result;
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

        ObjectMapper mapper=new ObjectMapper();
        Result<?> result=Result.failureResult(exception);
        String str=mapper.writeValueAsString(result);

        writer.print(
                str
        );
        response.setHeader("Content-Type", "application/json");
    }
}
