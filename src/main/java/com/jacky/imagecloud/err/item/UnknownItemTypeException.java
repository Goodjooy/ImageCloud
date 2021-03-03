package com.jacky.imagecloud.err.item;

import com.jacky.imagecloud.err.BaseException;

public class UnknownItemTypeException extends BaseException {
    public UnknownItemTypeException(String message){
        super(message);
    }
    public UnknownItemTypeException(String message,Throwable err){
        super(message,err);
    }
}
