package com.jacky.imagecloud.err.item;

import com.jacky.imagecloud.err.BaseException;
import com.jacky.imagecloud.err.BaseRuntimeException;

public class ItemNotFoundException extends BaseException {
    public ItemNotFoundException(String meg) {
        super(String.format("path<%s> not exist",meg));
    }
}
