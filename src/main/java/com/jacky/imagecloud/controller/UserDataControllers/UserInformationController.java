package com.jacky.imagecloud.controller.UserDataControllers;

import com.jacky.imagecloud.models.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserInformationController {
    Logger logger= LoggerFactory.getLogger(UserInformationController.class);

    @Autowired
    UserRepository userRepository;


}
