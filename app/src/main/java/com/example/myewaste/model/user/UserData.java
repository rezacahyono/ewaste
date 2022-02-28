package com.example.myewaste.model.user;

import android.os.Parcel;
import android.os.Parcelable;

public class UserData implements Parcelable {
    private String no_regis;
    private String avatar;
    private String photo_nik;
    private String nik;
    private String name;
    private String gender;
    private String phone;
    private String address;

    public UserData() {
    }


    protected UserData(Parcel in) {
        no_regis = in.readString();
        avatar = in.readString();
        photo_nik = in.readString();
        nik = in.readString();
        name = in.readString();
        gender = in.readString();
        phone = in.readString();
        address = in.readString();
    }

    public static final Creator<UserData> CREATOR = new Creator<UserData>() {
        @Override
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }

        @Override
        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };

    public UserData(String no_regis, String avatar, String photo_nik, String nik, String name, String gender, String phone, String address) {
        this.no_regis = no_regis;
        this.avatar = avatar;
        this.photo_nik = photo_nik;
        this.nik = nik;
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
    }

    public String getNo_regis() {
        return no_regis;
    }

    public void setNo_regis(String no_regis) {
        this.no_regis = no_regis;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhoto_nik() {
        return photo_nik;
    }

    public void setPhoto_nik(String photo_nik) {
        this.photo_nik = photo_nik;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(no_regis);
        parcel.writeString(avatar);
        parcel.writeString(photo_nik);
        parcel.writeString(nik);
        parcel.writeString(name);
        parcel.writeString(gender);
        parcel.writeString(phone);
        parcel.writeString(address);
    }
}
