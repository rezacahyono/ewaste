package com.example.myewaste;

import static com.example.myewaste.utils.Constant.NONE;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManagement {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private  final String SHARED_PREF_NAME = "sharedpref";
    private final String SHARED_PREF_USER = "usersharedpref";

    public SessionManagement(Context context){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUserSession(String noRegis){
        editor.putString(SHARED_PREF_USER, noRegis).commit();
    }

    public void removeUserSession(){
        editor.remove(SHARED_PREF_USER).commit();
    }

    public String getUserSession(){
        return sharedPreferences.getString(SHARED_PREF_USER, NONE);
    }


}
