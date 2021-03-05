package com.jacky.imagecloud.controller.UserDataControllers;

import com.jacky.imagecloud.FileStorage.FileService.FileSystemStorageService;
import com.jacky.imagecloud.FileStorage.FileService.HeadImageStorageService;
import com.jacky.imagecloud.data.Info;
import com.jacky.imagecloud.data.LoggerHandle;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.data.VerifyCodeContainer;
import com.jacky.imagecloud.email.EmailSender;
import com.jacky.imagecloud.err.BaseException;
import com.jacky.imagecloud.err.BaseRuntimeException;
import com.jacky.imagecloud.err.file.StorageException;
import com.jacky.imagecloud.err.user.*;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserImage;
import com.jacky.imagecloud.models.users.UserInformation;
import com.jacky.imagecloud.models.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@Scope(value = "session")
@RequestMapping("/user")
public class UserInformationController {
    LoggerHandle logger = LoggerHandle.newLogger(UserInformationController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    HeadImageStorageService storageService;
    @Autowired
    FileSystemStorageService fileSystemStorageService;

    @Autowired
    EmailSender emailSender;

    ReentrantLock lock = new ReentrantLock();
    VerifyCodeContainer verifyCode;

    @GetMapping(path = "/base")
    public Result<User> getUserFullInfo(Authentication authentication) {
        try {
            User user = User.databaseUser(userRepository, authentication);

            logger.userOperateSuccess(user, "Base Information");
            return Result.okResult(user);
        } catch (Exception e) {
            logger.userOperateFailure(authentication.getName(), "Base Information", e);
            logger.error(String.format("load User<%s> failure", authentication.getName()), e);
            return Result.failureResult(e);
        }
    }

    @GetMapping(path = "/information")
    public Result<UserInformation> getUserInfo(Authentication authentication) {
        try {
            User user = User.databaseUser(userRepository, authentication);
            user.information.checkUsedSize(fileSystemStorageService);
            userRepository.save(user);

            logger.userOperateSuccess(user, "Extra Information");
            return Result.okResult(user.information);
        } catch (UserNotFoundException e) {
            logger.userOperateFailure(authentication.getName(), "Extra Information", e);
            return Result.failureResult(e);
        }
    }

    @GetMapping(path = "/image")
    public Result<UserImage> getUserImage(Authentication authentication) {
        try {
            User user = User.databaseUser(userRepository, authentication);

            logger.userOperateSuccess(user, "Image Information");
            return Result.okResult(user.image);
        } catch (UserNotFoundException e) {
            logger.userOperateFailure(authentication.getName(), "Image Information", e);
            return Result.failureResult(e);
        }
    }

    @PostMapping("/head")
    public Result<Boolean> uploadHead(
            Authentication authentication,
            @RequestParam(name = "file") MultipartFile file
    ) {
        try {
            logger.dataAccept(Info.of(Objects.requireNonNull(file.getOriginalFilename()), "uploadedFile"));

            User user = User.databaseUser(userRepository, authentication);
            UserImage image;
            if (user.image.getSetHeaded())
                image = storageService.storage(file, user.image);
            else
                image = storageService.storage(file);
            user.image.combineImage(image);
            image.setUser(user);

            logger.dataAccept(Info.of(image,"User Image"));
            userRepository.save(user);
            logger.userOperateSuccess(user, "Upload Head Image",
                    Info.of(file.getOriginalFilename(), "Image Name"));
            return Result.okResult(true);
        } catch (UserNotFoundException | StorageException e) {
            logger.userOperateFailure(authentication.getName(),
                    "Upload Head Image",
                    e,
                    Info.of(Objects.requireNonNull(file.getOriginalFilename()),
                            "Image Name"));
            return Result.failureResult(e);
        }
    }

    @GetMapping(path = "/head{size:\\d+}/{filename:.+}")
    public ResponseEntity<Resource> getUserHead(
            Authentication authentication,
            @PathVariable(name = "size") int size,
            @PathVariable(name = "filename") String filename
    ) {
        logger.dataAccept(Info.of(size, "Head Image Size"));
        try {
            User user = User.databaseUser(userRepository, authentication);
            if (!user.image.getFileName().equals(filename))
                throw new NotAllowRequestException(user, filename);

            Resource file = storageService.loadAsResource(filename, size);

            logger.userOperateSuccess(user, "Get Head Image",
                    Info.of(filename, "ImageName"),
                    Info.of(size, "ImageSize"));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + filename + "\"").body(file);
        } catch (NotAllowRequestException | UserNotFoundException e) {
            logger.userOperateFailure(authentication.getName(), "Get Head Image", e,
                    Info.of(filename, "ImageName"),
                    Info.of(size, "ImageSize"));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping(path = "/verify")
    public Result<Boolean> userEmailVerify(
            Authentication authentication
    ) {
        logger.dataAccept(Info.of(authentication.getName(), "User For Verify"));

        try {
            User user = User.databaseUser(userRepository, authentication);
            if (user.information.verify)
                throw new UserVerifiedException(user);

            generateVerifyCode();
            emailSender.sendVerifyCode(verifyCode.getCode(), user.emailAddress);

            logger.userOperateSuccess(user, "Send Verify Email");
            return Result.okResult(Boolean.TRUE);
        } catch (NotEnoughTimeBetweenDifferentVerifyCodeGenerationException | UserNotFoundException | UserVerifiedException e) {
            logger.userOperateFailure(authentication.getName(), "Send Verify Email", e);
            return Result.failureResult(e);
        }
    }

    @PostMapping(path = "/verify")
    public Result<Boolean> verifyCodeCheck(
            Authentication authentication,
            @RequestParam(name = "code") String verifyCode
    ) {
        try {
            logger.dataAccept(Info.of(verifyCode, "Verify code"));

            User user = User.databaseUser(userRepository, authentication);
            var status = this.verifyCode.match(verifyCode);
            if (status) {
                user.information.verify = true;
                userRepository.save(user);

                logger.userOperateSuccess(user, "Verify Email");
                return Result.okResult(true);
            }
            throw new VerifyFailureException(verifyCode);
        } catch (Exception e) {
            logger.userOperateFailure(authentication.getName(), "Verify Email", e);
            return Result.failureResult(e);
        }
    }

    @PostMapping("/rename")
    public Result<String> renameUser(
            @RequestParam(name = "newName") String newName
    ) {
        try {
            User user = User.databaseUser(userRepository);
            String userOldName = user.name;
            user.setUserName(newName);

            userRepository.save(user);

            logger.userOperateSuccess(user, "User Rename",
                    Info.of(userOldName, "OldName"),
                    Info.of(user.name, "NewName"));
            return Result.okResult(user.toString());
        } catch (BaseException | BaseRuntimeException e) {
            logger.userOperateFailure(SecurityContextHolder.getContext().getAuthentication().getName(),
                    "User Rename", e,
                    Info.of(SecurityContextHolder.getContext().getAuthentication().getName(), "OldName"),
                    Info.of(newName, "NewName"));

            return Result.failureResult(e);
        }
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<?> handleException(StorageException e) {
        logger.operateFailure("Storage Server", e);
        return ResponseEntity.notFound().build();
    }


    private void generateVerifyCode() throws NotEnoughTimeBetweenDifferentVerifyCodeGenerationException {
        var now = LocalDateTime.now();
        if (verifyCode != null && verifyCode.noNeedNewGenerate(now))
            throw new NotEnoughTimeBetweenDifferentVerifyCodeGenerationException(verifyCode.deltaTime(now));

        lock.lock();
        verifyCode = VerifyCodeContainer.newVerify();
        lock.unlock();
    }
}
