package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.err.BaseException;

import java.time.LocalDateTime;

public class NotEnoughTimeBetweenDifferentVerifyCodeGenerationException extends BaseException {
    public<T extends Number> NotEnoughTimeBetweenDifferentVerifyCodeGenerationException(T time){
        super(String.format("the time<%s s> before generate different Verify Code is less then 60(s)",time));
    }
}
