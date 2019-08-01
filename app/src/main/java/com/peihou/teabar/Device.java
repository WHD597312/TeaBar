package com.peihou.teabar;

import java.io.Serializable;

public class Device implements Serializable {
    String deviceMac;//设备mac标识
    boolean online;//设备在线标识
    int state;//设备状态
    int error;//设备故障
    int control;//控制命令 0xc0休眠 0xc1预热 0xc2冲泡
    int control2;//控制命令2 0xf0
    int control3;//控制命令3 bit7 1：led显示开 0关

    int crossR;//十字灯 R
    int crossG;//十字灯 G
    int crossB;//十字灯 B
    int outR;//外圈灯光 R
    int outG;//外圈灯光 G
    int outB;//外圈灯光 B
    int inR;//内圈灯光 R
    int inG;//内圈灯光 G
    int inB;//内圈灯光 B
    int oiR;//大小灯光R
    int oiG;//大小灯光G
    int oiB;//大小灯光B
    double water;//制作水量
    int preTemp;//预热温度
    double brewTemp;//浸泡温度
    int brewTime;//浸泡时间
    int brewPreStartTime;//浸泡前水泵启动时间
    double brewBl;
    int furnace;
    int led;
    int light;

    public int getFurnace() {
        return furnace;
    }

    public void setFurnace(int furnace) {
        this.furnace = furnace;
    }

    public int getLight() {
        return light;
    }

    public void setLight(int light) {
        this.light = light;
    }

    public int getLed() {
        return led;
    }

    public void setLed(int led) {
        this.led = led;
    }

    public double getBrewBl() {
        return brewBl;
    }

    public void setBrewBl(double brewBl) {
        this.brewBl = brewBl;
    }

    int brewCount;//浸泡次数

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public int getBrewCount() {
        return brewCount;
    }

    public void setBrewCount(int brewCount) {
        this.brewCount = brewCount;
    }

    int bigFlow;//大杯流量
    int smallFlow;//小杯流量
    int clearCount;//清洗次数

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getControl() {
        return control;
    }

    public void setControl(int control) {
        this.control = control;
    }

    public int getControl2() {
        return control2;
    }

    public void setControl2(int control2) {
        this.control2 = control2;
    }

    public int getControl3() {
        return control3;
    }

    public void setControl3(int control3) {
        this.control3 = control3;
    }


    public int getOiR() {
        return oiR;
    }

    public void setOiR(int oiR) {
        this.oiR = oiR;
    }

    public int getOiG() {
        return oiG;
    }

    public void setOiG(int oiG) {
        this.oiG = oiG;
    }

    public int getOiB() {
        return oiB;
    }

    public void setOiB(int oiB) {
        this.oiB = oiB;
    }

    public int getCrossR() {
        return crossR;
    }

    public void setCrossR(int crossR) {
        this.crossR = crossR;
    }

    public int getCrossG() {
        return crossG;
    }

    public void setCrossG(int crossG) {
        this.crossG = crossG;
    }

    public int getCrossB() {
        return crossB;
    }

    public void setCrossB(int crossB) {
        this.crossB = crossB;
    }

    public int getOutR() {
        return outR;
    }

    public void setOutR(int outR) {
        this.outR = outR;
    }

    public int getOutG() {
        return outG;
    }

    public void setOutG(int outG) {
        this.outG = outG;
    }

    public int getOutB() {
        return outB;
    }

    public void setOutB(int outB) {
        this.outB = outB;
    }

    public int getInR() {
        return inR;
    }

    public void setInR(int inR) {
        this.inR = inR;
    }

    public int getInG() {
        return inG;
    }

    public void setInG(int inG) {
        this.inG = inG;
    }

    public int getInB() {
        return inB;
    }

    public void setInB(int inB) {
        this.inB = inB;
    }

    public double getWater() {
        return water;
    }

    public void setWater(double water) {
        this.water = water;
    }

    public int getPreTemp() {
        return preTemp;
    }

    public void setPreTemp(int preTemp) {
        this.preTemp = preTemp;
    }

    public double getBrewTemp() {
        return brewTemp;
    }

    public void setBrewTemp(double brewTemp) {
        this.brewTemp = brewTemp;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getBrewTime() {
        return brewTime;
    }

    public void setBrewTime(int brewTime) {
        this.brewTime = brewTime;
    }

    public int getBrewPreStartTime() {
        return brewPreStartTime;
    }

    public void setBrewPreStartTime(int brewPreStartTime) {
        this.brewPreStartTime = brewPreStartTime;
    }

    public int getBigFlow() {
        return bigFlow;
    }

    public void setBigFlow(int bigFlow) {
        this.bigFlow = bigFlow;
    }

    public int getSmallFlow() {
        return smallFlow;
    }

    public void setSmallFlow(int smallFlow) {
        this.smallFlow = smallFlow;
    }

    public int getClearCount() {
        return clearCount;
    }

    public void setClearCount(int clearCount) {
        this.clearCount = clearCount;
    }
}
