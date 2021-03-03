package com.jacky.imagecloud.err.file;

import com.jacky.imagecloud.err.BaseRuntimeException;

import java.net.MalformedURLException;

public class StorageFileNotFoundException extends BaseRuntimeException {
    public StorageFileNotFoundException(String s, Throwable e) {
        super(s,e);
    }

    public StorageFileNotFoundException(String s) {
        super(s);
    }
}
