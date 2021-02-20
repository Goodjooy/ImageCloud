package com.jacky.imagecloud.FileStorage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.jacky.imagecloud.err.StorageException;
import com.jacky.imagecloud.err.StorageFileNotFoundException;
import com.jacky.imagecloud.models.items.FileStorage;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;

@Service
public class FileSystemStorageService implements FileUploader<FileStorage> {


    private final Path rootRawLocation;
    private final Path rootThumbnailLocation;

    private static final float MaxWidth = 256;
    private static final float MaxHeight = 256;

    private static final float scale = 0.4f;


    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootRawLocation = Paths.get(properties.getSavePath(), "Storage");
        this.rootThumbnailLocation = Paths.get(properties.getSavePath(), "Thumbnail");

        this.init();
    }

    public void SaveImageWithThumbnail(InputStream inputStream,
                                       @NotNull String fileName,
                                       @NotNull String fileExtra) throws IOException {
        var RawImage = ImageIO.read(inputStream);
        int width = RawImage.getWidth();
        int height = RawImage.getHeight();
        int flag = RawImage.getType();

        //图像压缩比计算
        float consult = (width > height ? MaxWidth : MaxHeight) / (Math.max(width, height));
        consult = consult - 1 > 0 ? 1 : consult;
        //float consult = scale;
        int TWidth = Math.round(width * consult);
        int THeight = Math.round(height * consult);
        var ThumbnailImageMid = RawImage.getScaledInstance(TWidth, THeight,
                Image.SCALE_SMOOTH);
        var ThumbnailImage = new BufferedImage(TWidth, THeight, flag);

        ThumbnailImage.getGraphics().drawImage(ThumbnailImageMid, 0, 0, null);

        var RawPath = rootRawLocation.resolve(
                Path.of(Objects.requireNonNull(fileName))
        ).normalize().toAbsolutePath();
        var ThumbnailPath = rootThumbnailLocation.resolve(
                Path.of(fileName)
        ).normalize().toAbsolutePath();

        if (!RawPath.getParent().equals(rootRawLocation.toAbsolutePath()) || !
                ThumbnailPath.getParent().equals(rootThumbnailLocation.toAbsolutePath()))
            throw new StorageException("Cannot store file outside current directory.");


        ImageIO.write(RawImage, fileExtra, RawPath.toFile());
        ImageIO.write((RenderedImage) ThumbnailImage, fileExtra, ThumbnailPath.toFile());
    }

    @Override
    public FileStorage storage(MultipartFile file) {

        try {
            FileStorage storage = new FileStorage();
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            String exName = "";
            String fileExtra = "";
            var temp = Pattern.compile(".+(\\.([a-zA-Z0-9_-]+))$").matcher(file.getOriginalFilename());
            if (temp.find()) {
                exName = temp.group(1);
                fileExtra = temp.group(2);
            }
            String saveFileName = UUID.randomUUID().toString() + exName;
            storage.filePath = saveFileName;

            SaveImageWithThumbnail(file.getInputStream(), saveFileName, fileExtra);
            return storage;
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootRawLocation, 1)
                    .filter(path -> !path.equals(this.rootRawLocation))
                    .map(this.rootRawLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }
    public Stream<Path>loadAllInThumbnail(){
        try {
            return Files.walk(this.rootThumbnailLocation,1)
                    .filter(path -> !path.equals(this.rootThumbnailLocation))
                    .map(this.rootThumbnailLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootRawLocation.resolve(filename);
    }

    public Path loadThumbnail(String filename) {
        return rootThumbnailLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read raw file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read raw file: " + filename, e);
        }
    }

    public Resource loadThumbnailAsResource(String filename) {
        try {
            Path file = loadThumbnail(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read thumbnail file: " + filename
                );
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read thumbnail file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {

        FileSystemUtils.deleteRecursively(rootRawLocation.toFile());
        FileSystemUtils.deleteRecursively(rootThumbnailLocation.toFile());
    }

    @Override
    public long delete(String fileName) {
        File targetFile = rootRawLocation.resolve(fileName).toFile();
        File thumbFile = rootThumbnailLocation.resolve(fileName).toFile();
        long size = 0;
        if (targetFile.exists() && targetFile.isFile()) {
            size = targetFile.length();
        }
        FileSystemUtils.deleteRecursively(targetFile);
        FileSystemUtils.deleteRecursively(thumbFile);
        return size;
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootRawLocation);
            Files.createDirectories(rootThumbnailLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
