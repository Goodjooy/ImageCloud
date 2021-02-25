package com.jacky.imagecloud.security;

import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogOutSuccessHandle implements LogoutSuccessHandler {
    private final Logger logger;

    public LogOutSuccessHandle(Logger logger){
        this.logger = logger;
    }
    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        logger.info(String.format("user<%s> logout success",authentication.getName()));
    }
}
