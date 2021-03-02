package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.data.LoggerHandle;
import com.jacky.imagecloud.data.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletRequest;

@RestController
public class ErrController {
    LoggerHandle logger = LoggerHandle.newLogger(ErrController.class);

    @ExceptionHandler(Throwable.class)
    public Result<Throwable> handleException(
            ServletRequest request,
            Throwable exception
    ) {
        logger.error(exception);
        return Result.exceptionResult(exception, request);
    }
}
