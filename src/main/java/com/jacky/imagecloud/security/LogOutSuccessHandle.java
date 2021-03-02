package com.jacky.imagecloud.security;

import com.jacky.imagecloud.data.Info;
import com.jacky.imagecloud.data.LoggerHandle;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogOutSuccessHandle implements LogoutSuccessHandler {
    private final LoggerHandle logger;

    public LogOutSuccessHandle(LoggerHandle logger){
        this.logger = logger;
    }
    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        logger.authenticationSuccess(authentication.getName(), Info.of("User Logout","Operate"));
    }
}
