package com.jacky.imagecloud.data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.UUID;

public class VerifyCodeContainer {
    private String code;
    private boolean activate;

    private LocalDateTime createTime;

    private static final int activateTime = 5;

    public static VerifyCodeContainer newVerify() {
        var verify = new VerifyCodeContainer();
        verify.code = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8).toUpperCase();
        verify.activate = true;
        verify.createTime = LocalDateTime.now();

        return verify;
    }

    public String getCode() {
        return code;
    }

    public boolean match(String code) {
       try {
           if (isActivate()) {
               return code.equalsIgnoreCase(this.code);
           }
           return false;
       }finally {
           activate = false;
       }
    }

    public boolean isActivate() {
        if (!activate)return false;
        var nowTime = LocalDateTime.now();
        activate = createTime.plusMinutes(activateTime).isAfter(nowTime);
        return activate;
    }

    public boolean noNeedNewGenerate(LocalDateTime now){
        return !createTime.plusSeconds(60).isBefore(now);
    }

    public long deltaTime(LocalDateTime now){
        Duration duration=Duration.between(createTime,now);
        return duration.toSeconds();
    }

    @Override
    public String toString() {
        return "VerifyCodeContainer{" +
                "code='" + code + '\'' +
                ", activate=" + activate +
                ", createTime=" + createTime +
                '}';
    }
}
