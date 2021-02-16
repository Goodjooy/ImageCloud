package com.jacky.imagecloud.FileStorage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileUploader<T> {
        void init();
        T storage(MultipartFile file);
        Stream<Path> loadAll();
        Path load(String filePath);
        Resource loadAsResource(String filePath);
        void  deleteAll();
        long delete(String fileName);

}
