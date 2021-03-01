package com.jacky.imagecloud.data;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

public class LocalFile {
    private String filename;
    private File file;

    static void  newLocalFile(String filename, Path path){
        String exName = "";
        String fileExtra = "";
        var temp = Pattern.compile(".+(\\.([a-zA-Z0-9_-]+))$").matcher(filename);
        if (temp.find()) {
            exName = temp.group(1);
            fileExtra = temp.group(2);
        }
        String saveFileName = UUID.randomUUID().toString() + exName;


    }
}
