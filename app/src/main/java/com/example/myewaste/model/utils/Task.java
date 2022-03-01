package com.example.myewaste.model.utils;

public class Task {
    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getDesc() {
        return desc;
    }

    public void setDesc(int desc) {
        this.desc = desc;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    private int title;
    private int desc;
    private int icon;

    public Task(int title, int desc, int icon) {
        this.title = title;
        this.desc = desc;
        this.icon = icon;
    }

    public Task() {
    }
}
