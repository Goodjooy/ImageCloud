package com.jacky.imagecloud.err;

public abstract class BaseException extends Exception {
    public BaseException(String meg){
        super(meg);
    }
    public BaseException(String meg,Throwable throwable){
        super(meg,throwable);
    }
    public BaseException(Throwable throwable){
        super(throwable);
    }
}

