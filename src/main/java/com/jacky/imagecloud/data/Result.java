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
}
