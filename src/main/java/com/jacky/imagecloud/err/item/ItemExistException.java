package com.jacky.imagecloud.err.item;

import com.jacky.imagecloud.err.BaseException;

public class ItemExistException extends BaseException {
    public ItemExistException(String itemPath) {
        super(String.format("All Item On Path<%s> Are Exist",itemPath));
    }
}
