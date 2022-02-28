package com.example.myewaste.model.item;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemType implements Parcelable {
    private String no_item_type;
    private String no_item_master;
    private String no_unit_item;
    private String name;
    private double price;

    public ItemType(String no_item_type, String no_item_master, String no_unit_item, String name, double price) {
        this.no_item_type = no_item_type;
        this.no_item_master = no_item_master;
        this.no_unit_item = no_unit_item;
        this.name = name;
        this.price = price;
    }

    public ItemType() {
    }

    protected ItemType(Parcel in) {
        no_item_type = in.readString();
        no_item_master = in.readString();
        no_unit_item = in.readString();
        name = in.readString();
        price = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(no_item_type);
        dest.writeString(no_item_master);
        dest.writeString(no_unit_item);
        dest.writeString(name);
        dest.writeDouble(price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ItemType> CREATOR = new Creator<ItemType>() {
        @Override
        public ItemType createFromParcel(Parcel in) {
            return new ItemType(in);
        }

        @Override
        public ItemType[] newArray(int size) {
            return new ItemType[size];
        }
    };

    public String getNo_item_type() {
        return no_item_type;
    }

    public void setNo_item_type(String no_item_type) {
        this.no_item_type = no_item_type;
    }

    public String getNo_item_master() {
        return no_item_master;
    }

    public void setNo_item_master(String no_item_master) {
        this.no_item_master = no_item_master;
    }

    public String getNo_unit_item() {
        return no_unit_item;
    }

    public void setNo_unit_item(String no_unit_item) {
        this.no_unit_item = no_unit_item;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

