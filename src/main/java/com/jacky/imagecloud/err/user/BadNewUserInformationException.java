package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.err.BaseException;

public class BadNewUserInformationException extends BaseException {
    public BadNewUserInformationException(String message){
        super(message);
    }
    public BadNewUserInformationException(String message,Throwable throwable){
        super(message,throwable);
    }
}
