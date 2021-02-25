package com.jacky.imagecloud.controller.UserDataControllers;

import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.UserNotFoundException;
import com.jacky.imagecloud.models.users.User;
import com.jacky.imagecloud.models.users.UserInformation;
import com.jacky.imagecloud.models.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class UserInformationController {
    Logger logger= LoggerFactory.getLogger(UserInformationController.class);

    @Autowired
    UserRepository userRepository;

    @GetMapping(path = "/walk")
    @ResponseBody
    public Result<User> getUserFullInfo(Authentication authentication) {
        try {
            User user = User.databaseUser(userRepository,authentication);

            logger.info(String.format("load full User Info<%s|%s> success", user.emailAddress, user.name));
            return new Result<>(user);
        } catch (Exception e) {
            logger.error(String.format("load User<%s> failure", authentication.getName()), e);
            return new Result<>(e.getMessage());
        }
    }
    @GetMapping(path = "/information")
    public Result<UserInformation>getUserInfo(Authentication authentication){
        try {
            User user=User.databaseUser(userRepository,authentication);
            logger.info(String.format("load full User Info<%s|%s> success", user.emailAddress, user.name));
            return new Result<>(user.information);
        } catch (UserNotFoundException e) {
            logger.error(String.format("load User<%s> failure", authentication.getName()), e);
            return new Result<>(e.getMessage());
        }
    }

    @GetMapping(path = "/head{size:\\d+}/{filename:.+}")
    public ResponseEntity<Resource>getUserHead(
            Authentication authentication,
            @PathVariable(name = "size")int size,
            @PathVariable(name = "filename")String filename
    ){

        return new ResponseEntity<>(HttpStatus.GONE);
    }


}
