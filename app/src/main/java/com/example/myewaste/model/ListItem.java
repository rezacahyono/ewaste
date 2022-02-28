package com.example.myewaste.model;

public class ListItem {
    private String nameItem;
    private String nameItemType;
    private String nameUnit;
    private int price;
    private double total;

    public ListItem(String nameItem, String nameItemType, String nameUnit, int price, double total) {
        this.nameItem = nameItem;
        this.nameItemType = nameItemType;
        this.nameUnit = nameUnit;
        this.price = price;
        this.total = total;
    }

    public ListItem() {
    }

    public String getNameItem() {
        return nameItem;
    }

    public void setNameItem(String nameItem) {
        this.nameItem = nameItem;
    }

    public String getNameItemType() {
        return nameItemType;
    }

    public void setNameItemType(String nameItemType) {
        this.nameItemType = nameItemType;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getNameUnit() {
        return nameUnit;
    }

    public void setNameUnit(String nameUnit) {
        this.nameUnit = nameUnit;
    }
}
