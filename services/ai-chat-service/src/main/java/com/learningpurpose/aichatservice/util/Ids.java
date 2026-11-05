package com.learningpurpose.aichatservice.util;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import java.util.UUID;

public class Ids {
    private Ids() {}

    public static UUID timeBased() {
        return Uuids.timeBased();
    }

    public static UUID uuid(){
        return UUID.randomUUID();
    }

    public static String inviteCode(){
        return UUID.randomUUID().toString().replace("-","")
                .substring(0,8).toUpperCase();
    }
}
