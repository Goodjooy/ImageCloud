package com.jacky.imagecloud.err;

public class UnknownItemTypeException extends RuntimeException{
    public UnknownItemTypeException(String message){
        super(message);
    }
    public UnknownItemTypeException(String message,Throwable err){
        super(message,err);
    }
}
