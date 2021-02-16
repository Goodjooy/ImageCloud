package com.jacky.imagecloud.controller.UserDataControllers;

import ch.qos.logback.core.joran.action.NewRuleAction;
import com.jacky.imagecloud.FileStorage.FileUploader;
import com.jacky.imagecloud.FileStorage.StorageProperties;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.RootPathNotExistException;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.items.*;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserInformationRepository;
import com.jacky.imagecloud.models.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.LinkedList;

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
//@Controller
public class UserFileController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    FileStorageRepository fileStorageRepository;
    @Autowired
    FileUploader<FileStorage> fileUploader;

    @GetMapping(path = "/walk")
    public Result<User> getUserInfo(Authentication authentication) {
        try {
            User user = getAndInitUser(authentication);

            return new Result<>(user);

        } catch (Exception e) {
            return new Result<>(e.getMessage());
        }
    }

    @GetMapping(path = "/file")
    public Result<Item> getFile(Authentication authentication,
                                @RequestParam(name = "path", defaultValue = "root") String path) {
        try {
            var user = getAndInitUser(authentication);

            var item = user.getRootItem().GetTargetItem(path);
            return new Result<>(item);
        } catch (Exception e) {
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

            user.information.usedSize += file.getSize();

            userRepository.save(user);
            fileStorageRepository.save(fileStorage);
            itemRepository.save(lastItem);


            return new Result<>(true);
        } catch (Exception e) {
            return new Result<>(e.getMessage());
        }
    }


    @DeleteMapping(path = "/file")
    public Result<Boolean> deleteFile(Authentication authentication,
                                      @RequestParam(name = "path", defaultValue = "/root") String path
    ) {
        //如果结尾为文件，删除文件，如果结尾为文件夹 删除所有子文件夹和文件。如果为/root,报错
        try {
            User user = getAndInitUser(authentication);
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
                    deleteItem(target);
                    userRepository.save(user);
                    break;
                }
                default:
                    return new Result<>("unknown item type");
            }
            return new Result<>(true);

        } catch (UserNotFoundException | FileNotFoundException | RootPathNotExistException e) {
            return new Result<>(e.getMessage());
        }

    }

    private User getAndInitUser(Authentication authentication) throws UserNotFoundException {
        String emailAddress = authentication.getName();
        User user = new User();
        user.setEmailAddress(emailAddress);

        var itemResult = new Item();

        var result = userRepository.findOne(Example.of(user));

        if (result.isPresent()) {
            user = result.get();
            var info = user.information;

            itemResult.setUser(user);
            itemResult.isRemoved=false;
            itemResult.getUser().information = null;

            var items = itemRepository.findAll(Example.of(itemResult));
            user.addItems(items);

            user.constructItem();
            user.information = info;
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
        item.isRemoved=true;
        itemRepository.save(item);
    }

}
