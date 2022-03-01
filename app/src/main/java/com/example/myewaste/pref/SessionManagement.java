package com.example.myewaste.pref;

import static com.example.myewaste.utils.Constant.NONE;
import static com.example.myewaste.utils.Constant.PREF_NAME;
import static com.example.myewaste.utils.Constant.PREF_USER_KEY;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManagement {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManagement(Context context){
        sharedPreferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUserSession(String noRegis){
        editor.putString(PREF_USER_KEY, noRegis).commit();
    }

    public void removeUserSession(){
        editor.remove(PREF_USER_KEY).commit();
    }

    public String getUserSession(){
        return sharedPreferences.getString(PREF_USER_KEY, NONE);
    }


}
