package com.jacky.imagecloud.controller.UserDataControllers;

import com.jacky.imagecloud.FileStorage.FileService.FileSystemStorageService;
import com.jacky.imagecloud.data.Info;
import com.jacky.imagecloud.data.LoggerHandle;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.file.EmptyFileException;
import com.jacky.imagecloud.err.item.ItemExistException;
import com.jacky.imagecloud.err.item.ItemNotFoundException;
import com.jacky.imagecloud.err.item.RootPathNotExistException;
import com.jacky.imagecloud.err.item.UnknownItemTypeException;
import com.jacky.imagecloud.err.user.UserNotFoundException;
import com.jacky.imagecloud.err.user.UserSpaceNotEnoughException;
import com.jacky.imagecloud.models.items.*;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户信息控制器
 * <p>
 * /user/**
 * <p>
 * /user/total-file 总文件大小
 * /user/total 总可用空间
 * /user/total-file-count 总文件数量
 * <p>
 * /walk GET 返回全部文件的嵌套结构
 * <p>
 * /file?path=<path:string> GET 如果是文件夹，返回子文件夹结构【item】 ；如果是文件，返回对应item
 * /file/file-upload POST 上传文件 到指定位置 [自动创建文件夹]path=<path:string>&file=[targetFile]
 * /file?path=<path:string> DELETE 删除文件/文件夹
 */
@RestController
public class UserFileController {
    LoggerHandle logger = LoggerHandle.newLogger(UserFileController.class);

    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    FileStorageRepository fileStorageRepository;
    @Autowired
    FileSystemStorageService fileUploader;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping(path = "/file")
    public Result<Item> getFile(Authentication authentication,
                                @RequestParam(name = "path", defaultValue = "root") String path,
                                @RequestParam(name = "withHidden", defaultValue = "false") boolean withHidden,
                                @RequestParam(name = "sort", defaultValue = "off") ItemSort sort,
                                @RequestParam(name = "reverse", defaultValue = "false") boolean reverse) {


        try {
            var user = User.databaseUser(userRepository, authentication, withHidden, false);
            var item = user.rootItem.getTargetItem(path, withHidden);

            item.sortSubItems(sort, reverse);

            logger.findSuccess(user, path, item,
                    Info.of(withHidden, "withHidden"),
                    Info.of(sort, "sortBy"),
                    Info.of(reverse, "Reverse"));
            return Result.okResult(item);
        } catch (Exception e) {
            logger.operateFailure("Find Item", e, authentication,
                    Info.of(path, "Path"),
                    Info.of(withHidden, "withHidden"),
                    Info.of(sort, "sortBy"),
                    Info.of(reverse, "Reverse"));

            return Result.failureResult(e);
        }
    }

    @PostMapping(path = "/file")
    public Result<Boolean> uploadFile(Authentication authentication,
                                      @RequestParam(name = "path") String path,
                                      @RequestParam(name = "file") MultipartFile file,
                                      @RequestParam(name = "hidden", defaultValue = "false") Boolean hidden) {

        try {
            var user = User.databaseUser(userRepository, authentication,
                    true, false);

            if (user.information.availableSize() < file.getSize())
                throw new UserSpaceNotEnoughException(user.information.availableSize(), file.getSize());
            if (file.isEmpty())
                throw new EmptyFileException(file);

            Item last = appendNotExistItems(user, path, hidden, true);
            var lastItem = Item.FileItem(user, last, file.getOriginalFilename(), hidden);
            var fileStorage = fileUploader.storage(file);
            fileStorage.item = lastItem;
            lastItem.file = fileStorage;

            user.addItem(lastItem);
            user.information.AppendSize(file.getSize());

            fileStorageRepository.save(fileStorage);
            itemRepository.save(lastItem);
            userRepository.save(user);

            logger.uploadSuccess(user, file, path, Info.of(hidden, "hidden"));
            return Result.okResult(true);
        } catch (Exception e) {
            logger.operateFailure("Upload File Failure", e, authentication,
                    Info.of(path, "Path"),
                    Info.of(Objects.requireNonNull(file.getOriginalFilename()), "fileName"),
                    Info.of(hidden, "hidden")
            );
            fileOperateCheck();
            return Result.failureResult(e);
        }
    }


    @DeleteMapping(path = "/file")
    public Result<List<Result<Boolean>>> deleteFile(Authentication authentication,
                                                    @RequestParam(name = "path", defaultValue = "/root") String[] paths,
                                                    @RequestParam(name = "flat", defaultValue = "true") boolean flatRemove,
                                                    @RequestParam(name = "paswd", defaultValue = "") String password,
                                                    @RequestParam(name = "removeTargetDir", defaultValue = "false") boolean removeTargetDir
    ) {
        logger.dataAccept(Info.of(List.of(paths), "targetPaths"));

        //如果结尾为文件，删除文件，如果结尾为文件夹 删除所有子文件夹和文件。如果为/root,报错
        try {
            User user = User.databaseUser(userRepository, authentication, true, false);

            if (!encoder.matches(password, user.password)) return Result.failureResult("wrong password!");

            var rootItem = user.rootItem;
            Stream<Result<Boolean>> result = Stream.of(paths).map(path ->
            {
                if (path.equals("/root")) return Result.failureResult("can not remove /root dir");
                Item target;
                try {
                    target = rootItem.getTargetItem(path, true);

                    var subItems = target.transformSubItemsToList();

                    subItemDeleter(subItems, flatRemove);

                    if (target.getItemType() == ItemType.FILE) {
                        deleteItem(target, flatRemove);
                        userRepository.save(user);
                    } else if (target.getItemType() == ItemType.DIR) {
                        if (removeTargetDir)
                            deleteItem(target, flatRemove);
                        userRepository.save(user);
                    } else {
                        throw new UnknownItemTypeException("unknown item type");
                    }

                    logger.deleteSuccess(user, path, target,
                            Info.of(flatRemove, "flatRemove"),
                            Info.of(removeTargetDir, "removeTargetDir"));

                    return Result.okResult(true);
                } catch (FileNotFoundException | RootPathNotExistException | UnknownItemTypeException | ItemNotFoundException e) {
                    logger.operateFailure("Remove One Item In All Item", e, authentication,
                            Info.of(path, "targetPath"),
                            Info.of(flatRemove, "flatRemove"),
                            Info.of(removeTargetDir, "removeTargetDir"));
                    return Result.failureResult(e);
                }
            });
            return new Result<>(result.collect(Collectors.toList()));
        } catch (UserNotFoundException e) {
            logger.operateFailure("Remove Items", e,
                    authentication,
                    Info.of(List.of(paths), "TargetPaths"),
                    Info.of(flatRemove, "flatRemove"),
                    Info.of(removeTargetDir, "removeTargetDir"));
            return Result.failureResult(e);
        }

    }

    @GetMapping(path = "/remove-trees")
    public Result<Map<String, Item>> getRemovedItems(Authentication authentication) {
        try {
            User user = User.databaseUser(userRepository, authentication);
            var data = user.removedItems();

            logger.findRemovedTreeSuccess(user, data);
            return Result.okResult(data);
        } catch (UserNotFoundException e) {
            logger.operateFailure("Load Flat Remove File Tree", e,
                    authentication);
            return Result.failureResult(e);
        }

    }

    @PostMapping(path = "/dir")
    public Result<Boolean> createDir(Authentication authentication,
                                     @RequestParam(name = "path", defaultValue = "/root") String path,
                                     @RequestParam(name = "hidden", defaultValue = "false") Boolean hidden) {
        try {
            var user = User.databaseUser(userRepository, authentication, true, false);

            appendNotExistItems(user, path, hidden, false);
            userRepository.save(user);

            logger.createSuccess(user, path, Info.of(hidden, "hidden"));
            return new Result<>(true);
        } catch (UserNotFoundException | RootPathNotExistException | ItemExistException e) {
            logger.operateFailure("Create Directory", e, authentication,
                    Info.of(hidden, "hidden")
            );
            return Result.okResult(false);
        }
    }


    @PostMapping(path = "/rename")
    public Result<String> fileRename(Authentication authentication,
                                     @RequestParam(name = "oldPath", defaultValue = "/root") String oldFilePath,
                                     @RequestParam(name = "newName", defaultValue = "") String newName) {
        var temp = getFile(authentication, oldFilePath, true, ItemSort.name, false);
        if (!temp.err) {
            temp.data.setItemName(newName);
            itemRepository.save(temp.data);

            logger.userOperateFailure(authentication.getName(), "Rename File",
                    Info.of(oldFilePath, "oldName"),
                    Info.of(newName, "newName"));
            return Result.okResult(newName);
        }
        logger.operateFailure("Change File Name",
                authentication,
                Info.of(oldFilePath, "oldName"),
                Info.of(newName, "newName"));
        return Result.failureResult(temp.message);
    }

    @PostMapping(path = "/file-status")
    public Result<List<Result<Boolean>>> fileStatusChange(
            Authentication authentication,
            @RequestParam(name = "path", defaultValue = "/root") String[] targetPaths
    ) {
        logger.dataAccept(Info.of(List.of(targetPaths), "TargetPathToChangeHiddenStatus"));
        try {
            var user = User.databaseUser(userRepository, authentication, true, false);
            Function<String, Result<Boolean>> operator = targetPath ->
            {
                Item target;
                try {
                    target = user.rootItem.getTargetItem(targetPath, true);
                    target.hidden = !target.hidden;
                    itemRepository.save(target);

                    logger.fileOperateSuccess(user, "Change Status",
                            Info.of(targetPath, "targetPath"),
                            Info.of(target.hidden, "hiddenStatus"));
                    return Result.okResult(true);
                } catch (FileNotFoundException | RootPathNotExistException | ItemNotFoundException e) {
                    logger.operateFailure("Change Status",
                            authentication,
                            Info.of(targetPath, "targetPath"));
                    return Result.failureResult(e);
                }
            };
            var result = Stream.of(targetPaths).map(operator);

            userRepository.save(user);
            return Result.okResult(result.collect(Collectors.toList()));
        } catch (UserNotFoundException e) {
            logger.operateFailure("Change File Hidden Status", e, authentication,
                    Info.of(List.of(targetPaths), "paths"));
            return Result.failureResult(e);
        }
    }

    @PostMapping(path = "/restore-file")
    public Result<Boolean> restoreFile(
            Authentication authentication,
            @RequestParam(name = "path", defaultValue = "/root") String targetPath
    ) {
        try {
            var user = User.databaseUser(userRepository, authentication, true, true);
            var target = user.rootItem.getTargetItem(targetPath, true);

            target.setRemoved(false);
            userRepository.save(user);
            itemRepository.save(target);

            logger.fileOperateSuccess(user, "Restore File", Info.of(targetPath, "targetPath"));
            return Result.okResult(Boolean.TRUE);
        } catch (UserNotFoundException | FileNotFoundException | RootPathNotExistException | ItemNotFoundException e) {
            logger.operateFailure("Restore File", authentication, Info.of(targetPath, "TargetPath"));
            return Result.failureResult(e);
        }
    }

    /**
     * 检查文件操作异常状态
     */
    private void fileOperateCheck() {
        var RawFiles = fileUploader.loadAll();

        var allFiles = fileStorageRepository.findAll();
        var allFilesName = List.of(allFiles.stream().map(fileStorage -> fileStorage.filePath).toArray());

        var RawRemove = RawFiles.filter(path -> !allFilesName.contains(path.getFileName().toString()));

        //remove
        var fileSizes = RawRemove.map(path -> fileUploader.delete(path.getFileName().toString()));
        var sum = fileSizes.reduce(0L, Long::sum);
        logger.userOperateSuccess("ALL-USER", "Remove Failure File", Info.of(sum, "Remove file count"));
    }


    private LinkedList<Item> GetItemTree(String path, Item root, boolean hidden) throws RootPathNotExistException {
        var items = new LinkedList<Item>();
        var user = root.getUser();
        var groups = root.splitPath(path);

        Item temp = root;
        for (String p :
                groups) {
            var t = temp.findTargetItem(p, true);
            if (t != null) {
                items.add(t);
                temp = t;
            } else {
                t = Item.DirItem(user, items.getLast(), p, hidden);
                items.add(t);
            }
        }
        return items;
    }

    private void subItemDeleter(List<Item> items, boolean flatRemove) {
        for (Item item : items) {
            deleteItem(item, flatRemove);
        }
    }

    private void deleteItem(Item item, boolean flatRemove) {
        var user = item.getUser();
        if (item.getItemType() == ItemType.FILE && !flatRemove) {
            var size = item.file == null ? 0 : fileUploader.delete(item.file.filePath);
            user.information.AppendSize(-size);
        }
        if (!flatRemove) {
            if (item.getItemType() == ItemType.FILE && item.file != null) {
                fileStorageRepository.deleteById(item.file.id);
            }
            user.seizedFiles.remove(item);
        }
        item.setRemoved(true);
        itemRepository.save(item);
    }

    private Item appendNotExistItems(User user,
                                     String path,
                                     boolean hidden,
                                     boolean fileSave) throws RootPathNotExistException, ItemExistException {
        var items = GetItemTree(path, user.rootItem, hidden);
        Item lastItem = Item.DefaultItem();

        int count = 0;

        for (Item item :
                items) {
            if (!user.seizedFiles.contains(item)) {
                item.setParentItem(lastItem);
                user.addItem(item);
                itemRepository.save(item);
            } else
                count++;
            lastItem = item;
        }
        if (count == items.size() && !fileSave) {
            throw new ItemExistException(path);
        }
        return lastItem;
    }
}
