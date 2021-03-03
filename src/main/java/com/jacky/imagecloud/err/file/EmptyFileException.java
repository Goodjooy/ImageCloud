package com.jacky.imagecloud.err.file;

import com.jacky.imagecloud.err.BaseException;
import org.springframework.web.multipart.MultipartFile;

public class EmptyFileException extends BaseException{
    public EmptyFileException(MultipartFile file){
        super(String.format("target File<%s> is Empty" ,file.getOriginalFilename()));
    }
}
