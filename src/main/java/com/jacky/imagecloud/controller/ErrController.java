package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.data.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrController {

    public Result<Boolean>err_404(){
        return null;
    }
}
