package com.jacky.imagecloud.models.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.FileStorage.image.ImageProcess;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

@Entity
public class FileStorage {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    public Item item;

    @JsonIgnore
    @Column(name = "path", nullable = false)
    public String filePath;

    public String getFileURL() {
        return String.format("/storage/%s", filePath);
    }
    public String getThumbnailURL(){
        return String.format("/thumbnail/%s",filePath);
    }

    public static FileStorage newFileStorage(String filename){
        var fileFormat= ImageProcess.getFileFormat(filename);
        var newFilename= UUID.randomUUID().toString()+"."+fileFormat;

        FileStorage storage=new FileStorage();
        storage.filePath=newFilename;
        return storage;
    }

    public static FileStorage cloneFileStorage(File originFile){
        return newFileStorage(originFile.getName());
    }
    public static FileStorage newFileStorage(MultipartFile file){
        return newFileStorage(file.getOriginalFilename());

    }
}
