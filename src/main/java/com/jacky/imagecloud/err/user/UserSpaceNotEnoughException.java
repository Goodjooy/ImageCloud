package com.jacky.imagecloud.err.user;

import com.jacky.imagecloud.FileStorage.FileService.FileUploader;
import com.jacky.imagecloud.err.BaseException;

public class UserSpaceNotEnoughException extends BaseException {
    public UserSpaceNotEnoughException(long available,long now){
        super(String.format("User Storage Space[%s] Not Enough For Save Target File[%s]",
                FileUploader.formatSize(available),
                FileUploader.formatSize(now))
        );
    }
}
