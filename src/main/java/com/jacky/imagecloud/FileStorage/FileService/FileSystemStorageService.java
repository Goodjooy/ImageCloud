package com.jacky.imagecloud.FileStorage.FileService;

import com.jacky.imagecloud.FileStorage.Resource.OutputStreamResource;
import com.jacky.imagecloud.FileStorage.image.ImageProcess;
import com.jacky.imagecloud.configs.StorageProperties;
import com.jacky.imagecloud.err.file.FileFormatNotSupportException;
import com.jacky.imagecloud.err.file.StorageException;
import com.jacky.imagecloud.err.file.StorageFileNotFoundException;
import com.jacky.imagecloud.models.items.FileStorage;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements FileUploader<FileStorage> {


    private final Path rootRawLocation;
    private final Path rootThumbnailLocation;

    private static final float maxSize = 256;


    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootRawLocation = Paths.get(properties.getSavePath(), "Storage");
        this.rootThumbnailLocation = Paths.get(properties.getSavePath(), "Thumbnail");

        this.init();
    }

    public ByteArrayOutputStream copyStream(InputStream inputStream) {
        ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[inputStream.available()];
            var t = inputStream.read(buffer);
            byteArrayInputStream.write(buffer);
            byteArrayInputStream.flush();
            return byteArrayInputStream;
        } catch (IOException e) {
            e.printStackTrace();
            throw new StorageException(String.format("Clone InputStream Failure | Reason:<%s>: %s",
                    e.getClass().getName(), e.getMessage()
            ), e);
        }
    }

    public void SaveImageWithThumbnail(InputStream inputStream,
                                       @NotNull String fileName,
                                       @NotNull String fileExtra) throws IOException {
        var supports = List.of(ImageIO.getReaderFormatNames());
        if (!supports.contains(fileExtra.toLowerCase()))
            throw new FileFormatNotSupportException(String.format("file format name<%s> not support", fileExtra));

        var copyStream = copyStream(inputStream);
        //图像压缩比计算
        var ThumbnailImage = ImageProcess.transformImage(
                new ByteArrayInputStream(copyStream.toByteArray()), fileName, (int) maxSize);

        var RawPath = rootRawLocation.resolve(
                Path.of(Objects.requireNonNull(fileName))
        ).normalize().toAbsolutePath();
        var ThumbnailPath = rootThumbnailLocation.resolve(
                Path.of(fileName)
        ).normalize().toAbsolutePath();

        if (!RawPath.getParent().equals(rootRawLocation.toAbsolutePath()) || !
                ThumbnailPath.getParent().equals(rootThumbnailLocation.toAbsolutePath()))
            throw new StorageException("Cannot store file outside current directory.");

        Files.copy(new ByteArrayInputStream(copyStream.toByteArray()), RawPath, StandardCopyOption.REPLACE_EXISTING);
        ImageProcess.BufferImageToFile(ThumbnailImage,fileExtra,ThumbnailPath.toFile());

    }

    @Override
    public FileStorage storage(MultipartFile file) {

        try {
            FileStorage storage = new FileStorage();
            if (file.isEmpty()) {
                throw new StorageException(String.format("Failed to store empty file<%s>.", file.getOriginalFilename()));
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
            throw new StorageException(String.format("Failed to store file.| %s: %s"
                    , e.getClass().getName(), e.getMessage()
            ), e);
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

    public Stream<Path> loadAllInThumbnail() {
        try {
            return Files.walk(this.rootThumbnailLocation, 1)
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
                Path RawFile = load(filename);
                var Image = ImageProcess.transformImage(RawFile.toFile(), (int) maxSize);

                ImageProcess.BufferImageToFile(Image, ImageProcess.getFileFormat(file), file.toFile());

                return new OutputStreamResource(ImageProcess.BufferImageToOutputStream(Image,
                        ImageProcess.getFileFormat(filename)), file);
            }
        } catch (IOException e) {
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
