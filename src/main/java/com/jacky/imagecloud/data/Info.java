package com.jacky.imagecloud.data;

public class Info <T>{
     T value;
     Class<?>valueType;
     String name;

    public static<T> Info<T> of(T value,String name){
        var info=new Info<T>();
        info.value=value;
        info.name=name;
        info.valueType=value.getClass();

        return info;
    }

    @Override
    public String toString() {
        return String.format("[%s<%s>: %s]",name,valueType.getName(),value);
    }
}
