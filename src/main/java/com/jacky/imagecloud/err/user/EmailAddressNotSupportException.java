package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.err.BaseException;

public class EmailAddressNotSupportException extends BaseException {
    public EmailAddressNotSupportException(String email, String message){
        super(String.format("Email Address<%s> Not Support | Reason: %s",email,message));
    }
    public EmailAddressNotSupportException(String email, Throwable message){
        super(String.format("Email Address<%s> Not Support | Reason: %s",email,message.getMessage()),message);
    }
}
