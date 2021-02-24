package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.items.*;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserInformation;
import com.jacky.imagecloud.models.users.UserInformationRepository;
import com.jacky.imagecloud.models.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
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
        user.setEmailAddress(emailAddress);
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
            Item rootItem = new Item();
            User user = new User();
            UserInformation information = new UserInformation();

            user.setEmailAddress(emailAddress);
            user.setName(name);
            user.setPassword(encoder.encode(passWord));
            user.setRootItem(rootItem);

            rootItem.setItemName("root");
            rootItem.setItemType(ItemType.DIR);
            rootItem.setParentID(-1);
            rootItem.setUser(user);

            user.addItem(rootItem);

            information.user = user;

            userRepository.save(user);
            itemRepository.save(rootItem);
            informationRepository.save(information);

            logger.info(String.format("sign up new user->[email:%s][name:%s][rawPassword:%s]", emailAddress,
                    name, passWord));
            return new Result<>(true);

        } catch (Exception e) {
            logger.error("failure to sign up new user :-(", e);
            return new Result<>(e.getMessage());
        }
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
            user.setEmailAddress(authentication.getName());

            var result = userRepository.findOne(Example.of(user));
            if (result.isPresent()) {
                user = result.get();

                if (encoder.matches(oldPassword, user.getPasswordHash()) && !oldPassword.equals(newPassword)) {
                    user.setPassword(encoder.encode(newPassword));
                    userRepository.save(user);
                    logger.info(String.format("User<%s> change password success,new password<%s> ,auto logout", user.getName(), newPassword));
                    new SecurityContextLogoutHandler().logout(request, response, authentication);
                    return new Result<>(Boolean.TRUE);
                }
                logger.info(String.format("User<%s> change password failure,wrong old password or same new password and old password", user.getName()));
                return new Result<>(String.format("User<%s> change password failure,wrong old password or same new password and old password", user.getName()));
            }
            throw new UserNotFoundException(String.format("User<%s> not found", authentication.getName()));
        } catch (Exception e) {
            logger.error(String.format("fail to update the password of User<%s>", authentication.getName()), e);
            return new Result<>(e.getMessage());
        }
    }


}
