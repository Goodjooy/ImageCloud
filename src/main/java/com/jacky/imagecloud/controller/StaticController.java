package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.FileStorage.FileService.PageStaticFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/page/static", method = RequestMethod.GET)
public class StaticController {

    @Autowired
    PageStaticFileService fileService;

    @GetMapping("/html/**")
    @ResponseBody
    public ResponseEntity<Resource> loadHtml(
            HttpServletRequest request
    ) {
        var requestPath = request.getRequestURI().substring(13);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(fileService.loadAsResource(requestPath));
    }

    @GetMapping("/css/**")
    @ResponseBody
    public ResponseEntity<Resource> loadCss(
            HttpServletRequest request
    ) {
        var requestPath = request.getRequestURI().substring(13);
        return ResponseEntity
                .ok()
                .body(fileService.loadAsResource(requestPath));
    }

    @GetMapping("/js/**")
    @ResponseBody
    public ResponseEntity<Resource> loadJs(
            HttpServletRequest request
    ) {
        var requestPath = request.getRequestURI().substring(13);
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(fileService.loadAsResource(requestPath));
    }
}
