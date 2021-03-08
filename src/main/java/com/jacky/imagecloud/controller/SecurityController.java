package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.data.Info;
import com.jacky.imagecloud.data.LoggerHandle;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.data.VerifyCodeContainer;
import com.jacky.imagecloud.email.EmailSender;
import com.jacky.imagecloud.err.BaseException;
import com.jacky.imagecloud.err.BaseRuntimeException;
import com.jacky.imagecloud.err.user.*;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用于spring security 交互响应
 */
@Controller
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SecurityController {
    private final LoggerHandle logger = LoggerHandle.newLogger(SecurityController.class);
    private final Pattern emailPattern = Pattern.compile("^([a-zA-Z0-9]+([-|.])?)+@([a-zA-Z0-9]+(-[a-zA-Z0-9]+)?\\.)+[a-zA-Z]{2,}$");

    PasswordEncoder encoder = new BCryptPasswordEncoder();
    @Autowired
    UserRepository userRepository;
    @Autowired
    EmailSender sender;

    private final Map<String, VerifyCodeContainer> userVerifies = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    @Scheduled(fixedDelay = 30000)
    public void checkVerifyCode() {
        //logger.dataAccept(Info.of(userVerifies,"Verify Record"));

        if (userVerifies.size() == 0) return;
        lock.lock();
        var removeKeys = userVerifies.keySet().stream().filter(s -> !userVerifies.get(s).isActivate());
        for (String key :
                removeKeys.collect(Collectors.toSet())) {
            userVerifies.remove(key);
        }
        lock.unlock();

        logger.securityOperateSuccess("Check Verify Codes Status");
    }

    @GetMapping("/session-status")
    @ResponseBody
    public Result<Boolean> SessionStatus(HttpServletRequest request) {
        var context=SecurityContextHolder.getContext();

        Authentication authentication=context.getAuthentication();
        boolean result= authentication != null && (authentication.isAuthenticated()&&request.getSession(false)!=null);
        logger.securityOperateSuccess("Get User Authentication Status | Authentication Status :"+result);
        return Result.okResult(result);
    }

    @GetMapping(path = "/find-password")
    @ResponseBody
    public Result<Boolean> userFindPasswordEmailSend(
            @RequestParam(name = "email") String emailAddress
    ) {

        try {

            boolean isVerified = User.verifiedUser(userRepository, emailAddress);
            if (!isVerified)
                throw new UserNotVerifiedException(emailAddress);
            var old = userVerifies.get(emailAddress);
            var now = LocalDateTime.now();
            if (old != null && old.noNeedNewGenerate(now))
                throw new NotEnoughTimeBetweenDifferentVerifyCodeGenerationException(old.deltaTime(now));

            var code = VerifyCodeContainer.newVerify();
            lock.lock();
            userVerifies.put(emailAddress, code);
            lock.unlock();

            sender.sendPasswordFinderCode(code.getCode(), emailAddress);

            logger.securityOperateSuccess("Send Find Password Verify Email", Info.of(emailAddress, "Target Email"));
            return Result.okResult(true);
        } catch (BaseException | BaseRuntimeException e) {
            logger.securityOperateFailure("Send Find Password Verify Email", e, Info.of(emailAddress, "Target Email"));
            return Result.failureResult(e);
        }
    }


    @PostMapping(path = "/find-password")
    @ResponseBody
    public Result<Boolean> userFindBackPassword(
            @RequestParam(name = "email") String emailAddress,
            @RequestParam(name = "code") String verifyCode,
            @RequestParam(name = "paswd") String newPassword
    ) {
        lock.lock();
        var code = userVerifies.get(emailAddress);
        lock.unlock();
        try {

            User user = User.databaseUser(userRepository, emailAddress);

            if (code == null)
                throw new VerifyFailureException(verifyCode, "Verify code send User Not found");
            if (!code.match(verifyCode))
                throw new VerifyFailureException(verifyCode);
            if (encoder.matches(newPassword, user.password))
                throw new SamePasswordException();
            if (!user.information.verify)
                throw new UserNotVerifiedException(user.emailAddress);
            User.userPasswordCheck(newPassword);

            lock.lock();
            userVerifies.remove(emailAddress);
            lock.unlock();

            user.password = encoder.encode(newPassword);
            userRepository.save(user);

            logger.securityOperateSuccess("Find Back User Password", Info.of(user.emailAddress, "User")
                    , Info.of(newPassword, "new Password"));
            return Result.okResult(true);
        } catch (BaseException | BaseRuntimeException e) {
            logger.securityOperateFailure("Find Back User Password", e, Info.of(emailAddress, "User"));
            return Result.failureResult(e);
        }
    }

    @PostMapping("/check-email")
    @ResponseBody
    public Result<Boolean> CheckEmailExist(
            @RequestParam(name = "email") String emailAddress
    ) {
        try {
            var matcher = emailPattern.matcher(emailAddress);
            if (!matcher.matches())
                throw new EmailAddressNotSupportException(emailAddress, "Bad Email Pattern");
            User user = User.authUser(emailAddress);
            var result = userRepository.findAll(Example.of(user));
            if (!result.isEmpty())
                throw new EmailAddressNotSupportException(emailAddress, "Email Was Used");

            logger.securityOperateSuccess("Check Email", Info.of(emailAddress, "Email"));
            return Result.okResult(true);
        } catch (EmailAddressNotSupportException e) {
            logger.securityOperateFailure("Check Email", e, Info.of(emailAddress, "Email"));
            return Result.failureResult(e);
        }
    }

    @PostMapping(path = "/sign-up")
    @ResponseBody
    public Result<Boolean> postSignUp(
            @RequestParam(name = "uid") String emailAddress,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "paswd") String passWord
    ) {
        try {
            var check = CheckEmailExist(emailAddress);
            if (check.err)
                throw new EmailAddressNotSupportException(emailAddress, check.e);
            User.userNameCheck(name);
            User.userPasswordCheck(passWord);

            User user = User.newUser(name, encoder.encode(passWord), emailAddress);
            userRepository.save(user);

            logger.securityOperateSuccess("Sign Up New User",
                    Info.of(emailAddress, "UserEmail"),
                    Info.of(name, "UserName"),
                    Info.of(passWord, "RawPassword"));
            return Result.okResult(true);
        } catch (EmailAddressNotSupportException | BadUserInformationException e) {
            logger.securityOperateFailure("Sign Up New User", e,
                    Info.of(emailAddress, "UserEmail"),
                    Info.of(name, "UserName"),
                    Info.of(passWord, "RawPassword"));
            return Result.failureResult(e);
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
            User user = User.databaseUser(userRepository, authentication);
            if (!encoder.matches(oldPassword, user.password))
                throw new ResetPasswordFailureException("Bad Old Password");
            User.userPasswordCheck(newPassword);
            if (oldPassword.equals(newPassword))
                throw new SamePasswordException();

            user.password = (encoder.encode(newPassword));
            userRepository.save(user);
            new SecurityContextLogoutHandler().logout(request, response, authentication);

            logger.securityOperateSuccess("Reset User Password",
                    Info.of(user, "User"),
                    Info.of(newPassword, "NewPassword"));
            return Result.okResult(Boolean.TRUE);
        } catch (Exception e) {
            logger.securityOperateFailure("Reset User Password", e, Info.of(authentication.getName(),
                    "User"), Info.of(newPassword, "NewPassword"));
            return Result.failureResult(e.getMessage());
        }
    }

    @ExceptionHandler(value = NullPointerException.class)
    @ResponseBody
    public ResponseEntity<?> nullPointerHandle(Exception e) {
        return ResponseEntity.notFound().build();
    }

}
