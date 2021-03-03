package com.jacky.imagecloud.err.file;

import com.jacky.imagecloud.err.BaseRuntimeException;

public class FileFormatNotSupportException extends BaseRuntimeException {
    public FileFormatNotSupportException(String message){
        super(message);
    }
    public FileFormatNotSupportException(String message,Throwable throwable){
        super(message,throwable);
    }
}
