package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.err.BaseException;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String format) {
        super(format);
    }
}
