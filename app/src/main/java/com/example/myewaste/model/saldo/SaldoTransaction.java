package com.example.myewaste.model.saldo;

import android.os.Parcel;
import android.os.Parcelable;

public class SaldoTransaction implements Parcelable {
    private String no_saldo_transaction;
    private String no_nasabah;
    private String type_transaction;
    private String no_teller;
    private String status;
    private double total_income;
    private double cuts_transaction;
    private long date;

    public SaldoTransaction(String no_saldo_transaction, String no_nasabah, String type_transaction, String no_teller, String status, double total_income, double cuts_transaction, long date) {
        this.no_saldo_transaction = no_saldo_transaction;
        this.no_nasabah = no_nasabah;
        this.type_transaction = type_transaction;
        this.no_teller = no_teller;
        this.status = status;
        this.total_income = total_income;
        this.cuts_transaction = cuts_transaction;
        this.date = date;
    }

    public SaldoTransaction(){

    }

    protected SaldoTransaction(Parcel in) {
        no_saldo_transaction = in.readString();
        no_nasabah = in.readString();
        type_transaction = in.readString();
        no_teller = in.readString();
        status = in.readString();
        total_income = in.readDouble();
        cuts_transaction = in.readDouble();
        date = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(no_saldo_transaction);
        dest.writeString(no_nasabah);
        dest.writeString(type_transaction);
        dest.writeString(no_teller);
        dest.writeString(status);
        dest.writeDouble(total_income);
        dest.writeDouble(cuts_transaction);
        dest.writeLong(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SaldoTransaction> CREATOR = new Creator<SaldoTransaction>() {
        @Override
        public SaldoTransaction createFromParcel(Parcel in) {
            return new SaldoTransaction(in);
        }

        @Override
        public SaldoTransaction[] newArray(int size) {
            return new SaldoTransaction[size];
        }
    };

    public String getNo_saldo_transaction() {
        return no_saldo_transaction;
    }

    public void setNo_saldo_transaction(String no_saldo_transaction) {
        this.no_saldo_transaction = no_saldo_transaction;
    }

    public String getNo_nasabah() {
        return no_nasabah;
    }

    public void setNo_nasabah(String no_nasabah) {
        this.no_nasabah = no_nasabah;
    }

    public String getType_transaction() {
        return type_transaction;
    }

    public void setType_transaction(String type_transaction) {
        this.type_transaction = type_transaction;
    }

    public String getNo_teller() {
        return no_teller;
    }

    public void setNo_teller(String no_teller) {
        this.no_teller = no_teller;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotal_income() {
        return total_income;
    }

    public void setTotal_income(double total_income) {
        this.total_income = total_income;
    }

    public double getCuts_transaction() {
        return cuts_transaction;
    }

    public void setCuts_transaction(double cuts_transaction) {
        this.cuts_transaction = cuts_transaction;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
