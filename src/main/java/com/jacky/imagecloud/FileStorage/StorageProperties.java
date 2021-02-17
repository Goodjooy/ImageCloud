package com.jacky.imagecloud.FileStorage;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties("fileuploader")
public class StorageProperties {

    private String SavePath="/root/storage_data";

    public String getSavePath() {
        return SavePath;
    }

    public void setSavePath(String savePath) {
        SavePath = savePath;
    }
}
