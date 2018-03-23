package com.lalala.fangs.neutv;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by fang on 2018/3/23.
 */

public class DataSaver {
    private int loginTime;
    private SharedPreferences sp;
    private SharedPreferences.Editor spEdit;
    DataSaver(Context context){
        sp = context.getSharedPreferences("APPCONFIG", Context.MODE_PRIVATE);
        spEdit = context.getSharedPreferences("APPCONFIG", Context.MODE_PRIVATE).edit();
    }

    public int getLoginTime(){
        loginTime = sp.getInt("loginTime", 0);
        return loginTime;
    }

    public void incLoginTime(){
        loginTime++;
        spEdit.putInt("loginTime", loginTime);
        spEdit.apply();
    }

    public String getLiveUrl(){
        return sp.getString("liveUrl", "http://hdtv.neu6.edu.cn/hdtv.json");

    }

    public void setLiveUrl(String url){
        spEdit.putString("liveUrl", url);
        spEdit.apply();
    }
}
