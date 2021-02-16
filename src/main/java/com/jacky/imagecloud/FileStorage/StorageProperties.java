package com.jacky.imagecloud.FileStorage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;


@ConfigurationProperties("fileuploader")
public class StorageProperties {

    private String SavePath="E:\\test-upload\\";

    public String getSavePath() {
        return SavePath;
    }

    public void setSavePath(String savePath) {
        SavePath = savePath;
    }
}
