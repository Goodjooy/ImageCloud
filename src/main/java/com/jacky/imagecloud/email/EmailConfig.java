package com.jacky.imagecloud.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@ConfigurationProperties("email")
public class EmailConfig {
    @Bean
    public JavaMailSender getMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost("smtp.126.com");
        mailSender.setPort(25);

        mailSender.setUsername("imagestorage@126.com");
        mailSender.setPassword("ARUPFZMLULNSHFOI");

        mailSender.setDefaultEncoding("UTF-8");

        var props=mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}