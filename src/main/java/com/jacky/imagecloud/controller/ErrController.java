package com.jacky.imagecloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ErrController extends BasicErrorController {

    /**
     * Create a new {@link BasicErrorController} instance.
     *
     * @param errorProperties configuration properties
     */
    @Autowired
    public ErrController(ErrorProperties errorProperties) {
        super(new DefaultErrorAttributes(), errorProperties);
    }

    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        var method=request.getMethod();
        var url=request.getRequestURL().toString();
        var info=super.error(request);
        var body=info.getBody();

        Map<String ,Object>data=new HashMap<>();

        data.put("err",true);
        data.put("data",body);
        data.put("message",
                String.format(
                        "Method: %s\n" +
                                "requestURL : %s\n" +
                                "status : %s\n" ,
                        method,url,info.getStatusCode().toString()

                )
                );
        return ResponseEntity.status(info.getStatusCode()).body(data);
    }


}
