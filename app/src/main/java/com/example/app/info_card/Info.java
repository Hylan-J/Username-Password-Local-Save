package com.example.app.info_card;

import android.graphics.Bitmap;

public class Info {
    // 位移地点
    private String location;
    // 位移发生时间
    private String time;
    // 位移距离
    private String distance;
    // 位移等级
    private String level;
    // 位移图像
    private Bitmap map;



    public Info(String location, String time, String distance, String level, Bitmap map){
        this.location = location;
        this.time = time;
        this.distance = distance;
        this.level = level;
        this.map = map;
    }

    public String getLocation() {
        return location;
    }

    public String getTime() {
        return time;
    }

    public String getDistance() {
        return distance;
    }

    public String getLevel(){
        return level;
    }

    public Bitmap getMap(){
        return map;
    }
}
