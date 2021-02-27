package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.FileStorage.FileService.FileSystemStorageService;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FileController {
    Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    FileSystemStorageService fileUploader;
    @Autowired
    UserRepository repository;

    @GetMapping(path = "/upload")
    public String uploadFile() {
        logger.debug("upload file page");

        return "file-upload";
    }

    @GetMapping(path = "/headup")
    public String uploadHead() {
        return "head-upload";
    }

    @GetMapping("/headImagePreview")
    public String headImagePreview(
            Model model,
            Authentication authentication
    ) throws UserNotFoundException {

        User user = User.databaseUser(repository, authentication);
        var image = user.image;

        model.addAttribute("i512",image.getFileX512URL());
        model.addAttribute("i256",image.getFileX256URL());
        model.addAttribute("i128",image.getFileX128URL());
        model.addAttribute("i64",image.getFileX64URL());
        model.addAttribute("i32",image.getFileX32URL());
        model.addAttribute("i16",image.getFileX16URL());


        logger.info(String.format("preview user<%s> headImages", user.emailAddress));
        return "head-preview";
    }

    @GetMapping(path = "/imgPreview/{filename:.+}")
    public String imagePreview(@PathVariable String filename, Model model) {
        //storage,thumbnail
        model.addAttribute("fileS", "/storage/" + filename)
                .addAttribute("fileT", "/thumbnail/" + filename);
        logger.debug("preview file page filename:" + filename);
        return "file-preview";
    }

    @GetMapping("/storage/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        logger.info(String.format("finding file<%s> in Storage zone", filename));
        Resource file = fileUploader.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @GetMapping("/thumbnail/{filename:.+}")
    public ResponseEntity<Resource> thumbnailFIle(@PathVariable String filename) {
        logger.info(String.format("finding file<%s> in Thumbnail zone", filename));
        Resource file = fileUploader.loadThumbnailAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=\"%s\"", file.getFilename())).body(file);
    }
}
