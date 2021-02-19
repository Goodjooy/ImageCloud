package com.jacky.imagecloud.FileStorage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.util.Locale;


@ConfigurationProperties("fileuploader")
public class StorageProperties {


    private String SavePath;

    public StorageProperties(){
        var path= Path.of("/root/storage_data");

        if(!path.toAbsolutePath().toFile().exists()){
            SavePath="E:\\test-upload";
        }else {
            SavePath=path.toString();
        }
    }

    public String getSavePath() {
        return SavePath;
    }

    public void setSavePath(String savePath) {
        SavePath = savePath;
    }
}
