package com.jacky.imagecloud.controller.UserDataControllers;

import com.jacky.imagecloud.FileStorage.FileService.FileSystemStorageService;
import com.jacky.imagecloud.FileStorage.FileService.HeadImageStorageService;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.data.VerifyCodeContainer;
import com.jacky.imagecloud.email.EmailSender;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserImage;
import com.jacky.imagecloud.models.users.UserInformation;
import com.jacky.imagecloud.models.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@Scope(value = "session")
@RequestMapping("/user")
public class UserInformationController {
    Logger logger = LoggerFactory.getLogger(UserInformationController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    HeadImageStorageService storageService;
    @Autowired
    FileSystemStorageService fileSystemStorageService;

    @Autowired
    EmailSender emailSender;

    ReentrantLock lock=new ReentrantLock();
    VerifyCodeContainer verifyCode;

    @GetMapping(path = "/base")
    public Result<User> getUserFullInfo(Authentication authentication) {
        try {
            User user = User.databaseUser(userRepository, authentication);

            logger.info(String.format("load full User Info<%s|%s> success", user.emailAddress, user.name));
            return new Result<>(user);
        } catch (Exception e) {
            logger.error(String.format("load User<%s> failure", authentication.getName()), e);
            return new Result<>(e.getMessage());
        }
    }

    @GetMapping(path = "/information")
    public Result<UserInformation> getUserInfo(Authentication authentication) {
        try {
            User user = User.databaseUser(userRepository, authentication);
            user.information.checkUsedSize(fileSystemStorageService);
            userRepository.save(user);
            logger.info(String.format("load User extra Info<%s|%s> success", user.emailAddress, user.name));
            return new Result<>(user.information);
        } catch (UserNotFoundException e) {
            logger.error(String.format("load User<%s> failure", authentication.getName()), e);
            return new Result<>(e.getMessage());
        }
    }

    @GetMapping(path = "/image")
    public Result<UserImage> getUserImage(Authentication authentication) {
        try {
            User user = User.databaseUser(userRepository, authentication);
            logger.info(String.format("load User Image Info<%s|%s> success", user.emailAddress, user.name));
            return new Result<>(user.image);
        } catch (UserNotFoundException e) {
            logger.error(String.format("load User<%s> failure", authentication.getName()), e);
            return new Result<>(e.getMessage());
        }
    }

    @PostMapping("/head")
    public Result<Boolean> uploadHead(
            Authentication authentication,
            @RequestParam(name = "file") MultipartFile file
    ) {
        try {
            User user = User.databaseUser(userRepository, authentication);
            UserImage image;
            if (user.image.getSetHeaded())
                image = storageService.storage(file, user.image);
            else
                image = storageService.storage(file);
            user.image.combineImage(image);
            image.setUser(user);

            userRepository.save(user);
            logger.info(String.format("success upload head image <%s> for user<%s>", file.getOriginalFilename(), user.emailAddress));
            return new Result<>(true);
        } catch (UserNotFoundException e) {
            logger.error(String.format("fail upload head image <%s> for user<%s>", file.getOriginalFilename(), authentication.getName()
            ), e);
            return new Result<>(e.getLocalizedMessage());
        }
    }

    @GetMapping(path = "/head{size:\\d+}/{filename:.+}")
    public ResponseEntity<Resource> getUserHead(
            Authentication authentication,
            @PathVariable(name = "size") int size,
            @PathVariable(name = "filename") String filename
    ) {
        Resource file = storageService.loadAsResource(filename, size);
        logger.info(String.format("success find head image <%s> for user<%s> | size:%s", filename, authentication.getName(),size));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(file);
    }

    @GetMapping(path = "/verify")
    public Result<Boolean>userEmailVerify(
            Authentication authentication
    ){
        User user=User.databaseUser(userRepository,authentication);
        generateVerifyCode();
        emailSender.sendVerifyCode(verifyCode.getCode(),user.emailAddress);
        return new Result<>(Boolean.TRUE);
    }

    @PostMapping(path = "/verify")
    public Result<Boolean>verifyCodeCheck(
            Authentication authentication,
            @RequestParam(name = "code")String verifyCode
    ){
        try {
            User user = User.databaseUser(userRepository, authentication);
            var status = this.verifyCode.match(verifyCode);
            if (status) {
                user.information.verify = true;
                userRepository.save(user);

                logger.info(String.format("verify email of User<%s> success",user.emailAddress));
                return new Result<>(true);
            }
            logger.info(String.format("verify email of User<%s> failure",user.emailAddress));
            return new Result<>(false);
        }catch (Exception e){
            logger.info(String.format("verify email of User<%s> failure",authentication.getName()),e);
            return new Result<>(e.getMessage());
        }
    }


    private void generateVerifyCode(){
        lock.lock();
        verifyCode=VerifyCodeContainer.newVerify();
        lock.unlock();
    }
}
