package com.jacky.imagecloud.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;

@Service
public class EmailSender {
    final String from = "imagestorage@126.com";
    @Autowired
    JavaMailSender mailSender;

    Logger logger = LoggerFactory.getLogger(EmailSender.class);

    final String verifyInfo = "your verify code is<h2>%s</h2><br><h3>message:</h3><p> If You Never Known About the ImageCloud ,Ignore This Email<br>" +
            "The Email is for User`%s`</p>";

    public void sendEmail(String title, String sendMessage, boolean htmlType, String to) {
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, "UTF-8");
        try {
            helper.setSubject(title);
            helper.setText(sendMessage, htmlType);

            helper.setFrom(from);
            helper.setTo(to);

            logger.info(String.format("send email to <%s> success [%s]", to, title));
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error(String.format("send email to <%s> failure [%s]", to, title), e);
        }
    }

    public void sendVerifyCode(String code, String email) {
        sendEmail("Verify Code", String.format(verifyInfo, code, email), true, email);
    }

    public void sendPasswordFinderCode(String code, String email) {
        sendEmail("Find Password Verify Code",
                String.format(verifyInfo, code, email), true, email);
    }
}
