package com.jacky.imagecloud.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public class Result<DATA> {
    public final DATA data;
    public final boolean err;
    public String message;
    @JsonIgnore
    public final Throwable e;

    public Result(DATA data, boolean err, String message, Throwable throwable) {
        this.data = data;
        this.err = err;
        this.message = message;
        e = throwable;
    }

    public Result(DATA data, boolean err, String message) {
        this(data, err, message, null);

    }

    public Result(DATA data) {
        this(data, false, "");
    }

    public Result(String message) {
        this(null, true, message);
    }

    public static <T> Result<T> okResult(T data) {
        return new Result<>(data, false, "");
    }

    public static <T> Result<T> failureResult(String message) {
        return new Result<>(null, true, message);
    }

    public static <T ,E extends Throwable> Result<T> failureResult(E e) {
        var eNames=e.getClass().getName().split("\\.");
        var name="";
        if (!(eNames.length ==0)){
            name=eNames[eNames.length-1];
        }

        return new Result<>(null, true, String.format("Exception<%s>: %s", name, e.getMessage()), e);
    }
    public static <T,E extends Throwable> Result<T> exceptionCatchResult(E e, HttpServletRequest request){
        Result<T> t= Result.failureResult(e);
        t.message+=String.format(" | while Request URL: %s | %s | IP[%s]",request.getRequestURI(),request.getMethod(),
                request.getRemoteAddr());
        return t;
    }

    public static <T extends Throwable> Result<T> exceptionResult(T exception, ServletRequest request) {

        var message = String.format("IP:[%s] Request<%s>  throw Exception<%s>: %s"
                , request.getRemoteAddr(), request.getRemotePort(),
                exception.getClass().getName(), exception.getMessage()
        );
        return new Result<>(exception, true, message);
    }

}
