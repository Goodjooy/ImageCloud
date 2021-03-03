package com.jacky.imagecloud.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacky.imagecloud.data.Info;
import com.jacky.imagecloud.data.LoggerHandle;
import com.jacky.imagecloud.data.Result;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginSuccessHandle implements AuthenticationSuccessHandler {
    LoggerHandle logger;
    private final ObjectMapper mapper;

    public LoginSuccessHandle(LoggerHandle logger,ObjectMapper mapper){
        this.logger = logger;
        this.mapper = mapper;
    }
    /**
     * Called when a user has been successfully authenticated.
     *
     * @param request        the request which caused the successful authentication
     * @param response       the response
     * @param chain          the {@link FilterChain} which can be used to proceed other filters in
     *                       the chain
     * @param authentication the <tt>Authentication</tt> object which was created during
     *                       the authentication process.
     * @since 5.2.0
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authentication) throws IOException {
        onAuthenticationSuccess(request, response, authentication);
    }

    /**
     * Called when a user has been successfully authenticated.
     *
     * @param request        the request which caused the successful authentication
     * @param response       the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication
    ) throws IOException {
        //身份验证成功后行为
        logger.authenticationSuccess(authentication.getName(),Info.of("Finish Authentication","message"));

        response.setHeader("Content-Type", "application/json");


        var writer=response.getWriter();
        writer.print(
                mapper.writeValueAsString(Result.okResult(true))
        );
    }
}
