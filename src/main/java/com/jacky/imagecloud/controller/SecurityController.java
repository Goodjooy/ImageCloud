package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.models.items.*;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserInformation;
import com.jacky.imagecloud.models.users.UserInformationRepository;
import com.jacky.imagecloud.models.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于spring security 交互响应
 */
@Controller
public class SecurityController {
    private Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private final Pattern emailPattern = Pattern.compile("^([a-z0-9A-Z]+[-|\\\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\\\.)+[a-zA-Z]{2,}$");

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

    @PostMapping("/check-email")
    @ResponseBody
    public Result<Boolean> CheckEmailExist(
            @RequestParam(name = "email") String emailAddress
    ) {
        var matcher = emailAddress.matches(emailAddress);
        if (!matcher) {
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

            information.user = user;

            userRepository.save(user);
            itemRepository.save(rootItem);
            informationRepository.save(information);

            logger.info(String.format("sign up new user->[email:%s][name:%s][rawPassword:%s]", emailAddress,
                    name, passWord));
            return new Result<>(true);

        } catch (Exception e) {
            logger.error("failure to sign up new user :-(",e);
            return new Result<>(e.getMessage());
        }
    }


}
