package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.FileStorage.FileService.FileService;
import com.jacky.imagecloud.err.BaseException;

public class UserSpaceNotEnoughException extends BaseException {
    public UserSpaceNotEnoughException(long available,long now){
        super(String.format("User Storage Space[%s] Not Enough For Save Target File[%s]",
                FileService.formatSize(available),
                FileService.formatSize(now))
        );
    }
}
