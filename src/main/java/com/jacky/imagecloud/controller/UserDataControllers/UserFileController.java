package com.jacky.imagecloud.controller.UserDataControllers;

import com.jacky.imagecloud.FileStorage.FileService.FileSystemStorageService;
import com.jacky.imagecloud.data.Info;
import com.jacky.imagecloud.data.LoggerHandle;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.BaseException;
import com.jacky.imagecloud.err.BaseRuntimeException;
import com.jacky.imagecloud.err.file.EmptyFileException;
import com.jacky.imagecloud.err.item.*;
import com.jacky.imagecloud.err.user.UserNotFoundException;
import com.jacky.imagecloud.err.user.UserSpaceNotEnoughException;
import com.jacky.imagecloud.models.items.*;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
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
    public Result<List<Result<Boolean>>> uploadFile(Authentication authentication,
                                                    @RequestParam(name = "path") String path,
                                                    @RequestParam(name = "file") MultipartFile[] files,
                                                    @RequestParam(name = "hidden", defaultValue = "false") Boolean hidden) {

        try {
            logger.dataAccept(Info.of(Arrays.stream(files)
                            .map(file -> file == null ? "unknown" : file.getOriginalFilename()).collect(Collectors.toList()),
                    "Files"));

            var user = User.databaseUser(userRepository, authentication,
                    true, false);

            Item last = appendNotExistItems(user, path, hidden, true);
            Function<MultipartFile, Result<Boolean>> handle = file -> {
                try {

                    var lastItem = Item.FileItem(user, last, file.getOriginalFilename(), hidden);

                    if (file.isEmpty()) {
                        throw new EmptyFileException(file);
                    }
                    if (user.information.availableSize() < file.getSize()) {
                        throw new UserSpaceNotEnoughException(user.information.availableSize(), file.getSize());
                    }

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
                } catch (BaseException | BaseRuntimeException e) {
                    logger.operateFailure("Upload File Failure",
                            e,
                            Info.of(user, "User"),
                            Info.of(Objects.requireNonNull(file.getOriginalFilename()), "fileName"),
                            Info.of(path, "SavePath"),
                            Info.of(hidden, "hidden"));
                    return Result.okResult(true);
                }
            };
            return Result.okResult(Arrays.stream(files).map(handle).collect(Collectors.toList()));
        } catch (BaseException | BaseRuntimeException e) {
            logger.operateFailure("Upload File", e, authentication,
                    Info.of(path, "Path"),
                    Info.of(Arrays.stream(files).map(MultipartFile::getOriginalFilename).collect(Collectors.toList()), "filesName"),
                    Info.of(hidden, "hidden")
            );
            fileOperateCheck();
            return Result.failureResult(e);
        }
    }


    @DeleteMapping(path = "/file")
    public Result<List<Result<Boolean>>> deleteFile(
            Authentication authentication,
            @RequestParam(name = "path") String[] paths,
            @RequestParam(name = "flat", defaultValue = "true") boolean flatRemove,
            @RequestParam(name = "paswd") String password,
            @RequestParam(name = "removeTargetDir", defaultValue = "false") boolean removeTargetDir
    ) {
        var a = SecurityContextHolder.getContext();
        logger.dataAccept(Info.of(List.of(paths), "targetPaths"));

        //如果结尾为文件，删除文件，如果结尾为文件夹 删除所有子文件夹和文件。如果为/root,报错
        try {
            User user = User.databaseUser(userRepository, authentication, true, false);

            if (!encoder.matches(password, user.password)) return Result.failureResult("wrong password!");

            var rootItem = user.rootItem;
            Stream<Result<Boolean>> result = Stream.of(paths).map(path ->
            {
                Item target;
                try {
                    if (path.equals("/root"))
                        throw new RootDeleteException();

                    user.resetItemsStatus();
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
                } catch (BaseException | BaseRuntimeException e) {
                    logger.operateFailure("Remove One Item In All Item", e, authentication,
                            Info.of(path, "targetPath"),
                            Info.of(flatRemove, "flatRemove"),
                            Info.of(removeTargetDir, "removeTargetDir"));
                    return Result.failureResult(e);
                }
            });
            return new Result<>(result.collect(Collectors.toList()));
        } catch (BaseException | BaseRuntimeException e) {
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
        } catch (UserNotFoundException | ItemNotFoundException e) {
            logger.operateFailure("Load Flat Remove File Tree", e,
                    authentication);
            return Result.failureResult(e);
        }

    }

    @PostMapping(path = "/dir")
    public Result<Boolean> createDir(Authentication authentication,
                                     @RequestParam(name = "path") String path,
                                     @RequestParam(name = "hidden", defaultValue = "false") Boolean hidden) {
        try {
            var user = User.databaseUser(userRepository, authentication, true, false);

            appendNotExistItems(user, path, hidden, false);
            userRepository.save(user);

            logger.createSuccess(user, path, Info.of(hidden, "hidden"));
            return new Result<>(true);
        } catch (BaseException | BaseRuntimeException e) {
            logger.operateFailure("Create Directory", e, authentication,
                    Info.of(hidden, "hidden")
            );
            return Result.okResult(false);
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


    private LinkedList<Item> GetItemTree(String path, Item root, boolean hidden) throws RootPathNotExistException, ItemNotFoundException {
        var items = new LinkedList<Item>();
        var user = root.getUser();
        var groups = root.splitPath(path);

        Item t;
        Item temp = root;
        for (String p :
                groups) {

            t = temp.findInSubItems(p, true);
            if (t != null && !items.contains(t)) {
                items.add(t);
                temp = t;
            } else {
                t = Item.DirItem(user, items.getLast(), p, hidden);
                items.add(t);
            }
        }
        return items;
    }

    private void subItemDeleter(List<Item> items, boolean flatRemove) throws RootDeleteException {
        for (Item item : items) {
            deleteItem(item, flatRemove);
        }
    }

    private void deleteItem(Item item, boolean flatRemove) throws RootDeleteException {
        if (item.isRootItem())
            throw new RootDeleteException();

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
                                     boolean fileSave) throws RootPathNotExistException, ItemExistException, ItemNotFoundException {
        var items = GetItemTree(path, user.rootItem, hidden);
        Item lastItem = Item.DefaultItem();

        int count = 0;

        for (Item item :
                items) {
            if (!user.seizedFiles.contains(item)) {
                item.setParentItem(lastItem);
                itemRepository.save(item);
                user.addItem(item);
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
