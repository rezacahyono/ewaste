package com.example.myewaste.model.item;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable {
    private String no_type_item;
    private double total;


    public Item(String no_type_item, double total) {
        this.no_type_item = no_type_item;
        this.total = total;
    }

    public Item(){}

    protected Item(Parcel in) {
        no_type_item = in.readString();
        total = in.readDouble();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public String getNo_type_item() {
        return no_type_item;
    }

    public void setNo_type_item(String no_type_item) {
        this.no_type_item = no_type_item;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(no_type_item);
        parcel.writeDouble(total);
    }
}
