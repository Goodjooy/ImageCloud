package com.jacky.imagecloud.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacky.imagecloud.data.LoggerHandle;
import com.jacky.imagecloud.models.users.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    ObjectMapper mapper;
    LoggerHandle logger = LoggerHandle.newLogger(WebSecurityConfig.class);

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //身份验证
        auth
                //数据库身份验证
                .userDetailsService(new MySQLUserDetailsService(userRepository, logger)).passwordEncoder(encoder)

                .and()

                //内存身份验证
                .inMemoryAuthentication()
                .withUser("12345").password("12345").roles("USER")
                .and().passwordEncoder(new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encodedPassword.equals(rawPassword.toString());
            }
        })
        ;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                        "/test/**",
                        "/sign-up",
                        "/check-email",
                        "/sharePort",
                        "/verify-email-page",
                        "/verify-email",
                        "/session-status",
                        "/find-password",
                        "/user-find-password").permitAll()
                .antMatchers("/file", "/upload", "/walk", "/dir").hasAnyRole("USER", "ADMIN")
                .antMatchers("/unchecked/**", "/admin/**").hasAnyRole("ADMIN")

                .anyRequest()
                .authenticated()
                .and()

                .formLogin()
                .loginPage("/test/sign-in")
                .loginProcessingUrl("/sign-in")
                .usernameParameter("uid")
                .passwordParameter("paswd")

                .successHandler(new LoginSuccessHandle(logger,mapper))
                .failureHandler(new LoginFailureHandle(logger,mapper))
                .permitAll()
                .and()

                .logout()
                .logoutUrl("/sign-out")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .logoutSuccessHandler(new LogOutSuccessHandle(logger))

                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .and()

                .and()
                .rememberMe()
                .tokenValiditySeconds(12 * 3600 * 60)

                .and()
                .cors()
                .configurationSource(request -> {
                    CorsConfiguration configuration=new CorsConfiguration();
                    configuration.addAllowedMethod(HttpMethod.GET);
                    configuration.addAllowedOrigin("https://www.bilibili.com");
                    return  configuration;
                })

                .and() //;
                .csrf().disable()

        ;

    }
}
