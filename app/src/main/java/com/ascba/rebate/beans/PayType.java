package com.ascba.rebate.beans;

/**
 * 支付方式实体类
 */

public class PayType {
    private boolean isSelect;//是否被选择
    private int icon;
    private String title;
    private String content;
    private String type;
    private boolean isEnable;//是否可用

    public PayType(boolean isSelect, int icon, String title, String content,String type ,boolean isEnable) {
        this.isSelect = isSelect;
        this.icon = icon;
        this.title = title;
        this.content = content;
        this.type=type;
        this.isEnable=isEnable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
