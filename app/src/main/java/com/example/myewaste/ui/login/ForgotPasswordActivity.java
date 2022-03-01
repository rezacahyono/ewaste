package com.example.myewaste.ui.login;

import static com.example.myewaste.utils.Utils.convertMd5;
import static com.example.myewaste.utils.Constant.USER;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myewaste.R;
import com.example.myewaste.databinding.ActivityForgotPasswordBinding;
import com.example.myewaste.model.user.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity {

    private DatabaseReference database;
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();


        database = FirebaseDatabase.getInstance().getReference();

        binding.btnSave.setOnClickListener(v -> new AlertDialog.Builder(ForgotPasswordActivity.this)
                .setTitle(getResources().getString(R.string.alert))
                .setMessage(getResources().getString(R.string.you_sure_change_password))
                .setPositiveButton("OK", (dialog, which) -> performLupaPassword())
                .setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(ForgotPasswordActivity.this, "Kamu memilih Cancel", Toast.LENGTH_SHORT).show())
                .show());

        binding.btnCancel.setOnClickListener(v -> onBackPressed());
    }

    private void performLupaPassword() {
        String username = Objects.requireNonNull(binding.edtUsername.getText()).toString().trim();
        String newPassword = convertMd5(Objects.requireNonNull(binding.edtPasswordNew.getText()).toString().trim());
        String confirmPassword = convertMd5(Objects.requireNonNull(binding.edtPasswordConfirm.getText()).toString().trim());
        try {
            database.child(USER).child(username).get()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_LONG).show();

                            Log.e("firebase", "Error getting data", task.getException());
                        } else {
                            DataSnapshot result = task.getResult();
                            if (result != null) {
                                User user = result.getValue(new GenericTypeIndicator<User>() {
                                });
                                if (user != null) {
                                    if (newPassword.equals(confirmPassword)) {
                                        user.setPassword(newPassword);
                                        database.child(USER).child(username)
                                                .setValue(user).addOnCompleteListener((task1) -> {
                                            binding.edtUsername.setText("");
                                            binding.edtPasswordNew.setText("");
                                            binding.edtPasswordConfirm.setText("");
                                            binding.edtPasswordConfirm.clearFocus();

                                            Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_LONG).show();
                                        });
                                    } else {
                                        Toast.makeText(this, "Password baru tidak sesuai dengan konfirmasi", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_LONG).show();
                            }
                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        }
                    });
        } catch (Exception e) {
            Log.e("performLupaPassword", e.getMessage());
        }
    }
}
