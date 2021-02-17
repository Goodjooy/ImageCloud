package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.FileStorage.FileUploader;
import com.jacky.imagecloud.models.items.FileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FileController {
    Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    FileUploader<FileStorage> fileUploader;

    @GetMapping(path = "/upload")
    public String uploadFile() {
        return "file-upload";
    }

    @GetMapping("/storage/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        logger.info(String.format("finding file<%s> in Storage zone",filename));
        Resource file = fileUploader.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
}
