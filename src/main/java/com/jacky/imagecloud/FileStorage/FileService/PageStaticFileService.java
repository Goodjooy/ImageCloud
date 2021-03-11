package com.jacky.imagecloud.FileStorage.FileService;

import com.jacky.imagecloud.configs.StorageProperties;
import com.jacky.imagecloud.err.file.StorageException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class PageStaticFileService implements FileService<Boolean> {
    private final Path localPath;

    public PageStaticFileService(StorageProperties properties){
        localPath =Path.of(properties.getSavePath(),"pages");
    }

    @Override
    public void init() {
        try {
            if(!localPath.toAbsolutePath().toFile().exists())
                Files.createDirectories(localPath.toAbsolutePath());
        } catch (IOException e) {
                e.printStackTrace();
        }
    }

    @Override
    public Boolean storage(MultipartFile file) {
        return null;
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.localPath, 1)
                    .filter(path -> !path.equals(this.localPath))
                    .map(this.localPath::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filePath) {
        return localPath.resolve(filePath).toAbsolutePath();
    }

    @Override
    public Resource loadAsResource(String filePath) {
        try {
            return new UrlResource(load(filePath).toUri());
        } catch (MalformedURLException e) {
            throw new StorageException("Target file Not Found | "+filePath);
        }
    }

    @Override
    public void deleteAll() {

    }

    @Override
    public long delete(String fileName) {
        return 0;
    }
}
