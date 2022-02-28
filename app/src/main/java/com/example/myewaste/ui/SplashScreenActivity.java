package com.example.myewaste.ui;

import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Util.getRegisterCode;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.NONE;
import static com.example.myewaste.utils.Constant.SUPER_ADMIN;
import static com.example.myewaste.utils.Constant.TELLER;
import static com.example.myewaste.utils.Constant.USER_DATA;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myewaste.ui.nasabah.NasabahActivity;
import com.example.myewaste.SessionManagement;
import com.example.myewaste.databinding.ActivitySplashScreenBinding;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.ui.admin.AdminActivity;
import com.example.myewaste.ui.login.LoginActivitiy;
import com.example.myewaste.ui.teller.TellerActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashScreenBinding binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            SessionManagement sessionManagement = new SessionManagement(getApplicationContext());
            if (sessionManagement.getUserSession().equals(NONE)) {
                startActivity(new Intent(getApplicationContext(), LoginActivitiy.class));
                finish();
            } else {
                String noRegis = sessionManagement.getUserSession();
                String initialCode = getRegisterCode(noRegis.toLowerCase());
                lookForUserData(noRegis, initialCode);
            }
        }, 2000L);
    }



    private void lookForUserData(String noRegis, String initialCode) {
        Query userDataQuery = databaseReference.child(USER_DATA).child(noRegis);
        userDataQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    UserData userData = task.getResult().getValue(UserData.class);
                    switch (initialCode) {
                        case SUPER_ADMIN:
                            routeToSuperAdmin(userData);
                            break;
                        case TELLER:
                            routeToTeller(userData);
                            break;
                        case NASABAH:
                            routeToNasabah(userData);
                            break;
                    }
                }
            }
        });
    }

    private void routeToSuperAdmin(UserData userData) {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra(EXTRAS_USER_DATA, userData);
        startActivity(intent);
        finish();
    }

    private void routeToTeller(UserData userData) {
        Intent intent = new Intent(this, TellerActivity.class);
        intent.putExtra(EXTRAS_USER_DATA, userData);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void routeToNasabah(UserData userData) {
        Intent intent = new Intent(this, NasabahActivity.class);
        intent.putExtra(EXTRAS_USER_DATA, userData);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}