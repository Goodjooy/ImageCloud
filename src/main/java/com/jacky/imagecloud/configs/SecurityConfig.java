package com.jacky.imagecloud.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration("security")
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoderGenerate(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public ObjectMapper objectMapperGenerate(){
        return new ObjectMapper();
    }
}
