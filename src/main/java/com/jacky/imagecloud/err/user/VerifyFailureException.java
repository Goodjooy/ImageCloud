package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.err.BaseException;

public class VerifyFailureException extends BaseException {
    public VerifyFailureException(String code){
        super(String.format("Bad Verify Code: input verify code<%s> not match the generate verify code",code));
    }
    public VerifyFailureException(String code,String reason){
        super(String.format("Bad Verify Code: %s Reason: %s",code,reason));
    }
}
