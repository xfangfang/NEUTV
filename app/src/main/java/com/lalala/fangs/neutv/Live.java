package com.lalala.fangs.neutv;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by FANGs on 2017/3/2.
 */

public class Live extends DataSupport implements Serializable {
//            "num":50001,
//            "itemid":"uid0",
//            "name":"CCTV-1高清",
//            "epgid":"cntv-zhejiang",
//            "quality":"HD",
//            "urllist":"http://media2.neu6.edu.cn/hls/cctv1hd.m3u8#http://media2.neu6.edu.cn/hls/jlu_cctv1hd.m3u8"

    private boolean isFavorite;
    private int num;
    private String  itemid,name,epgid,quality,urllist;

    Live(){
        isFavorite = false;
    }

    public boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getItemid() {
        return itemid;
    }

    public void setItemid(String itemid) {
        this.itemid = itemid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEpgid() {
        return epgid;
    }

    public void setEpgid(String epgid) {
        this.epgid = epgid;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getUrllist() {
        return urllist;
    }

    public void setUrllist(String urllist) {
        this.urllist = urllist;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Live) {
            if (this.getNum() == ((Live) obj).getNum()) {
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }

}
