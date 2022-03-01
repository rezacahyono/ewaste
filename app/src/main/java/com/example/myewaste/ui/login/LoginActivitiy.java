package com.example.myewaste.ui.login;

import static com.example.myewaste.utils.Constant.AKTIF;
import static com.example.myewaste.utils.Constant.EXTRAS_ACTION_MODE;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.FROM_LOGIN;
import static com.example.myewaste.utils.Constant.MODE;
import static com.example.myewaste.utils.Constant.MODE_ADD;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.SUPER_ADMIN;
import static com.example.myewaste.utils.Constant.TELLER;
import static com.example.myewaste.utils.Constant.USER;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Mode.MODE_NASABAH;
import static com.example.myewaste.utils.Utils.convertMd5;
import static com.example.myewaste.utils.Utils.getRegisterCode;
import static com.example.myewaste.utils.Utils.showMessage;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myewaste.ui.component.task.AddUserActivity;
import com.example.myewaste.ui.nasabah.NasabahActivity;
import com.example.myewaste.R;
import com.example.myewaste.pref.SessionManagement;
import com.example.myewaste.databinding.ActivityLoginBinding;
import com.example.myewaste.model.user.User;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.ui.admin.AdminActivity;
import com.example.myewaste.ui.teller.TellerActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;

import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;


public class LoginActivitiy extends AppCompatActivity {
    private DatabaseReference databaseReference;

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        requestPermission();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        binding.btnLogin.setOnClickListener(v -> performLogin());

        binding.tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivitiy.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivitiy.this, AddUserActivity.class);
            intent.putExtra(MODE, MODE_NASABAH);
            intent.putExtra(FROM_LOGIN, true);
            intent.putExtra(EXTRAS_ACTION_MODE,MODE_ADD);
            startActivity(intent);
        });

    }

    private void performLogin() {
        String username = Objects.requireNonNull(binding.edtUsername.getText()).toString().trim();
        String password = convertMd5(Objects.requireNonNull(binding.edtPassword.getText()).toString().trim());
        if (username.isEmpty())
            binding.edtUsername.setError(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.username)));
        else if (password.isEmpty())
            binding.edtUsername.setError(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.password)));
        else {
            try {
                Query userQuery = databaseReference.child(USER).child(username);
                userQuery.get()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Toast.makeText(this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_LONG).show();
                            } else {
                                DataSnapshot result = task.getResult();
                                if (result != null) {
                                    User user = result.getValue(new GenericTypeIndicator<User>() {
                                    });
                                    if (user != null) {
                                        if (user.getPassword().equals(password) && user.getStatus().equals(AKTIF)) {
                                            String initialCode = getRegisterCode(user.getNo_regis().toLowerCase());
                                            lookForUserData(user.getNo_regis(), initialCode);
                                        } else {
                                            if (!user.getPassword().equals(password)) {
                                                Toast.makeText(this, getResources().getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(this, getResources().getString(R.string.accont_inactive), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this, getResources().getString(R.string.account_not_found), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(this, getResources().getString(R.string.data_not_found), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            } catch (Exception e) {
                showMessage(this, e.getMessage());
            }
        }
    }

    private void lookForUserData(String noRegis, String initialCode) {
        Query userDataQuery = databaseReference.child(USER_DATA).child(noRegis);
        userDataQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    UserData userData = task.getResult().getValue(UserData.class);
                    SessionManagement sessionManagement = new SessionManagement(getApplicationContext());
                    assert userData != null;
                    sessionManagement.saveUserSession(userData.getNo_regis());
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
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void routeToSuperAdmin(UserData userData) {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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


    private void requestPermission() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        EasyPermissions.requestPermissions(
                this,
                getResources().getString(R.string.message_permission),
                101,
                perms
        );
    }
}