package com.jacky.imagecloud.err.item;

import com.jacky.imagecloud.err.BaseException;

public class ItemExistException extends BaseException {
    public ItemExistException(String itemPath) {
        super(String.format("All Item On Path<%s> Are Exist",itemPath));
    }
    public ItemExistException(String itemPath,String name) {
        super(String.format("Item<%s> On Path<%s> Are Exist",name,itemPath));
    }
}
