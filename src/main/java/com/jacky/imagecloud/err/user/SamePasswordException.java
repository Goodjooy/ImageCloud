package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.err.BaseException;

public class SamePasswordException extends BaseException {
    public SamePasswordException(){
        super("new Password is the same as the old one");
    }
}
