package com.jacky.imagecloud.err;

public class StorageException extends RuntimeException {
    public StorageException(String s) {
        super(s);
    }
    public StorageException(String s,Throwable t){
        super(s,t);
    }
}
