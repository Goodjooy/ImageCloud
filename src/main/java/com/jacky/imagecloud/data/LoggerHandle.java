package com.jacky.imagecloud.data;

import com.jacky.imagecloud.FileStorage.FileService.FileUploader;
import com.jacky.imagecloud.err.BaseException;
import com.jacky.imagecloud.err.BaseRuntimeException;
import com.jacky.imagecloud.models.items.Item;
import com.jacky.imagecloud.models.users.User;
import com.sun.istack.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Map;

public class LoggerHandle {
    private Logger logger;

    public static LoggerHandle newLogger(String name) {
        var logger = new LoggerHandle();
        logger.logger = LoggerFactory.getLogger(name);
        return logger;
    }

    public static LoggerHandle newLogger(Class<?> className) {
        var logger = new LoggerHandle();
        logger.logger = LoggerFactory.getLogger(className);
        return logger;
    }

    public void info(@NotNull String format, Object... objects) {
        var s = String.format(format, objects);
        logger.info(s);
    }

    public void error(Throwable e, @NotNull String format, Object... objects) {
        var s = String.format(format, objects);
        logger.error(s, e);
    }

    public void error(String message, Throwable e) {
        if(e==null)
            logger.error(message);
        if (e instanceof BaseException || e instanceof BaseRuntimeException) {
            logger.error(message + "| Exception<" + e.getClass().getName() + ">: " + e.getMessage());
        } else
            logger.error(message, e);
    }

    public void dataAccept(Info<?> info) {
        logger.info(String.format("`Accept Data` [%s<%s>:%s]"
                , info.name, info.valueType, info.value
        ));
    }

    private String extraInformation(Info<?>... infoList) {
        var builder = new StringBuilder();
        for (Info<?> info : infoList) {
            builder.append(String.format(" [%s<%s>: %s]",
                    info.name, info.valueType.getName(), info.value));
        }
        return builder.toString();
    }

    //UserFileLogger
    public void uploadSuccess(User user, MultipartFile file, String path, Info<?>... infoList) {
        var information = String.format(
                "`Upload` Success ! | File<%s | %s> | Path<%s> | User<%s | %s> |%s"
                , file.getOriginalFilename()
                , FileUploader.formatSize(file.getSize()), path
                , user.name, user.emailAddress, extraInformation(infoList)
        );
        logger.info(information);
    }

    public void findRemovedTreeSuccess(User user, Map<String, Item> items, Info<?>... infoList) {
        var information = String.format(
                "`Find Flat Deleted Items` Success ! | ItemCount<%s> | User<%s | %s> |%s",
                items.size(),
                user.name, user.emailAddress, extraInformation(infoList)
        );
        logger.info(information);
    }

    public void findSuccess(User user, String path, Item targetItem, Info<?>... infoList) {
        var information = String.format(
                "`Find Item` Success ! | Path<%s> | Item<name: %s | type: %s | hidden: %s> | User<%s | %s> |%s", path,
                targetItem.getItemName(), targetItem.getItemType(), targetItem.hidden,
                user.name, user.emailAddress, extraInformation(infoList)
        );
        logger.info(information);
    }

    public void deleteSuccess(User user, String path, Item targetItem, Info<?>... infoList) {
        var information = String.format(
                "`Delete Item` Success ! | Path<%s> | Item<name: %s | type: %s | hidden: %s> | User<%s | %s> |%s",
                path,
                targetItem.getItemName(), targetItem.getItemType(), targetItem.hidden,
                user.name, user.emailAddress, extraInformation(infoList)
        );
        logger.info(information);
    }

    public void createSuccess(User user, String path, Info<?>... infoList) {
        var information = String.format(
                "`Create Items` Success ! | path<%s> | User<%s | %s> |%s",
                path,
                user.name, user.emailAddress, extraInformation(infoList)
        );
        logger.info(information);
    }

    public void fileOperateSuccess(String user, String operate, Info<?>... infoList) {
        var information = String.format(
                "`%s Items` Success ! | User<%s> |%s",
                operate,
                user, extraInformation(infoList)
        );
        logger.info(information);
    }

    public void fileOperateSuccess(User user, String operate, Info<?>... infoList) {
        var information = String.format(
                "`%s items` Success ! | User<%s | %s> |%s",
                operate,
                user.name, user.emailAddress, extraInformation(infoList)
        );
        logger.info(information);
    }

    //user Information
    public void userOperateSuccess(User user, String messageName, Info<?>... extraInfo) {
        logger.info(String.format(
                "`Load User Information` Success | InformationType<%s> | User<%s | %s> |%s",
                messageName, user.name, user.emailAddress, extraInformation(extraInfo)
        ));

    }

    public void userOperateSuccess(String user, String messageName, Info<?>... extraInfo) {
        logger.info(String.format(
                "`Load User Information` Success | InformationType<%s> | User<%s> |%s",
                messageName, user, extraInformation(extraInfo)
        ));

    }

    public void userOperateFailure(String user, String messageName, Throwable throwable, Info<?>... extraInfo) {
        error(String.format(
                "`Load User Information` Failure | InformationType<%s> | User<%s> |%s",
                messageName, user, extraInformation(extraInfo)
        ), throwable);
    }

    public void userOperateFailure(String user, String messageName, Info<?>... extraInfo) {
        error(String.format(
                "`Load User Information` Failure | InformationType<%s> | User<%s> |%s",
                messageName, user, extraInformation(extraInfo)
        ), null);
    }

    public void storageFileOperateSuccess(String filename, String action, Info<?>... extraInfo) {
        logger.info(String.format(
                "`%s Storage File` Success | filename: %s |%s"
                , action, filename, extraInformation(extraInfo)
        ));
    }

    public void storageFileOperateFailure(String filename, String action, Throwable throwable, Info<?>... extraInfo) {
        error(String.format(
                "`%s Storage File` Failure | filename: %s |%s"
                , action, filename, extraInformation(extraInfo)
        ), throwable);
    }

    public void storageFileOperateFailure(Throwable throwable, Info<?>... extraInfo) {
        error(String.format(
                "`Operation Storage File` Failure |%s"
                , extraInformation(extraInfo)
        ), throwable);
    }
    public void storageFileOperateFailure(Throwable throwable, ServletRequest request, ServletResponse response, Info<?>... extraInfo) {

        error(String.format(
                "`Operation Storage File` Failure |%s"
                , extraInformation(extraInfo)
        ), throwable);
    }

    public void authenticationSuccess(String username, Info<?>... extraInfo) {
        logger.info(String.format(
                "`User Authentication Operation` Success | User<%s> |%s",
                username, extraInformation(extraInfo)
        ));
    }

    public void authenticationFailure(String username, Throwable throwable, Info<?>... extraInfo) {
        error(String.format(
                "`User Authentication Operation` Failure | User<%s> |%s",
                username, extraInformation(extraInfo)
        ), throwable);
    }

    public void authenticationFailure(String username, Info<?>... extraInfo) {
        error(String.format(
                "`User Authentication Operation` Failure | User<%s> |%s",
                username, extraInformation(extraInfo)
        ), null);
    }

    public void securityOperateSuccess(String operate, Info<?>... extraInfo) {
        logger.info(String.format(
                "`%s | Security` Success |%s",
                operate, extraInformation(extraInfo)
        ));
    }

    public void securityOperateFailure(String operate, Throwable throwable, Info<?>... extraInfo) {
        error(String.format(
                "`%s | Security` Failure |%s",
                operate, extraInformation(extraInfo)
        ), throwable);
    }

    public void operateFailure(String description, Info<?>... infoList) {
        error(String.format("`%s` Failure|%s", description, extraInformation(infoList)), null);
    }

    public void operateFailure(String description, Exception exception, Info<?>... infoList) {
        error(String.format("`%s` Failure|%s", description, extraInformation(infoList)), exception);
    }

    public void operateFailure(String description, Exception exception, Authentication authentication, Info<?>... infoList) {
        error(String.format("`%s` Failure |User<%s> |%s", description, authentication.getName(), extraInformation(infoList)), exception);
    }

    public void operateFailure(String description, Authentication authentication, Info<?>... infoList) {
        error(String.format("`%s` Failure |User<%s> |%s", description, authentication.getName(), extraInformation(infoList)), null);
    }

    public void error(Throwable exception) {
        error(exception.getMessage(), exception);
    }
}
