package com.example.myewaste.model.saldo;

import android.os.Parcel;
import android.os.Parcelable;

public class Saldo implements Parcelable {
    private String no_regis;
    private int saldo;


    public Saldo() {
    }

    public Saldo(String no_regis, int saldo) {
        this.no_regis = no_regis;
        this.saldo = saldo;
    }

    public Saldo (Parcel parcel){
        no_regis = parcel.readString();
        saldo = parcel.readInt();

    }



    public String getNo_regis() {
        return no_regis;
    }

    public void setNo_regis(String no_regis) {
        this.no_regis = no_regis;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(no_regis);
        parcel.writeInt(saldo);
    }

    public static final Creator<Saldo> CREATOR = new Creator<Saldo>() {
        @Override
        public Saldo createFromParcel(Parcel in) {
            return new Saldo(in);
        }

        @Override
        public Saldo[] newArray(int size) {
            return new Saldo[size];
        }
    };
}
