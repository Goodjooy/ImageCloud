package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.items.*;
import com.jacky.imagecloud.models.users.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于spring security 交互响应
 */
@Controller
public class SecurityController {
    private final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private final Pattern emailPattern = Pattern.compile("^([a-zA-Z0-9]+([-|.])?)+@([a-zA-Z0-9]+(-[a-zA-Z0-9]+)?\\.)+[a-zA-Z]{2,}$");

    PasswordEncoder encoder = new BCryptPasswordEncoder();
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserInformationRepository informationRepository;
    @Autowired
    private UserImageRepository imageRepository;

    @GetMapping("/sign-in")
    public String getSignInPage() {
        return "sign_in";
    }

    @GetMapping("/sign-up")
    public String getSignUpPage() {
        return "sign-up";
    }

    @GetMapping("/reset-paswd")
    public String resetPassword() {
        return "reset-paswd";
    }

    @GetMapping("/verify-email-page")
    public String getVerifyPage(Model model,@RequestParam(name = "email")String emailAddress){
        model.addAttribute("email",emailAddress);
        return "verify-email";
    }

    @PostMapping("/check-email")
    @ResponseBody
    public Result<Boolean> CheckEmailExist(
            @RequestParam(name = "email") String emailAddress
    ) {
        var matcher = emailPattern.matcher(emailAddress);
        if (!matcher.matches()) {
            return new Result<>("bad email address");
        }
        User user = new User();
        user.emailAddress=(emailAddress);
        var result = userRepository.findAll(Example.of(user));
        if (result.isEmpty()) {
            logger.info(String.format("Check email: Email<%s> is available", emailAddress));
            return new Result<>(true);
        }
        logger.info(String.format("Check email: Email<%s> was exists", emailAddress));
        return new Result<>("email is exist");
    }

    @PostMapping(path = "/sign-up")
    @ResponseBody
    public Result<Boolean> postSignUp(
            @RequestParam(name = "uid") String emailAddress,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "paswd") String passWord
    ) {
        var check = CheckEmailExist(emailAddress);
        if (check.err) {
            return new Result<>(check.message);
        }
        if (passWord.length() < 6 || passWord.length() > 32)
            return new Result<>("password length out of size [6,32]");
        if (name.length() > 16 || name.length() == 0)
            return new Result<>("user `name` length out of size [1,16]");
        try {
            User user = User.newUser( name,encoder.encode(passWord),emailAddress);
            userRepository.save(user);

            logger.info(String.format("sign up new user->[email:%s][name:%s][rawPassword:%s]", emailAddress,
                    name, passWord));
            return new Result<>(true);

        } catch (Exception e) {
            logger.error("failure to sign up new user :-(", e);
            return new Result<>(e.getMessage());
        }
    }

    @PostMapping(path = "/verify-email")
    public String verifyEmail(
            @RequestParam(name = "uid")String emailAddress,
            @RequestParam(name = "paswd")String password,
            Model model
    ){
        User user=User.authUser(emailAddress);
        var users=userRepository.findAll(Example.of(user));
        var result=users.stream().filter(user1 -> encoder.matches(password,user1.password));
        model.addAttribute("user",user);
        if(result.toArray().length==1){
            model.addAttribute("status",true);
        }else {
            model.addAttribute("status",false);
        }
        return "verify-done";
    }


    @PostMapping(value = "/reset-paswd")
    @ResponseBody
    public Result<Boolean> resetPassword(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(name = "old") String oldPassword,
            @RequestParam(name = "new") String newPassword
    ) {
        try {
            //check user
            User user = new User();
            user.emailAddress=(authentication.getName());

            var result = userRepository.findOne(Example.of(user));
            if (result.isPresent()) {
                user = result.get();

                if (encoder.matches(oldPassword, user.password) && !oldPassword.equals(newPassword)) {
                    user.password=(encoder.encode(newPassword));
                    userRepository.save(user);
                    logger.info(String.format("User<%s> change password success,new password<%s> ,auto logout", user.name, newPassword));
                    new SecurityContextLogoutHandler().logout(request, response, authentication);
                    return new Result<>(Boolean.TRUE);
                }
                logger.info(String.format("User<%s> change password failure,wrong old password or same new password and old password", user.name));
                return new Result<>(String.format("User<%s> change password failure,wrong old password or same new password and old password", user.name));
            }
            throw new UserNotFoundException(String.format("User<%s> not found", authentication.getName()));
        } catch (Exception e) {
            logger.error(String.format("fail to update the password of User<%s>", authentication.getName()), e);
            return new Result<>(e.getMessage());
        }
    }


}
