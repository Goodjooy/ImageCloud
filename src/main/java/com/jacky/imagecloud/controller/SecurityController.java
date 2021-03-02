package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.data.VerifyCodeContainer;
import com.jacky.imagecloud.email.EmailSender;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用于spring security 交互响应
 */
@Controller
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SecurityController {
    private final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private final Pattern emailPattern = Pattern.compile("^([a-zA-Z0-9]+([-|.])?)+@([a-zA-Z0-9]+(-[a-zA-Z0-9]+)?\\.)+[a-zA-Z]{2,}$");

    PasswordEncoder encoder = new BCryptPasswordEncoder();
    @Autowired
    UserRepository userRepository;
    @Autowired
    EmailSender sender;

    private final Map<String, VerifyCodeContainer> userVerifies = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    @Scheduled(fixedDelay = 15000)
    public void checkVerifyCode() {
        if (userVerifies.size()==0)return;
        lock.lock();
        logger.info("start check deactivate code");
        var removeKeys = userVerifies.keySet().stream().filter(s -> !userVerifies.get(s).isActivate());
        for (String key :
                removeKeys.collect(Collectors.toSet())) {
            userVerifies.remove(key);
        }
        logger.info("end check deactivate code");
        lock.unlock();
    }

    @GetMapping(path = "/find-password")
    @ResponseBody
    public Result<Boolean> sendVerifyCode(
            @RequestParam(name = "email") String emailAddress
    ) {
        boolean isVerified = User.verifiedUser(userRepository, emailAddress);
        if (!isVerified) {
            return new Result<>(String.format("Not Verify User<%s>", emailAddress));
        }

        var code = VerifyCodeContainer.newVerify();
        lock.lock();
        userVerifies.put(emailAddress, code);
        lock.unlock();

        sender.sendPasswordFinderCode( code.getCode(), emailAddress);
        return new Result<>(true);
    }

    @GetMapping("/session-status")
    @ResponseBody
    public Result<Boolean> SessionStatus(Authentication authentication) {
        if (authentication == null)
            return new Result<>(false);
        else
            return new Result<>(authentication.isAuthenticated());
    }


    @PostMapping(path = "/find-password")
    @ResponseBody
    public Result<Boolean> userVerify(
            @RequestParam(name = "email") String emailAddress,
            @RequestParam(name = "code") String verifyCode,
            @RequestParam(name = "paswd") String newPassword
    ) {
        lock.lock();
        var code = userVerifies.get(emailAddress);
        lock.unlock();
        if (code.match(verifyCode)) {
            User user = User.authUser(emailAddress);
            var result = userRepository.findOne(Example.of(user));
            if (result.isPresent()) {
                user = result.get();
                if (!encoder.matches(newPassword, user.password) && user.information.verify) {
                    user.password = encoder.encode(newPassword);
                    userRepository.save(user);

                    logger.info(String.format("success find back user<%s> [%s]",user.emailAddress,newPassword));
                    return new Result<>(true);
                }
            }
        }
        lock.lock();
        userVerifies.remove(emailAddress);
        lock.unlock();
        logger.info(String.format("failure find back user<%s> password",emailAddress));
        return new Result<>("failure to find back password of USER " + emailAddress);
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
        user.emailAddress = (emailAddress);
        var result = userRepository.findAll(Example.of(user));
        if (result.isEmpty()) {
            logger.info(String.format("Check email: Email<%s> is available", emailAddress));
            return new Result<>(true);
        }
        logger.info(String.format("Check email: Email<%s> was exists", emailAddress));
        return new Result<>(false, false, "email is exist");
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
            User user = User.newUser(name, encoder.encode(passWord), emailAddress);
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
            @RequestParam(name = "uid") String emailAddress,
            @RequestParam(name = "paswd") String password,
            Model model
    ) {
        User user = User.authUser(emailAddress);
        var users = userRepository.findAll(Example.of(user));
        var result = users.stream().filter(user1 -> encoder.matches(password, user1.password));
        model.addAttribute("user", user);
        if (result.toArray().length == 1) {
            model.addAttribute("status", true);
        } else {
            model.addAttribute("status", false);
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
            user.emailAddress = (authentication.getName());

            var result = userRepository.findOne(Example.of(user));
            if (result.isPresent()) {
                user = result.get();

                if (encoder.matches(oldPassword, user.password) && !oldPassword.equals(newPassword)) {
                    user.password = (encoder.encode(newPassword));
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

    @ExceptionHandler(value = NullPointerException.class)
    @ResponseBody
    public ResponseEntity<?>nullPointerHandle(Exception e){
        return ResponseEntity.notFound().build();
    }

}
