package com.jacky.imagecloud.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jacky.imagecloud.FileStorage.FileService.FileSystemStorageService;
import com.jacky.imagecloud.FileStorage.FileService.FileService;
import com.jacky.imagecloud.models.items.ItemType;

import javax.persistence.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

@Entity
public class UserInformation {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User user;


    @Column(nullable = false)
    public Long totalSize;

    @Column(nullable = false)
    public Long usedSize = 0L;

    @Column(nullable = false)
    public Boolean verify;


    public static UserInformation defaultUserInformation(User user){
        UserInformation information=new UserInformation();
        information.user=user;
        information.totalSize=5368709120L;
        information.usedSize=0L;
        information.verify=false;

        return information;
    }

    public String getFormatTotalSize(){return FileService.formatSize(totalSize);
    }
    public String getFormatUsedSize(){return FileService.formatSize(usedSize);}

    public Long availableSize() {
        return totalSize - usedSize;
    }

    public void AppendSize(Long size) {
        var t = usedSize + size;
        t = t < 0 ? 0 : t;
        usedSize = t;
    }
    public void checkUsedSize(FileSystemStorageService storageService){
        var allFiles=user.seizedFiles.stream().filter(item -> item.getItemType()== ItemType.FILE)
                .map(item -> item.file.filePath);
        var allFilesPath=allFiles.map(storageService::load);
        var allFileSize=allFilesPath.map(path -> {
            try {
                return Files.size(path);
            } catch (IOException e) {
                return 0L;
            }
        }).collect(Collectors.toList());
        long usedSize=0;
        for (long size :
                allFileSize) {
            usedSize+=size;
        }
        this.usedSize=usedSize;
    }
}
