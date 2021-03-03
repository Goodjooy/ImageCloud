package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.err.BaseException;

public class ResetPasswordFailureException extends BaseException {

    public ResetPasswordFailureException(String message) {
        super("Reset Password Failure : " + message);
    }
}
