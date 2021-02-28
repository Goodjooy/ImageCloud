package com.jacky.imagecloud.err;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String format) {
        super(format);
    }
}
