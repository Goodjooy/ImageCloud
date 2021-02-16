package com.jacky.imagecloud.controller.UserDataControllers;

import ch.qos.logback.core.joran.action.NewRuleAction;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.models.items.FileStorageRepository;
import com.jacky.imagecloud.models.items.Item;
import com.jacky.imagecloud.models.items.ItemRepository;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

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
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    FileStorageRepository fileStorageRepository;

    @GetMapping(path = "/file")
    public Result<Item> getFile(Authentication authentication,
                                @RequestParam(name = "path", defaultValue = "root") String path) {
        try {
            String emailAddress = authentication.getName();
            User user = new User();
            user.setEmailAddress(emailAddress);

            var result = userRepository.findOne(Example.of(user));
            if (result.isPresent()) {
                user = result.get();

                user.constructItem();

                var item = user.getRootItem().GetTargetItem(path);
                return new Result<>(item);
            }
        } catch (Exception e) {
            return new Result<>(e.getMessage());
        }
        return new Result<>("unknown exception");
    }

    @PostMapping(path = "/file")
    public Result<Boolean> uploadFile(Authentication authentication,
                                      @RequestParam(name = "path") String path,
                                      @RequestParam(name = "file") MultipartFile file) {

        //TODO 文件上传
        //TODO 检查文件大小，是否超过上限
        return null;
    }

    @DeleteMapping(path = "/file")
    public Result<Boolean>deleteFile(Authentication authentication,
                                     @RequestParam(name = "path",defaultValue = "/root")String path
                                     ){
        //TODO 文件删除
        return null;
    }
}
