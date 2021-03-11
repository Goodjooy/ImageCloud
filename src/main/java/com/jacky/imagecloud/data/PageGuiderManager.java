package com.jacky.imagecloud.data;

import java.util.HashMap;

public class PageGuiderManager {
     final HashMap<String ,PageGuider> guiders=new HashMap<>();

    public PageGuider get(String name){
        return guiders.get(name);
    }
}
