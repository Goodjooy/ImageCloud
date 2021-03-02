package com.jacky.imagecloud.data;

import com.jacky.imagecloud.FileStorage.FileService.FileUploader;
import com.jacky.imagecloud.models.items.Item;
import com.jacky.imagecloud.models.users.User;
import com.sun.istack.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

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

    public void error(@NotNull String format, Object... objects) {
        var s = String.format(format, objects);
        logger.error(s);
    }

    public void error(Throwable e, @NotNull String format, Object... objects) {
        var s = String.format(format, objects);
        logger.error(s, e);
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

    public void findSuccess(User user,String path, Item targetItem, Info<?>... infoList) {
        var information = String.format(
                "`Find Item` Success ! | Path<%s> | Item<name: %s | type: %s | hidden: %s> | User<%s | %s> |%s",path,
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
                "Load User Information Success | User<%s | %s>| InformationType<%s> |%s",
                user.name, user.emailAddress, messageName, extraInformation(extraInfo)
        ));

    }

    public void userOperateFailure(String user, String messageName, Throwable throwable, Info<?>... extraInfo) {
        logger.info(String.format(
                "Load User Information Success | User<%s>| InformationType<%s> |%s",
                user, messageName, extraInformation(extraInfo)
        ), throwable);
    }

    public void userOperateFailure(String user, String messageName, Info<?>... extraInfo) {
        logger.info(String.format(
                "Load User Information Success | User<%s>| InformationType<%s> |%s",
                user, messageName, extraInformation(extraInfo)
        ));
    }

    public void fileOperateSuccess(String filename) {//TODO: 文件操作成功日志
    }

    public void fileOperateFailure(String filename) {
        //TODO: 文件操作失败日志
    }

    public void authenticationSuccess(String username, Info<?>... extraInfo) {
        logger.info(String.format(
                "`User Authentication Operation` Success | User<%s> |%s",
                username, extraInformation(extraInfo)
        ));
    }

    public void authenticationFailure(String username, Throwable throwable, Info<?>... extraInfo) {
        logger.error(String.format(
                "`User Authentication Operation` Failure | User<%s> |%s",
                username, extraInformation(extraInfo)
        ), throwable);
    }

    public void authenticationFailure(String username, Info<?>... extraInfo) {
        logger.error(String.format(
                "`User Authentication Operation` Failure | User<%s> |%s",
                username, extraInformation(extraInfo)
        ));
    }


    public void operateFailure(String description, Info<?>... infoList) {
        logger.error(String.format("`%s` Failure|%s", description, extraInformation(infoList)));
    }

    public void operateFailure(String description, Exception exception, Info<?>... infoList) {
        logger.error(String.format("`%s` Failure|%s", description, extraInformation(infoList)), exception);
    }

    public void operateFailure(String description, Exception exception, Authentication authentication, Info<?>... infoList) {
        logger.error(String.format("`%s` Failure |User<%s> |%s", description, authentication.getName(), extraInformation(infoList)), exception);
    }

    public void operateFailure(String description, Authentication authentication, Info<?>... infoList) {
        logger.error(String.format("`%s` Failure |User<%s> |%s", description, authentication.getName(), extraInformation(infoList)));
    }

    public void error(Throwable exception) {
        logger.error(exception.getMessage(), exception);
    }
}
