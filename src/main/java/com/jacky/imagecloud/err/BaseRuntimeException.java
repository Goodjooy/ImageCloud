package com.jacky.imagecloud.err;

public abstract class BaseRuntimeException extends RuntimeException{

    public BaseRuntimeException(String meg){
        super(meg);
    }
    public BaseRuntimeException(String meg,Throwable throwable){
        super(meg,throwable);
    }
    public BaseRuntimeException(Throwable throwable){
        super(throwable);
    }
}
