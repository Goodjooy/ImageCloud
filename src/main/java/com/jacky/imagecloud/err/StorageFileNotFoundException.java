package com.jacky.imagecloud.err;

import java.net.MalformedURLException;

public class StorageFileNotFoundException extends RuntimeException {
    public StorageFileNotFoundException(String s, Throwable e) {
        super(s,e);
    }

    public StorageFileNotFoundException(String s) {
        super(s);
    }
}
