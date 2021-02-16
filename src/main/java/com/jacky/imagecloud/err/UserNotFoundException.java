package com.jacky.imagecloud.err;

public class UserNotFoundException extends Exception{
    public UserNotFoundException(String format) {
        super(format);
    }
}
