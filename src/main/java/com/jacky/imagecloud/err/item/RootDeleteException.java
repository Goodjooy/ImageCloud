package com.jacky.imagecloud.err.item;

import com.jacky.imagecloud.err.BaseException;

public class RootDeleteException extends BaseException {
    public RootDeleteException() {
        super("Root Item Can Not Delete");
    }
}
