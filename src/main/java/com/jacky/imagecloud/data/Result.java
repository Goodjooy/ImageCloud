package com.jacky.imagecloud.data;

public class Result<DATA> {
    public final DATA data;
    public final boolean err;
    public final String message;


    public Result(DATA data, boolean err, String message){

        this.data = data;
        this.err = err;
        this.message = message;
    }
    public Result(DATA data){
        this(data,false,"");
    }
    public Result(String message){
        this(null,true,message);
    }

    public <T> Result<T>okResult(T data){
        return new Result<>(data,false,"");
    }
    public <T> Result<T>failureResult(String message){
        return new Result<>(null,true,message);
    }
    public <T> Result<T>failureResult(Exception e){
        return new Result<>(null,true,String.format("exception: <%s> ; message: %s",e.toString(),e.getMessage()));
    }
}
