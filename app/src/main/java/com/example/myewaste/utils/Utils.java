package com.example.myewaste.utils;

import static com.example.myewaste.utils.Constant.FORMATE_DATE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myewaste.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static String getRegisterCode(String registerNumber) {
        String code;
        String[] array = registerNumber.split("-");
        if (array.length > 0)
            code = registerNumber.split("-")[0];
        else
            code = "-";
        return code;
    }

    public static String getRegisterAs(String registerCode) {
        String as;
        switch (registerCode) {
            case "SA":
            case "sa":
                as = "SuperAdmin";
                break;
            case "T":
            case "t":
                as = "Teller";
                break;
            case "N":
            case "n":
                as = "Nasabah";
                break;
            default:
                as = "-";
                break;
        }
        return as;
    }

    public static String increseNumber(String dataOnCode) {
        String[] split = dataOnCode.split("-");
        int newVal = Integer.parseInt(split[1]) + 1;
        StringBuilder digit = new StringBuilder();
        for (int i = 0; i < split[1].length() - String.valueOf(newVal).length(); i++) {
            digit.append("0");
        }
        digit.append(newVal);
        return split[0].toUpperCase() + "-" + digit;
    }

    public static String convertToRupiah(int balance) {
        String balanceOnString = String.valueOf(balance);
        String[] arrBalance = balanceOnString.split("(?=(?:...)*$)");
        StringBuilder newBalance = new StringBuilder("Rp. ");
        for (int i = 0; i < arrBalance.length; i++) {
            if (!arrBalance[i].equals("")) {
                newBalance.append(arrBalance[i]);
                if (i != arrBalance.length - 1) {
                    newBalance.append(".");
                }
            }
        }
        return newBalance.toString();
    }


    public static void loadImage(String imageSource, ImageView bindOn, Context context) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Drawable drawableBitmap = new BitmapDrawable(context.getResources(), bitmap);
                bindOn.setImageDrawable(drawableBitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                bindOn.setImageResource(R.drawable.ic_placeholder);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                bindOn.setImageResource(R.drawable.ic_placeholder);
            }
        };
        bindOn.setTag(target);
        Picasso.get().load(imageSource).into(target);
        Picasso.get().setLoggingEnabled(true);
    }

    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String convertMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashtext = new StringBuilder(no.toString(16));
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
            return hashtext.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public static String convertDate(long dateLong) {
        Date date = new Date(dateLong);
        Format format = new SimpleDateFormat(FORMATE_DATE, Locale.US);
        return format.format(date);
    }

    public static BigDecimal parseCurrencyValue(String value) {
        try {
            String replaceRegex = "[a-zA-Z,.]";
            String currencyValue = value.replaceAll(replaceRegex, "");
            if (!currencyValue.isEmpty()) {
                return new BigDecimal(currencyValue);
            }
        } catch (Exception e) {
            Log.e("MyApp", e.getMessage(), e);
        }

        return BigDecimal.ZERO;
    }

}
