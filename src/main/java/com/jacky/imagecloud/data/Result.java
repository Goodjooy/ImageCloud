package com.jacky.imagecloud.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.servlet.ServletRequest;

public class Result<DATA> {
    public final DATA data;
    public final boolean err;
    public final String message;
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

    public static <T> Result<T> failureResult(Exception e) {
        return new Result<>(null, true, String.format("exception: <%s>: %s", e.getClass().getName(), e.getMessage()), e);
    }

    public static <T extends Throwable> Result<T> exceptionResult(T exception, ServletRequest request) {
        var message = String.format("IP:[%s] Request<%s>  throw Exception<%s>: %s"
                , request.getRemoteAddr(), request.getRemotePort(),
                exception.getClass().getName(), exception.getMessage()
        );
        return new Result<>(exception, true, message);
    }

}
