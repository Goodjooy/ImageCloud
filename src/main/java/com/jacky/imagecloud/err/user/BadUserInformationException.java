package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.err.BaseException;

public class BadUserInformationException extends BaseException {
    public BadUserInformationException(String message){
        super(message);
    }
    public BadUserInformationException(String message, Throwable throwable){
        super(message,throwable);
    }
}
