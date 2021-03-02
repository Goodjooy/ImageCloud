package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.data.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/m")
    public Result<List<String>>testMutGet(
            @RequestParam(value = "m",defaultValue = "1,2,1")String []data
    ){
        return new Result<>(List.of(data));
    }
}
