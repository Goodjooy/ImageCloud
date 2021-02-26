package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.security.PublicKey;

@RestController
public class EmailSenderController {

    final String from="imagestorage@126.com";

    final String verifyBody="<body>\n" +
            "    <h2>邮箱验证</h2>\n" +
            "    <p>您成功收到这封邮件，请点击<a href=\"http://%s:8080/verify-email-page?email=%s\">验证邮箱</a>以进行验证</p>\n" +
            "</body>";

    @Autowired
    JavaMailSender mailSender;
    @Autowired
    UserRepository repository;

    @GetMapping(path = "/verify-email")
    public Result<Boolean>sendVerifyEmail(
            Authentication authentication
    ){
        var message=mailSender.createMimeMessage();
        var helper=new MimeMessageHelper(message,"UTF-8");
        try {
            User user= User.databaseUser(repository,authentication);

            helper.setSubject(String.format("verify user<%s|%s> email address",user.emailAddress,user.name));
            helper.setText(String.format(verifyBody,"127.0.0.1",user.emailAddress),true);

            helper.setFrom(from);
            helper.setTo(user.emailAddress);

            mailSender.send(message);
            return new Result<>(true);
        } catch (MessagingException | UserNotFoundException e) {
            return  new Result<>(e.getMessage());
        }

    }

}
