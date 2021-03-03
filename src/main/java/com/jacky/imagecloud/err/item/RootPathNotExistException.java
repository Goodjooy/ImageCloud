package com.jacky.imagecloud.err.item;

import com.jacky.imagecloud.err.BaseException;

public class RootPathNotExistException extends BaseException {
    public RootPathNotExistException(){
        super("root path <root> not exist");
    }
}
