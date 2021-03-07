package com.jacky.imagecloud.controller.UserDataControllers;

import com.jacky.imagecloud.data.Info;
import com.jacky.imagecloud.data.LoggerHandle;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.BaseException;
import com.jacky.imagecloud.err.BaseRuntimeException;
import com.jacky.imagecloud.err.item.ItemExistException;
import com.jacky.imagecloud.err.item.ItemNotFoundException;
import com.jacky.imagecloud.err.user.UserNotFoundException;
import com.jacky.imagecloud.models.items.Item;
import com.jacky.imagecloud.models.items.ItemRepository;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserFileOperateController {
    LoggerHandle logger = LoggerHandle.newLogger(UserFileOperateController.class);
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;

    @PostMapping(path = "/rename")
    public Result<String> fileRename(Authentication authentication,
                                     @RequestParam(name = "oldPath") String oldFilePath,
                                     @RequestParam(name = "newName", defaultValue = "") String newName) {
        logger.dataAccept(Info.of(oldFilePath, "Old Path"));
        try {
            User user = User.databaseUser(userRepository, authentication, true, false);
            var target = user.rootItem.getTargetItem(oldFilePath, true);
            if (!user.targetItemNameSupport(newName, target.getParentID()))
                throw new ItemExistException(oldFilePath.substring(0, oldFilePath.lastIndexOf("/")), newName);

            target.setItemName(newName);
            itemRepository.save(target);

            logger.fileOperateSuccess(authentication.getName(), "Rename File",
                    Info.of(oldFilePath, "oldName"),
                    Info.of(newName, "newName"));
            return Result.okResult(newName);

        } catch (BaseException | BaseRuntimeException e) {
            logger.operateFailure("Rename File", e,
                    authentication,
                    Info.of(oldFilePath, "oldName"),
                    Info.of(newName, "newName"));
            return Result.failureResult(e);
        }
    }

    @PostMapping(path = "/file-status")
    public Result<List<Result<Boolean>>> fileStatusChange(
            Authentication authentication,
            @RequestParam(name = "path") String[] targetPaths
    ) {
        logger.dataAccept(Info.of(List.of(targetPaths), "TargetPathToChangeHiddenStatus"));
        try {
            var user = User.databaseUser(userRepository, authentication, true, false);
            Function<String, Result<Boolean>> operator = targetPath ->
            {
                Item target;
                try {
                    user.resetItemsStatus();
                    target = user.rootItem.getTargetItem(targetPath, true);

                    target.reverseHidden();
                    itemRepository.save(target);

                    logger.fileOperateSuccess(user, "Change Status",
                            Info.of(targetPath, "targetPath"),
                            Info.of(target.getHidden(), "hiddenStatus"));
                    return Result.okResult(true);
                } catch (BaseException | BaseRuntimeException e) {
                    logger.operateFailure("Change Status", e,
                            authentication,
                            Info.of(targetPath, "targetPath"));
                    return Result.failureResult(e);
                }
            };
            var result = Stream.of(targetPaths).map(operator);

            userRepository.save(user);
            return Result.okResult(result.collect(Collectors.toList()));
        } catch (UserNotFoundException | ItemNotFoundException e) {
            logger.operateFailure("Change File Hidden Status", e, authentication,
                    Info.of(List.of(targetPaths), "paths"));
            return Result.failureResult(e);
        }
    }

    @PostMapping(path = "/restore-file")
    public Result<Boolean> restoreFile(
            Authentication authentication,
            @RequestParam(name = "path") String targetPath
    ) {
        try {
            var user = User.databaseUser(userRepository, authentication, true, true);
            var target = user.rootItem.getTargetItem(targetPath, true);
            user.constructItem(true, false);
            if (!user.targetItemNameSupport(target.getItemName(), target.getParentID()))
                throw new ItemExistException(targetPath);

            target.setRemoved(false);
            userRepository.save(user);
            itemRepository.save(target);

            logger.fileOperateSuccess(user, "Restore File", Info.of(targetPath, "targetPath"));
            return Result.okResult(Boolean.TRUE);
        } catch (BaseException | BaseRuntimeException e) {
            logger.operateFailure("Restore File", authentication, Info.of(targetPath, "TargetPath"));
            return Result.failureResult(e);
        }
    }

    @PostMapping(value = "/move")
    public Result<List<Result<Boolean>>> moveItems(
            @RequestParam("origins") String[] paths,
            @RequestParam("target") String targetPath,
            @RequestParam(value = "sameNameOK", defaultValue = "true") boolean sameNameOK
    ) {
        try {
            User user = User.databaseUser(userRepository);
            user.constructItem(true, true);

            Item targetItem = user.rootItem.getTargetItem(targetPath, true);
            var rawItems = Arrays.stream(paths).map(s -> {
                try {
                    user.resetItemsStatus();
                    var TItem = user.rootItem.getTargetItem(s, true);

                    //todo logger
                    return Result.okResult(TItem);
                } catch (BaseException e) {
                    //todo logger
                    return Result.<Item, BaseException>failureResult(e);
                }
            });
            var result = targetItem.moveItemsInto(sameNameOK, rawItems.collect(Collectors.toList()));

            userRepository.save(user);

            //todo Logger
            return Result.okResult(result);
        } catch (BaseException | BaseRuntimeException e) {
            //todo logger
            return Result.failureResult(e);
        }
    }

    @PostMapping(value = "/share")
    public Result<String> shareItems(
            @RequestParam("targets") String[] targets
    ) {
        try {
            User user = User.databaseUser(userRepository);
            user.constructItem(true, true);
        } catch (BaseException | BaseRuntimeException e) {

        }
        return null;
    }
}
