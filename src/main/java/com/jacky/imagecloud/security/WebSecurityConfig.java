package com.jacky.imagecloud.security;

import com.jacky.imagecloud.models.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    UserRepository userRepository;
    PasswordEncoder encoder = new BCryptPasswordEncoder();
    Logger logger= LoggerFactory.getLogger(WebSecurityConfig.class);

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //身份验证
        auth
                //数据库身份验证
                .userDetailsService(new MySQLUserDetailsService(userRepository,logger)).passwordEncoder(encoder)

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
                .antMatchers("/sign-up","check-email").permitAll()
                .antMatchers("/file/**", "/upload", "/walk").hasAnyRole("USER", "ADMIN")
                .antMatchers("/unchecked/**").hasAnyRole("ADMIN")

                .anyRequest()
                .authenticated()
                .and()

                .formLogin()
                .loginPage("/sign-in")
                .loginProcessingUrl("/sign-in")
                .usernameParameter("uid")
                .passwordParameter("paswd")

                .successHandler(new LoginSuccessHandle(logger))
                .failureHandler(new LoginFailureHandle(logger))
                .permitAll()
                .and()

                .logout()
                .logoutUrl("/sign-out")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .logoutSuccessHandler(new LogOutSuccessHandle(logger))

                .and() //;
                .csrf().disable();

    }
}
