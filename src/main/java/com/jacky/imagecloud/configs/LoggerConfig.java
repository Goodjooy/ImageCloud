package com.jacky.imagecloud.configs;

import com.jacky.imagecloud.data.LoggerHandle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerConfig {
    public LoggerHandle loggerHandle(Class<?> classIn){
        return LoggerHandle.newLogger(classIn);
    }
}
