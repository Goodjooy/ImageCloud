package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.err.BaseException;
import com.jacky.imagecloud.models.users.User;

public class UserVerifiedException extends BaseException {
    public UserVerifiedException(User meg) {
        super(String.format("User<%s | %s> had Verified",meg.name,meg.emailAddress));
    }
}
