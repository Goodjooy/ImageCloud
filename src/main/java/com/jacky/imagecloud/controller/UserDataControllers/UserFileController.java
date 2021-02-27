package com.jacky.imagecloud.controller.UserDataControllers;

import com.jacky.imagecloud.FileStorage.FileService.FileSystemStorageService;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.RootPathNotExistException;
import com.jacky.imagecloud.err.UnknownItemTypeException;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.items.FileStorageRepository;
import com.jacky.imagecloud.models.items.Item;
import com.jacky.imagecloud.models.items.ItemRepository;
import com.jacky.imagecloud.models.items.ItemType;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
    Logger logger = LoggerFactory.getLogger(UserFileController.class);

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
                                @RequestParam(name = "withHidden", defaultValue = "false") boolean withHidden) {
        try {
            var user = getAndInitUser(authentication, withHidden, false);
            var item = user.rootItem.getTargetItem(path, withHidden);

            logger.info(String.format("load item of User<%s|%s> in path<%s> success status<withHidden:%s>",
                    user.emailAddress, user.name, path, withHidden));
            return new Result<>(item);
        } catch (Exception e) {
            logger.error(String.format("load item of User<%s> in path<%s> failure status<withHidden:%s>",
                    authentication.getName(), path, withHidden), e);
            return new Result<>(e.getMessage());
        }
    }

    @PostMapping(path = "/file")
    public Result<Boolean> uploadFile(Authentication authentication,
                                      @RequestParam(name = "path") String path,
                                      @RequestParam(name = "file") MultipartFile file,
                                      @RequestParam(name = "hidden", defaultValue = "false") Boolean hidden) {

        try {
            var user = getAndInitUser(authentication);

            if (user.information.availableSize() < file.getSize()) return new Result<>(
                    String.format("user space<%d|%d> not enough",
                            user.information.availableSize(), file.getSize()));
            if (file.isEmpty()) return new Result<>("empty file");
            Item last = appendNotExistItems(user, path, hidden);
            var lastItem = Item.FileItem(user, last, file.getOriginalFilename(), hidden);
            var fileStorage = fileUploader.storage(file);
            fileStorage.item = lastItem;
            lastItem.file = fileStorage;

            user.addItem(lastItem);
            user.information.AppendSize(file.getSize());

            fileStorageRepository.save(fileStorage);
            itemRepository.save(lastItem);
            userRepository.save(user);

            logger.info(String.format("upload item of User<%s|%s> in path<%s> name<%s> success status<hidden:%s>",
                    user.emailAddress, user.name, path, file.getOriginalFilename(), hidden));
            return new Result<>(true);
        } catch (Exception e) {
            logger.error(String.format("upload item of User<%s> in path<%s> name<%s> failure status<hidden:%s>",
                    authentication.getName(), path, file.isEmpty() ? "unknown" : file.getOriginalFilename(), hidden), e);
            fileOperateCheck();
            return new Result<>(e.getMessage());
        }
    }


    @DeleteMapping(path = "/file")
    public Result<Boolean> deleteFile(Authentication authentication,
                                      @RequestParam(name = "path", defaultValue = "/root") String path,
                                      @RequestParam(name = "flat", defaultValue = "true") boolean flatRemove,
                                      @RequestParam(name = "paswd", defaultValue = "") String password,
                                      @RequestParam(name = "removeTargetDir", defaultValue = "false") boolean removeTargetDir
    ) {
        //如果结尾为文件，删除文件，如果结尾为文件夹 删除所有子文件夹和文件。如果为/root,报错
        try {
            User user = getAndInitUser(authentication, true,false);

            if (!encoder.matches(password, user.password)) return new Result<>("wrong password!");
            if (path.equals("/root")) return new Result<>("can not remove /root dir");

            var rootItem = user.rootItem;

            var target = rootItem.getTargetItem(path, true);
            var subItems = target.transformSubItemsToList();

            subItemDeleter(subItems, flatRemove);

            switch (target.getItemType()) {
                case FILE: {
                    deleteItem(target, flatRemove);
                    userRepository.save(user);
                    break;
                }
                case DIR: {
                    if (removeTargetDir)
                        deleteItem(target, flatRemove);
                    userRepository.save(user);
                    break;
                }
                default:
                    throw new UnknownItemTypeException("unknown item type");
            }
            logger.info(String.format("remove item of User<%s|%s> under path<%s> success | status<flat:%s; removeTargetDir%s>",
                    user.emailAddress, user.name, path, flatRemove, removeTargetDir));
            return new Result<>(true);

        } catch (UserNotFoundException | FileNotFoundException | RootPathNotExistException | UnknownItemTypeException e) {
            logger.error(String.format("remove item of User<%s|> under path<%s> failure | status<flat:%s; removeTargetDir%s>",
                    authentication.getName(), path, flatRemove, removeTargetDir), e);

            return new Result<>(e.getMessage());
        }

    }

    @GetMapping(path = "/remove-trees")
    public Result<Set<Item>> getRemovedItems(Authentication authentication) {
        try {
            User user = User.databaseUser(userRepository, authentication);

            logger.info(String.format("load flat removed files success User<%s>", user.emailAddress));
            return new Result<>(user.removedItems());
        } catch (UserNotFoundException e) {
            logger.error(String.format("load flat removed files failure User<%s>", authentication.getName()), e);
            return new Result<>(e.getMessage());
        }

    }

    @PostMapping(path = "/dir")
    public Result<Boolean> createDir(Authentication authentication,
                                     @RequestParam(name = "path", defaultValue = "/root") String path,
                                     @RequestParam(name = "hidden", defaultValue = "false") Boolean hidden) {
        try {
            var user = getAndInitUser(authentication, false);

            appendNotExistItems(user, path, hidden);
            userRepository.save(user);
            logger.info(String.format("User<%s|%s> Create path %s Success! | status<hidden:%s>",
                    user.name, user.emailAddress, path, hidden));
            return new Result<>(true);
        } catch (UserNotFoundException | RootPathNotExistException e) {
            logger.error(String.format("User<%s> create dir %s failure | status<hidden:%s>"
                    , authentication.getName(), path , hidden), e);
            return new Result<>(false);
        }
    }

    @PostMapping(path = "/rename")
    public Result<String> fileRename(Authentication authentication,
                                     @RequestParam(name = "oldPath", defaultValue = "/root") String oldFilePath,
                                     @RequestParam(name = "newName", defaultValue = "") String newName) {
        var temp = getFile(authentication, oldFilePath, true);
        if (!temp.err) {
            temp.data.setItemName(newName);
            itemRepository.save(temp.data);

            logger.info(String.format("user<%s> change file<%s> name to <%s>", authentication.getName()
                    , oldFilePath, newName));
            return new Result<>(newName, false, "");
        }
        logger.error(String.format("User<%s> change filename<%s> failure", authentication.getName(), oldFilePath));
        return new Result<>(null, true, temp.message);
    }

    @PostMapping(path = "/file-status")
    public Result<Boolean> fileStatusChange(
            Authentication authentication,
            @RequestParam(name = "path", defaultValue = "/root") String targetPath
    ) {
        try {
            var user = getAndInitUser(authentication, true);
            var target = user.rootItem.getTargetItem(targetPath, true);
            target.hidden = !target.hidden;

            userRepository.save(user);
            itemRepository.save(target);

            logger.info(String.format("change file status in path<%s> for User<%s|%s> success[->%s]",
                    targetPath, user.emailAddress, user.name, target.hidden));
            return new Result<>(target.hidden);
        } catch (UserNotFoundException | FileNotFoundException | RootPathNotExistException e) {
            logger.error(String.format("change file status in path<%s> for User<%s> failure",
                    targetPath, authentication.getName()), e);
            return new Result<>(e.getMessage());
        }
    }

    @PostMapping(path = "/restore-file")
    public Result<Boolean> restoreFile(
            Authentication authentication,
            @RequestParam(name = "path", defaultValue = "/root") String targetPath
    ) {
        try {
            var user = getAndInitUser(authentication);
            user.constructItem(true, true);
            var target = user.rootItem.getTargetItem(targetPath, true);

            target.removed = false;
            userRepository.save(user);
            itemRepository.save(target);

            logger.info(String.format("restore file <%s> for user<%s> success", targetPath, user.emailAddress));
            return new Result<>(Boolean.TRUE);
        } catch (UserNotFoundException | FileNotFoundException | RootPathNotExistException e) {
            logger.info(String.format("restore file <%s> for user<%s> failure", targetPath, authentication.getName()), e);
            return new Result<>(e.getMessage());
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
        logger.info(String.format("Exception remove file %d", sum));
    }

    private User getAndInitUser(Authentication authentication, boolean withRemove) throws UserNotFoundException {
        return getAndInitUser(authentication, false, withRemove);
    }

    private User getAndInitUser(Authentication authentication) throws UserNotFoundException {
        return getAndInitUser(authentication, false, false);
    }

    private User getAndInitUser(Authentication authentication, boolean withHidden, boolean withRemoved) throws UserNotFoundException {
        User user = User.databaseUser(userRepository, authentication, withHidden, withRemoved);

        logger.info(String.format("organization User<%s|%s> data struct success!", user.emailAddress, user.name));
        return user;

    }

    private LinkedList<Item> GetItemTree(String path, Item root, boolean hidden) throws RootPathNotExistException {
        var items = new LinkedList<Item>();
        var user = root.getUser();
        var groups = root.splitPath(path);

        Item temp = root;
        for (String p :
                groups) {
            var t = temp.findTargetItem(p, false);
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
        item.removed = true;
        itemRepository.save(item);
        logger.info(String.format("Remove file success<%s> [totally]", item.getItemName()));
    }

    private Item appendNotExistItems(User user, String path, boolean hidden) throws RootPathNotExistException {
        var items = GetItemTree(path, user.rootItem, hidden);
        Item lastItem = Item.DefaultItem();
        for (Item item :
                items) {
            if (!user.seizedFiles.contains(item)) {
                item.setParentItem(lastItem);
                user.addItem(item);
                itemRepository.save(item);
            }
            lastItem = item;
        }
        return lastItem;
    }
}
