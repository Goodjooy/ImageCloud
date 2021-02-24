package com.jacky.imagecloud.controller.UserDataControllers;

import com.jacky.imagecloud.FileStorage.FileSystemStorageService;
import com.jacky.imagecloud.FileStorage.FileUploader;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.RootPathNotExistException;
import com.jacky.imagecloud.err.UnknownItemTypeException;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.items.*;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.apache.catalina.LifecycleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

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
 * /file/file-upload POST 上传文件 到指定位置 [自动创建文件夹]path=<path:string>&file=[targetfile]
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

    @GetMapping(path = "/walk")
    public Result<User> getUserInfo(Authentication authentication) {
        try {
            User user = getAndInitUser(authentication);

            logger.info(String.format("load full User Info<%s|%s> success", user.getEmailAddress(), user.getName()));
            return new Result<>(user);
        } catch (Exception e) {
            logger.error(String.format("load User<%s> failure", authentication.getName()), e);
            return new Result<>(e.getMessage());
        }
    }

    @GetMapping(path = "/file")
    public Result<Item> getFile(Authentication authentication,
                                @RequestParam(name = "path", defaultValue = "root") String path) {
        try {
            var user = getAndInitUser(authentication);
            var item = user.getRootItem().GetTargetItem(path);

            logger.info(String.format("load item of User<%s|%s> in path<%s> success", user.getEmailAddress(), user.getName(), path));
            return new Result<>(item);
        } catch (Exception e) {
            logger.error(String.format("load item of User<%s> in path<%s> failure", authentication.getName(), path), e);
            return new Result<>(e.getMessage());
        }
    }

    @PostMapping(path = "/file")
    public Result<Boolean> uploadFile(Authentication authentication,
                                      @RequestParam(name = "path") String path,
                                      @RequestParam(name = "file") MultipartFile file) {

        try {
            var user = getAndInitUser(authentication);
            var items = GetItemString(path, user.getRootItem());

            if (file.isEmpty()) return new Result<>("empty file");

            Integer lastID = -1;
            for (Item item :
                    items) {
                if (!user.getAllItems().contains(item)) {
                    item.setParentID(lastID);
                    user.addItem(item);
                    itemRepository.save(item);
                }
                lastID = item.getId();
            }

            var lastItem = new Item();
            lastItem.setItemName(file.getOriginalFilename());
            lastItem.setItemType(ItemType.FILE);
            lastItem.setUser(user);
            lastItem.setParentID(items.getLast().getId());

            var fileStorage = fileUploader.storage(file);
            fileStorage.item = lastItem;
            lastItem.file = fileStorage;

            user.addItem(lastItem);

            user.information.usedSize += file.getSize();

            fileStorageRepository.save(fileStorage);
            itemRepository.save(lastItem);
            userRepository.save(user);

            logger.info(String.format("upload item of User<%s|%s> in path<%s> name<%s> success",
                    user.getEmailAddress(), user.getName(), path, file.getOriginalFilename()));
            return new Result<>(true);
        } catch (Exception e) {
            logger.error(String.format("upload item of User<%s> in path<%s> name<%s> failure",
                    authentication.getName(), path, file.isEmpty() ? "unknown" : file.getOriginalFilename()), e);
            fileOperateCheck();
            return new Result<>(e.getMessage());
        }
    }


    @DeleteMapping(path = "/file")
    public Result<Boolean> deleteFile(Authentication authentication,
                                      @RequestParam(name = "path", defaultValue = "/root") String path,
                                      @RequestParam(name = "paswd", defaultValue = "") String password
    ) {
        //如果结尾为文件，删除文件，如果结尾为文件夹 删除所有子文件夹和文件。如果为/root,报错
        try {
            User user = getAndInitUser(authentication);

            if (!encoder.matches(password, user.getPasswordHash())) return new Result<>("wrong password!");
            if (path.equals("/root")) return new Result<>("can not remove /root dir");

            var rootItem = user.getRootItem();

            var target = rootItem.GetTargetItem(path);

            switch (target.getItemType()) {
                case FILE: {
                    //删除文件
                    var size = fileUploader.delete(target.file.filePath);
                    user.information.usedSize -= size;

                    deleteItem(target);

                    userRepository.save(user);
                    break;
                }
                case DIR: {
                    //删除子文件
                    var allItems = target.getAllSubItem();
                    for (Item item :
                            allItems) {
                        switch (item.getItemType()) {
                            case FILE: {
                                //删除文件

                                var size = item.file == null ? 0 : fileUploader.delete(item.file.filePath);
                                user.information.usedSize -= size;

                                deleteItem(item);
                                break;
                            }
                            case DIR: {
                                deleteItem(item);
                                break;
                            }
                        }
                    }
                    if (!path.endsWith("/"))
                        deleteItem(target);
                    userRepository.save(user);
                    break;
                }
                default:
                    throw new UnknownItemTypeException("unknown item type");
            }
            logger.info(String.format("remove item of User<%s|%s> under path<%s> success",
                    user.getEmailAddress(), user.getName(), path));
            return new Result<>(true);

        } catch (UserNotFoundException | FileNotFoundException | RootPathNotExistException | UnknownItemTypeException e) {
            logger.error(String.format("remove item of User<%s|> under path<%s> failure",
                    authentication.getName(), path), e);

            return new Result<>(e.getMessage());
        }

    }

    @PostMapping(path = "/dir")
    public Result<Boolean> createDir(Authentication authentication,
                                     @RequestParam(name = "path", defaultValue = "/root") String path) {
        try {
            var user = getAndInitUser(authentication);
            var items = GetItemString(path, user.getRootItem());

            Integer lastID = -1;
            for (Item item :
                    items) {
                if (!user.getAllItems().contains(item)) {
                    item.setParentID(lastID);
                    itemRepository.save(item);
                    user.addItem(item);
                }
                lastID = item.getId();
            }
            userRepository.save(user);
            logger.info(String.format("User<%s|%s> Create path %s Success!", user.getName(), user.getEmailAddress(), path));
            return new Result<>(true);
        } catch (UserNotFoundException | RootPathNotExistException e) {
            logger.error(String.format("User<%s> create dir %s failure", authentication.getName(), path), e);
            return new Result<>(false);
        }
    }

    @PostMapping(path = "/rename")
    public Result<String> fileRename(Authentication authentication,
                                     @RequestParam(name = "oldPath", defaultValue = "/root") String oldFilePath,
                                     @RequestParam(name = "newName", defaultValue = "") String newName) {
        var temp = getFile(authentication, oldFilePath);
        if (!temp.err ) {
            temp.data.setItemName(newName);
            itemRepository.save(temp.data);

            logger.info(String.format("user<%s> change file<%s> name to <%s>", authentication.getName()
                    , oldFilePath, newName));
            return new Result<>(newName, false, "");
        }
        logger.error(String.format("User<%s> change filename<%s> failure", authentication.getName(), oldFilePath));
        return new Result<>(null, true, temp.message);
    }

    /**
     * 检查文件操作异常状态
     */
    private void fileOperateCheck(){
        var RawFiles=fileUploader.loadAll();

        var allFiles=fileStorageRepository.findAll();
        var allFilesName=List.of(allFiles.stream().map(fileStorage -> fileStorage.filePath).toArray());

        var RawRemove=RawFiles.filter(path -> !allFilesName.contains(path.getFileName().toString()));

        //remove
        var fileSizes=RawRemove.map(path -> fileUploader.delete(path.getFileName().toString()));
        var sum=fileSizes.reduce(0L, Long::sum);
        logger.info(String.format("Exception remove file %d",sum));
    }


    private User getAndInitUser(Authentication authentication) throws UserNotFoundException {
        String emailAddress = authentication.getName();
        User user = new User();
        user.setEmailAddress(emailAddress);

        //var itemResult = new Item();

        var result = userRepository.findOne(Example.of(user));

        if (result.isPresent()) {
            user = result.get();
            user.constructItem();

            logger.info(String.format("organization User<%s|%s> data struct success!", user.getEmailAddress(), user.getName()));
            return user;
        }
        throw new UserNotFoundException(String.format("User<%s> not found", authentication.getName()));

    }

    private LinkedList<Item> GetItemString(String path, Item root) throws RootPathNotExistException {
        var items = new LinkedList<Item>();
        var groups = root.splitPath(path);

        Item temp = root;
        for (String p :
                groups) {
            var t = temp.findTargetItem(temp, p);
            if (t != null) {
                items.add(t);
                temp = t;
            } else {
                t = new Item();
                t.setItemName(p);
                t.setUser(root.getUser());
                t.setItemType(ItemType.DIR);
                //连接上一个
                t.setParentID(items.getLast().getParentID());

                items.add(t);
            }
        }
        return items;

    }

    public void deleteItem(Item item) {
        if (item.getItemType() == ItemType.FILE && item.file != null) {
            fileStorageRepository.deleteById(item.file.id);
        }
        var user = item.getUser();
        user.getAllItems().remove(item);

        item.isRemoved = true;
        itemRepository.save(item);

        logger.info(String.format("Remove file success<%s>", item.getItemName()));
    }

}
