package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.err.BaseException;
import com.jacky.imagecloud.models.users.User;

public class UserNotVerifiedException extends BaseException {
    public UserNotVerifiedException(String user){
        super(String.format("User<%s> not Verify",user));
    }
}
