package com.jacky.imagecloud.security;

import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogOutSuccessHandle implements LogoutSuccessHandler {
    private Logger logger;

    public LogOutSuccessHandle(Logger logger){
        this.logger = logger;
    }
    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        logger.info(String.format("user<%s> logout success",authentication.getName()));
    }
}
