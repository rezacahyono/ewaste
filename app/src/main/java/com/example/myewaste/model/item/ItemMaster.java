package com.example.myewaste.model.item;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemMaster implements Parcelable {
    private String no_item_master;
    private String name;
    private String photo;

    public ItemMaster(String no_item_master, String name, String photo) {
        this.no_item_master = no_item_master;
        this.name = name;
        this.photo = photo;
    }

    public ItemMaster() {
    }

    protected ItemMaster(Parcel in) {
        no_item_master = in.readString();
        name = in.readString();
        photo = in.readString();
    }

    public static final Creator<ItemMaster> CREATOR = new Creator<ItemMaster>() {
        @Override
        public ItemMaster createFromParcel(Parcel in) {
            return new ItemMaster(in);
        }

        @Override
        public ItemMaster[] newArray(int size) {
            return new ItemMaster[size];
        }
    };

    public String getNo_item_master() {
        return no_item_master;
    }

    public void setNo_item_master(String no_item_master) {
        this.no_item_master = no_item_master;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(no_item_master);
        parcel.writeString(name);
        parcel.writeString(photo);
    }
}
