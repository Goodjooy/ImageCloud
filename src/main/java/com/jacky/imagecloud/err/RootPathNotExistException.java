package com.jacky.imagecloud.err;

public class RootPathNotExistException extends Exception{
    public RootPathNotExistException(){
        super("root path <root> not exist");
    }
}
