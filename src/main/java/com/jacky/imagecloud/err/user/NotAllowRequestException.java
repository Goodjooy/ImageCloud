package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.err.BaseException;
import com.jacky.imagecloud.models.users.User;

public class NotAllowRequestException extends BaseException {
    public NotAllowRequestException(User user,String  file){
        super(String.format("The User <%s | %s> do not have authority to get file<%s>",
                user.emailAddress,user.name,file));
    }
}
