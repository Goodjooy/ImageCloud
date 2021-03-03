package com.jacky.imagecloud.FileStorage.FileService;

import com.jacky.imagecloud.FileStorage.Resource.OutputStreamResource;
import com.jacky.imagecloud.configs.StorageProperties;
import com.jacky.imagecloud.FileStorage.image.ImageProcess;
import com.jacky.imagecloud.err.file.FileFormatNotSupportException;
import com.jacky.imagecloud.err.file.ImageSizeNotSupport;
import com.jacky.imagecloud.err.file.StorageException;
import com.jacky.imagecloud.models.users.UserImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Service
public class HeadImageStorageService implements FileUploader<UserImage> {
    private final Path localPath;
    private final List<Integer> sizeRange = List.of(16, 32, 64, 128, 256, 512);

    @Autowired
    public HeadImageStorageService(StorageProperties properties) {
        localPath = Path.of(properties.getSavePath(), "UserHead");
        init();
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
        return storage(file, UserImage.generateNameImage());
    }

    public UserImage storage(MultipartFile file, UserImage image) {
        if (file.isEmpty())
            throw new StorageException("Failed to store empty file.");
        if (file.getOriginalFilename() == null)
            throw new StorageException("Failed to store file with empty file name");

        var filename = file.getOriginalFilename();
        var generateName = image.getFileName().split("\\.")[0];
        var fileFormat = filename.substring(filename.lastIndexOf(".") + 1);

        if (!List.of(ImageIO.getReaderFormatNames()).contains(fileFormat))
            throw new FileFormatNotSupportException(String.format("the file format of <%s> not support", fileFormat));
        var SaveFilename = String.format("%s.%s", generateName, fileFormat);
        var savePath = localPath.resolve(Path.of(SaveFilename)).normalize().toAbsolutePath();

        image.setFileName(SaveFilename);
        image.setSetHeaded(true);
        try {
            var img = ImageIO.read(file.getInputStream());
            var splitLen = Math.min(img.getWidth(), img.getHeight());
            var x = Math.round((img.getWidth() - splitLen) / 2.0);
            var y = Math.round((img.getHeight() - splitLen) / 2.0);
            var SplitImage = img.getSubimage((int) x, (int) y, splitLen, splitLen);
            var resizedImage = ImageProcess.transformImageIntoSquareFromBufferedImage(
                    SplitImage, 512
            );
            ImageProcess.BufferImageToFile(resizedImage, fileFormat, savePath.toFile());
            return image;
        } catch (IOException e) {
            throw new StorageException("Failure to store file", e);
        }
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


        return localPath.resolve(filePath);
    }

    public Resource loadAsResource(String filePath, int size) {


        if (!sizeRange.contains(size))
            throw new ImageSizeNotSupport(String.format("size<%s> not in range <%s>", size, sizeRange));
        var filename = load(filePath);
        var fileFormat = filePath.substring(filePath.lastIndexOf(".") + 1);
        try {
            BufferedImage image;

            image = ImageProcess.transformImageFromFile(filename.toFile(), size / 512.0f);
            var output = ImageProcess.BufferImageToOutputStream(image, fileFormat);
            return new OutputStreamResource(output, filename);
        } catch (IOException | FileFormatNotSupportException e) {
            throw new StorageException("Fail to get resource", e);
        }
    }

    @Override
    public Resource loadAsResource(String filePath) {
        return loadAsResource(filePath, 512);

    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(localPath.toFile());
    }

    @Override
    public long delete(String fileName) {
        var file = localPath.resolve(fileName).toFile();
        var size = file.length();
        FileSystemUtils.deleteRecursively(file);
        return size;
    }
}
