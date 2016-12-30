package com.ascba.rebate.beans;

/**
 * Created by Administrator on 2016/12/16.
 */

public class WhiteTicket {
    private String money;
    private int id;
    private int status;
    private String time;
    private String leftTime;


    public WhiteTicket(String money, int id) {
        this.money = money;
        this.id = id;
    }

    public WhiteTicket(String money, int id, int status, String time, String leftTime) {
        this.money = money;
        this.id = id;
        this.status = status;
        this.time = time;
        this.leftTime = leftTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(String leftTime) {
        this.leftTime = leftTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public WhiteTicket(String money) {
        this.money = money;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }
}
