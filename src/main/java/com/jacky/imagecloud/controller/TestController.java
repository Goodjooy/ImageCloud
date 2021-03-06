package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.data.Result;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/test")
public class TestController {

    @GetMapping("/m")
    @ResponseBody
    public Result<List<String>> testMutGet(
            @RequestParam(value = "m", defaultValue = "1|2|1%2cffff") String[] data
    ) {
        return new Result<>(List.of(data));
    }

    @GetMapping("/e")
    @ResponseBody
    public void exp() {
        throw new NullPointerException("ababab");
    }

    @GetMapping("/sign-in")
    public String getSignInPage() {
        return "sign_in";
    }

    @GetMapping("/sign-up")
    public String getSignUpPage() {
        return "sign-up";
    }

    @GetMapping("/reset-paswd")
    public String resetPassword() {
        return "reset-paswd";
    }

    @GetMapping("/user-verify")
    public String userVerify() {
        return "user-verify";
    }

    @GetMapping("/user-find-password")
    public String findPassword(
            Model model,
            @RequestParam(name = "email") String email
    ) {
        model.addAttribute("email", email);
        return "user-find-password";
    }

    @GetMapping(path = "/upload")
    public String uploadFile() {
        return "file-upload";
    }

    @GetMapping(path = "/headup")
    public String uploadHead() {
        return "head-upload";
    }

    @GetMapping("/test-cors")
    public String testCors() {
        return "test-cors";
    }
}
