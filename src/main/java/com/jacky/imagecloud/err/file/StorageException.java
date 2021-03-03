package com.jacky.imagecloud.err.file;

import com.jacky.imagecloud.err.BaseRuntimeException;

public class StorageException extends BaseRuntimeException {
    public StorageException(String s) {
        super(s);
    }
    public StorageException(String s,Throwable t){
        super(s,t);
    }
}
