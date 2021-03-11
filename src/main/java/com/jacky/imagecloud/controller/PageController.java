package com.jacky.imagecloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacky.imagecloud.configs.StorageProperties;
import com.jacky.imagecloud.data.PageGuiderManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Path;

@Controller
@RequestMapping("/pages")
public class PageController {

    Path configFilePath;
    PageGuiderManager guiders = new PageGuiderManager();

    public PageController(StorageProperties properties) {
        configFilePath = Path.of(properties.getSavePath(), "config", "config.json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            guiders = mapper.readValue(configFilePath.toFile(), guiders.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/{pageName}")
    public String loadPage(Model model,
                           @PathVariable("pageName") String pageName){
        var targetStartFile=guiders.get(pageName);
        if(targetStartFile!=null){
            model.addAttribute("jsUrl",targetStartFile.jsPath);
        }

        return "page";
    }

}
