package com.jacky.imagecloud.err;

public class FileFormatNotSupportException extends RuntimeException{
    public FileFormatNotSupportException(String message){
        super(message);
    }
    public FileFormatNotSupportException(String message,Throwable throwable){
        super(message,throwable);
    }
}
