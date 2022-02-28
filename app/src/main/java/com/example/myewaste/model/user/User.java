package com.example.myewaste.model.user;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String no_regis;
    private String username;
    private String password;
    private String status;

    protected User(Parcel in) {
        no_regis = in.readString();
        username = in.readString();
        password = in.readString();
        status = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User() {
    }

    public User(String no_regis, String username, String password, String status) {
        this.no_regis = no_regis;
        this.username = username;
        this.password = password;
        this.status = status;
    }

    public String getNo_regis() {
        return no_regis;
    }

    public void setNo_regis(String no_regis) {
        this.no_regis = no_regis;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(no_regis);
        parcel.writeString(username);
        parcel.writeString(password);
        parcel.writeString(status);
    }
}
