package com.jacky.imagecloud.err_config;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErrConfig {
    @Bean
    public ErrorProperties getErrorProperties(){
        var p=new ErrorProperties();
        p.setIncludeMessage(ErrorProperties.IncludeAttribute.ALWAYS);
        p.setIncludeBindingErrors(ErrorProperties.IncludeAttribute.ALWAYS);
        p.setIncludeException(true);
        p.setIncludeStacktrace(ErrorProperties.IncludeStacktrace.ALWAYS);
        return p;
    }
}
