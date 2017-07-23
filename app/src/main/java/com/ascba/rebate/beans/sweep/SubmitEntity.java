package com.ascba.rebate.beans.sweep;

/**
 * Created by lenovo on 2017/7/1.
 */

public class SubmitEntity {


    /**
     * uuid : 82
     * token : f1e0712de4486b48d101fe7e7cc52cfe
     * expiring_time : 1501662545
     * update_status : 0
     * tokenFail : false
     * isLogin : true
     * isExpiringTime : false
     * count : 0
     * info : {"order_id":"555","order_number":"XF20170703164331515453","avatar":"/public/uploads/seller/logo/85/85.png","name":"钱来钱往实体店体验","money":"-100.00","pay_type_text":"记账","order_status_text":"交易中","member_username":"158****2251","score":3000,"pay_commission":"¥8.00","pay_type":1,"create_time":1499071411}
     */

    private int uuid;
    private String token;
    private int expiring_time;
    private int update_status;
    private boolean tokenFail;
    private boolean isLogin;
    private boolean isExpiringTime;
    private int count;
    private InfoBean info;

    public int getUuid() {
        return uuid;
    }

    public void setUuid(int uuid) {
        this.uuid = uuid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpiring_time() {
        return expiring_time;
    }

    public void setExpiring_time(int expiring_time) {
        this.expiring_time = expiring_time;
    }

    public int getUpdate_status() {
        return update_status;
    }

    public void setUpdate_status(int update_status) {
        this.update_status = update_status;
    }

    public boolean isTokenFail() {
        return tokenFail;
    }

    public void setTokenFail(boolean tokenFail) {
        this.tokenFail = tokenFail;
    }

    public boolean isIsLogin() {
        return isLogin;
    }

    public void setIsLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }

    public boolean isIsExpiringTime() {
        return isExpiringTime;
    }

    public void setIsExpiringTime(boolean isExpiringTime) {
        this.isExpiringTime = isExpiringTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public InfoBean getInfo() {
        return info;
    }

    public void setInfo(InfoBean info) {
        this.info = info;
    }

    public static class InfoBean {
        /**
         * order_id : 555
         * order_number : XF20170703164331515453
         * avatar : /public/uploads/seller/logo/85/85.png
         * name : 钱来钱往实体店体验
         * money : -100.00
         * pay_type_text : 记账
         * order_status_text : 交易中
         * member_username : 158****2251
         * score : 3000
         * pay_commission : ¥8.00
         * pay_type : 1
         * create_time : 1499071411
         */

        private int  order_id;
        private String order_number;
        private String avatar;
        private String name;
        private String money;
        private String pay_type_text;
        private String order_status_text;
        private String member_username;
        private int score;
        private String pay_commission;
        private int pay_type;
        private long create_time;

        public int getOrder_id() {
            return order_id;
        }

        public void setOrder_id(int order_id) {
            this.order_id = order_id;
        }

        public String getOrder_number() {
            return order_number;
        }

        public void setOrder_number(String order_number) {
            this.order_number = order_number;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMoney() {
            return money;
        }

        public void setMoney(String money) {
            this.money = money;
        }

        public String getPay_type_text() {
            return pay_type_text;
        }

        public void setPay_type_text(String pay_type_text) {
            this.pay_type_text = pay_type_text;
        }

        public String getOrder_status_text() {
            return order_status_text;
        }

        public void setOrder_status_text(String order_status_text) {
            this.order_status_text = order_status_text;
        }

        public String getMember_username() {
            return member_username;
        }

        public void setMember_username(String member_username) {
            this.member_username = member_username;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getPay_commission() {
            return pay_commission;
        }

        public void setPay_commission(String pay_commission) {
            this.pay_commission = pay_commission;
        }

        public int getPay_type() {
            return pay_type;
        }

        public void setPay_type(int pay_type) {
            this.pay_type = pay_type;
        }


        public long getCreate_time() {
            return create_time;
        }

        public void setCreate_time(long create_time) {
            this.create_time = create_time;
        }
    }
}
