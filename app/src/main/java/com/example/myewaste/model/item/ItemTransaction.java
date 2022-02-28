package com.example.myewaste.model.item;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ItemTransaction implements Parcelable {
    private String no_item_transaction;
    private String no_nasabah;
    private String no_teller;
    private String no_saldo_transaction;
    private ArrayList<Item> item_list;
    private long date;
    private double total_price;


    public ItemTransaction(String no_item_transaction, String no_nasabah, String no_teller, String no_saldo_transaction, ArrayList<Item> item_list, long date, double total_price) {
        this.no_item_transaction = no_item_transaction;
        this.no_nasabah = no_nasabah;
        this.no_teller = no_teller;
        this.no_saldo_transaction = no_saldo_transaction;
        this.item_list = item_list;
        this.date = date;
        this.total_price = total_price;
    }

    public ItemTransaction() {
    }

    protected ItemTransaction(Parcel in) {
        no_item_transaction = in.readString();
        no_nasabah = in.readString();
        no_teller = in.readString();
        no_saldo_transaction = in.readString();
        item_list = new ArrayList<>();
        in.readList(item_list, Item.class.getClassLoader());
        date = in.readLong();
        total_price = in.readDouble();
    }

    public static final Creator<ItemTransaction> CREATOR = new Creator<ItemTransaction>() {
        @Override
        public ItemTransaction createFromParcel(Parcel in) {
            return new ItemTransaction(in);
        }

        @Override
        public ItemTransaction[] newArray(int size) {
            return new ItemTransaction[size];
        }
    };

    public String getNo_item_transaction() {
        return no_item_transaction;
    }

    public void setNo_item_transaction(String no_item_transaction) {
        this.no_item_transaction = no_item_transaction;
    }

    public String getNo_nasabah() {
        return no_nasabah;
    }

    public void setNo_nasabah(String no_nasabah) {
        this.no_nasabah = no_nasabah;
    }

    public String getNo_teller() {
        return no_teller;
    }

    public void setNo_teller(String no_teller) {
        this.no_teller = no_teller;
    }

    public String getNo_saldo_transaction() {
        return no_saldo_transaction;
    }

    public void setNo_saldo_transaction(String no_saldo_transaction) {
        this.no_saldo_transaction = no_saldo_transaction;
    }

    public ArrayList<Item> getItem_list() {
        return item_list;
    }

    public void setItem_list(ArrayList<Item> item_list) {
        this.item_list = item_list;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getTotal_price() {
        return total_price;
    }

    public void setTotal_price(double total_price) {
        this.total_price = total_price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(no_item_transaction);
        parcel.writeString(no_nasabah);
        parcel.writeString(no_teller);
        parcel.writeString(no_saldo_transaction);
        parcel.writeList(item_list);
        parcel.writeLong(date);
        parcel.writeDouble(total_price);
    }
}
