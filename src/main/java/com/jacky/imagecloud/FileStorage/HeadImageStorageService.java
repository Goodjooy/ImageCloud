package com.jacky.imagecloud.FileStorage;

import com.jacky.imagecloud.models.users.UserImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class HeadImageStorageService implements FileUploader<UserImage>{
    private Path localPath;
    @Autowired
    public HeadImageStorageService(StorageProperties properties){
        localPath=Path.of(properties.getSavePath(),"UserHead");
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(localPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserImage storage(MultipartFile file) {
        return null;
    }

    @Override
    public Stream<Path> loadAll() {
        return null;
    }

    @Override
    public Path load(String filePath) {
        return null;
    }

    @Override
    public Resource loadAsResource(String filePath) {
        return null;

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public long delete(String fileName) {
        return 0;
    }
}
