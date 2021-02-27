package com.jacky.imagecloud.FileStorage.FileService;

import com.jacky.imagecloud.FileStorage.Resource.OutputStreamResource;
import com.jacky.imagecloud.FileStorage.StorageProperties;
import com.jacky.imagecloud.err.FileFormatNotSupportException;
import com.jacky.imagecloud.err.ImageSizeNotSupport;
import com.jacky.imagecloud.err.StorageException;
import com.jacky.imagecloud.models.users.UserImage;
import org.hibernate.id.GUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class HeadImageStorageService implements FileUploader<UserImage> {
    private final Path localPath;
    private final List<Integer> sizeRange=List.of(16,32,64,128,256,512);
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
        if (file.isEmpty())
            throw new StorageException("Failed to store empty file.");
        if(file.getOriginalFilename()==null)
            throw new StorageException("Failed to store file with empty file name");
        UserImage image=new UserImage();
        var filename=file.getOriginalFilename();
        var generateName= UUID.randomUUID().toString();
        var fileFormat=filename.substring(filename.lastIndexOf(".")+1);

        if(!List.of(ImageIO.getReaderFormatNames()).contains(fileFormat))
            throw new FileFormatNotSupportException(String.format("the file format of <%s> not support",fileFormat));
        var SaveFilename=String.format("%s.%s",generateName,fileFormat);
        var savePath=localPath.resolve(Path.of(SaveFilename)).normalize().toAbsolutePath();

        image.setFileName(SaveFilename);
        image.setSetHeaded(true);
        try {
            var img= ImageIO.read(file.getInputStream());
            var flag=img.getType();
            var maxSized=img.getScaledInstance(512,512, Image.SCALE_SMOOTH);
            var result=new BufferedImage(512,512,flag);
            result.getGraphics().drawImage(maxSized,0,0, null);

            ImageIO.write(result,fileFormat,savePath.toFile());
            return image;
        } catch (IOException e) {
            throw new StorageException("Failure to store file",e);
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

    public Resource loadAsResource(String filePath,int size){
        if(!sizeRange.contains(size))throw new ImageSizeNotSupport(String.format("size<%s> not in range <%s>",size,sizeRange));
        var filename=load(filePath);
        var fileFormat=filePath.substring(filePath.lastIndexOf(".")+1);
        try {
            var img=ImageIO.read(filename.toFile());
            var flag=img.getType();

            var midImage=img.getScaledInstance(size,size,Image.SCALE_SMOOTH);
            var result=new BufferedImage(size,size,flag);
            result.getGraphics().drawImage(midImage,0,0,null);

            var Output=new ByteArrayOutputStream();

            ImageIO.write(result, fileFormat,Output);

            return new OutputStreamResource(Output,filename);
        } catch (IOException e) {
            throw new StorageException("Fail to get resource",e);
        }
    }
    @Override
    public Resource loadAsResource(String filePath) {
        return loadAsResource(filePath,512);

    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(localPath.toFile());
    }

    @Override
    public long delete(String fileName) {
        var file=localPath.resolve(fileName).toFile();
        var size=file.length();
        FileSystemUtils.deleteRecursively(file);
        return size;
    }
}
